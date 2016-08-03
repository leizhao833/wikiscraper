package wikiscraper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.nodes.Document;

public class Main {

	private static final Logger LOGGER = Logger.getGlobal();
	private static LocalDate lastExpiringDate = LocalDate.ofEpochDay(0);

	public static void main(String[] args) {
		initialize();
		serviceLoop();
	}

	private static void initialize() {
		FileHandler handler;
		try {
			File log = new File("log");
			if (!log.exists()) {
				log.mkdir();
			}
			handler = new FileHandler("log" + File.separator + "wikiscraper-log.%u.%g.xml", 1024 * 1024 * 50, 1000, true);
		    SimpleFormatter formatter = new SimpleFormatter();
		    handler.setFormatter(formatter);
			LOGGER.addHandler(handler);
		} catch (SecurityException e) {
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		} catch (IOException e) {
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
		Config.initialize();

	}

	private static void serviceLoop() {
		Document doc = null;

		try {
			doc = ChangePageCrawler.getDocument(true, true);
		} catch (Throwable t) {
			LOGGER.severe(ExceptionUtils.getStackTrace(t));
		}

		while (true) {
			try {
				ZonedDateTime crawlTime = ChangePageCrawler.getLastCrawlTime().atZone(ZoneId.systemDefault());
				Set<ChangeRecordDoc> changeRecordSet = ChangePagerParser.parse(doc);
				ChangeRecordDoc changeMin = new ChangeRecordDoc();
				ChangeRecordDoc changeMax = new ChangeRecordDoc();
				findMaxMinRecords(changeRecordSet, changeMax, changeMin);
				boolean overlap = detectRecordOverlap(changeMin);
				storeChangeRecords(changeRecordSet);
				storeCrawlRecord(crawlTime, changeMax, changeMin);
				expiringRecords();
				doc = ChangePageCrawler.getDocument(overlap, false);
			} catch (Throwable t) {
				LOGGER.severe(ExceptionUtils.getStackTrace(t));
			}
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
