package wikitools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import scraper.db.ChangeRecordDoc;
import scraper.wikipedia.Parser;

public class ChangeUrlExtractor {
	
	public static void main(String[] args) {
		Parser parser = new Parser(Logger.getGlobal());
		String dumpFilePath = "C:\\Users\\leizh\\Downloads\\BlwSfsCrawledWebContentTable.txt";
//		String dumpFilePath = args[0];
		System.out.print("Input: " + dumpFilePath);
		File dumpFile = new File(dumpFilePath);
		HashSet<String> urls = new HashSet<String>();
		try (BufferedReader in = new BufferedReader(new FileReader(dumpFile))) {
			String line = null;
			while ((line = in.readLine()) != null) {
				String[] tokens = line.split("\t");
				String base64EncodedBody = tokens[0];
				byte[] bodyBinary = Base64.getDecoder().decode(base64EncodedBody);
				String body = new String(bodyBinary);
				Document doc = Jsoup.parse(body, "https://en.wikipedia.org");
				Set<ChangeRecordDoc> recordSet = parser.parse(doc);
				for (ChangeRecordDoc changeRecordDoc : recordSet) {
					urls.add(changeRecordDoc.url);
				}
				System.out.print('.');
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String file = "url.txt";
		try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
			for (String string : urls) {
				out.write(string);
				out.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
