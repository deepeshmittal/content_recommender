package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;

public class Searcher {

	public ArrayList<String> search(String querystr) throws ParseException, IOException {
		ParseString stem = new ParseString();
		ArrayList<String> finalreco = new ArrayList<String>();
		StringBuffer finalQuery = new StringBuffer();
		ScoreDoc[] hits = null;
		int hitsPerPage = 10;
		IndexReader reader = null;
		TopScoreDocCollector collector = null;
		IndexSearcher searcher = null;
		reader = DirectoryReader.open(Crawl.indexDir);
		searcher = new IndexSearcher(reader);
		collector = TopScoreDocCollector.create(hitsPerPage);
		boolean success = true;
		
		Set<String> temp = stem.GetStemWord(querystr);
		for (String s : temp){
				finalQuery.append(s + " ");
		}
		
		querystr = "stem_contents: "+ finalQuery.toString() + " OR stem_header: " + finalQuery.toString() ;
		try{
			StandardAnalyzer analyzer = new StandardAnalyzer();
			Query q = new QueryParser("contents", analyzer).parse(querystr);
			searcher.search(q, collector);
			hits = collector.topDocs().scoreDocs;
		}catch (Exception e){
			success = false;
		}

		if(!success || (hits.length == 0) ){
			finalreco.add("No Content Found !!");
		}
		else {
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d;
				d = searcher.doc(docId);
				String final_text = ("<a href='" + d.get("url") + "'>" + d.get("header") + ": </a><br>" + d.get("contents")).replaceAll("<A>", " A").replaceAll("<B>", " B");
				final_text = final_text.substring(0, Math.min(final_text.length(),500));
				final_text = final_text.concat("...");
				finalreco.add(final_text);
			}

		}
				reader.close();
				return finalreco;
	}
}