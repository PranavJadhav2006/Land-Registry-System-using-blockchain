package com.aditya.bro.transfer.service;

import com.aditya.bro.land.entity.LandParcel;
import com.aditya.bro.land.repository.LandRepository;
import com.aditya.bro.transfer.dto.TransferRequest;
import com.aditya.bro.transfer.entity.OwnershipTransfer;
import com.aditya.bro.transfer.repository.OwnershipTransferRepository;
import com.aditya.bro.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OwnershipTransferService {

    private final OwnershipTransferRepository transferRepository;
    private final LandRepository landRepository;
    private final NotificationService notificationService;

    @Transactional
    public OwnershipTransfer initiateTransfer(TransferRequest request) {
        String surveyNumber = request.getSurveyNumber();

        // 1. Validate the land parcel
        LandParcel land = landRepository.findBySurveyNumber(surveyNumber)
                .orElseThrow(() -> new IllegalStateException("Land with survey number " + surveyNumber + " not found."));

        // 2. Check ownership
        if (!land.getCurrentOwnerId().equalsIgnoreCase(request.getFromWallet())) {
            throw new IllegalStateException("The sender is not the current owner of the land.");
        }

        // 3. Prevent transferring to oneself
        if (request.getFromWallet().equalsIgnoreCase(request.getToWallet())) {
            throw new IllegalStateException("Cannot transfer land to the same wallet address.");
        }

        // 4. Check for existing pending transfers
        Optional<OwnershipTransfer> existingTransfer = transferRepository
                .findBySurveyNumberAndStatus(surveyNumber, "INITIATED");
        if (existingTransfer.isPresent()) {
            throw new IllegalStateException("A transfer for this land is already in progress.");
        }

        // 5. Create and save the new transfer record
        OwnershipTransfer newTransfer = new OwnershipTransfer();
        newTransfer.setSurveyNumber(surveyNumber);
        newTransfer.setFromWallet(request.getFromWallet());
        newTransfer.setToWallet(request.getToWallet());
        newTransfer.setReason(request.getReason());
        newTransfer.setRemarks(request.getRemarks());
        newTransfer.setStatus("INITIATED");
        newTransfer.setInitiatedAt(LocalDateTime.now());

        OwnershipTransfer savedTransfer = transferRepository.save(newTransfer);

        // Create notification for admin
        String notificationMessage = String.format("New land transfer initiated for Survey #%s from %s to %s.",
                surveyNumber, request.getFromWallet(), request.getToWallet());
        notificationService.createNotification(notificationMessage, "TRANSFER_INITIATED", "ADMIN", null);

        return savedTransfer;
    }

    @Transactional
    public OwnershipTransfer confirmTransfer(String surveyNumber, String confirmingWallet) {
        // 1. Find the initiated transfer
        OwnershipTransfer transfer = transferRepository
                .findBySurveyNumberAndStatus(surveyNumber, "INITIATED")
                .orElseThrow(() -> new IllegalStateException("No initiated transfer found for this land."));

        // 2. Verify the confirming party is the new owner
        if (!transfer.getToWallet().equalsIgnoreCase(confirmingWallet)) {
            throw new IllegalStateException("Only the designated new owner can confirm the transfer.");
        }

        // 3. Update the land parcel's owner
        LandParcel land = landRepository.findBySurveyNumber(surveyNumber)
                .orElseThrow(() -> new IllegalStateException("Land not found during confirmation."));

        land.setCurrentOwnerId(transfer.getToWallet());
        landRepository.save(land);

        // 4. Update the transfer status
        transfer.setStatus("CONFIRMED");
        transfer.setConfirmedAt(LocalDateTime.now());
        return transferRepository.save(transfer);
    }

    public Optional<OwnershipTransfer> getTransferStatus(String surveyNumber) {
        return transferRepository.findTopBySurveyNumberOrderByInitiatedAtDesc(surveyNumber);
    }

    public List<OwnershipTransfer> getTransferHistory(String surveyNumber) {
        return transferRepository.findBySurveyNumberOrderByInitiatedAtDesc(surveyNumber);
    }

    public List<OwnershipTransfer> getAllTransfers() {
        return transferRepository.findAll();
    }
}
