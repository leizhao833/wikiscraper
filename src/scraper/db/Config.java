package scraper.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

import scraper.Main;
import scraper.Utils;

public class Config {

	private static final Logger LOGGER = Logger.getGlobal();
	private static final String PROPERTIES_FILENAME = Main.PROD ? "config.db.prod.properties"
			: "config.db.test.properties";
	public static int maxRetries;
	public static long retryIntervalInMillis;
	public static long queryIntervalInMillis;
	public static String databaseHostName;
	public static String databaseId;

	public static void initialize() {
		try {
			InputStream in = Class.forName("scraper.db.Config").getClassLoader()
					.getResourceAsStream(PROPERTIES_FILENAME);
			Properties prop = new Properties();
			prop.load(in);
			maxRetries = Integer.parseInt(prop.getProperty("maxRetries"));
			retryIntervalInMillis = Long.parseLong(prop.getProperty("retryIntervalInMillis"));
			queryIntervalInMillis = Long.parseLong(prop.getProperty("queryIntervalInMillis"));
			databaseHostName = prop.getProperty("databaseHostName");
			databaseId = prop.getProperty("databaseId");
			LOGGER.info("configuration initialization done");
		} catch (ClassNotFoundException | IOException e) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("failed to load config file %s%n", PROPERTIES_FILENAME));
			sb.append(String.format("%s%n", ExceptionUtils.getStackTrace(e)));
			sb.append("sleep forever ...");
			LOGGER.severe(sb.toString());
			Utils.sleepForever();
		}
	}

}
