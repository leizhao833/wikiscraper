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
	private static long crawlIntervalInSec;
	private static LocalDateTime lastCrawlTime;

	public static void initialize() {
		crawlIntervalInSec = Config.crawlIntervalMaxInSeconds / 2;
		lastCrawlTime = LocalDateTime.now();
	}

	public static String getDocument(float overlap, boolean immediately) {
		waitUntilNextCrawl(overlap, immediately);
		String html = crawlPage();
		lastCrawlTime = LocalDateTime.now();
		return html;
	}

	public static LocalDateTime getLastCrawlTime() {
		return lastCrawlTime;
	}

	private static void waitUntilNextCrawl(float overlap, boolean immediately) {
		if (immediately) {
			LOGGER.info("start next crawl immediately");
			return;
		}
		if (overlap < 0.2) {
			crawlIntervalInSec /= 2;
			LOGGER.info(String.format("overlap [%.1f]. interval=/2 to [%ds]", overlap, crawlIntervalInSec));
		} else {
			crawlIntervalInSec += Config.crawlIntervalIncInSeconds;
			LOGGER.info(String.format("overlap [%.1f]. interval+[%ds] to [%ds]", overlap,
					Config.crawlIntervalIncInSeconds, crawlIntervalInSec));
		}
		if (crawlIntervalInSec > Config.crawlIntervalMaxInSeconds) {
			crawlIntervalInSec = Config.crawlIntervalMaxInSeconds;
			LOGGER.info(String.format("interval too larg. cap at [%ds]", crawlIntervalInSec));
		}
		LocalDateTime targetTime = lastCrawlTime.plusSeconds(crawlIntervalInSec);
		LOGGER.info(String.format("next crawl at [%s]", targetTime.toString()));
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
