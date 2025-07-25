package com.aditya.bro.land.controller;

import com.aditya.bro.land.entity.LandParcel;
import com.aditya.bro.land.service.LandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/land")
@RequiredArgsConstructor
public class LandController {

    private final LandService landService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerLand(@RequestBody LandParcel land) {
        LandParcel registeredLand = landService.registerLand(land);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("land", registeredLand);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{surveyNumber}")
    public LandParcel getLand(@PathVariable String surveyNumber) {
        return landService.getLand(surveyNumber).orElse(null);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listLands(
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status
    ) {
        List<LandParcel> lands = landService.listLands(owner, location, status);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("lands", lands);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{surveyNumber}/documents")
    public LandParcel updateDocuments(
            @PathVariable String surveyNumber,
            @RequestBody List<String> documentIpfsHashes
    ) {
        return landService.updateDocuments(surveyNumber, documentIpfsHashes);
    }

    @GetMapping("/{surveyNumber}/history")
    public List<String> getHistory(@PathVariable String surveyNumber) {
        return landService.getHistory(surveyNumber);
    }

    @PostMapping("/{surveyNumber}/transfer")
    public ResponseEntity<Map<String, Object>> transferOwnership(
            @PathVariable String surveyNumber,
            @RequestBody Map<String, String> request
    ) {
        String newOwnerWallet = request.get("newOwner");
        LandParcel updatedLand = landService.transferOwnership(surveyNumber, newOwnerWallet);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("land", updatedLand);
        return ResponseEntity.ok(response);
    }
}
