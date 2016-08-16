/**
 * 
 */
package wikiscraper;

public class ChangeRecordDao extends AbstractCollectionDao {

	public static final ChangeRecordDao INSTANCE = new ChangeRecordDao();

	private static final String QRY_TIMESTAMP_LE = "SELECT * FROM c WHERE c.timestamp < %d";

	private ChangeRecordDao() {
	}

	@Override
	protected String getCollectionId() {
		return Config.changeRecordCollectionId;
	}

	@Override
	public String getQueryStringOlderThan(long timestamp) {
		return String.format(QRY_TIMESTAMP_LE, timestamp);
	}

	@Override
	protected int getDefaultExpiryInSeconds() {
		return Config.changeRecordExpiryInDays * 86400;
	}

}
