package com.aditya.bro.document.service;

import com.aditya.bro.document.entity.DocumentRecord;
import com.aditya.bro.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentRecord uploadDocument(String surveyNumber, String documentType, String description, String ipfsHash, String uploadedBy) {
        DocumentRecord doc = new DocumentRecord();
        doc.setSurveyNumber(surveyNumber);
        doc.setDocumentType(documentType);
        doc.setDescription(description);
        doc.setIpfsHash(ipfsHash);
        doc.setUploadedBy(uploadedBy);
        doc.setUploadedAt(LocalDateTime.now());
        return documentRepository.save(doc);
    }

    public List<DocumentRecord> getDocuments(String surveyNumber) {
        return documentRepository.findBySurveyNumber(surveyNumber);
    }

    public List<DocumentRecord> getAllDocuments() {
        return documentRepository.findAll();
    }

    public void deleteDocument(String id) {
        documentRepository.deleteById(id);
    }
}
