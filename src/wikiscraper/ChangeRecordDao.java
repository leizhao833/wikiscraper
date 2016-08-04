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

	/*
	 * public boolean add(ChangeRecordDoc record) { boolean found =
	 * exist(record); if (!found) { create(record); } return !found; }
	 * 
	 * public boolean exist(ChangeRecordDoc record) { String queryStr =
	 * String.format(QRY_EXACT, record.url, record.timestamp); List<Document>
	 * docList = documentClient.queryDocuments(getCollection().getSelfLink(),
	 * queryStr, null) .getQueryIterable().toList(); return docList.size() > 0;
	 * }
	 * 
	 * public int deleteOlderThan(ZonedDateTime cutoff) { long timestamp =
	 * cutoff.toEpochSecond(); String queryStr = String.format(QRY_TIMESTAMP_LE,
	 * timestamp); QueryIterable<Document> docs =
	 * documentClient.queryDocuments(getCollection().getSelfLink(), queryStr,
	 * null) .getQueryIterable(); int deletedCount = 0; try { for (Document doc
	 * : docs) { Callable<Void> func = () -> {
	 * documentClient.deleteDocument(doc.getSelfLink(), null); return null; };
	 * UTILS.retry(Config.maxRetries, Config.retryIntervalInMillis, false,
	 * func); deletedCount++; } } catch (Throwable t) {
	 * LOGGER.severe(ExceptionUtils.getStackTrace(t)); }
	 * LOGGER.info(String.format("deleted %d change records older than %s",
	 * deletedCount, cutoff.toString())); return deletedCount; }
	 */
}
