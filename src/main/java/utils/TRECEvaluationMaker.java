package utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class TRECEvaluationMaker {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    private static final String TEAM_NAME = "vvolo01";

    public void createReport(Path reportDirectoryPath, Map<QueryWithId, Map<String, Double>> queriesResults) throws IOException {
        var listOfRows = new ArrayList<ArrayList<TRECEvaluationRow>>();
        var size = 0;
        for (var entry : queriesResults.entrySet()) {
            var queryWithId = entry.getKey();
            var resultsForQuery = entry.getValue();
            var rowsForQuery = new ArrayList<TRECEvaluationRow>(resultsForQuery.size());
            for (var scoresForTable : resultsForQuery.entrySet()) {
                var tableName = scoresForTable.getKey();
                var score = scoresForTable.getValue();
                var row = new TRECEvaluationRow(queryWithId.getId(), tableName, score, TEAM_NAME);
                rowsForQuery.add(row);
                rowsForQuery.sort((o1, o2) -> Double.compare(o2.score, o1.score));
                int bound = rowsForQuery.size();
                for (int i = 0; i < bound; i++) {
                    rowsForQuery.get(i).rank = i + 1;
                }
            }
            listOfRows.add(rowsForQuery);
            size += rowsForQuery.size();
        }
        var rows = new ArrayList<TRECEvaluationRow>(size);
        listOfRows.forEach(rows::addAll);
        rows.sort((o1, o2) -> {
            var queryIdCompare = Long.compare(o1.queryId, o2.queryId);
            if (queryIdCompare != 0) {
                return queryIdCompare;
            } else {
                return Integer.compare(o1.rank, o2.rank);
            }
        });
        createReport(reportDirectoryPath, rows);
    }

    public void createReport(Path reportDirectoryPath, Collection<TRECEvaluationRow> rows) throws IOException {
        var timeStamp = DATE_TIME_FORMATTER.format(LocalDateTime.now());
        var filename = timeStamp + ".txt";
        var reportPath = reportDirectoryPath.resolve(filename);
        Files.createDirectories(reportDirectoryPath);
        Files.createFile(reportPath);
        for (TRECEvaluationRow row : rows) {
            // <query id><TAB>Q0<TAB><table id><TAB><rank><TAB><score><TAB><team name>
            var line = row.toString() + "\n";
            Files.writeString(reportPath, line, StandardOpenOption.APPEND);
        }
        Files.copy(reportPath, reportDirectoryPath.resolve("top.txt"), StandardCopyOption.REPLACE_EXISTING);
    }

    public static class TRECEvaluationRow implements Comparable<TRECEvaluationRow> {

        private final long queryId;
        private final String tableId;
        private final double score;
        private final String teamName;
        private int rank;

        public TRECEvaluationRow(long queryId, String tableId, int rank, double score, String teamName) {
            this.queryId = queryId;
            this.tableId = tableId;
            this.rank = rank;
            this.score = score;
            this.teamName = teamName;
        }

        public TRECEvaluationRow(long queryId, String tableId, double score, String teamName) {
            this.queryId = queryId;
            this.tableId = tableId;
            this.score = score;
            this.teamName = teamName;
        }

        @Override
        public int compareTo(@NotNull TRECEvaluationMaker.TRECEvaluationRow o) {
            var compareId = Long.compare(this.queryId, o.queryId);
            if (compareId == 0) {
                return Integer.compare(this.rank, o.rank);
            } else
                return compareId;
        }

        @Override
        public String toString() {
            return String.format("%d\tQ0\t%s\t%d\t%s\t%s", queryId, tableId, rank, score, teamName);
        }
    }

}
