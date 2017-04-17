package scraper.wikipedia;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
			Document doc = Jsoup.parse(html, "https://en.wikipedia.org");
			return parser.parse(doc);
		} catch (Throwable t) {
			logger.severe(ExceptionUtils.getStackTrace(t));
			return null;
		}
	}

	@Override
	protected String getHtmlFileName(String crawlStartTimeString) {
		return "wikipedia_" + crawlStartTimeString + ".html";
	}

	@Override
	protected String getPageContent() throws IOException  {
		Connection conn = Jsoup.connect(config.urlString);
		conn.timeout(60 * 1000); // 1 min
		conn.maxBodySize(0);
		return conn.execute().body();

	}

	@Override
	protected String getConfigFileName() {
		return Main.PROD ? "config.wikipedia.prod.properties" : "config.wikipedia.test.properties";
	}

	@Override
	protected String getLogFileNamePrefix() {
		return "wikipedia";
	}

	@Override
	protected String getLoggerName() {
		return "wikipedia";
	}

}
