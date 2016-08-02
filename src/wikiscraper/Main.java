package wikiscraper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
	static File LOCAL_TEST_FILE = new File("dat\\wikirecentchange.html");
	static String XPATH_ROOT_DATE = "div.mw-changeslist > h4";
	static String XPATH_ROOT_LIST = "div.mw-changeslist > ul.special > li";
	static String XPATH_ENTRY_TITLE = "span.mw-title > a, abbr.wikibase-edit + a";
	static String XPATH_ENTRY_DATE = "span.mw-changeslist-date";

	static String WIKI_CHANGE_URL = "https://en.wikipedia.org/w/index.php?namespace=0&days=1&limit=5000&hideminor=0&title=Special:RecentChanges&hideWikibase=0&hidebots=0";
	static String WIKI_CHANGE_URL_TEST = "https://en.wikipedia.org/w/index.php?namespace=0&days=1&limit=100&hideminor=0&title=Special:RecentChanges&hideWikibase=0&hidebots=0";

	static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm");

	private static ChangeRecordDao changeRecordDao = new ChangeRecordDao();
	private static CrawlRecordDao crawlRecordDao = new CrawlRecordDao();

	private final static Logger logger = Logger.getGlobal();

	public static void main(String[] args) throws Exception {
		process(getDoc());
	}

	private static void process(Document doc) {
		ZonedDateTime crawlTime = ZonedDateTime.now();
		ChangeRecordDoc changeRecord = null;
		ChangeRecordDoc changeMin = new ChangeRecordDoc(null, Long.MAX_VALUE);
		ChangeRecordDoc changeMax = new ChangeRecordDoc(null, 0);
		String dateStr = doc.select(XPATH_ROOT_DATE).first().text();
		List<ChangeRecordDoc> changeRecordList = new ArrayList<ChangeRecordDoc>();
		for (Element elem : doc.select(XPATH_ROOT_LIST)) {
			Set<String> classNames = elem.classNames();
			assert (classNames != null);
			EnumChangeType changeType = detectChangeType(elem.classNames());
			switch (changeType) {
			case CT_MODIFICATION:
				changeRecord = parseModification(elem, dateStr);
				changeRecordList.add(changeRecord);
//				System.out.println(">> " + changeRecord);
				if (changeRecord.timestamp > changeMax.timestamp) {
					changeMax = changeRecord;
				}
				if (changeRecord.timestamp < changeMin.timestamp) {
					changeMin = changeRecord;
				}
				break;
			case CT_LOG_DELETION:
			case CT_LOG_MOVE:
			case CT_LOG_CURATION:
			case CT_LOG_PROTECT:
			case CT_UNKNOWN:
				System.err.println("Ignoring: " + changeType + ' ' + elem.text());
			}
			// byte[] buf = new byte[1];
			// System.in.read(buf);
		}
		CrawlRecordDoc crawlRecord = new CrawlRecordDoc(crawlTime.toEpochSecond(), changeMin.timestamp,
				changeMax.timestamp);
		System.out.println("Adding crawl record: " + crawlRecord);
		if (!crawlRecordDao.add(crawlRecord)) {
			// TODO: something is wrong
			System.err.println("Crawl record duplication");
		}
		System.out.println("Verifying crawl frequency: " + changeMin);
		if (!changeRecordDao.exist(changeMin)) {
			// TODO:
			System.err.println("No overlap between stored and current change records. Some changes might be lost");
		}
		System.out.println("Adding change records: " + changeRecordList.size());
		for (ChangeRecordDoc changeRecordDoc : changeRecordList) {
			boolean added = changeRecordDao.add(changeRecordDoc);
			System.out.println("+ " + changeRecordDoc + " " + added);

		}
	}

	private static ChangeRecordDoc parseModification(Element elem, String dateStr) {
		Elements elems = elem.select(XPATH_ENTRY_TITLE);
		String url = elems.first().attr("href");
		String timeStr = elem.select(XPATH_ENTRY_DATE).text();
		String dateTimeStr = dateStr + ' ' + timeStr;
		LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
		long timestamp = dateTime.toEpochSecond(ZoneOffset.UTC);

		return new ChangeRecordDoc(url, timestamp);
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

	private static Document getDoc() {
		try {
			// return Jsoup.parse(LOCAL_TEST_FILE,
			// Charset.defaultCharset().name());
			return crawlPage(WIKI_CHANGE_URL_TEST);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static Document crawlPage(String urlStr) throws IOException {
		URL url;
		try {
			url = new URL(urlStr);
			return Jsoup.parse(url, 20000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
