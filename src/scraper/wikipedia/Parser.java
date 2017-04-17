package scraper.wikipedia;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import scraper.db.ChangeRecordDoc;

public class Parser {

	private static final String XPATH_ROOT = "div.mw-changeslist";
	private static final String XPATH_ROOT_LIST = "li";
	private static final String XPATH_ENTRY_TITLE = "span.mw-title a, abbr.wikibase-edit + a";
	private static final String XPATH_ENTRY_DATE = "span.mw-changeslist-date";
	private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm");
	private static final Pattern PATTERN_FILTER = Pattern.compile("^https?://([a-z]{2})\\.wikipedia\\.org/wiki/(.+)");
	
	private Logger logger;

	public Parser(Logger logger) {
		this.logger = logger;
	}

	public static long[] getChangeLogTimeRange(Document doc) {
		String dateStr = null;
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		for (Element h4ul : doc.select(XPATH_ROOT).first().children()) {
			if (h4ul.tagName().equals("h4")) {
				dateStr = h4ul.text();
			} else if (h4ul.tagName().equals("ul")) {
				for (Element span : h4ul.select(XPATH_ENTRY_DATE)) {
					String timeStr = span.text();
					String dateTimeStr = dateStr + ' ' + timeStr;
					LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
					long timestamp = dateTime.toEpochSecond(ZoneOffset.UTC);
					if (timestamp > max) {
						max = timestamp;
					}
					if (timestamp < min) {
						min = timestamp;
					}
				}
			}
		}
		return new long[] { min, max };
	}

	public Set<ChangeRecordDoc> parse(Document doc) {
		String dateStr = null;
		Set<ChangeRecordDoc> recordSet = new HashSet<ChangeRecordDoc>();
		int modifyCount = 0;
		int ignoreCount = 0;
		for (Element h4ul : doc.select(XPATH_ROOT).first().children()) {
			if (h4ul.tagName().equals("h4")) {
				dateStr = h4ul.text();
			} else if (h4ul.tagName().equals("ul")) {
				ChangeRecordDoc changeRecord = null;
				for (Element li : h4ul.select(XPATH_ROOT_LIST)) {
					Set<String> classNames = li.classNames();
					assert (classNames != null);
					EnumChangeType changeType = detectChangeType(li.classNames());
					switch (changeType) {
					case CT_MODIFICATION:
						changeRecord = parseModification(li, dateStr);
						if (changeRecord == null) {
							ignoreCount++;
							logger.fine(String.format("Filtering: %s %s", changeType.toString(), li.text()));
							continue;
						}
						recordSet.add(changeRecord);
						modifyCount++;
						break;
					default:
						ignoreCount++;
						logger.fine(String.format("Ignoring: %s %s", changeType.toString(), li.text()));
					}
				}
			}
		}
		logger.info(String.format("done parsing page. [changed|distinct|ignored %d|%d|%d]", modifyCount,
				recordSet.size(), ignoreCount));
		return recordSet;
	}

	private static ChangeRecordDoc parseModification(Element elem, String dateStr) {
		Elements elems = elem.select(XPATH_ENTRY_TITLE);
		String urlStr = elems.first().attr("abs:href");
		if (filter(urlStr)) {
			return null;
		}
		String timeStr = elem.select(XPATH_ENTRY_DATE).text();
		String dateTimeStr = dateStr + ' ' + timeStr;
		LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
		long timestamp = dateTime.toEpochSecond(ZoneOffset.UTC);
		return new ChangeRecordDoc(urlStr, timestamp);
	}

	/**
	 * This is the same filter applied in DSI WikiFDA. It eliminates URLs that
	 * are not supposed to be included in the testset.
	 */
	private static boolean filter(String url) {
		return !PATTERN_FILTER.matcher(url).matches();
	}

	private static EnumChangeType detectChangeType(Set<String> classNames) {
		for (String cname : classNames) {
			if (cname.startsWith("mw-changeslist-ns0")) {
				return EnumChangeType.CT_MODIFICATION;
			}
			if (cname.startsWith("mw-changeslist-log")) {
				if (cname == "mw-changeslist-log-delete") {
					return EnumChangeType.CT_LOG_DELETION;
				}
				if (cname == "mw-changeslist-log-move") {
					return EnumChangeType.CT_LOG_MOVE;
				}
				if (cname == "mw-changeslist-log-pagetriage-curation") {
					return EnumChangeType.CT_LOG_CURATION;
				}
				if (cname == "mw-changeslist-log-protect") {
					return EnumChangeType.CT_LOG_PROTECT;
				}
			}
		}
		return EnumChangeType.CT_UNKNOWN;
	}

	public static void main(String[] args) throws Exception {
		testFilter();
	}

	private static void testFilter() {
		String u = "https://en.wikipedia.org/wiki/Microsoft";
		System.out.println(String.format("%s (%b)", u, filter(u)));
		u = "https://fr.wikipedia.org/wiki/Microsoft";
		System.out.println(String.format("%s (%b)", u, filter(u)));
		u = "https://en.wikipedia.org/w/index.php?title=Arlie_Honeycutt&curid=36300049&diff=734172160&oldid=721926619";
		System.out.println(String.format("%s (%b)", u, filter(u)));
	}
}
