package scraper;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import scraper.db.Config;

/**
 * This scraper downloads the wikipedia change log page (URL by wikiChangeUrl in
 * properties file) in a dynamic adjusted frequency, parses it for change
 * records constituted of URL and time stamps pair, and stores the records in
 * DocumentDB. The scraper also stores the crawl history in the database, of
 * which each record includes the crawl time and the time range of the change
 * history.
 * 
 * @author leizh
 *
 */
public class Main {

	public static final boolean PROD = false;

	private static final Logger LOGGER = Logger.getGlobal();

	private final static ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2);

	private final static ArrayList<AbstractScraper> scraperList = new ArrayList<>();

	public static void main(String[] args) {
		LOGGER.info("== Scraper Initialization ==");
		String tempPath = System.getProperty("user.home") + File.separator + ".scraperWorkingDirectory";
		LOGGER.info("Working directory: " + tempPath);
		Config.initialize();
		scraperList.add(new scraper.wikipedia.Scraper(tempPath));
		scraperList.add(new scraper.redfin.Scraper(tempPath));

		for (AbstractScraper scraper : scraperList) {
			if (scraper.enabled()) {
				SCHEDULER.scheduleAtFixedRate(scraper, scraper.getCrawlInitialDelayInSeconds(),
						scraper.getCrawlIntervalInSeconds(), TimeUnit.SECONDS);
			}
		}
	}
}