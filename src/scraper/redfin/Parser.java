package scraper.redfin;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import scraper.db.ChangeRecordDoc;

public class Parser {

	private static final String XPATH_URL_LIST = "url";
	private static final String XPATH_URL_LOC = "loc";
	private static final String XPATH_URL_LASTMOD = "lastmod";

	private Logger logger;

	public Parser(Logger logger) {
		this.logger = logger;
	}

	public static long[] getChangeLogTimeRange(Document doc) {
		// String dateStr = null;
		// long min = Long.MAX_VALUE;
		// long max = Long.MIN_VALUE;
		// for (Element h4ul : doc.select(XPATH_ROOT).first().children()) {
		// if (h4ul.tagName().equals("h4")) {
		// dateStr = h4ul.text();
		// } else if (h4ul.tagName().equals("ul")) {
		// for (Element span : h4ul.select(XPATH_ENTRY_DATE)) {
		// String timeStr = span.text();
		// String dateTimeStr = dateStr + ' ' + timeStr;
		// LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr,
		// DATETIME_FORMATTER);
		// long timestamp = dateTime.toEpochSecond(ZoneOffset.UTC);
		// if (timestamp > max) {
		// max = timestamp;
		// }
		// if (timestamp < min) {
		// min = timestamp;
		// }
		// }
		// }
		// }
		// return new long[] { min, max };
		return null;
	}

	public Set<ChangeRecordDoc> parse(String html) {
		Document doc = Jsoup.parse(html, "", org.jsoup.parser.Parser.xmlParser());
		return parse(doc);
	}

	public Set<ChangeRecordDoc> parse(Document doc) {
		Set<ChangeRecordDoc> recordSet = new HashSet<ChangeRecordDoc>();
		int modifyCount = 0;
		int ignoreCount = 0;

		for (Element urlElem : doc.select(XPATH_URL_LIST)) {
			String loc = urlElem.select(XPATH_URL_LOC).text();
			String lastmod = urlElem.select(XPATH_URL_LASTMOD).text();
			ChangeRecordDoc changeRecord = new ChangeRecordDoc(loc, parseLastMod(lastmod));
			recordSet.add(changeRecord);
			modifyCount++;
		}

		logger.info(String.format("done parsing page. [changed|distinct|ignored %d|%d|%d]", modifyCount,
				recordSet.size(), ignoreCount));
		return recordSet;
	}

	private long parseLastMod(String lastmod) {
		return ZonedDateTime.parse(lastmod, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toEpochSecond();
	}

}
