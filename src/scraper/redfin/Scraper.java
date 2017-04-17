package scraper.redfin;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import scraper.AbstractScraper;
import scraper.Main;
import scraper.db.ChangeRecordDoc;

public class Scraper extends AbstractScraper {

	private Parser parser;

	public Scraper(String tempPath) {
		super(tempPath);
		parser = new Parser(logger);
	}

	@Override
	protected Set<ChangeRecordDoc> getChangeRecord(String html) {
		try {
			return parser.parse(html);
		} catch (Throwable t) {
			logger.severe(ExceptionUtils.getStackTrace(t));
			return null;
		}
	}

	@Override
	protected String getHtmlFileName(String crawlStartTimeString) {
		return "redfin_" + crawlStartTimeString + ".html";
	}

	@Override
	protected String getPageContent() throws IOException {
		URL url = new URL(config.urlString);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		int responseCode = httpConn.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			InputStream inputStream = httpConn.getInputStream();
			GZIPInputStream gzis = new GZIPInputStream(inputStream);
			StringWriter writer = new StringWriter();
			IOUtils.copy(gzis, writer, Charset.forName("UTF-8"));
			return writer.toString();
		}
		return null;
	}

	@Override
	protected String getConfigFileName() {
		return Main.PROD ? "config.redfin.prod.properties" : "config.redfin.test.properties";
	}

	@Override
	protected String getLogFileNamePrefix() {
		return "redfin";
	}

	@Override
	protected String getLoggerName() {
		return "redfin";
	}

}
