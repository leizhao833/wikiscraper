package wikiscraper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.nodes.Document;

public class Main {

	private static final Logger LOGGER = Logger.getGlobal();

	public static void main(String[] args) {

		LOGGER.setLevel(Level.ALL);

		Document doc = ChangePageCrawler.getDocument(true, true);

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
				doc = ChangePageCrawler.getDocument(overlap, false);
			} catch (Throwable t) {
				LOGGER.severe(t.getMessage());
			}
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
