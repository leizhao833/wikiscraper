package scraper.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DocumentClient;

import scraper.Utils;

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
		File masterKeyFile = new File(new File(System.getProperty("user.home"), ".auth"), "master");
		try (BufferedReader in = new BufferedReader(new FileReader(masterKeyFile))) {
			String key = in.readLine();
			LOGGER.info("successfully loaded master key");
			return key;
		} catch (IOException e) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("cannot load master key file %s%n", masterKeyFile.toString()));
			sb.append(String.format("%s%n", ExceptionUtils.getStackTrace(e)));
			sb.append("sleep forever ...");
			LOGGER.severe(sb.toString());
			Utils.sleepForever();
			return null;
		}
	}

}
