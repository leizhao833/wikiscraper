package wikitools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import wikiscraper.CrawlRecordDoc;
import wikiscraper.Parser;

public class ChangeLogRecoverer {

	public static void main(String[] args) {
		String dumpFilePath = "C:\\Users\\leizh\\Downloads\\BlwSfsCrawledWebContentTable.txt";
		File dumpFile = new File(dumpFilePath);
		SortedMap<Long, CrawlRecordDoc> crawls = new TreeMap<Long, CrawlRecordDoc>();
		try (BufferedReader in = new BufferedReader(new FileReader(dumpFile))) {
			String line = null;
			while ((line = in.readLine()) != null) {
				String[] tokens = line.split("\t");
				String base64EncodedBody = tokens[0];
				String timeString = tokens[1];
				long filetimeUtc = Long.parseLong(timeString);
				long epochSeconds = (filetimeUtc - 116444736000000000L) / 10000000;
				byte[] bodyBinary = Base64.getDecoder().decode(base64EncodedBody);
				String body = new String(bodyBinary);
				storeHtmlFile(body, LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC));
				if (body.length() == 0) {
					System.err.print('.');
				} else {
					Document doc = Jsoup.parse(body, "https://en.wikipedia.org");
					long[] timeRange = Parser.getChangeLogTimeRange(doc);
					CrawlRecordDoc rec = new CrawlRecordDoc(epochSeconds, timeRange[0], timeRange[1]);
					crawls.put(rec.changeTimeMin, rec);
					System.out.print('.');
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// printCrawlIntervals(crawls);
		printChangeTimeDelay(crawls);
	}

	private static void printChangeTimeDelay(SortedMap<Long, CrawlRecordDoc> crawls) {
		SortedMap<Long, Long> delays = new TreeMap<Long, Long>();
		for (CrawlRecordDoc e : crawls.values()) {
			Long diff = e.crawlTime - e.changeTimeMax;
			if (!delays.containsKey(diff)) {
				delays.put(diff, 1L);
			} else {
				Long count = delays.get(diff);
				delays.remove(diff);
				delays.put(diff, count + 1);
			}
		}
		for (Map.Entry<Long, Long> e : delays.entrySet()) {
			System.out.println(String.format("%d\t%d", e.getKey(), e.getValue()));
		}
	}

	private static void printCrawlIntervals(SortedMap<Long, CrawlRecordDoc> crawls) {
		System.out.println();
		System.out.print(f(crawls.firstKey()));
		Long max = crawls.get(crawls.firstKey()).changeTimeMax;
		for (Map.Entry<Long, CrawlRecordDoc> e : crawls.entrySet()) {
			if (e.getValue().changeTimeMin >= max) {
				System.out.print(" - ");
				System.out.println(f(max));
				System.out.print(f(e.getValue().changeTimeMin));
				max = e.getValue().changeTimeMax;
			} else {
				max = Math.max(max, e.getValue().changeTimeMax);
			}
		}
		System.out.print(" - ");
		System.out.println(f(max));
	}

	private static ZonedDateTime f(long time) {
		return ZonedDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneOffset.UTC);
	}

	private static final DateTimeFormatter HTML_FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

	private static String storeHtmlFile(String html, LocalDateTime crawlTime) {
		String htmlFile = "changePage_" + HTML_FILE_FORMATTER.format(crawlTime) + ".html";
		try (BufferedWriter out = new BufferedWriter(new FileWriter(htmlFile))) {
			out.write(html);
			return htmlFile;
		} catch (IOException e) {
			return null;
		}
	}
}
