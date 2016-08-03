package wikiscraper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ChangePageCrawler {

	private static final Logger LOGGER = Logger.getGlobal();
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
			LOGGER.info(String.format("overlap detected - increasing crawl interval by %d to %d", Config.crawlIntervalIncInSeconds,
					crawlIntervalInSec));
		}
		if (crawlIntervalInSec > Config.crawlIntervalMaxInSeconds) {
			crawlIntervalInSec = Config.crawlIntervalMaxInSeconds;
			LOGGER.info(String.format("crawl interval too large - cap it at %d", crawlIntervalInSec));
		}
		LocalDateTime targetTime = lastCrawlTime.plusSeconds(crawlIntervalInSec);
		LOGGER.info(String.format("crawl interval: %d - next crawl at %s", crawlIntervalInSec, targetTime.toString()));
		sleepUntil(targetTime);
	}

	private static URL getUrl() {
		if (url == null) {
			try {
				url = new URL(Config.wikiChangeUrl);
			} catch (MalformedURLException e) {
				LOGGER.severe(String.format("invalid url %s", Config.wikiChangeUrl));
				LOGGER.severe(e.getMessage());
				LOGGER.severe("sleep forever ...");
				sleepUntil(LocalDateTime.now().plusYears(1));
			}
		}
		return url;
	}

	private static Document crawlPage() {
		int attempts = 0;
		while (attempts++ < Config.maxDownloadRetries) {
			try {
				LOGGER.info(String.format("downloading %s at attempt %d", Config.wikiChangeUrl, attempts));
				Document doc = Jsoup.parse(getUrl(), 20000);
				LOGGER.info(String.format("downloading succeeded"));
				return doc;
			} catch (IOException e) {
				long sleepInSec = (long) Math.pow(2, attempts);
				LocalDateTime next = LocalDateTime.now().plusSeconds(sleepInSec);
				LOGGER.warning(String.format("downloading failed at attempt %d. Sleep for %d seconds and retry.",
						attempts, sleepInSec));
				LOGGER.warning(e.toString());
				sleepUntil(next);
			}
		}
		LOGGER.severe(String.format("maximum %d attempts reached. Unable to download the page. Sleep forever ...",
				attempts));
		sleepUntil(LocalDateTime.now().plusYears(1));
		return null;
	}

	private static void sleepUntil(LocalDateTime next) {
		while (true) {
			try {
				LocalDateTime now = LocalDateTime.now();
				long intervalInMillis = now.until(next, ChronoUnit.MILLIS);
				if (intervalInMillis > 0) {
					Thread.sleep(intervalInMillis);
				}
				return;
			} catch (InterruptedException e) {
				LOGGER.warning("Sleeping interruptted. Resuming ...");
			}
		}
	}
}
