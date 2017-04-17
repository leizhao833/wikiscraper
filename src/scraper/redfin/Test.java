package scraper.redfin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import scraper.db.ChangeRecordDoc;

public class Test {

	public static void main(String[] args) throws Exception {
		testParse();
		// testDocEqual();
		// testMisc();
		// utilTicksToZonedDateTime();
		// zonedDateTimeToTicks();

		// testUntil();
		// testUrl();
		// testDatabaseAddCrawlRecord();
		// testDatabaseAddChangeRecord();
		// testDatabaseCreate();
		testDatetime();
	}

	static void testParse() throws IOException {
		Parser parser = new Parser(Logger.getGlobal());
		String html = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" >  <url>    <loc>https://www.redfin.com/GA/Cumming/4060-Hunters-Walk-Way-30028/home/24248507</loc>    <lastmod>2017-04-17T07:52:16.930-07:00</lastmod>    <changefreq>daily</changefreq>    <priority>1.0</priority>  </url>  <url>    <loc>https://www.redfin.com/VA/Mechanicsville/8364-Springset-Ln-23116/home/56192668</loc>    <lastmod>2017-04-17T07:52:17.488-07:00</lastmod>    <changefreq>daily</changefreq>    <priority>1.0</priority>  </url>  </urlset>  ";
		Set<ChangeRecordDoc> docs = parser.parse(html);
		for (ChangeRecordDoc d : docs) {
			System.out.println(d.toString());
		}
	}

	private static void testDocEqual() {
		Set<ChangeRecordDoc> s = new HashSet<ChangeRecordDoc>();
		ChangeRecordDoc d1 = new ChangeRecordDoc("aaa", 123);
		ChangeRecordDoc d2 = new ChangeRecordDoc("aaa", 123);
		ChangeRecordDoc d3 = new ChangeRecordDoc("bbb", 123);
		System.out.println(String.format("%s.equals(%s) %b", "d1", "d2", d1.equals(d2)));
		System.out.println(String.format("%s.equals(%s) %b", "d1", "d3", d1.equals(d3)));
		System.out.println(String.format("%s.equals(%s) %b", "d2", "d3", d2.equals(d3)));
		System.out.println(String.format("add(%s) %b", "d1", s.add(d1)));
		System.out.println(String.format("add(%s) %b", "d2", s.add(d2)));
		System.out.println(String.format("add(%s) %b", "d3", s.add(d3)));
		for (ChangeRecordDoc d : s) {
			System.out.println(d.toString());
		}
	}

	private static void testMisc() {
		System.out.println(System.getProperty("user.home"));
	}

	private static void zonedDateTimeToTicks() {
		ZonedDateTime time = ZonedDateTime.parse("2016-08-15T00:00:00Z");
		System.out.println(time.toEpochSecond());
	}

	private static void utilTicksToZonedDateTime() {
		long timestamp = 1470855240;
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

	private static void testDatetime() {
		String s1 = "2017-04-17T07:52:16.930-07:00";
		DateTimeFormatter f = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		LocalDateTime t = LocalDateTime.parse(s1, f);
		ZonedDateTime.parse(s1, f);
		System.out.println(t.toString());
		System.out.println(ZonedDateTime.parse(s1, f).toString());
	}
}
