package scraper.wikipedia;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import scraper.ScraperConfig;
import scraper.db.ChangeRecordDao;
import scraper.db.ChangeRecordDoc;
import scraper.db.CrawlRecordDao;
import scraper.db.CrawlRecordDoc;

public class Test {
	/*
	 * public static void main(String[] args) throws Exception { // testParse();
	 * // testDocEqual(); // testMisc(); // utilTicksToZonedDateTime();
	 * zonedDateTimeToTicks();
	 * 
	 * // testUntil(); // testUrl(); // testDatabaseAddCrawlRecord(); //
	 * testDatabaseAddChangeRecord(); // testDatabaseCreate(); //
	 * testDatetime(); }
	 * 
	 * static void testParse() throws IOException { Document doc =
	 * Jsoup.parse(new File(ScraperConfig.LOCAL_TEST_FILE),
	 * Charset.defaultCharset().name(), "https://www.wikipedia.org");
	 * Set<ChangeRecordDoc> res = Parser.parse(doc);
	 * 
	 * File file = new File("out.txt"); try (BufferedWriter out = new
	 * BufferedWriter(new FileWriter(file))) { for (ChangeRecordDoc
	 * changeRecordDoc : res) { out.write(changeRecordDoc.toString() + "\n"); }
	 * } catch (IOException e) { } }
	 * 
	 * private static void testDocEqual() { Set<ChangeRecordDoc> s = new
	 * HashSet<ChangeRecordDoc>(); ChangeRecordDoc d1 = new
	 * ChangeRecordDoc("aaa", 123); ChangeRecordDoc d2 = new
	 * ChangeRecordDoc("aaa", 123); ChangeRecordDoc d3 = new
	 * ChangeRecordDoc("bbb", 123);
	 * System.out.println(String.format("%s.equals(%s) %b", "d1", "d2",
	 * d1.equals(d2))); System.out.println(String.format("%s.equals(%s) %b",
	 * "d1", "d3", d1.equals(d3)));
	 * System.out.println(String.format("%s.equals(%s) %b", "d2", "d3",
	 * d2.equals(d3))); System.out.println(String.format("add(%s) %b", "d1",
	 * s.add(d1))); System.out.println(String.format("add(%s) %b", "d2",
	 * s.add(d2))); System.out.println(String.format("add(%s) %b", "d3",
	 * s.add(d3))); for (ChangeRecordDoc d : s) {
	 * System.out.println(d.toString()); } }
	 * 
	 * private static void testMisc() {
	 * System.out.println(System.getProperty("user.home")); }
	 * 
	 * private static void zonedDateTimeToTicks() { ZonedDateTime time =
	 * ZonedDateTime.parse("2016-08-15T00:00:00Z");
	 * System.out.println(time.toEpochSecond()); }
	 * 
	 * private static void utilTicksToZonedDateTime() { long timestamp =
	 * 1470855240; ZonedDateTime time =
	 * ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp),
	 * ZoneOffset.UTC); System.out.println(time); }
	 * 
	 * private static void testUntil() { LocalDateTime t1 = LocalDateTime.now();
	 * LocalDateTime t2 = t1.minusSeconds(20); long intervalInMillis =
	 * t1.until(t2, ChronoUnit.MILLIS); System.out.println(intervalInMillis); }
	 * 
	 * private static void testUrl() { // String urlStr =
	 * "/wiki/Alfonso_Zirpoli"; String urlStr =
	 * "https://en.wikipedia.org/w/index.php?namespace=";
	 * 
	 * try { URI url = new URI(urlStr); if (!url.isAbsolute()) { urlStr = new
	 * URI("https", "en.wikipedia.org", urlStr, null).toString(); }
	 * System.out.println(urlStr); } catch (URISyntaxException e) {
	 * e.printStackTrace(); } }
	 * 
	 * private static void testDatabaseAddCrawlRecord() { CrawlRecordDao dao =
	 * CrawlRecordDao.INSTANCE; ZonedDateTime crawlTime = ZonedDateTime.now();
	 * ZonedDateTime changeTimeMin =
	 * ZonedDateTime.parse("2015-12-06T14:10:10Z"); ZonedDateTime changeTimeMax
	 * = ZonedDateTime.parse("2016-01-10T09:00:00Z"); CrawlRecordDoc doc = new
	 * CrawlRecordDoc(crawlTime, changeTimeMin, changeTimeMax);
	 * System.out.println(String.format("Adding entry: %s, %b", doc.toString(),
	 * dao.add(doc))); System.out.println(String.format("Adding entry: %s, %b",
	 * doc.toString(), dao.add(doc))); }
	 * 
	 * private static void testDatabaseAddChangeRecord() { ChangeRecordDao dao =
	 * ChangeRecordDao.INSTANCE; ZonedDateTime now = ZonedDateTime.now();
	 * ChangeRecordDoc doc = new ChangeRecordDoc("http://www.foo.com", now);
	 * System.out.println(String.format("Adding entry: %s, %b", doc.toString(),
	 * dao.add(doc))); System.out.println(String.format("Adding entry: %s, %b",
	 * doc.toString(), dao.add(doc))); }
	 * 
	 * private static void testDatabaseCreate() { ChangeRecordDao dao =
	 * ChangeRecordDao.INSTANCE; ChangeRecordDoc doc = new
	 * ChangeRecordDoc("http://www.test.com", ZonedDateTime.now());
	 * dao.create(doc); System.out.println(doc);
	 * 
	 * }
	 * 
	 * private static void testDatetime() { String s1 = "28 July 2016 04:07";
	 * String s2 = "28 July 2016 14:07"; DateTimeFormatter f =
	 * DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm"); LocalDateTime t =
	 * LocalDateTime.parse(s2, f); System.out.println(t.toString()); }
	 */
}
