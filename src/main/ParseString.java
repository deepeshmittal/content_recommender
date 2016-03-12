package main;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;



public class ParseString {
	
	public Set<String> GetStemWord(String input_text) throws IOException{
		Set<String> stem_words = new HashSet<String>();
		Reader reader = new StringReader(input_text);
		StandardAnalyzer temp = new StandardAnalyzer();
		TokenStream stream  = temp.tokenStream(null, reader);
		stream = new LowerCaseFilter(stream);
		stream = new PorterStemFilter(stream);
		
		
		CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);

		stream.reset();
		while (stream.incrementToken()) {
		    String term = charTermAttribute.toString();
		    stem_words.add(term);
		}
		
		temp.close();
		stream.close();
		return stem_words;
		
	}
	

}
