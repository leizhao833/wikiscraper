package wikiscraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DocumentClient;

public class DocumentClientFactory {

	private static final Logger LOGGER = Logger.getGlobal();
	private static String MASTER_KEY;

	private static DocumentClient documentClient;

	public static DocumentClient getDocumentClient() {
		if (documentClient == null) {
			if (MASTER_KEY == null) {
				MASTER_KEY = loadMasterKey();
			}
			documentClient = new DocumentClient(Config.databaseHostName, MASTER_KEY, ConnectionPolicy.GetDefault(),
					ConsistencyLevel.Session);
		}

		return documentClient;
	}

	private static String loadMasterKey() {
		try (BufferedReader in = new BufferedReader(new FileReader(new File(new File(System.getProperty("user.home"), ".auth"), "master")))) {
			String key = in.readLine();
			LOGGER.info("successfully loaded master key");
			return key;
		} catch (FileNotFoundException e) {
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		} catch (IOException e) {
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
		return null;
	}

}
