package strategy.table;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.json.JSONObject;

import java.util.List;

public interface TableStrategy {
    void populateDocument(JSONObject tableJsonObject, Document document);

    Query parseQuery(String query) throws ParseException;

    List<String> getDocumentLabels(Document document);
}
