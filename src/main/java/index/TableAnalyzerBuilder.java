package index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.CapitalizationFilter;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizer;

public class TableAnalyzerBuilder {

    public TableAnalyzerBuilder() {
    }

    public Analyzer build() {
        return new EnglishAnalyzer();
    }

    static class TableAnalyzer extends Analyzer {

        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            WikipediaTokenizer src = new WikipediaTokenizer();
            TokenStream result = new LowerCaseFilter(src);
            result = new StopFilter(result, EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
            result = new CapitalizationFilter(result);
            return new TokenStreamComponents(src, result);
        }
    }

}
