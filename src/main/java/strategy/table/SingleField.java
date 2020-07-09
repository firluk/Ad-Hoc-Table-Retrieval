package strategy.table;

import index.TableAnalyzerBuilder;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.LuceneTools;

import java.util.List;

public class SingleField implements TableStrategy {

    public static final String SINGLE_FIELD_NAME = "singleField";
    final static String NAME = "singleField";
    private final static Analyzer analyzer = new TableAnalyzerBuilder().build();

    @Override
    public void populateDocument(JSONObject tableJsonObject, Document document) {
        // document per table
        /*
         "numHeaderRows" -> Integer
         "data" -> JSONArray // The data in ordered array
         "secondTitle" -> String // ?
         "caption" -> String // Caption (? under the table)
         "numericColumns" -> JSONArray // which of the columns are numeric
         "title" -> JSONArray // Column titles
         "numDataRows" -> Integer // Number of rows
         "numCols" -> Integer // Number of columns
         "pgTitle" -> String // Title of the page
         */
        JSONArray data = tableJsonObject.getJSONArray("data");
        Integer numCols = tableJsonObject.getInt("numCols");
        Integer numHeaderRows = tableJsonObject.getInt("numHeaderRows");
        Integer numDataRows = tableJsonObject.getInt("numDataRows"); // TODO use it
        String secondTitle = tableJsonObject.getString("secondTitle");
        String caption = tableJsonObject.getString("caption");
        JSONArray numericColumns = tableJsonObject.getJSONArray("numericColumns"); // TODO use it // to measure diversity
        JSONArray title = tableJsonObject.getJSONArray("title");
        String pgTitle = tableJsonObject.getString("pgTitle");

        document.add(new Field(SINGLE_FIELD_NAME, secondTitle, TextField.TYPE_STORED));
        document.add(new Field(SINGLE_FIELD_NAME, caption, TextField.TYPE_STORED));
        document.add(new Field(SINGLE_FIELD_NAME, pgTitle, TextField.TYPE_STORED));

        addTitlesToDocument(document, title);
        addRowsToDocument(document, data);
    }

    @Override
    public Query parseQuery(String query) throws ParseException {
        return new QueryParser(SingleField.SINGLE_FIELD_NAME, analyzer)
                .parse(query);
    }

    @Override
    public List<String> getDocumentLabels(Document document) {
        var singleField = document.getValues(SINGLE_FIELD_NAME);
        return LuceneTools.tokenizeString(new EnglishAnalyzer(), singleField);
    }

    private void addTitlesToDocument(Document document, JSONArray title) {
        // iterate over 'data' - this is the table's rows
        // each table row is a JsonArray
        for (Object rowObj : title) {
            // table has the following fields:
            String columnValue = (String) rowObj;
            document.add(new Field(SingleField.SINGLE_FIELD_NAME, columnValue, TextField.TYPE_STORED));
        }
    }

    private void addRowsToDocument(Document document, JSONArray title) {
        // iterate over 'data' - this is the table's rows
        // each table row is a JsonArray
        for (Object rowObj : title) {
            // table has the following fields:
            JSONArray row = (JSONArray) rowObj;
            for (Object colObj : row) {
                String columnValue = (String) colObj;
                document.add(new Field(SingleField.SINGLE_FIELD_NAME, columnValue, TextField.TYPE_STORED));
            }
        }
    }


}
