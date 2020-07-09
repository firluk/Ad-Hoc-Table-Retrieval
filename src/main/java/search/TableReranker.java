package search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.nd4j.linalg.ops.transforms.Transforms;
import strategy.enwiki.ENWikiIndexStrategy;
import strategy.table.TableStrategy;
import utils.Consts;
import utils.LuceneTools;

import java.io.IOException;
import java.util.Arrays;

public class TableReranker {

    public static final double GAMMA = 0.2;
    private static final Logger logger = LogManager.getLogger(TableReranker.class);
    private final WordVectors wordVectors;
    private final ENWikiSearcher enWikiSearcher;
    private final TableStrategy tableStrategy;

    public TableReranker(WordVectors wordVectors, ENWikiSearcher enWikiSearcher, TableStrategy tableStrategy) {
        this.wordVectors = wordVectors;
        this.enWikiSearcher = enWikiSearcher;
        this.tableStrategy = tableStrategy;
    }

    public ScoreDoc[] rerank(ScoreDoc[] scoreDocs, IndexSearcher indexSearcher, String queryString) throws IOException {
        ScoreDoc[] rerankedScores = new ScoreDoc[scoreDocs.length];
        try {
            for (int i = 0; i < scoreDocs.length; i++) {
                var scoreDoc = scoreDocs[i];
                var docID = scoreDoc.doc;
                var newScore = calculateScore(indexSearcher.doc(docID), queryString);
                rerankedScores[i] = new ScoreDoc(docID, newScore);
            }
            Arrays.sort(rerankedScores, (o1, o2) -> Float.compare(o2.score, o1.score));
        } catch (Exception e) {
            return scoreDocs;
        }

        return rerankedScores;
    }

    public float calculateScore(Document document, String queryString) throws IOException {
        var pgTitle = document.get(Consts.PAGE_TITLE);
        var documentLabels = tableStrategy.getDocumentLabels(document);
        var documentMean = wordVectors.getWordVectorsMean(documentLabels);
        var topWikiDoc = enWikiSearcher.searchTopDocument(pgTitle);
        var wikiText = topWikiDoc.get(ENWikiIndexStrategy.FIELD_TEXT);
        var engAnalyzer = new EnglishAnalyzer();
        var queryLabels = LuceneTools.tokenizeString(engAnalyzer, queryString);
        var queryMean = wordVectors.getWordVectorsMean(queryLabels);
        var wikitextLabels = LuceneTools.tokenizeString(engAnalyzer, wikiText);
        var wikitextMean = wordVectors.getWordVectorsMean(wikitextLabels);
        var cosineSimWikitextToQuery = Transforms.cosineSim(queryMean, wikitextMean);
        var cosineSimDocumentToQuery = Transforms.cosineSim(queryMean, documentMean);
        return (float) ((1 - GAMMA) * cosineSimWikitextToQuery + (GAMMA) * cosineSimDocumentToQuery);
    }
}
