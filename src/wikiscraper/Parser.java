package wikiscraper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Parser {

	private static final Logger LOGGER = Logger.getGlobal();
	private final static String XPATH_ROOT = "div.mw-changeslist";
	private final static String XPATH_ROOT_LIST = "li";
	private final static String XPATH_ENTRY_TITLE = "span.mw-title a, abbr.wikibase-edit + a";
	private final static String XPATH_ENTRY_DATE = "span.mw-changeslist-date";
	private final static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm");

	public static Set<ChangeRecordDoc> parse(Document doc) {
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
						recordSet.add(changeRecord);
						modifyCount++;
						break;
					default:
						ignoreCount++;
						LOGGER.fine(String.format("Ignoring: %s %s", changeType.toString(), li.text()));
					}
				}
			}
		}
		LOGGER.info(String.format("done parsing page, modify(distinct) %d(%d), ignored %d", modifyCount,
				recordSet.size(), ignoreCount));
		return recordSet;
	}

	private static ChangeRecordDoc parseModification(Element elem, String dateStr) {
		Elements elems = elem.select(XPATH_ENTRY_TITLE);
		String urlStr = elems.first().attr("abs:href");
		String timeStr = elem.select(XPATH_ENTRY_DATE).text();
		String dateTimeStr = dateStr + ' ' + timeStr;
		LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
		long timestamp = dateTime.toEpochSecond(ZoneOffset.UTC);
		return new ChangeRecordDoc(urlStr, timestamp);
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
}
