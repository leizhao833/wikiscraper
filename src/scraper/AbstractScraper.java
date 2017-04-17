package scraper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang3.exception.ExceptionUtils;

import scraper.db.ChangeRecordDao;
import scraper.db.ChangeRecordDoc;
import scraper.db.Config;
import scraper.db.CrawlRecordDao;
import scraper.db.CrawlRecordDoc;

public abstract class AbstractScraper implements Runnable {

	private static final DateTimeFormatter HTML_FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
	protected static Utils<String> UTILS = new Utils<String>();
	protected Logger logger;
	protected String tempPath;
	protected String logPath;
	protected String htmlPath;
	protected ScraperConfig config;
	protected CrawlRecordDao crawlRecordDao;
	protected ChangeRecordDao changeRecordDao;

	public AbstractScraper(String tempPath) {
		this.tempPath = tempPath;
		this.logPath = tempPath + File.separator + "log";
		this.htmlPath = tempPath + File.separator + "html";
		createPaths();
		this.logger = createLogger();
		this.config = createConfig();
		this.crawlRecordDao = new CrawlRecordDao(config);
		this.changeRecordDao = new ChangeRecordDao(config);
	}

	private String crawlPage() {
		Callable<String> func = () -> {
			return getPageContent();
		};
		try {
			logger.info(String.format("begin downloading %s", config.urlString));
			String html = UTILS.retry(Config.maxRetries, Config.retryIntervalInMillis, true, func);
			logger.info(String.format("download succeeded"));
			return html;
		} catch (Throwable e) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("maximum %d attempts reached, unable to download the page%n", Config.maxRetries));
			sb.append(String.format("%s%n", ExceptionUtils.getStackTrace(e)));
			sb.append("sleep forever ...");
			logger.severe(sb.toString());
			Utils.sleepForever();
			return null;
		}
	}

	public boolean enabled() {
		return config.enabled;
	}

	public long getCrawlIntervalInSeconds() {
		return config.crawlIntervalInSeconds;
	}

	public long getCrawlInitialDelayInSeconds() {
		return config.crawlInitialDelayInSeconds;
	}

	protected ScraperConfig createConfig() {
		return new ScraperConfig(getConfigFileName(), logger);
	}

	protected Logger createLogger() {
		Logger logger = Logger.getLogger(getLoggerName());
		try {
			String logFileName = new File(logPath, getLogFileNamePrefix() + "-log.%u.%g.log").getPath();
			FileHandler fileHandler = new FileHandler(logFileName, 1024 * 1024 * 50, 1000, true);
			fileHandler.setFormatter(new SimpleFormatter());
			logger.addHandler(fileHandler);
			if (!Main.PROD) {
				fileHandler.setLevel(Level.ALL);
				logger.setLevel(Level.ALL);
			}
		} catch (SecurityException | IOException e) {
			logger.severe(ExceptionUtils.getStackTrace(e));
		}
		return logger;
	}

	private void createPaths() {
		File lp = new File(logPath);
		if (!lp.exists()) {
			lp.mkdirs();
		}
		File hp = new File(htmlPath);
		if (!hp.exists()) {
			hp.mkdirs();
		}
	}

	private void findMaxMinRecords(Set<ChangeRecordDoc> records, ChangeRecordDoc max, ChangeRecordDoc min) {
		ChangeRecordDoc maxPtr = new ChangeRecordDoc(null, 0);
		ChangeRecordDoc minPtr = new ChangeRecordDoc(null, Long.MAX_VALUE);
		for (ChangeRecordDoc changeRecord : records) {
			if (changeRecord.timestamp > maxPtr.timestamp) {
				maxPtr = changeRecord;
			}
			if (changeRecord.timestamp < minPtr.timestamp) {
				minPtr = changeRecord;
			}
		}
		maxPtr.copyTo(max);
		minPtr.copyTo(min);

	}

	protected abstract Set<ChangeRecordDoc> getChangeRecord(String html);

	protected abstract String getConfigFileName();

	protected abstract String getHtmlFileName(String crawlStartTimeString);

	protected abstract String getLogFileNamePrefix();

	protected abstract String getLoggerName();

	protected abstract String getPageContent() throws IOException;

	public void run() {
		try {
			// step 1: download the change page
			LocalDateTime crawlStartTime = LocalDateTime.now();
			logger.info("Scraping begins");
			String html = crawlPage();
			// step 2: store the html into a temporary file
			File htmlFile = storeHtmlFile(html, crawlStartTime);
			// step 3: convert the html to DOM tree
			Set<ChangeRecordDoc> changeRecordSet = getChangeRecord(html);
			if (changeRecordSet == null) {
				// parsing failed, ignore the page
				logger.warning("Page parsing failed. Ingore.");
				return;
			}
			// step 5: delete the html file if parse succeeded
			htmlFile.delete();
			// parsing succeeded
			// step 6: store the change records to DB
			storeChangeRecords(changeRecordSet);
			// step 7: store crawl record into DB
			ChangeRecordDoc changeMin = new ChangeRecordDoc();
			ChangeRecordDoc changeMax = new ChangeRecordDoc();
			findMaxMinRecords(changeRecordSet, changeMax, changeMin);
			storeCrawlRecord(crawlStartTime.atZone(ZoneId.systemDefault()), changeMax, changeMin);
			logger.warning("Scraping ends.");
		} catch (Throwable t) {
			logger.severe(ExceptionUtils.getStackTrace(t));
		}
	}

	private int storeChangeRecords(Set<ChangeRecordDoc> records) {
		logger.info(String.format("adding [%d] change records into database", records.size()));
		int newCount = 0;
		for (ChangeRecordDoc rec : records) {
			boolean added = changeRecordDao.add(rec);
			Utils.exceptionFreeSleep(Config.queryIntervalInMillis);
			if (added) {
				newCount++;
			}
			logger.fine(String.format("added change record [ %s ] [%b]", rec.toString(), added));
		}
		logger.info(String.format("[%d(%d new)] change records added to database", records.size(), newCount));
		return newCount;
	}

	private void storeCrawlRecord(ZonedDateTime crawlTime, ChangeRecordDoc max, ChangeRecordDoc min) {
		CrawlRecordDoc rec = new CrawlRecordDoc(crawlTime.toEpochSecond(), min.timestamp, max.timestamp);
		if (crawlRecordDao.add(rec)) {
			logger.info(String.format("added crawl record [ %s ] into database", rec.toString()));
		} else {
			logger.info(String.format("duplicated crawl record existing: %s. ignore ...", rec.toString()));
		}
	}

	private File storeHtmlFile(String html, LocalDateTime crawlTime) {

		File htmlFile = new File(htmlPath, getHtmlFileName(HTML_FILE_FORMATTER.format(crawlTime)));
		try (BufferedWriter out = new BufferedWriter(new FileWriter(htmlFile))) {
			out.write(html);
			logger.info(String.format("store html to %s", htmlFile.toString()));
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}
		return htmlFile;
	}

}
