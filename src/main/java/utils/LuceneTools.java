package utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LuceneTools {

    public static List<String> tokenizeString(Analyzer analyzer, String string) {
        List<String> tokens = new ArrayList<>();
        try (TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(string))) {
            tokenStream.reset();  // required
            while (tokenStream.incrementToken()) {
                tokens.add(tokenStream.getAttribute(CharTermAttribute.class).toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tokens;
    }

    public static List<String> tokenizeString(Analyzer analyzer, String[] values) {
        var tokens = new LinkedList<String>();
        for (String value : values) {
            tokens.addAll(tokenizeString(analyzer, value));
        }
        return tokens;
    }
}
