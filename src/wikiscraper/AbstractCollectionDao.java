package wikiscraper;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.gson.Gson;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.QueryIterable;
import com.microsoft.azure.documentdb.RequestOptions;

public abstract class AbstractCollectionDao {

	private static final Logger LOGGER = Logger.getGlobal();
	private static final Gson GSON = new Gson();
	private static final Utils<Void> UTILS = new Utils<Void>();
	private static Database databaseCache;

	private String collectionId;
	private DocumentCollection collectionCache;

	protected static DocumentClient documentClient = DocumentClientFactory.getDocumentClient();

	public AbstractCollectionDao() {
		collectionId = getCollectionId();
	}

	protected abstract String getCollectionId();

	protected abstract String getQueryStringOlderThan(long timestamp);

	private Database getDatabase() {
		if (databaseCache == null) {
			List<Database> databaseList = documentClient
					.queryDatabases("SELECT * FROM root r WHERE r.id='" + Config.databaseId + "'", null)
					.getQueryIterable().toList();
			if (databaseList.size() > 0) {
				databaseCache = databaseList.get(0);
			} else {
				Utils<Database> utils = new Utils<Database>();
				Callable<Database> func = () -> {
					Database databaseDefinition = new Database();
					databaseDefinition.setId(Config.databaseId);
					return documentClient.createDatabase(databaseDefinition, null).getResource();
				};
				try {
					databaseCache = utils.retry(Config.maxRetries, Config.retryIntervalInMillis, true, func);
				} catch (Throwable e) {
					LOGGER.severe(ExceptionUtils.getStackTrace(e));
				}
			}
		}
		return databaseCache;
	}

	protected DocumentCollection getCollection() {
		if (collectionCache == null) {
			List<DocumentCollection> collectionList = documentClient.queryCollections(getDatabase().getSelfLink(),
					"SELECT * FROM root r WHERE r.id='" + collectionId + "'", null).getQueryIterable().toList();
			if (collectionList.size() > 0) {
				collectionCache = collectionList.get(0);
			} else {
				Utils<DocumentCollection> utils = new Utils<DocumentCollection>();
				Callable<DocumentCollection> func = () -> {
					DocumentCollection collectionDefinition = new DocumentCollection();
					collectionDefinition.setId(collectionId);
					RequestOptions requestOptions = new RequestOptions();
					requestOptions.setOfferType("S1");
					return documentClient
							.createCollection(getDatabase().getSelfLink(), collectionDefinition, requestOptions)
							.getResource();
				};
				try {
					collectionCache = utils.retry(Config.maxRetries, Config.retryIntervalInMillis, true, func);
				} catch (Throwable e) {
					LOGGER.severe(ExceptionUtils.getStackTrace(e));
				}
			}
		}
		return collectionCache;
	}

	public void create(AbstractDocument record) {
		final Document doc = new Document(GSON.toJson(record));
		Callable<Void> func = () -> {
			documentClient.createDocument(getCollection().getSelfLink(), doc, null, false);
			return null;
		};
		try {
			UTILS.retry(Config.maxRetries, Config.retryIntervalInMillis, true, func);
		} catch (Throwable e) {
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
	}

	public boolean add(AbstractDocument record) {
		boolean found = exist(record);
		if (!found) {
			create(record);
		}
		return !found;
	}

	public boolean exist(AbstractDocument record) {
		String queryStr = record.getQueryStringEqual();
		List<Document> docList = documentClient.queryDocuments(getCollection().getSelfLink(), queryStr, null)
				.getQueryIterable().toList();
		return docList.size() > 0;
	}

	public int deleteOlderThan(ZonedDateTime cutoff) {
		long timestamp = cutoff.toEpochSecond();
		String queryStr = getQueryStringOlderThan(timestamp);
		QueryIterable<Document> docs = documentClient.queryDocuments(getCollection().getSelfLink(), queryStr, null)
				.getQueryIterable();
		int deletedCount = 0;
		try {
			for (Document doc : docs) {
				Callable<Void> func = () -> {
					documentClient.deleteDocument(doc.getSelfLink(), null);
					return null;
				};
				UTILS.retry(Config.maxRetries, Config.retryIntervalInMillis, true, func);
				Utils.exceptionFreeSleep(Config.queryIntervalInMillis);
				deletedCount++;
			}
		} catch (Throwable t) {
			LOGGER.severe(ExceptionUtils.getStackTrace(t));
		}
		LOGGER.info(String.format("[%d] records older than [%s] deleted", deletedCount, cutoff.toString()));
		return deletedCount;
	}

}
