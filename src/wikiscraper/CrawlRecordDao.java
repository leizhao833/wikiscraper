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

public class CrawlRecordDao extends WikiChangeCollectionDao {

	public static final CrawlRecordDao INSTANCE = new CrawlRecordDao();

	private static final Gson GSON = new Gson();
	private static final Logger LOGGER = Logger.getGlobal();
	private static final String QRY_EXACT = "SELECT * FROM c WHERE c.crawlTime = %d";
	private static final String QRY_CRAWLTIME_LE = "SELECT * FROM c WHERE c.crawlTime < %d";

	private CrawlRecordDao() {
	}

	@Override
	protected String getCollectionId() {
		return Config.crawlRecordCollectionId;
	}

	public CrawlRecordDoc create(CrawlRecordDoc record) {
		Document doc = new Document(GSON.toJson(record));

		try {
			doc = documentClient.createDocument(getCollection().getSelfLink(), doc, null, false).getResource();
		} catch (DocumentClientException e) {
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
			return null;
		}

		return GSON.fromJson(doc.toString(), CrawlRecordDoc.class);
	}

	public boolean add(CrawlRecordDoc record) {
		String queryStr = String.format(QRY_EXACT, record.crawlTime);
		List<Document> docList = documentClient.queryDocuments(getCollection().getSelfLink(), queryStr, null)
				.getQueryIterable().toList();
		if (docList.size() > 0) {
			return false;
		} else {
			create(record);
			return true;
		}
	}

	public int deleteOlderThan(ZonedDateTime cutoff) {
		long timestamp = cutoff.toEpochSecond();
		String queryStr = String.format(QRY_CRAWLTIME_LE, timestamp);
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
		LOGGER.info(String.format("deleted %d crawl records older than %s", deletedCount, cutoff.toString()));
		return deletedCount;
	}

}
