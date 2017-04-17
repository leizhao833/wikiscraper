package scraper;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import scraper.db.Config;

/**
 * @author leizh
 *
 */
public class Main {

	public static final boolean PROD = true;

	private final static Logger LOGGER = Logger.getGlobal();
	private final static ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2);
	private final static ArrayList<AbstractScraper> SCRAPERS = new ArrayList<>();

	public static void main(String[] args) {
		initialization();
		serviceLoop();
	}

	private static void initialization() {
		LOGGER.info("== Scraper Initialization ==");
		String tempPath = System.getProperty("user.home") + File.separator + ".scraperWorkingDirectory";
		LOGGER.info("Working directory: " + tempPath);
		Config.initialize();
		SCRAPERS.add(new scraper.wikipedia.Scraper(tempPath));
		SCRAPERS.add(new scraper.redfin.Scraper(tempPath));
	}

	private static void serviceLoop() {
		for (AbstractScraper scraper : SCRAPERS) {
			if (scraper.enabled()) {
				SCHEDULER.scheduleAtFixedRate(scraper, scraper.getCrawlInitialDelayInSeconds(),
						scraper.getCrawlIntervalInSeconds(), TimeUnit.SECONDS);
			}
		}
	}
}