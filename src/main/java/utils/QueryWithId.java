package utils;

import java.util.Random;

public class QueryWithId {

    private final long id;
    private final String queryText;

    public QueryWithId(long id, String queryText) {
        this.id = id;
        this.queryText = queryText;
    }

    public QueryWithId(String queryText) {
        this(new Random().nextLong(), queryText);
    }

    public long getId() {
        return id;
    }

    public String getQueryText() {
        return queryText;
    }

    @Override
    public String toString() {
        return "QueryWithId{" +
                "id=" + id +
                ", queryText='" + queryText + '\'' +
                '}';
    }
}
