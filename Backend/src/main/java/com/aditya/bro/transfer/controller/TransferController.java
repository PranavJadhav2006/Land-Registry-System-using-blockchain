package com.aditya.bro.transfer.controller;

import com.aditya.bro.transfer.entity.OwnershipTransfer;
import com.aditya.bro.transfer.service.OwnershipTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final OwnershipTransferService transferService;

    @GetMapping
    public List<OwnershipTransfer> getAllTransfers() {
        return transferService.getAllTransfers();
    }
}
