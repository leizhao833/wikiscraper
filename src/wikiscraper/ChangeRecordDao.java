/**
 * 
 */
package wikiscraper;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.gson.Gson;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.QueryIterable;

public class ChangeRecordDao extends WikiChangeCollectionDao {

	public static final ChangeRecordDao INSTANCE = new ChangeRecordDao();

	private static final Gson GSON = new Gson();
	private static final Logger LOGGER = Logger.getGlobal();
	private static final String QRY_EXACT = "SELECT * FROM c WHERE c.url = '%s' AND c.timestamp = %d";
	private static final String QRY_TIMESTAMP_LE = "SELECT * FROM c WHERE c.timestamp < %d";
	private static final Utils<Void> UTILS = new Utils<Void>();

	private ChangeRecordDao() {
	}

	@Override
	protected String getCollectionId() {
		return Config.changeRecordCollectionId;
	}

	public void create(ChangeRecordDoc record) {
		final Document doc = new Document(GSON.toJson(record));

		Callable<Void> c = () -> {
			documentClient.createDocument(getCollection().getSelfLink(), doc, null, false);
			return null;
		};
		
		try {
			UTILS.retry(Config.maxDatabaseQueryRetries, Config.intervalBetweenQueriesInMillis, false, c);
		} catch (Throwable e) {
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
	}

	public boolean add(ChangeRecordDoc record) {
		boolean found = exist(record);
		if (!found) {
			create(record);
		}
		return !found;
	}

	public boolean exist(ChangeRecordDoc record) {
		String queryStr = String.format(QRY_EXACT, record.url, record.timestamp);
		List<Document> docList = documentClient.queryDocuments(getCollection().getSelfLink(), queryStr, null)
				.getQueryIterable().toList();
		return docList.size() > 0;
	}

	public int deleteOlderThan(ZonedDateTime cutoff) {
		long timestamp = cutoff.toEpochSecond();
		String queryStr = String.format(QRY_TIMESTAMP_LE, timestamp);
		QueryIterable<Document> docs = documentClient.queryDocuments(getCollection().getSelfLink(), queryStr, null)
				.getQueryIterable();
		int deletedCount = 0;
		try {
			for (Document doc : docs) {
				Callable<Void> c = () -> {
					documentClient.deleteDocument(doc.getSelfLink(), null);
					return null;
				};
				UTILS.retry(Config.maxDatabaseQueryRetries, Config.intervalBetweenQueriesInMillis, false, c);
				deletedCount++;
			}
		} catch (Throwable t) {
			LOGGER.severe(ExceptionUtils.getStackTrace(t));
		}
		LOGGER.info(String.format("deleted %d change records older than %s", deletedCount, cutoff.toString()));
		return deletedCount;
	}
}
