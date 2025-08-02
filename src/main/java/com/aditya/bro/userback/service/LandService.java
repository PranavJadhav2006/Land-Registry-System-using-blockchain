package com.aditya.bro.userback.service;

import com.aditya.bro.userback.dto.LandDTO;
import com.aditya.bro.userback.exception.ResourceNotFoundException;
import com.aditya.bro.land.entity.LandParcel; // Changed import
import com.aditya.bro.land.repository.LandRepository; // Changed import
import com.aditya.bro.userback.model.User;
import com.aditya.bro.userback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime; // Changed from LocalDate
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LandService {
    private final LandRepository landRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public List<LandDTO> getAllLands() {
        return landRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public LandDTO getLandById(String id) {
        LandParcel land = landRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land not found with id: " + id));
        return convertToDto(land);
    }

    public LandDTO getLandBySurveyNumber(String surveyNumber) {
        LandParcel land = landRepository.findBySurveyNumber(surveyNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Land not found with survey number: " + surveyNumber));
        return convertToDto(land);
    }

    public List<LandDTO> searchLands(String location, String surveyNumber) {
        if (location != null && surveyNumber != null) {
            // Search by both location and survey number
            return landRepository.findByLocationContainingIgnoreCaseAndSurveyNumberContainingIgnoreCase(location, surveyNumber)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (location != null) {
            // Search by location only
            return landRepository.findByLocationContainingIgnoreCase(location)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (surveyNumber != null) {
            // Search by survey number only
            return landRepository.findBySurveyNumberContainingIgnoreCase(surveyNumber)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            // Return all available lands if no search criteria provided
            return landRepository.findByStatus("AVAILABLE")
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
    }

    public List<LandDTO> getLandsByOwner(String ownerId) {
        return landRepository.findByCurrentOwnerId(ownerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public LandDTO createLand(LandDTO landDTO) {
        LandParcel land = modelMapper.map(landDTO, LandParcel.class); // Use LandParcel
        land.setCreatedDate(LocalDateTime.now()); // Changed from setRegistrationDate
        land.setStatus("AVAILABLE");
        LandParcel savedLand = landRepository.save(land);

        // Add land to user's owned lands
        if (savedLand.getCurrentOwnerId() != null) {
            userRepository.findById(savedLand.getCurrentOwnerId()).ifPresent(user -> {
                user.getOwnedLands().add(savedLand.getSurveyNumber()); // Use getSurveyNumber
                userRepository.save(user);
            });
        }

        return convertToDto(savedLand);
    }

    public LandDTO updateLand(String id, LandDTO landDTO) {
        LandParcel existingLand = landRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land not found with id: " + id));

        modelMapper.map(landDTO, existingLand);
        LandParcel updatedLand = landRepository.save(existingLand);
        return convertToDto(updatedLand);
    }

    public void deleteLand(String id) {
        LandParcel land = landRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Land not found with id: " + id));

        // Remove land from user's owned lands
        if (land.getCurrentOwnerId() != null) {
            userRepository.findById(land.getCurrentOwnerId()).ifPresent(user -> {
                user.getOwnedLands().remove(id);
                userRepository.save(user);
            });
        }

        landRepository.deleteById(id);
    }

    private LandDTO convertToDto(LandParcel land) {
        LandDTO landDTO = modelMapper.map(land, LandDTO.class);
        landDTO.setRegistrationDate(land.getCreatedDate().toString()); // Changed from getRegistrationDate

        // Set owner email if available
        if (land.getCurrentOwnerId() != null) {
            userRepository.findById(land.getCurrentOwnerId()).ifPresent(user -> {
                landDTO.setCurrentOwnerEmail(user.getEmail());
            });
        }

        return landDTO;
    }

    private LandParcel convertToEntity(LandDTO landDTO) {
        return modelMapper.map(landDTO, LandParcel.class);
    }
}