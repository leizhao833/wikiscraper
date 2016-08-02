/**
 * 
 */
package wikiscraper;

import java.util.List;

import com.google.gson.Gson;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClientException;

public class ChangeRecordDao extends WikiChangeCollectionDao {

	private static Gson gson = new Gson();

	private final String QRY_EXACT_DOC = "SELECT * FROM c WHERE c.url = '%s' AND c.timestamp = %d";

	@Override
	protected String getCollectionId() {
		return "ChangeRecordCollection";
	}

	public ChangeRecordDoc create(ChangeRecordDoc record) {
		Document doc = new Document(gson.toJson(record));

		try {
			doc = documentClient.createDocument(getCollection().getSelfLink(), doc, null, false).getResource();
		} catch (DocumentClientException e) {
			e.printStackTrace();
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
