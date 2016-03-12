package main;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawl {
	static Directory indexDir;
	StandardAnalyzer analyzer;
	IndexWriterConfig config;
	IndexWriter writer;
	static Set<String> keywords = new HashSet<String>();

	public Crawl() throws IOException {
		indexDir = new RAMDirectory();
		analyzer = new StandardAnalyzer();
		config = new IndexWriterConfig(analyzer);
		writer = new IndexWriter(indexDir, config);
	}

	public void startCrawl() throws IOException, ParseException {
		Crawl crawl = new Crawl();
		crawl.crawlWikiPage("https://en.wikibooks.org/wiki/Java_Programming");
		crawl.crawlOraclePage("https://docs.oracle.com/javase/tutorial/java/TOC.html");
		
	}

	public  void crawlWikiPage(String url) throws IOException {
		HashSet<String> VisitedUrl = new HashSet<String>();
		Queue<String> newUrl = new LinkedList<String>();
		ParseString stem = new ParseString();

		newUrl.add(url);

		while (!newUrl.isEmpty()) {
			String tempUrl = newUrl.remove();
			if (!VisitedUrl.contains(tempUrl)) {

				VisitedUrl.add(tempUrl);
				
				if(tempUrl.equals("https://en.wikibooks.org/wiki/Java_Programming/Print_version")) continue;

				Document doc = Jsoup
						.connect(tempUrl)
						.timeout(5 * 1000)
						.userAgent(
								"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
						.referrer("http://www.google.com")
						.ignoreHttpErrors(true).ignoreContentType(true).get();
				
				Element title_tag = doc.getElementById("firstHeading");
				StringBuffer builder = new StringBuffer();
				StringBuffer stem_builder = new StringBuffer();
				StringBuffer stem_title = new StringBuffer();
				String title_text = title_tag.text();

				Element body_tag = doc.getElementById("mw-content-text");
				
				for (Element element : body_tag.children()) {
					if (!element.tagName().equals("h2")) {
						if (element.text().compareTo("") == 0
								|| element.hasClass("wikitable")
								|| element.hasClass("noprint") || element.hasClass("collapsible")) {
							continue;
						}
						
						builder.append(element.text());
						builder.append(System.lineSeparator());
					} else {
						org.apache.lucene.document.Document indexDoc = new org.apache.lucene.document.Document();
						
						indexDoc.add(new TextField("url", tempUrl, TextField.Store.YES));
						
						TextField header = new TextField("header", title_text.replace("[edit]", ""), TextField.Store.YES);
						indexDoc.add(header);
						
						Set<String> temp_header = stem.GetStemWord(title_text.replace("[edit]", ""));
						for(String s : temp_header){
							stem_title.append(s + " ");
						}
						TextField stem_header = new TextField("stem_header", stem_title.toString(), TextField.Store.YES);
						stem_header.setBoost(2.0f);
						indexDoc.add(stem_header);
						
						indexDoc.add(new TextField("contents", builder.toString().replace("[edit]", ""), TextField.Store.YES));
						
						Set<String> temp = stem.GetStemWord(builder.toString().replace("[edit]", ""));
						for(String s : temp){
						stem_builder.append(s + " ");
						}	
						indexDoc.add(new TextField("stem_contents", stem_builder.toString(), TextField.Store.YES));
						writer.addDocument(indexDoc);
						
						builder = new StringBuffer();
						title_text = element.text();
						stem_builder = new StringBuffer();
						stem_title = new StringBuffer();
					}
				}
				
				org.apache.lucene.document.Document indexDoc = new org.apache.lucene.document.Document();
				indexDoc.add(new TextField("url", tempUrl, TextField.Store.YES));
				TextField header = new TextField("header", title_text.replace("[edit]", ""), TextField.Store.YES);
				indexDoc.add(header);
				Set<String> temp_header = stem.GetStemWord(title_text.replace("[edit]", ""));
				for(String s : temp_header){
					stem_title.append(s + " ");
				}
				TextField stem_header = new TextField("stem_header", stem_title.toString(), TextField.Store.YES);
				stem_header.setBoost(2.0f);
				indexDoc.add(stem_header);
				indexDoc.add(new TextField("contents", builder.toString().replace("[edit]", ""), TextField.Store.YES));
				Set<String> temp = stem.GetStemWord(builder.toString().replace("[edit]", ""));
				for(String s : temp){
				stem_builder.append(s + " ");
				}	
				indexDoc.add(new TextField("stem_contents", stem_builder.toString(), TextField.Store.YES));
				writer.addDocument(indexDoc);
				
					Elements questions = doc.select("a[href]");

					for (Element link : questions) {
						if (link.attr("href").contains("wiki/Java_Programming"))
							newUrl.add(link.attr("abs:href"));
					}
			}
		}
	}
	
	public  void crawlOraclePage(String url) throws IOException {
		HashSet<String> VisitedUrl = new HashSet<String>();
		Queue<String> newUrl = new LinkedList<String>();
		ParseString stem = new ParseString();

		newUrl.add(url);

		while (!newUrl.isEmpty()) {
			String tempUrl = newUrl.remove();
			if (!VisitedUrl.contains(tempUrl)) {

				VisitedUrl.add(tempUrl);
				
				Document doc = Jsoup
						.connect(tempUrl)
						.timeout(5 * 1000)
						.userAgent(
								"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
						.referrer("http://www.google.com")
						.ignoreHttpErrors(true).ignoreContentType(true).get();
				
				if(tempUrl.equals("https://docs.oracle.com/javase/tutorial/java/TOC.html")){
					Element keyword_tag = doc.getElementById("PageContent");
					Elements questions = keyword_tag.select("a[href]");
					for (Element link : questions) {
						if (!link.attr("href").contains("QandE") && !link.attr("href").contains("index.html"))
							newUrl.add(link.attr("abs:href"));
					}						
				}else {
					Elements title_tag = doc.getElementById("PageTitle").select("h1");
					StringBuffer builder = new StringBuffer();
					StringBuffer stem_builder = new StringBuffer();
					StringBuffer stem_title = new StringBuffer();
					String title_text = title_tag.text();
	
					Element body_tag = doc.getElementById("PageContent");
					
					for (Element element : body_tag.children()) {
						if (!element.tagName().equals("h2")) {
							if (element.text().compareTo("") == 0 || element.tagName().equals("center")) {
								continue;
							}
							
							builder.append(element.text());
							builder.append(System.lineSeparator());
						} else {
							org.apache.lucene.document.Document indexDoc = new org.apache.lucene.document.Document();
							
							indexDoc.add(new TextField("url", tempUrl, TextField.Store.YES));
							
							TextField header = new TextField("header", title_text, TextField.Store.YES);
							indexDoc.add(header);
							
							Set<String> temp_header = stem.GetStemWord(title_text);
							for(String s : temp_header){
								stem_title.append(s + " ");
							}
							TextField stem_header = new TextField("stem_header", stem_title.toString(), TextField.Store.YES);
							stem_header.setBoost(2.0f);
							indexDoc.add(stem_header);
							
							indexDoc.add(new TextField("contents", builder.toString(), TextField.Store.YES));
							
							Set<String> temp = stem.GetStemWord(builder.toString());
							for(String s : temp){
							stem_builder.append(s + " ");
							}	
							indexDoc.add(new TextField("stem_contents", stem_builder.toString(), TextField.Store.YES));
							writer.addDocument(indexDoc);
							
							builder = new StringBuffer();
							title_text = element.text();
							stem_builder = new StringBuffer();
							stem_title = new StringBuffer();
						}
					}

					org.apache.lucene.document.Document indexDoc = new org.apache.lucene.document.Document();
					
					indexDoc.add(new TextField("url", tempUrl, TextField.Store.YES));
					
					TextField header = new TextField("header", title_text, TextField.Store.YES);
					indexDoc.add(header);
					
					Set<String> temp_header = stem.GetStemWord(title_text);
					for(String s : temp_header){
						stem_title.append(s + " ");
					}
					TextField stem_header = new TextField("stem_header", stem_title.toString(), TextField.Store.YES);
					stem_header.setBoost(2.0f);
					indexDoc.add(stem_header);
					
					indexDoc.add(new TextField("contents", builder.toString(), TextField.Store.YES));
					
					Set<String> temp = stem.GetStemWord(builder.toString());
					for(String s : temp){
					stem_builder.append(s + " ");
					}	
					indexDoc.add(new TextField("stem_contents", stem_builder.toString(), TextField.Store.YES));
					writer.addDocument(indexDoc);
				}
			}
		}
		writer.close();
	}
}