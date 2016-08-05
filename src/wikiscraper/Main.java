package wikiscraper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
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

public class Main {

	public static final boolean PROD = true;
	private static final Logger LOGGER = Logger.getGlobal();
	private static LocalDate lastExpiringDate = LocalDate.ofEpochDay(0);

	public static void main(String[] args) {
		initialize();
		serviceLoop();
	}

	private static void initialize() {
		FileHandler fileHandler;
		try {
			File log = new File("log");
			if (!log.exists()) {
				log.mkdir();
			}
			fileHandler = new FileHandler("log" + File.separator + "wikiscraper-log.%u.%g.log", 1024 * 1024 * 50, 1000,
					true);
			SimpleFormatter formatter = new SimpleFormatter();
			fileHandler.setFormatter(formatter);
			LOGGER.addHandler(fileHandler);
//			if (!PROD) {
				fileHandler.setLevel(Level.ALL);
				LOGGER.setLevel(Level.ALL);
//			}
		} catch (SecurityException | IOException e) {
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
		Config.initialize();

	}

	private static void serviceLoop() {
		Document doc = null;
		boolean overlap = true;
		File htmlFile = null;
		LocalDateTime crawlTime = null;

		try {
			String html = Crawler.getDocument(overlap, true);
			crawlTime = Crawler.getLastCrawlTime();
			htmlFile = storeHtmlFile(html, crawlTime);
			doc = Jsoup.parse(html, "https://en.wikipedia.org");
		} catch (Throwable t) {
			LOGGER.severe(ExceptionUtils.getStackTrace(t));
		}

		while (true) {
			Set<ChangeRecordDoc> changeRecordSet = null;
			try {
				changeRecordSet = Parser.parse(doc);
				// delete the html file if parse passed
				htmlFile.delete();
				LOGGER.info(String.format("[%s] deleted", htmlFile.toString()));
			} catch (Throwable t) {
				LOGGER.severe(ExceptionUtils.getStackTrace(t));
			}
			try {
				ChangeRecordDoc changeMin = new ChangeRecordDoc();
				ChangeRecordDoc changeMax = new ChangeRecordDoc();
				findMaxMinRecords(changeRecordSet, changeMax, changeMin);
				int added = storeChangeRecords(changeRecordSet);
				overlap = added < changeRecordSet.size();
				storeCrawlRecord(crawlTime.atZone(ZoneId.systemDefault()), changeMax, changeMin);
				expiringRecords();

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
		File htmlDir = new File("html");
		if (!htmlDir.exists()) {
			htmlDir.mkdir();
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

	private static void expiringRecords() {
		LocalDate today = LocalDate.now();
		if (today.compareTo(lastExpiringDate) > 0) {
			ZonedDateTime changeCutoff = ZonedDateTime.now().minusDays(Config.changeRecordExpiryInDays);
			ZonedDateTime crawlCutoff = ZonedDateTime.now().minusDays(Config.crawlRecordExpiryInDays);
			ChangeRecordDao.INSTANCE.deleteOlderThan(changeCutoff);
			CrawlRecordDao.INSTANCE.deleteOlderThan(crawlCutoff);
			lastExpiringDate = today;
		}
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

	private static void findMaxMinRecords(Set<ChangeRecordDoc> records, ChangeRecordDoc max, ChangeRecordDoc min) {
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