package strategy.table;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

class MultiField implements TableStrategy {
    static final String NAME = "multiField";

    @Override
    public void populateDocument(JSONObject tableJsonObject, Document document) {
        JSONArray data = tableJsonObject.getJSONArray("data");
        Integer numCols = tableJsonObject.getInt("numCols");
        Integer numHeaderRows = tableJsonObject.getInt("numHeaderRows");
        Integer numDataRows = tableJsonObject.getInt("numDataRows");
        String secondTitle = tableJsonObject.getString("secondTitle");
        String caption = tableJsonObject.getString("caption");
        JSONArray numericColumns = tableJsonObject.getJSONArray("numericColumns");
        JSONArray title = tableJsonObject.getJSONArray("title");
        String pgTitle = tableJsonObject.getString("pgTitle");
        // TODO implement multi field document population
        throw new UnsupportedOperationException("This function is not implemented");
    }

    @Override
    public Query parseQuery(String query) {
        // TODO implement
        return null;
    }

    @Override
    public List<String> getDocumentLabels(Document document) {
        // TODO implement
        return null;
    }
    // TODO
}
