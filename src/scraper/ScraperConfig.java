package scraper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ScraperConfig {

	public static String LOCAL_TEST_FILE = "dat" + File.separator + "wikirecentchange.html";
	public boolean enabled;
	public int changeRecordExpiryInDays;
	public int crawlRecordExpiryInDays;
	public long crawlInitialDelayInSeconds;
	public long crawlIntervalInSeconds;
	public String urlString;
	public String changeRecordCollectionId;
	public String crawlRecordCollectionId;

	public ScraperConfig(String configFileName, Logger logger) {
		try {
			InputStream in = Class.forName("scraper.ScraperConfig").getClassLoader()
					.getResourceAsStream(configFileName);
			Properties prop = new Properties();
			prop.load(in);
			enabled = Boolean.parseBoolean(prop.getProperty("enabled"));
			changeRecordExpiryInDays = Integer.parseInt(prop.getProperty("changeRecordExpiryInDays"));
			crawlRecordExpiryInDays = Integer.parseInt(prop.getProperty("crawlRecordExpiryInDays"));
			crawlInitialDelayInSeconds = Long.parseLong(prop.getProperty("crawlInitialDelayInSeconds"));
			crawlIntervalInSeconds = Long.parseLong(prop.getProperty("crawlIntervalInSeconds"));
			urlString = prop.getProperty("urlString");
			changeRecordCollectionId = prop.getProperty("changeRecordCollectionId");
			crawlRecordCollectionId = prop.getProperty("crawlRecordCollectionId");
			logger.info("configuration initialization done");
		} catch (ClassNotFoundException | IOException e) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("failed to load config file %s%n", configFileName));
			sb.append(String.format("%s%n", ExceptionUtils.getStackTrace(e)));
			sb.append("sleep forever ...");
			logger.severe(sb.toString());
			Utils.sleepForever();
		}
	}

}
