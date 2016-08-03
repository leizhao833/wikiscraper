/**
 * 
 */
package wikiscraper;

import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClientException;

public class CrawlRecordDao extends WikiChangeCollectionDao {

	public static final CrawlRecordDao INSTANCE = new CrawlRecordDao();

	private static final Logger LOGGER = Logger.getGlobal();
	private static final Gson GSON = new Gson();
	private static final String QRY_EXACT_DOC = "SELECT * FROM c WHERE c.crawlTime = %d";

	private CrawlRecordDao() {
	}

	@Override
	protected String getCollectionId() {
		return "CrawlRecordCollection";
	}

	public CrawlRecordDoc create(CrawlRecordDoc record) {
		Document doc = new Document(GSON.toJson(record));

		try {
			doc = documentClient.createDocument(getCollection().getSelfLink(),
					doc, null, false).getResource();
		} catch (DocumentClientException e) {
			LOGGER.severe(e.getMessage());
			return null;
		}

		return GSON.fromJson(doc.toString(), CrawlRecordDoc.class);
	}

	public boolean add(CrawlRecordDoc record) {
		String queryStr = String.format(QRY_EXACT_DOC, record.crawlTime);
		List<Document> docList = documentClient
				.queryDocuments(getCollection().getSelfLink(), queryStr, null)
				.getQueryIterable().toList();
		if (docList.size() > 0) {
			return false;
		} else {
			create(record);
			return true;
		}
	}

}
