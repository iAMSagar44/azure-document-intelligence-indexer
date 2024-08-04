package dev.sagar.doc_intelligence.documentanalyser;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.models.*;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class AnalyseDocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyseDocumentService.class);
    private final DocumentAnalysisClient documentAnalysisClient;
    private final VectorStore vectorStore;

    public AnalyseDocumentService(DocumentAnalysisClient documentAnalysisClient, VectorStore vectorStore) {
        this.documentAnalysisClient = documentAnalysisClient;
        this.vectorStore = vectorStore;
    }

    // Analyse the document using Azure Form Recognizer
    public void analyseDocument(byte[] document, String fileName) throws IOException {
        List<Document> readDocuments = new ArrayList<>();
        try {
            // Analyse the document
            SyncPoller<OperationResult, AnalyzeResult> analyzeLayoutResultPoller = documentAnalysisClient.beginAnalyzeDocument("prebuilt-layout",
                    BinaryData.fromBytes(document));

            final var analyzeLayoutResult = analyzeLayoutResultPoller.getFinalResult();

            for (DocumentPage documentPage : analyzeLayoutResult.getPages()) {
                StringBuilder pageText = new StringBuilder();
                int pageNumber = documentPage.getPageNumber();
                LOGGER.info("Analysing page number {} in document {}\n", pageNumber, fileName);
                final String paragraphs = analyzeLayoutResult.getParagraphs().stream()
                        .filter(paragraph -> paragraph.getBoundingRegions().getFirst().getPageNumber() == pageNumber)
                        .map(DocumentParagraph::getContent).collect(Collectors.joining("\n \n"));
                LOGGER.debug("Page number: {} \t Paragraphs: {}", pageNumber, paragraphs);
                pageText.append(paragraphs);

                //Extract tables
                if (analyzeLayoutResult.getTables() != null) {
                    LOGGER.info("Extracting tables from page number {} in document {}\n", pageNumber, fileName);
                    final List<DocumentTable> tablesOnPage = analyzeLayoutResult.getTables().stream()
                            .filter(table -> table.getBoundingRegions().getFirst().getPageNumber() == pageNumber)
                            .toList();

                    List<TableData> tableDataList = new ArrayList<>();
                    for (DocumentTable table : tablesOnPage) {
                        tableDataList.add(extractTableData(table));
                    }

                    tableDataList.forEach(tableData -> LOGGER.debug("Table Data: \n{}", tableData));
                    tableDataList.forEach(tableData -> pageText.append("\n[Tabular Data]:\n").append(tableData.toString()));
                }

                readDocuments.add(this.toDocument(pageText.toString(), pageNumber, fileName));
            }

            readDocuments.forEach(doc -> LOGGER.debug("Document: {}", doc));

            var tokenTextSplitter = new TokenTextSplitter();
            final var splitDocuments = tokenTextSplitter.apply(readDocuments);

            LOGGER.info("==============Azure Doc Intelligence: Splitting the document into tokens=================");

            splitDocuments.forEach(doc -> LOGGER.debug("Azure Doc Intelligence: Split Document: {}", doc));

            LOGGER.info("==============Azure Doc Intelligence: Storing the documents in the Vector Store===========");
            vectorStore.add(splitDocuments);
            LOGGER.info("==============Azure Doc Intelligence: Vector Store indexing completed===========");
        } catch (HttpResponseException e) {
            LOGGER.error("Error occurred while analyzing the document", e);
            throw new RuntimeException(e);
        }
    }

    private TableData extractTableData(DocumentTable table) {
        TableData tableData = new TableData();

        // Extract rows and column headers if available
        Map<Integer, String> columnHeaders = new HashMap<>();
        Map<Integer, String> rowHeaders = new HashMap<>();

        for (DocumentTableCell cell : table.getCells()) {
            String cellContent = cell.getContent().isBlank() ? "Column" : cell.getContent();

            if (cell.getKind().equals(DocumentTableCellKind.COLUMN_HEADER)) {
                columnHeaders.put(cell.getColumnIndex(), cellContent);
            } else if (cell.getKind().equals(DocumentTableCellKind.ROW_HEADER)) {
                rowHeaders.put(cell.getRowIndex(), cellContent);
            } else {
                // Normal cell, get the table name, row name, and column name
                String tableName = "Table";
                String rowName = rowHeaders.getOrDefault(cell.getRowIndex(), "Row " + cell.getRowIndex());
                String columnName = columnHeaders.getOrDefault(cell.getColumnIndex(), "Column " + cell.getColumnIndex());

                tableData.addEntry(tableName, rowName, columnName, cellContent);
            }
        }

        return tableData;
    }

    private Document toDocument(String docText, int startPageNumber, String resourceFileName) {
        Document doc = new Document(docText);
        doc.getMetadata().put("page_number", startPageNumber);
        doc.getMetadata().put("file_name", resourceFileName);
        return doc;
    }
}
