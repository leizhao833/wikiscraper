/**
 * 
 */
package wikiscraper;

import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClientException;

public class ChangeRecordDao extends WikiChangeCollectionDao {

	public static final ChangeRecordDao INSTANCE = new ChangeRecordDao();

	private static final Logger LOGGER = Logger.getGlobal();
	private static Gson gson = new Gson();
	private final String QRY_EXACT_DOC = "SELECT * FROM c WHERE c.url = '%s' AND c.timestamp = %d";

	private ChangeRecordDao() {
	}

	@Override
	protected String getCollectionId() {
		return "ChangeRecordCollection";
	}

	public ChangeRecordDoc create(ChangeRecordDoc record) {
		Document doc = new Document(gson.toJson(record));

		try {
			doc = documentClient.createDocument(getCollection().getSelfLink(), doc, null, false).getResource();
		} catch (DocumentClientException e) {
			LOGGER.severe(e.getMessage());
			return null;
		}

		return gson.fromJson(doc.toString(), ChangeRecordDoc.class);
	}

	public boolean add(ChangeRecordDoc record) {
		boolean found = exist(record);
		if (!found) {
			create(record);
		}
		return !found;
	}

	public boolean exist(ChangeRecordDoc record) {
		String queryStr = String.format(QRY_EXACT_DOC, record.url, record.timestamp);
		List<Document> docList = documentClient.queryDocuments(getCollection().getSelfLink(), queryStr, null)
				.getQueryIterable().toList();
		return docList.size() > 0;
	}

}
