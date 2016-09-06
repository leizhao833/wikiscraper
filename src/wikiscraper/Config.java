package wikiscraper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class Config {

	private static final Logger LOGGER = Logger.getGlobal();
	private static final String PROPERTIES_FILENAME = Main.PROD ? "config.prod.properties" : "config.test.properties";
	public static String LOCAL_TEST_FILE = "dat" + File.separator + "wikirecentchange.html";
	public static int maxRetries;
	public static int changeRecordExpiryInDays;
	public static int crawlRecordExpiryInDays;
	public static long crawlIntervalMinInSeconds;
	public static long crawlIntervalMaxInSeconds;
	public static long crawlIntervalIncInSeconds;
	public static long retryIntervalInMillis;
	public static long queryIntervalInMillis;
	public static String wikiChangeUrl;
	public static String changeRecordCollectionId;
	public static String crawlRecordCollectionId;
	public static String databaseHostName;
	public static String databaseId;

	public static void initialize() {
		try {
			InputStream in = Class.forName("wikiscraper.Config").getClassLoader()
					.getResourceAsStream(PROPERTIES_FILENAME);
			Properties prop = new Properties();
			prop.load(in);
			maxRetries = Integer.parseInt(prop.getProperty("maxRetries"));
			changeRecordExpiryInDays = Integer.parseInt(prop.getProperty("changeRecordExpiryInDays"));
			crawlRecordExpiryInDays = Integer.parseInt(prop.getProperty("crawlRecordExpiryInDays"));
			crawlIntervalMinInSeconds = Long.parseLong(prop.getProperty("crawlIntervalMinInSeconds"));
			crawlIntervalMaxInSeconds = Long.parseLong(prop.getProperty("crawlIntervalMaxInSeconds"));
			crawlIntervalIncInSeconds = Long.parseLong(prop.getProperty("crawlIntervalIncInSeconds"));
			retryIntervalInMillis = Long.parseLong(prop.getProperty("retryIntervalInMillis"));
			queryIntervalInMillis = Long.parseLong(prop.getProperty("queryIntervalInMillis"));
			wikiChangeUrl = prop.getProperty("wikiChangeUrl");
			changeRecordCollectionId = prop.getProperty("changeRecordCollectionId");
			crawlRecordCollectionId = prop.getProperty("crawlRecordCollectionId");
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
