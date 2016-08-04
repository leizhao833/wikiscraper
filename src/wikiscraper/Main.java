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
			if (!Config.PROD) {
				fileHandler.setLevel(Level.ALL);
				LOGGER.setLevel(Level.ALL);
			}
		} catch (SecurityException | IOException e) {
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
		Config.initialize();

	}

	private static void serviceLoop() {

		Document doc = null;
		boolean overlap = true;
		try {
			String html = Crawler.getDocument(overlap, true);
			storeHtmlFile(html);
			doc = Jsoup.parse(html, "https://en.wikipedia.org");
		} catch (Throwable t) {
			LOGGER.severe(ExceptionUtils.getStackTrace(t));
		}

		while (true) {
			try {
				ZonedDateTime crawlTime = Crawler.getLastCrawlTime().atZone(ZoneId.systemDefault());
				Set<ChangeRecordDoc> changeRecordSet = Parser.parse(doc);
				ChangeRecordDoc changeMin = new ChangeRecordDoc();
				ChangeRecordDoc changeMax = new ChangeRecordDoc();
				findMaxMinRecords(changeRecordSet, changeMax, changeMin);
				overlap = detectRecordOverlap(changeMin);
				storeChangeRecords(changeRecordSet);
				storeCrawlRecord(crawlTime, changeMax, changeMin);
				expiringRecords();
				String html = Crawler.getDocument(overlap, false);
				storeHtmlFile(html);
				doc = Jsoup.parse(html, "https://en.wikipedia.org");
			} catch (Throwable t) {
				LOGGER.severe(ExceptionUtils.getStackTrace(t));
			}
		}
	}

	private static final DateTimeFormatter HTML_FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

	private static void storeHtmlFile(String html) {
		File htmlDir = new File("html");
		if (!htmlDir.exists()) {
			htmlDir.mkdir();
		}
		String htmlFile = "changePage_" + HTML_FILE_FORMATTER.format(LocalDateTime.now()) + ".html";
		File file = new File(htmlDir, htmlFile);
		try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
			out.write(html);
			LOGGER.info(String.format("storing html body to %s", file.toString()));
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
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
			LOGGER.info(String.format("Added crawl record %s into database", rec.toString()));
		} else {
			LOGGER.info(String.format("Duplicated crawl record existing: %s. Ignore ...", rec.toString()));
		}

	}

	private static void storeChangeRecords(Set<ChangeRecordDoc> records) {
		int newCount = 0;
		for (ChangeRecordDoc rec : records) {
			boolean added = ChangeRecordDao.INSTANCE.add(rec);
			Utils.exceptionFreeSleep(Config.queryIntervalInMillis);
			if (added) {
				newCount++;
			}
			LOGGER.fine(String.format("Added change record %s %b", rec.toString(), added));
		}
		LOGGER.info(String.format("Added %d total (%d new) change records into database", records.size(), newCount));
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

	private static boolean detectRecordOverlap(ChangeRecordDoc min) {
		return ChangeRecordDao.INSTANCE.exist(min);
	}

}
