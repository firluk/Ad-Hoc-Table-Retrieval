package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import strategy.enwiki.ENWikiIndexStrategy;
import strategy.table.TableStrategy;
import strategy.table.TableStrategyFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PropertyUtils {

    private static final Logger logger = LogManager.getLogger(PropertyUtils.class);

    private final Properties properties;

    public PropertyUtils() throws IOException {
        logger.info("Looking for config.properties in /resources folder");
        InputStream inputStream = PropertyUtils.class.getClassLoader().getResourceAsStream(Consts.PROPERTIES_FILENAME);
        properties = new Properties();
        properties.load(inputStream);
        logger.info("Loaded properties " + properties.toString());
    }

    public PropertyUtils(Path propertiesFilePath) throws IOException {
        logger.info("Looking for config.properties under path " + propertiesFilePath);
        InputStream inputStream = Files.newInputStream(propertiesFilePath);
        properties = new Properties();
        properties.load(inputStream);
        logger.info("Loaded properties " + properties.toString());
    }


    public Properties getProperties() {
        return this.properties;
    }

    public Path getMount() {
        return Paths.get(properties.getProperty("mount"));
    }

    public Path getWorkDirectory() {
        return getMount().resolve(properties.getProperty("work_directory"));
    }

    public Path getTablesIndexDirectory() {
        return getWorkDirectory().resolve(properties.getProperty("tables_index_directory"));
    }

    public Path getJsonTableDirectory() {
        return getWorkDirectory().resolve(properties.getProperty("json_table_directory"));
    }

    public Path getQueriesFile() {
        return getWorkDirectory().resolve(properties.getProperty("queries"));
    }

    public Path getEnWikiIndexDirectory() {
        return getWorkDirectory().resolve(properties.getProperty("enwiki_index_directory"));
    }

    public Path getEnWikiFile() {
        return getWorkDirectory().resolve(properties.getProperty("enwiki"));
    }

    public Path getWordVectorsFile() {
        return getWorkDirectory().resolve(properties.getProperty("word_vectors_model_file"));
    }

    public Path getTRECOutputDirectory() {
        return getWorkDirectory().resolve(properties.getProperty("trec_output_directory"));
    }

    public TableStrategy getTableStrategy() {
        return TableStrategyFactory.createStrategy(properties.getProperty("table_strategy"));
    }

    public ENWikiIndexStrategy getENWikiIndexStrategy() {
        return new ENWikiIndexStrategy();
    }

    public Stream<QueryWithId> getQueriesStream() throws IOException, QueryParseException {
        var reader = Files.newBufferedReader(getQueriesFile());
        return reader
                .lines()
                .map(this::parseQueryWithId);
    }

    public QueryWithId getQuery(int index) throws IOException, QueryParseException {
        try (var reader = Files.newBufferedReader(getQueriesFile());
             var stringStream = reader.lines().onClose(() -> System.out.println("Closed reader.lines()"))) {
            var iterator = stringStream
                    .iterator();
            while (index > 0) {
                iterator.next();
                index--;
            }
            var fullString = iterator.next();

            return parseQueryWithId(fullString);
        }
    }

    private QueryWithId parseQueryWithId(String queryWithIdString) throws QueryParseException {
        var pattern = Pattern.compile("(\\w+)(\\s+)(.*)");
        var matcher = pattern.matcher(queryWithIdString);

        if (matcher.matches()) {
            var id = Long.parseLong(matcher.group(1));
            var queryText = matcher.group(3);
            return new QueryWithId(id, queryText);
        } else {
            throw new QueryParseException();
        }
    }

    private static class QueryParseException extends RuntimeException {
    }
}
