package main;

import index.ENWikiIndexer;
import index.TableIndexer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import picocli.CommandLine;
import search.ENWikiSearcher;
import search.TableReranker;
import search.TableSearcher;
import utils.Consts;
import utils.PropertyUtils;
import utils.QueryWithId;
import utils.TRECEvaluationMaker;
import word_embedding.WordVectorBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static utils.Consts.PROPERTIES_FILENAME;

public class Main {

    private final static Logger logger = LogManager.getLogger(Main.class);

    private final PropertyUtils propertyUtils;

    private TableIndexer tableIndexer;
    private ENWikiIndexer enWikiIndexer;
    private TableSearcher tableSearcher;

    public Main(PropertyUtils propertyUtils) throws IOException {
        this.propertyUtils = propertyUtils;
    }

    public static void main(String[] args) throws IOException {
        // arguments helper
        var mainOptions = new MainOptions();
        var cmd = new CommandLine(mainOptions);
        var parseResult = cmd.parseArgs(args);

        // properties initialization
        PropertyUtils propertyUtils;
        logger.info("Loading properties...");
        if (parseResult.hasMatchedOption('p')) {
            logger.info("Parsing properties from args " + mainOptions.propertyFile);
            propertyUtils = new PropertyUtils(mainOptions.propertyFile);
        } else {
            logger.info("Looking for properties under execution directory...");
            var propertiesUnderExecFolder = Paths.get(".\\" + PROPERTIES_FILENAME);
            if (Files.exists(propertiesUnderExecFolder)) {
                logger.info(String.format("Found %s under execution directory", PROPERTIES_FILENAME));
                propertyUtils = new PropertyUtils(propertiesUnderExecFolder);
            } else {
                logger.info(String.format("Haven't found %s under execution directory", PROPERTIES_FILENAME));
                propertyUtils = new PropertyUtils();
            }
        }

        var main = new Main(propertyUtils);

        // wiki indexing
        if (parseResult.hasMatchedOption('w')
                && mainOptions.indexWiki) {
            logger.info("Indexing ENWiki...");
            main.requireWikiIndex();
            main.indexENWiki(propertyUtils.getEnWikiFile());
        }
        // tables indexing
        else if (parseResult.hasMatchedOption('i')
                && mainOptions.indexTables) {
            logger.info("Indexing Tables...");
            main.requireTablesIndex();
            main.indexTables(propertyUtils.getJsonTableDirectory());
        }
        // search
        else {
            main.requireTablesIndex();
            main.requireWikiIndex();
            main.requireSearcher();
            try {
                if (parseResult.hasMatchedOption('q')) {

                    var queryNumber = mainOptions.queryNumber;
                    logger.info("Retrieving query with number: " + queryNumber + " from queries file");
                    var query = propertyUtils.getQuery(queryNumber);
                    logger.info("Found query '" + query.getQueryText() + "'");
                    var searchResults = main.searchTableNamesWithScores(query);
                    System.out.println(searchResults);

                } else if (parseResult.hasMatchedOption('s')
                        && mainOptions.searchQuery != null) {

                    var searchQuery = mainOptions.searchQuery;
                    logger.info("Parsed search query " + searchQuery + " from user");
                    var queryWithId = new QueryWithId(searchQuery);
                    logger.info("Found query '" + queryWithId + "'");
                    logger.info("Searching the index...");
                    var searchResults = main.searchTableDocuments(queryWithId);
                    searchResults
                            .keySet()
                            .forEach(indexableFields -> System.out.println(indexableFields.get(Consts.PAGE_TITLE)));

                } else if (parseResult.hasMatchedOption("a")
                        && mainOptions.allQueries) {

                    logger.info("Parsed perform search on all queries in queries file (defined by properties) from user");

                    var queriesResults = new HashMap<QueryWithId, Map<String, Double>>();
                    try (var queriesStream = propertyUtils.getQueriesStream()) {
                        var queryWithIdIterator = queriesStream.iterator();
                        while (queryWithIdIterator.hasNext()) {
                            var queryWithId = queryWithIdIterator.next();
                            var scoresForTable = main.searchTableNamesWithScores(queryWithId);
                            queriesResults.put(queryWithId, scoresForTable);
                        }
                    }
                    var trecEvaluationMaker = new TRECEvaluationMaker();
                    trecEvaluationMaker.createReport(propertyUtils.getTRECOutputDirectory(), queriesResults);
                }
            } catch (IndexDoesNotExistException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void initTableIndexer() throws IOException {
        var tableStrategy = this.propertyUtils.getTableStrategy();
        var tablesIndexDirectory = this.propertyUtils.getTablesIndexDirectory();
        this.tableIndexer =
                new TableIndexer(tableStrategy,
                        tablesIndexDirectory);
    }

    private void initEnwiki() throws IOException {
        var enWikiIndexStrategy = this.propertyUtils.getENWikiIndexStrategy();
        var enWikiIndexDirectory = this.propertyUtils.getEnWikiIndexDirectory();
        this.enWikiIndexer = new ENWikiIndexer(enWikiIndexStrategy,
                enWikiIndexDirectory);
    }

    private void initSearcher() throws IOException {
        var tableStrategy = this.propertyUtils.getTableStrategy();
        var tablesIndexDirectory = this.propertyUtils.getTablesIndexDirectory();
        var enWikiIndexDirectory = this.propertyUtils.getEnWikiIndexDirectory();
        var wordVectorsFile = this.propertyUtils.getWordVectorsFile();
        var wordVectors = new WordVectorBuilder().buildFromFile(wordVectorsFile);
        var enwikiSearcher = new ENWikiSearcher(enWikiIndexDirectory);

        var tableReranker = new TableReranker(wordVectors, enwikiSearcher, tableStrategy);
        this.tableSearcher = new TableSearcher(tablesIndexDirectory, tableReranker, tableStrategy);
    }

    private void requireWikiIndex() throws IOException, IndexDoesNotExistException {
        if (enWikiIndexer == null)
            initEnwiki();
        if (!(Files.size(this.propertyUtils.getTablesIndexDirectory()) > 0)) {
            throw new IndexDoesNotExistException("ENWiki index does not exist");
        }
    }

    private void requireTablesIndex() throws IOException, IndexDoesNotExistException {
        if (tableIndexer == null)
            initTableIndexer();
        if (!(Files.size(this.propertyUtils.getEnWikiFile()) > 0)) {
            throw new IndexDoesNotExistException("Tables index does not exist");
        }
    }

    private void requireSearcher() throws IOException, IndexDoesNotExistException {
        if (tableSearcher == null)
            initSearcher();
    }

    private void indexTables(Path directoryPath) throws IOException {
        tableIndexer.index(directoryPath);
        tableIndexer.close();
    }

    private void indexENWiki(Path enWikiXMLDumpPath) throws IOException {
        enWikiIndexer.index(enWikiXMLDumpPath);
        enWikiIndexer.close();
    }

    private Map<String, Double> searchTableNamesWithScores(QueryWithId queryWithId) throws IOException {
        return tableSearcher.searchTableNamesWithScores(queryWithId.getQueryText());
    }

    private Map<Document, Double> searchTableDocuments(QueryWithId queryWithId) throws IOException {
        return tableSearcher.searchDocumentsWithScores(queryWithId.getQueryText());
    }

    private static class IndexDoesNotExistException extends RuntimeException {

        public IndexDoesNotExistException(String message) {
            super(message);
        }
    }

    @CommandLine.Command
    private static class MainOptions {

        @CommandLine.Option(names = {"-i", "--index"}, description = "index the directory with tables jsons")
        boolean indexTables;

        @CommandLine.Option(names = {"-w", "--wiki"}, description = "index the wiki xml dump")
        boolean indexWiki;

        @CommandLine.Option(names = {"-p", "--properties_file"}, description = "explicit properties file")
        Path propertyFile;

        @CommandLine.Option(names = {"-s", "--search"}, description = "search the index for query")
        String searchQuery;

        @CommandLine.Option(names = {"-q", "--query_number"}, description = "search the index for query with given index from queries file")
        Integer queryNumber;

        @CommandLine.Option(names = {"-a", "--all_queries"}, description = "perform search on all queries in queries file (defined by properties)")
        boolean allQueries;

    }

}
