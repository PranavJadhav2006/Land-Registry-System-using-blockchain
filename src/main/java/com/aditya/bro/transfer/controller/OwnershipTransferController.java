package com.aditya.bro.transfer.controller;

import com.aditya.bro.transfer.dto.TransferRequest;
import com.aditya.bro.transfer.entity.OwnershipTransfer;
import com.aditya.bro.transfer.service.OwnershipTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class OwnershipTransferController {

    private final OwnershipTransferService transferService;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiateTransfer(@Valid @RequestBody TransferRequest request, @AuthenticationPrincipal Jwt jwt) {
        try {
            // Set the sender's wallet from the authenticated JWT token
            request.setFromWallet(jwt.getSubject());
            OwnershipTransfer transfer = transferService.initiateTransfer(request);
            return ResponseEntity.ok(transfer);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/confirm/{surveyNumber}")
    public ResponseEntity<?> confirmTransfer(@PathVariable String surveyNumber, @AuthenticationPrincipal Jwt jwt) {
        try {
            String confirmingWallet = jwt.getSubject();
            OwnershipTransfer transfer = transferService.confirmTransfer(surveyNumber, confirmingWallet);
            return ResponseEntity.ok(transfer);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/{surveyNumber}")
    public ResponseEntity<List<OwnershipTransfer>> getTransferHistory(@PathVariable String surveyNumber) {
        return ResponseEntity.ok(transferService.getTransferHistory(surveyNumber));
    }

    @GetMapping("/status/{surveyNumber}")
    public ResponseEntity<OwnershipTransfer> getTransferStatus(@PathVariable String surveyNumber) {
        return transferService.getTransferStatus(surveyNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
