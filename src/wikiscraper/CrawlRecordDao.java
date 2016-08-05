/**
 * 
 */
package wikiscraper;

public class CrawlRecordDao extends AbstractCollectionDao {

	public static final CrawlRecordDao INSTANCE = new CrawlRecordDao();

	private static final String QRY_CRAWLTIME_LE = "SELECT * FROM c WHERE c.crawlTime < %d";

	private CrawlRecordDao() {
	}

	@Override
	protected String getCollectionId() {
		return Config.crawlRecordCollectionId;
	}
	
	@Override
	public String getQueryStringOlderThan(long timestamp) {
		return String.format(QRY_CRAWLTIME_LE, timestamp);
	}

}
