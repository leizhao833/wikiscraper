/**
 * 
 */
package scraper.db;

import scraper.ScraperConfig;

public class CrawlRecordDao extends AbstractCollectionDao {

	private static final String QRY_CRAWLTIME_LE = "SELECT * FROM c WHERE c.crawlTime < %d";

	private ScraperConfig config;

	public CrawlRecordDao(ScraperConfig config) {
		this.config = config;
	}

	@Override
	protected String getCollectionId() {
		return config.crawlRecordCollectionId;
	}

	@Override
	public String getQueryStringOlderThan(long timestamp) {
		return String.format(QRY_CRAWLTIME_LE, timestamp);
	}

	@Override
	protected int getDefaultExpiryInSeconds() {
		return config.crawlRecordExpiryInDays * 86400;
	}

}
