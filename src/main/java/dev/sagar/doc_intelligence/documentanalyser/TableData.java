package dev.sagar.doc_intelligence.documentanalyser;

import java.util.ArrayList;
import java.util.List;

public class TableData {
    private final List<Entry> entries;

    public TableData() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(String tableName, String rowName, String columnName, String value) {
        this.entries.add(new Entry(tableName, rowName, columnName, value));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry entry : entries) {
            sb.append("Table Name: ").append(entry.tableName).append("\n")
                    .append("Row Name: ").append(entry.rowName).append("\n")
                    .append("Column Name: ").append(entry.columnName).append("\n")
                    .append("Value: ").append(entry.value).append("\n")
                    .append("---------\n");
        }
        return sb.toString();
    }

    private record Entry(String tableName, String rowName, String columnName, String value) {
    }
}