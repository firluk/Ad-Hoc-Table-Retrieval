package search;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import strategy.table.TableStrategy;
import utils.Consts;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TableSearcher {

    public static final int N_TOP_DEFAULT = 20; // number of top documents
    public static final int N_BEFORE_RERANK_DEFAULT = 200; // number of documents before re-ranking

    private final Path indexDirectory;
    private final TableReranker tableReranker;
    private final TableStrategy tableStrategy;

    public TableSearcher(Path indexDirectory, TableReranker tableReranker, TableStrategy tableStrategy) {
        this.indexDirectory = indexDirectory;
        this.tableReranker = tableReranker;
        this.tableStrategy = tableStrategy;
    }

    public Map<Document, Double> searchDocumentsWithScores(String queryString) throws IOException {
        try {
            var query = tableStrategy.parseQuery(queryString);

            var fsDirectory = FSDirectory.open(indexDirectory);
            var directoryReader = DirectoryReader.open(fsDirectory);
            var indexSearcher = new IndexSearcher(directoryReader);
            var topDocs = indexSearcher.search(query, N_BEFORE_RERANK_DEFAULT);

            var scoreDocs = topDocs.scoreDocs;
            scoreDocs = tableReranker.rerank(scoreDocs, indexSearcher, queryString);

            var ret = new HashMap<Document, Double>();

            for (int i = 0; i < N_TOP_DEFAULT; i++) {
                var scoreDoc = scoreDocs[i];
                var docInd = scoreDoc.doc;
                if (ret.put(indexSearcher.doc(docInd), (double) scoreDoc.score) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            }

            return Collections.unmodifiableMap(ret);

        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public Map<String, Double> searchTableNamesWithScores(String queryString) throws IOException {
        Map<String, Double> map = new HashMap<>();
        var documentDoubleMap = searchDocumentsWithScores(queryString);
        var entries = documentDoubleMap.entrySet();
        for (var documentFloatEntry : entries) {
            var document = documentFloatEntry.getKey();
            var tableName = document.get(Consts.TABLE_NAME);
            if (map.put(tableName, documentFloatEntry.getValue()) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }
        return map;
    }
}
