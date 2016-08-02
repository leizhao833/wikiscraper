package wikiscraper;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Test {

	public static void main(String[] args) throws Exception {
		// testDatabaseAddCrawlRecord();
		// testDatabaseAddChangeRecord();
		// testDatabaseCreate();
		// testDatetime();
	}

	private static void testDatabaseAddCrawlRecord() {
		CrawlRecordDao dao = new CrawlRecordDao();
		ZonedDateTime crawlTime = ZonedDateTime.now();
		ZonedDateTime changeTimeMin = ZonedDateTime.parse("2015-12-06T14:10:10Z");
		ZonedDateTime changeTimeMax = ZonedDateTime.parse("2016-01-10T09:00:00Z");
		CrawlRecordDoc doc = new CrawlRecordDoc(crawlTime, changeTimeMin, changeTimeMax);
		System.out.println(String.format("Adding entry: %s, %b", doc.toString(), dao.add(doc)));
		System.out.println(String.format("Adding entry: %s, %b", doc.toString(), dao.add(doc)));
	}

	private static void testDatabaseAddChangeRecord() {
		ChangeRecordDao dao = new ChangeRecordDao();
		ZonedDateTime now = ZonedDateTime.now();
		ChangeRecordDoc doc = new ChangeRecordDoc("http://www.foo.com", now);
		System.out.println(String.format("Adding entry: %s, %b", doc.toString(), dao.add(doc)));
		System.out.println(String.format("Adding entry: %s, %b", doc.toString(), dao.add(doc)));
	}

	private static void testDatabaseCreate() {
		ChangeRecordDao dao = new ChangeRecordDao();
		ChangeRecordDoc doc = new ChangeRecordDoc("http://www.test.com", ZonedDateTime.now());
		dao.create(doc);
		System.out.println(doc);

	}

	private static void testDatetime() {
		String s1 = "28 July 2016 04:07";
		String s2 = "28 July 2016 14:07";
		DateTimeFormatter f = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");
		LocalDateTime t = LocalDateTime.parse(s2, f);
		System.out.println(t.toString());
	}

}
