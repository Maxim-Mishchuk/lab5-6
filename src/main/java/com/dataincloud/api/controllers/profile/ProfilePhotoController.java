package com.dataincloud.api.controllers.profile;

import com.dataincloud.services.profile.ProfilePhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profiles/{profileId}/photo")
@RequiredArgsConstructor
public class ProfilePhotoController {
    private final ProfilePhotoService profilePhotoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@PathVariable UUID profileId, @RequestParam("file") MultipartFile file) {
        String createdFileName = profilePhotoService.upload(profileId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFileName);
    }

    @GetMapping(produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getByProfileId(@PathVariable UUID profileId) {
        Resource photo = profilePhotoService.load(profileId);
        return ResponseEntity.status(HttpStatus.OK).body(photo);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> update(@PathVariable UUID profileId, @RequestParam("file") MultipartFile file) {
        String updatedFileName = profilePhotoService.update(profileId, file);
        return ResponseEntity.ok(updatedFileName);
    }

    @DeleteMapping
    public ResponseEntity<List<String>> delete(@PathVariable UUID profileId) {
        List<String> deletedPhotos = profilePhotoService.delete(profileId);
        return ResponseEntity.status(HttpStatus.OK).body(deletedPhotos);
    }
}
