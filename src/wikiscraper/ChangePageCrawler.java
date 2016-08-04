package wikiscraper;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ChangePageCrawler {

	private static final Logger LOGGER = Logger.getGlobal();
	private static final Utils<Document> UTILS = new Utils<Document>();
	private static long crawlIntervalInSec = Config.crawlIntervalMaxInSeconds;
	private static LocalDateTime lastCrawlTime = LocalDateTime.now();
	private static URL url;

	public static Document getDocument(boolean overlap, boolean immediately) {
		// return Jsoup.parse(LOCAL_TEST_FILE, Charset.defaultCharset().name());
		waitUntilNextCrawl(overlap, immediately);
		Document doc = crawlPage();
		lastCrawlTime = LocalDateTime.now();
		return doc;
	}

	public static LocalDateTime getLastCrawlTime() {
		return lastCrawlTime;
	}

	private static void waitUntilNextCrawl(boolean overlap, boolean immediately) {
		if (immediately) {
			LOGGER.info("start next crawl immediately");
			return;
		}
		if (!overlap) {
			crawlIntervalInSec /= 2;
			LOGGER.info(String.format("no overlap - cut crawl interval by half to %d", crawlIntervalInSec));
		} else {
			crawlIntervalInSec += Config.crawlIntervalIncInSeconds; // +5min
			LOGGER.info(String.format("overlap detected - increasing crawl interval by %d to %d",
					Config.crawlIntervalIncInSeconds, crawlIntervalInSec));
		}
		if (crawlIntervalInSec > Config.crawlIntervalMaxInSeconds) {
			crawlIntervalInSec = Config.crawlIntervalMaxInSeconds;
			LOGGER.info(String.format("crawl interval too large - cap it at %d", crawlIntervalInSec));
		}
		LocalDateTime targetTime = lastCrawlTime.plusSeconds(crawlIntervalInSec);
		LOGGER.info(String.format("crawl interval: %d - next crawl at %s", crawlIntervalInSec, targetTime.toString()));
		Utils.sleepUntil(targetTime);
	}

	private static URL getUrl() {
		if (url == null) {
			try {
				url = new URL(Config.wikiChangeUrl);
			} catch (MalformedURLException e) {
				LOGGER.severe(String.format("invalid url %s", Config.wikiChangeUrl));
				LOGGER.severe(ExceptionUtils.getStackTrace(e));
				LOGGER.severe("sleep forever ...");
				Utils.sleepUntil(LocalDateTime.now().plusYears(1));
			}
		}
		return url;
	}

	private static Document crawlPage() {

		Callable<Document> c = () -> {
			Document doc = Jsoup.parse(getUrl(), 20000);
			return doc;
		};
		
		LOGGER.info(String.format("begin downloading %s", Config.wikiChangeUrl));

		try {
			Document doc = UTILS.retry(Config.maxDownloadRetries, 2000, true, c);
			LOGGER.info(String.format("succeeded"));
			return doc;
		} catch (Throwable e) {
			LOGGER.severe(String.format("maximum %d attempts reached. Unable to download the page. Sleep forever ...",
					Config.maxDownloadRetries));
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
			Utils.sleepUntil(LocalDateTime.now().plusYears(1));
			return null;
		}
	}

}
