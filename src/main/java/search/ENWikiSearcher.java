package search;

import index.ENWikiAnalyzerBuilder;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import strategy.enwiki.ENWikiIndexStrategy;

import java.io.IOException;
import java.nio.file.Path;

public class ENWikiSearcher {

    private final Path enWikiIndexDirectory;

    public ENWikiSearcher(Path enWikiIndexDirectory) {
        this.enWikiIndexDirectory = enWikiIndexDirectory;
    }

    public Document searchTopDocument(String queryString) throws IOException {
        try {
            var analyzer = new ENWikiAnalyzerBuilder().build();

            var query = new QueryParser(ENWikiIndexStrategy.FIELD_TITLE, analyzer)
                    .parse(queryString);

            var fsDirectory = FSDirectory.open(enWikiIndexDirectory);
            var directoryReader = DirectoryReader.open(fsDirectory);
            var indexSearcher = new IndexSearcher(directoryReader);
            var topDocs = indexSearcher.search(query, 1);

            var scoreDocs = topDocs.scoreDocs;
            if (topDocs.totalHits.value > 0) {
                return indexSearcher.doc(scoreDocs[0].doc);
            } else {
                return null;
            }

        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
