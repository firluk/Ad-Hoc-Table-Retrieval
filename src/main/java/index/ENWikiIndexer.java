package index;

import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import strategy.enwiki.ENWikiIndexStrategy;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * ENWiki Indexer class.
 */
public class ENWikiIndexer implements Closeable {

    private final IndexWriter writer;
    private final ENWikiIndexStrategy enWikiIndexStrategy;

    /**
     * Indexer constructor.
     *
     * @param enWikiIndexStrategy  how to index the EN Wiki dump
     * @param enWikiIndexDirectory location for the index directory
     * @throws IOException if an I/O error occurs reading from the file or a malformed or
     *                     unmappable byte sequence is read
     */
    public ENWikiIndexer(ENWikiIndexStrategy enWikiIndexStrategy, Path enWikiIndexDirectory) throws IOException {
        var dir = FSDirectory.open(enWikiIndexDirectory);
        var writerConfig = new IndexWriterConfig(new ENWikiAnalyzerBuilder().build());
        this.writer = new IndexWriter(dir, writerConfig);
        this.enWikiIndexStrategy = enWikiIndexStrategy;
    }

    /**
     * Close the Indexer.
     *
     * @throws IOException if an I/O error occurs reading from the file or a malformed or
     *                     unmappable byte sequence is read
     */
    public void close() throws IOException {
        writer.close();
    }

    /**
     * Builds/updates the index, reading the Wiki Dump file.
     *
     * @param dumpFilePath where the contents for indexing reside
     */
    public void index(Path dumpFilePath) throws IOException {
        Objects.requireNonNull(dumpFilePath);
        try {
            var wxsp = WikiXMLParserFactory.getSAXParser(dumpFilePath.toAbsolutePath().toString());
            wxsp.setPageCallback(page -> {
                var document = new Document();
                // index page if none of listed is true
                var title = page.getTitle();
                if (!page.isCategoryPage()
                        && !page.isDisambiguationPage()
                        && !page.isRedirect()
                        && !page.isSpecialPage()
                        && !page.isStub()
                        && !title.startsWith("Wikipedia:")
                        && !title.startsWith("File:")) {
                    try {
                        writer.addDocument(enWikiIndexStrategy.populateDocument(document, page));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            wxsp.parse();

        } catch (Exception jsonException) {
            jsonException.printStackTrace();
        }
    }

}
