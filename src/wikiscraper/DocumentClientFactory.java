package wikiscraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DocumentClient;

public class DocumentClientFactory {

	private static final Logger LOGGER = Logger.getGlobal();
	private static final String HOST = "https://wikichange.documents.azure.com:443/";
	private static String MASTER_KEY;

	private static DocumentClient documentClient;

	public static DocumentClient getDocumentClient() {
		if (documentClient == null) {
			if (MASTER_KEY == null) {
				MASTER_KEY = loadMasterKey();
			}
			documentClient = new DocumentClient(HOST, MASTER_KEY, ConnectionPolicy.GetDefault(),
					ConsistencyLevel.Session);
		}

		return documentClient;
	}

	private static String loadMasterKey() {
		try (BufferedReader in = new BufferedReader(new FileReader(new File(new File("auth"), "master")))) {
			return in.readLine();
		} catch (FileNotFoundException e) {
			LOGGER.severe(e.getMessage());
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
		return null;
	}

}
