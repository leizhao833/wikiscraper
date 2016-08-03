/**
 * 
 */
package wikiscraper;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.gson.Gson;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.QueryIterable;

public class ChangeRecordDao extends WikiChangeCollectionDao {

	public static final ChangeRecordDao INSTANCE = new ChangeRecordDao();

	private static final Gson GSON = new Gson();
	private static final Logger LOGGER = Logger.getGlobal();
	private static final String QRY_EXACT = "SELECT * FROM c WHERE c.url = '%s' AND c.timestamp = %d";
	private static final String QRY_TIMESTAMP_LE = "SELECT * FROM c WHERE c.timestamp < %d";

	private ChangeRecordDao() {
	}

	@Override
	protected String getCollectionId() {
		return Config.changeRecordCollectionId;
	}

	public ChangeRecordDoc create(ChangeRecordDoc record) {
		Document doc = new Document(GSON.toJson(record));

		try {
			doc = documentClient.createDocument(getCollection().getSelfLink(), doc, null, false).getResource();
		} catch (DocumentClientException e) {
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
			return null;
		}

		return GSON.fromJson(doc.toString(), ChangeRecordDoc.class);
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
				documentClient.deleteDocument(doc.getSelfLink(), null);
				deletedCount++;
			}
		} catch (DocumentClientException e) {
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
		LOGGER.info(String.format("deleted %d change records older than %s", deletedCount, cutoff.toString()));
		return deletedCount;
	}
}
