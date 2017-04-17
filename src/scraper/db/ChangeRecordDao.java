/**
 * 
 */
package scraper.db;

import scraper.ScraperConfig;

public class ChangeRecordDao extends AbstractCollectionDao {

	private static final String QRY_TIMESTAMP_LE = "SELECT * FROM c WHERE c.timestamp < %d";

	private ScraperConfig config;

	public ChangeRecordDao(ScraperConfig config) {
		this.config = config;
	}

	@Override
	protected String getCollectionId() {
		return config.changeRecordCollectionId;
	}

	@Override
	public String getQueryStringOlderThan(long timestamp) {
		return String.format(QRY_TIMESTAMP_LE, timestamp);
	}

	@Override
	protected int getDefaultExpiryInSeconds() {
		return config.changeRecordExpiryInDays * 86400;
	}

}
