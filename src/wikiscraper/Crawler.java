package wikiscraper;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Crawler {

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
			LOGGER.info(String.format("no overlap, cut crawl interval by half to %ds", crawlIntervalInSec));
		} else {
			crawlIntervalInSec += Config.crawlIntervalIncInSeconds;
			LOGGER.info(String.format("overlap detected, increasing crawl interval by %ds to %ds",
					Config.crawlIntervalIncInSeconds, crawlIntervalInSec));
		}
		if (crawlIntervalInSec > Config.crawlIntervalMaxInSeconds) {
			crawlIntervalInSec = Config.crawlIntervalMaxInSeconds;
			LOGGER.info(String.format("crawl interval too large - cap it at %ds", crawlIntervalInSec));
		}
		LocalDateTime targetTime = lastCrawlTime.plusSeconds(crawlIntervalInSec);
		LOGGER.info(String.format("crawl interval %ds, next crawl at %s", crawlIntervalInSec, targetTime.toString()));
		Utils.sleepUntil(targetTime);
	}

	private static URL getUrl() {
		if (url == null) {
			try {
				url = new URL(Config.wikiChangeUrl);
			} catch (MalformedURLException e) {
				StringBuilder sb = new StringBuilder();
				sb.append(String.format("invalid url %s%n", Config.wikiChangeUrl));
				sb.append(String.format("%s%n", ExceptionUtils.getStackTrace(e)));
				sb.append("sleep forever ...");
				LOGGER.severe(sb.toString());
				Utils.sleepForever();
			}
		}
		return url;
	}

	private static Document crawlPage() {
		LOGGER.info(String.format("begin downloading %s", Config.wikiChangeUrl));
		Callable<Document> func = () -> {
			Document doc = Jsoup.parse(getUrl(), 20000);
			return doc;
		};
		try {
			Document doc = UTILS.retry(Config.maxRetries, Config.retryIntervalInMillis, true, func);
			LOGGER.info(String.format("download succeeded"));
			return doc;
		} catch (Throwable e) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("maximum %d attempts reached, unable to download the page%n",
					Config.maxRetries));
			sb.append(String.format("%s%n", ExceptionUtils.getStackTrace(e)));
			sb.append("sleep forever ...");
			LOGGER.severe(sb.toString());
			Utils.sleepForever();
			return null;
		}
	}

}
