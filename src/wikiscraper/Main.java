package wikiscraper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * This scraper downloads the wikipedia change log page (URL by wikiChangeUrl in
 * properties file) in a dynamic adjusted frequency, parses it for change
 * records constituted of URL and time stamps pair, and stores the records in
 * DocumentDB. The scraper also stores the crawl history in the database, of
 * which each record includes the crawl time and the time range of the change
 * history.
 * 
 * @author leizh
 *
 */
public class Main {

	public static final boolean PROD = true;
	private static final Logger LOGGER = Logger.getGlobal();

	public static void main(String[] args) {
		initialize();
		serviceLoop();
	}

	/**
	 * Initialize logger, configuration, and crawler.
	 */
	private static void initialize() {
		FileHandler fileHandler;
		try {
			File log = new File("log");
			if (!log.exists()) {
				log.mkdir();
			}
			File logPath = new File(System.getProperty("user.home") + File.separator + "wikitemp" + File.separator
					+ "log");
			if (!logPath.exists()) {
				logPath.mkdirs();
			}
			fileHandler = new FileHandler(logPath.getPath() + File.separator + "wikiscraper-log.%u.%g.log",
					1024 * 1024 * 50, 1000, true);
			SimpleFormatter formatter = new SimpleFormatter();
			fileHandler.setFormatter(formatter);
			LOGGER.addHandler(fileHandler);
			if (!PROD) {
				fileHandler.setLevel(Level.ALL);
				LOGGER.setLevel(Level.ALL);
			}
		} catch (SecurityException | IOException e) {
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
		Config.initialize();
		Crawler.initialize();
	}

	private static void serviceLoop() {
		Document doc = null;
		float overlap = 0.5f;
		File htmlFile = null;
		LocalDateTime crawlTime = null;

		try {
			// step 1: download the change page
			String html = Crawler.getDocument(overlap, true);
			crawlTime = Crawler.getLastCrawlTime();
			// step 2: store the html into a temporary file
			htmlFile = storeHtmlFile(html, crawlTime);
			// step 3: convert the html to DOM tree
			doc = Jsoup.parse(html, "https://en.wikipedia.org");
		} catch (Throwable t) {
			LOGGER.severe(ExceptionUtils.getStackTrace(t));
		}

		while (true) {
			Set<ChangeRecordDoc> changeRecordSet = null;
			try {
				// step 4: parse the DOM tree for change records
				changeRecordSet = Parser.parse(doc);
				// step 5: delete the html file if parse succeeded
				htmlFile.delete();
				LOGGER.info(String.format("[%s] deleted", htmlFile.toString()));
			} catch (Throwable t) {
				LOGGER.severe(ExceptionUtils.getStackTrace(t));
			}
			try {
				if (changeRecordSet != null) {
					// parsing succeeded
					// step 6: store the change records to DB
					int added = storeChangeRecords(changeRecordSet);
					// step 7: calculate overlap rate between the current record
					// set and the previous one. High rate indicates crawl
					// frequency is too high. This is used in crawl frequency
					// estimation in Crawler.getDocument();
					overlap = 1 - added / (float) changeRecordSet.size();

					// step 8: store crawl record into DB
					ChangeRecordDoc changeMin = new ChangeRecordDoc();
					ChangeRecordDoc changeMax = new ChangeRecordDoc();
					findMaxMinRecords(changeRecordSet, changeMax, changeMin);
					storeCrawlRecord(crawlTime.atZone(ZoneId.systemDefault()), changeMax, changeMin);
				} else {
					// parsing failed, do nothing for this round
					overlap = 1;
				}
				// step 9: delete records that have expired according to
				// configuration
				// update: disabled due to long run time. Using collection level
				// expiry setting instead.
				// expiringRecords();

				// back to loop step 1
				String html = Crawler.getDocument(overlap, false);
				crawlTime = Crawler.getLastCrawlTime();
				htmlFile = storeHtmlFile(html, crawlTime);
				doc = Jsoup.parse(html, "https://en.wikipedia.org");
			} catch (Throwable t) {
				LOGGER.severe(ExceptionUtils.getStackTrace(t));
			}
		}
	}

	private static final DateTimeFormatter HTML_FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

	private static File storeHtmlFile(String html, LocalDateTime crawlTime) {
		File htmlDir = new File(System.getProperty("user.home") + File.separator + "wikitemp" + File.separator + "html");
		if (!htmlDir.exists()) {
			htmlDir.mkdirs();
		}
		String htmlFile = "changePage_" + HTML_FILE_FORMATTER.format(crawlTime) + ".html";
		File file = new File(htmlDir, htmlFile);
		try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
			out.write(html);
			LOGGER.info(String.format("store html to %s", file.toString()));
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
		return file;
	}

	private static void storeCrawlRecord(ZonedDateTime crawlTime, ChangeRecordDoc max, ChangeRecordDoc min) {
		CrawlRecordDoc rec = new CrawlRecordDoc(crawlTime.toEpochSecond(), min.timestamp, max.timestamp);
		if (CrawlRecordDao.INSTANCE.add(rec)) {
			LOGGER.info(String.format("added crawl record [ %s ] into database", rec.toString()));
		} else {
			LOGGER.info(String.format("duplicated crawl record existing: %s. ignore ...", rec.toString()));
		}

	}

	private static int storeChangeRecords(Set<ChangeRecordDoc> records) {
		LOGGER.info(String.format("adding [%d] change records into database", records.size()));
		int newCount = 0;
		for (ChangeRecordDoc rec : records) {
			boolean added = ChangeRecordDao.INSTANCE.add(rec);
			Utils.exceptionFreeSleep(Config.queryIntervalInMillis);
			if (added) {
				newCount++;
			}
			LOGGER.fine(String.format("added change record [ %s ] [%b]", rec.toString(), added));
		}
		LOGGER.info(String.format("[%d(%d new)] change records added to database", records.size(), newCount));
		return newCount;
	}

	/**
	 * Finding the two records among all changes that have the smallest and
	 * largest time stamp respectively.
	 */
	public static void findMaxMinRecords(Set<ChangeRecordDoc> records, ChangeRecordDoc max, ChangeRecordDoc min) {
		ChangeRecordDoc maxPtr = new ChangeRecordDoc(null, 0);
		ChangeRecordDoc minPtr = new ChangeRecordDoc(null, Long.MAX_VALUE);
		for (ChangeRecordDoc changeRecord : records) {
			if (changeRecord.timestamp > maxPtr.timestamp) {
				maxPtr = changeRecord;
			}
			if (changeRecord.timestamp < minPtr.timestamp) {
				minPtr = changeRecord;
			}
		}
		maxPtr.copyTo(max);
		minPtr.copyTo(min);
	}

}