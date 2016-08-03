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
	 private static final String WIKI_CHANGE_URL =
	 "https://en.wikipedia.org/w/index.php?namespace=0&days=1&limit=5000&hideminor=0&title=Special:RecentChanges&hideWikibase=0&hidebots=0";
	 private static final long CRAWL_INTERVAL_MAX = 30 * 60; // 30 min
	 private static final long CRAWL_INTERVAL_INC = 5 * 60; // 5 min
	// private static final String WIKI_CHANGE_URL =
	// "https://en.wikipedia.org/w/index.php?namespace=0&days=1&limit=100&hideminor=0&title=Special:RecentChanges&hideWikibase=0&hidebots=0";
	// private static final long CRAWL_INTERVAL_MAX = 3 * 60; // 30 sec
	// private static final long CRAWL_INTERVAL_INC = 5; // 5 s
	private static final int MAX_RETRIES = 10;
	private static long crawlIntervalInSec = CRAWL_INTERVAL_MAX;
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
			crawlIntervalInSec += CRAWL_INTERVAL_INC; // +5min
			LOGGER.info(String.format("overlap detected - increasing crawl interval by %d to %d", CRAWL_INTERVAL_INC,
					crawlIntervalInSec));
		}
		if (crawlIntervalInSec > CRAWL_INTERVAL_MAX) {
			crawlIntervalInSec = CRAWL_INTERVAL_MAX;
			LOGGER.info(String.format("crawl interval too large - cap it at %d", crawlIntervalInSec));
		}
		LocalDateTime targetTime = lastCrawlTime.plusSeconds(crawlIntervalInSec);
		LOGGER.info(String.format("crawl interval: %d - next crawl at %s", crawlIntervalInSec,
				targetTime.toString()));
		sleepUntil(targetTime);
	}

	private static URL getUrl() {
		if (url == null) {
			try {
				url = new URL(WIKI_CHANGE_URL);
			} catch (MalformedURLException e) {
				LOGGER.severe(String.format("invalid url %s", WIKI_CHANGE_URL));
				LOGGER.severe(e.getMessage());
				LOGGER.severe("sleep forever ...");
				sleepUntil(LocalDateTime.now().plusYears(1));
			}
		}
		return url;
	}

	private static Document crawlPage() {
		int attempts = 0;
		while (attempts++ < MAX_RETRIES) {
			try {
				LOGGER.info(String.format("downloading %s at attempt %d", WIKI_CHANGE_URL, attempts));
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
		LOGGER.severe(
				String.format("maximum %d attempts reached. Unable to download the page. Sleep forever ...", attempts));
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
