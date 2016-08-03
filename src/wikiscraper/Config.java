package wikiscraper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class Config {

	private static final boolean PROD = true;
	private static final Logger LOGGER = Logger.getGlobal();
	private static final String PROPERTIES_FILENAME = PROD ? "config.prod.properties" : "config.test.properties";

	public static int maxDownloadRetries;
	public static int changeRecordExpiryInDays;
	public static int crawlRecordExpiryInDays;
	public static long crawlIntervalMaxInSeconds;
	public static long crawlIntervalIncInSeconds;
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
			databaseHostName = prop.getProperty("databaseHostName");
			databaseId = prop.getProperty("databaseId");
			changeRecordCollectionId = prop.getProperty("changeRecordCollectionId");
			crawlRecordCollectionId = prop.getProperty("crawlRecordCollectionId");
			changeRecordExpiryInDays = Integer.parseInt(prop.getProperty("changeRecordExpiryInDays"));
			crawlRecordExpiryInDays = Integer.parseInt(prop.getProperty("crawlRecordExpiryInDays"));
			wikiChangeUrl = prop.getProperty("wikiChangeUrl");
			crawlIntervalMaxInSeconds = Long.parseLong(prop.getProperty("crawlIntervalMaxInSeconds"));
			crawlIntervalIncInSeconds = Long.parseLong(prop.getProperty("crawlIntervalIncInSeconds"));
			maxDownloadRetries = Integer.parseInt(prop.getProperty("maxDownloadRetries"));
			LOGGER.info("configuration initialization done");
		} catch (ClassNotFoundException e) {
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		} catch (IOException e) {
			LOGGER.severe(ExceptionUtils.getStackTrace(e));
		}
	}

}
