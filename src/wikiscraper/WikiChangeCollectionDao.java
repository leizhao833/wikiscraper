package wikiscraper;

import java.util.List;

import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.RequestOptions;

public abstract class WikiChangeCollectionDao {

	// The name of our database.
	private static final String DATABASE_ID = "WikiChangeDB";

	// The DocumentDB Client
	protected static DocumentClient documentClient = DocumentClientFactory
			.getDocumentClient();

	// Cache for the database object, so we don't have to query for it to
	// retrieve self links.
	private static Database databaseCache;

	// The name of our collection.
	private String collectionId;

	// Cache for the collection object, so we don't have to query for it to
	// retrieve self links.
	private DocumentCollection collectionCache;

	public WikiChangeCollectionDao() {
		collectionId = getCollectionId();
	}

	protected abstract String getCollectionId();

	private Database getDatabase() {
		if (databaseCache == null) {
			// Get the database if it exists
			List<Database> databaseList = documentClient
					.queryDatabases(
							"SELECT * FROM root r WHERE r.id='" + DATABASE_ID
									+ "'", null).getQueryIterable().toList();

			if (databaseList.size() > 0) {
				// Cache the database object so we won't have to query for it
				// later to retrieve the selfLink.
				databaseCache = databaseList.get(0);
			} else {
				// Create the database if it doesn't exist.
				try {
					Database databaseDefinition = new Database();
					databaseDefinition.setId(DATABASE_ID);

					databaseCache = documentClient.createDatabase(
							databaseDefinition, null).getResource();
				} catch (DocumentClientException e) {
					// TODO: Something has gone terribly wrong - the app wasn't
					// able to query or create the collection.
					// Verify your connection, endpoint, and key.
					e.printStackTrace();
				}
			}
		}

		return databaseCache;
	}

	protected DocumentCollection getCollection() {
		if (collectionCache == null) {
			// Get the collection if it exists.
			List<DocumentCollection> collectionList = documentClient
					.queryCollections(
							getDatabase().getSelfLink(),
							"SELECT * FROM root r WHERE r.id='" + collectionId
									+ "'", null).getQueryIterable().toList();

			if (collectionList.size() > 0) {
				// Cache the collection object so we won't have to query for it
				// later to retrieve the selfLink.
				collectionCache = collectionList.get(0);
			} else {
				// Create the collection if it doesn't exist.
				try {
					DocumentCollection collectionDefinition = new DocumentCollection();
					collectionDefinition.setId(collectionId);

					// Configure the new collection performance tier to S1.
					RequestOptions requestOptions = new RequestOptions();
					requestOptions.setOfferType("S1");

					collectionCache = documentClient.createCollection(
							getDatabase().getSelfLink(),
							collectionDefinition, requestOptions).getResource();
				} catch (DocumentClientException e) {
					// TODO: Something has gone terribly wrong - the app wasn't
					// able to query or create the collection.
					// Verify your connection, endpoint, and key.
					e.printStackTrace();
				}
			}
		}

		return collectionCache;
	}

}