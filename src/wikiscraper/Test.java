package wikiscraper;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Test {

	public static void main(String[] args) throws Exception {
		utilTicksToZonedDateTime();
		// testUntil();
		// testUrl();
		// testDatabaseAddCrawlRecord();
		// testDatabaseAddChangeRecord();
		// testDatabaseCreate();
		// testDatetime();
	}

	private static void utilTicksToZonedDateTime() {
		long timestamp = 1470206820;
		ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);
		System.out.println(time);
	}

	private static void testUntil() {
		LocalDateTime t1 = LocalDateTime.now();
		LocalDateTime t2 = t1.minusSeconds(20);
		long intervalInMillis = t1.until(t2, ChronoUnit.MILLIS);
		System.out.println(intervalInMillis);
	}

	private static void testUrl() {
		// String urlStr = "/wiki/Alfonso_Zirpoli";
		String urlStr = "https://en.wikipedia.org/w/index.php?namespace=";

		try {
			URI url = new URI(urlStr);
			if (!url.isAbsolute()) {
				urlStr = new URI("https", "en.wikipedia.org", urlStr, null).toString();
			}
			System.out.println(urlStr);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private static void testDatabaseAddCrawlRecord() {
		CrawlRecordDao dao = CrawlRecordDao.INSTANCE;
		ZonedDateTime crawlTime = ZonedDateTime.now();
		ZonedDateTime changeTimeMin = ZonedDateTime.parse("2015-12-06T14:10:10Z");
		ZonedDateTime changeTimeMax = ZonedDateTime.parse("2016-01-10T09:00:00Z");
		CrawlRecordDoc doc = new CrawlRecordDoc(crawlTime, changeTimeMin, changeTimeMax);
		System.out.println(String.format("Adding entry: %s, %b", doc.toString(), dao.add(doc)));
		System.out.println(String.format("Adding entry: %s, %b", doc.toString(), dao.add(doc)));
	}

	private static void testDatabaseAddChangeRecord() {
		ChangeRecordDao dao = ChangeRecordDao.INSTANCE;
		ZonedDateTime now = ZonedDateTime.now();
		ChangeRecordDoc doc = new ChangeRecordDoc("http://www.foo.com", now);
		System.out.println(String.format("Adding entry: %s, %b", doc.toString(), dao.add(doc)));
		System.out.println(String.format("Adding entry: %s, %b", doc.toString(), dao.add(doc)));
	}

	private static void testDatabaseCreate() {
		ChangeRecordDao dao = ChangeRecordDao.INSTANCE;
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
