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


	// public void create(CrawlRecordDoc record) {
	// final Document doc = new Document(GSON.toJson(record));
	// Callable<Void> func = () -> {
	// documentClient.createDocument(getCollection().getSelfLink(), doc, null,
	// false);
	// return null;
	// };
	// try {
	// UTILS.retry(Config.maxRetries, Config.retryIntervalInMillis, false,
	// func);
	// } catch (Throwable e) {
	// LOGGER.severe(ExceptionUtils.getStackTrace(e));
	// }
	// }
	//
	// public boolean add(CrawlRecordDoc record) {
	// String queryStr = String.format(QRY_EXACT, record.crawlTime);
	// List<Document> docList =
	// documentClient.queryDocuments(getCollection().getSelfLink(), queryStr,
	// null)
	// .getQueryIterable().toList();
	// if (docList.size() > 0) {
	// return false;
	// } else {
	// create(record);
	// return true;
	// }
	// }
	//
	// public int deleteOlderThan(ZonedDateTime cutoff) {
	// long timestamp = cutoff.toEpochSecond();
	// String queryStr = String.format(QRY_CRAWLTIME_LE, timestamp);
	// QueryIterable<Document> docs =
	// documentClient.queryDocuments(getCollection().getSelfLink(), queryStr,
	// null)
	// .getQueryIterable();
	// int deletedCount = 0;
	// try {
	// for (Document doc : docs) {
	// Callable<Void> func = () -> {
	// documentClient.deleteDocument(doc.getSelfLink(), null);
	// return null;
	// };
	// UTILS.retry(Config.maxRetries, Config.retryIntervalInMillis, false,
	// func);
	// deletedCount++;
	// }
	// } catch (Throwable t) {
	// LOGGER.severe(ExceptionUtils.getStackTrace(t));
	// }
	// LOGGER.info(String.format("deleted %d crawl records older than %s",
	// deletedCount, cutoff.toString()));
	// return deletedCount;
	// }

}
