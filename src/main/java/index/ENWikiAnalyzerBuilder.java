package index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

/**
 * Utility class for centralized Analyzer initialization
 */
public class ENWikiAnalyzerBuilder {
    /**
     * Build new Analyzer specific for ENWiki
     *
     * @return
     */
    public Analyzer build() {
        return new EnglishAnalyzer();
    }
}
