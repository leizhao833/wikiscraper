package wikiscraper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
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
				ZonedDateTime crawlTime = ChangePageCrawler.getLastCrawlTime()
						.atZone(ZoneId.systemDefault());
				List<ChangeRecordDoc> changeRecordList = ChangePagerParser
						.parse(doc);
				ChangeRecordDoc changeMin = new ChangeRecordDoc();
				ChangeRecordDoc changeMax = new ChangeRecordDoc();
				findMaxMinRecords(changeRecordList, changeMax, changeMin);
				boolean overlap = detectRecordOverlap(changeMin);
				storeChangeRecords(changeRecordList);
				storeCrawlRecord(crawlTime, changeMax, changeMin);
				doc = ChangePageCrawler.getDocument(overlap, false);
			} catch (Throwable t) {
				LOGGER.log(Level.SEVERE, t.getMessage());
			}
		}

	}

	private static void storeCrawlRecord(ZonedDateTime crawlTime,
			ChangeRecordDoc max, ChangeRecordDoc min) {
		CrawlRecordDoc rec = new CrawlRecordDoc(crawlTime.toEpochSecond(),
				min.timestamp, max.timestamp);
		if (CrawlRecordDao.INSTANCE.add(rec)) {
			LOGGER.log(Level.INFO,
					String.format("ADD crawl record %s", rec.toString()));
		} else {
			LOGGER.log(Level.WARNING, String.format(
					"Duplicated crawl record existing: %s. Ignore ...",
					rec.toString()));
		}

	}

	private static void storeChangeRecords(List<ChangeRecordDoc> records) {
		LOGGER.log(Level.INFO,
				String.format("Adding %d change records into database", records.size()));
		for (ChangeRecordDoc rec : records) {
			boolean added = ChangeRecordDao.INSTANCE.add(rec);
			LOGGER.log(Level.FINE,
					String.format("ADD change record %s %b", rec.toString(), added));
		}
	}

	private static void findMaxMinRecords(List<ChangeRecordDoc> records,
			ChangeRecordDoc max, ChangeRecordDoc min) {
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
