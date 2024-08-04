package dev.sagar.doc_intelligence.documentanalyser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class AnalyseDocumentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyseDocumentController.class);

    private final AnalyseDocumentService analyseDocumentService;

    public AnalyseDocumentController(AnalyseDocumentService analyseDocumentService) {
        this.analyseDocumentService = analyseDocumentService;
    }

    @PostMapping("/analyse")
    public ResponseEntity<String> analyseDocument(@RequestParam("file") MultipartFile file) {
        LOGGER.info("Analyzing document: {}", file.getOriginalFilename());
        try {
            analyseDocumentService.analyseDocument(file.getBytes(), file.getOriginalFilename());
            return ResponseEntity.ok("Document analysed successfully");
        } catch (IOException e) {
            LOGGER.error("Error analysing document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error analysing document");
        }
    }
}
