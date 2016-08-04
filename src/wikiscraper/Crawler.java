package wikiscraper;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class Crawler {

	private static final Logger LOGGER = Logger.getGlobal();
	private static final Utils<String> UTILS = new Utils<String>();
	private static long crawlIntervalInSec = Config.crawlIntervalMaxInSeconds;
	private static LocalDateTime lastCrawlTime = LocalDateTime.now();

	public static String getDocument(boolean overlap, boolean immediately) {
		waitUntilNextCrawl(overlap, immediately);
		String html = crawlPage();
		lastCrawlTime = LocalDateTime.now();
		return html;
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


	private static String crawlPage() {
		Callable<String> func = () -> {
			Connection conn = Jsoup.connect(Config.wikiChangeUrl);
			conn.timeout(60 * 1000); // 1 min
			conn.maxBodySize(0);
			return conn.execute().body();
		};
		try {
			LOGGER.info(String.format("begin downloading %s", Config.wikiChangeUrl));
			String html = UTILS.retry(Config.maxRetries, Config.retryIntervalInMillis, true, func);
			LOGGER.info(String.format("download succeeded"));
			return html;
		} catch (Throwable e) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("maximum %d attempts reached, unable to download the page%n", Config.maxRetries));
			sb.append(String.format("%s%n", ExceptionUtils.getStackTrace(e)));
			sb.append("sleep forever ...");
			LOGGER.severe(sb.toString());
			Utils.sleepForever();
			return null;
		}
	}

}
