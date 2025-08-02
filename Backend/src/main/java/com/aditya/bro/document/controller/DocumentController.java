package com.aditya.bro.document.controller;

import com.aditya.bro.document.entity.DocumentRecord;
import com.aditya.bro.document.service.DocumentService;
import com.aditya.bro.ipfs.IpfsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
 
    private final DocumentService documentService;
    private final IpfsService ipfsService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("surveyNumber") String surveyNumber,
            @RequestParam("documentType") String documentType, // Changed from documentName
            @RequestParam("description") String description // Changed from uploadedBy
    ) {
        try {
            String ipfsHash = ipfsService.addFile(file);
            DocumentRecord savedDoc = documentService.uploadDocument(surveyNumber, documentType, description, ipfsHash, "user"); // Assuming a static user for now
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDoc);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file to IPFS: " + e.getMessage());
        }
    }
 
    @GetMapping("/bySurvey/{surveyNumber}")
    public List<DocumentRecord> getDocsBySurveyNumber(@PathVariable String surveyNumber) {
        return documentService.getDocuments(surveyNumber);
    }

    @GetMapping("/all")
    public List<DocumentRecord> getAllDocs() {
        return documentService.getAllDocuments();
    }
 
    @GetMapping("/{ipfsHash}")
    public ResponseEntity<byte[]> getDocument(@PathVariable String ipfsHash) throws IOException {
        byte[] file = ipfsService.getFile(ipfsHash);
        return ResponseEntity.ok().body(file);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable String id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok().build();
    }
}
