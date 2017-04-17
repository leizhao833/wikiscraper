package scraper.redfin;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.logging.Logger;

import scraper.db.ChangeRecordDoc;

public class Test {

	public static void main(String[] args) throws Exception {
		testParse();
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

	private static void testDatetime() {
		String s1 = "2017-04-17T07:52:16.930-07:00";
		DateTimeFormatter f = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		LocalDateTime t = LocalDateTime.parse(s1, f);
		ZonedDateTime.parse(s1, f);
		System.out.println(t.toString());
		System.out.println(ZonedDateTime.parse(s1, f).toString());
	}
}
