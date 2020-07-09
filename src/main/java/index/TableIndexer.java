package index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONException;
import org.json.JSONObject;
import strategy.table.TableStrategy;
import utils.Consts;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class TableIndexer implements Closeable {

    private final IndexWriter writer;
    private final TableStrategy tableStrategy;

    /**
     * Indexer constructor.
     *
     * @param tableStrategy  how to index the tables
     * @param indexDirectory location for the index directory
     * @throws IOException if an I/O error occurs reading from the file or a malformed or
     *                     unmappable byte sequence is read
     */
    public TableIndexer(TableStrategy tableStrategy, Path indexDirectory) throws IOException {
        var dir = FSDirectory.open(indexDirectory);
        var writerConfig = new IndexWriterConfig(new TableAnalyzerBuilder().build());
        this.writer = new IndexWriter(dir, writerConfig);
        this.tableStrategy = tableStrategy;
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
     * Builds/updates the index, reading the files in the directory.
     *
     * @param directoryPath where the contents for indexing reside
     */
    public void index(Path directoryPath) throws IOException {
        Objects.requireNonNull(directoryPath);
        for (Path filePath : Files.list(directoryPath).collect(Collectors.toList())) {
            System.out.println("Indexing " + filePath.toAbsolutePath().toString());
            indexFile(filePath);
        }
    }

    /**
     * Indexes a single file containing multiple tables.
     *
     * @param filePath of JSON file containing tables
     * @throws IOException if an I/O error occurs reading from the file or a malformed or
     *                     unmappable byte sequence is read
     */
    private void indexFile(Path filePath) throws IOException {
        Objects.requireNonNull(filePath);
        try {
            var documents = getDocuments(filePath);
            writer.addDocuments(documents.values());
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    /**
     * Creates and returns Documents from provided JSON file.
     *
     * @param filePath of JSON file containing tables
     * @return map of created Documents
     * @throws IOException if an I/O error occurs reading from the file or a malformed or
     *                     unmappable byte sequence is read
     */
    private Map<String, Document> getDocuments(Path filePath) throws IOException, JSONException {
        Objects.requireNonNull(filePath);
        // read file
        // transform json to file
        var tablesJsonObject = new JSONObject(Files.readString(filePath));
        return fromTablesJsonObject(tablesJsonObject);
    }

    /**
     * Returns a {@link Map} where table name is the key (e.g "table-0001-590") and {@link Document} is the value.
     * Documents are created and populated according to Single-Field strategy.
     *
     * @param tablesJsonObject {@link JSONObject} containing tables
     * @return Map of tables
     */
    Map<String, Document> fromTablesJsonObject(JSONObject tablesJsonObject) {

        HashMap<String, Document> documentHashMap = new HashMap<>();

        for (String key : tablesKeys(tablesJsonObject)) {
            var document = new Document();
            document.add(new TextField(Consts.TABLE_NAME, key, Field.Store.YES));
            var tableJsonObject = tablesJsonObject.getJSONObject(key);
            var pgTitle = tableJsonObject.getString("pgTitle");
            document.add(new TextField(Consts.PAGE_TITLE, pgTitle, Field.Store.YES));
            tableStrategy.populateDocument(tableJsonObject, document);

            documentHashMap.put(key, document);
        }

        return documentHashMap;
    }

    /**
     * Keys getter convenience method.
     *
     * @param tablesJsonObject {@link JSONObject} containing tables
     * @return Set of keys
     */
    private Set<String> tablesKeys(JSONObject tablesJsonObject) {
        return Collections.unmodifiableSet(tablesJsonObject.keySet());
    }
}
