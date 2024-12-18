package com.dataincloud.services.profile;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.TaggedBlobItem;
import com.dataincloud.core.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class ProfilePhotoService {
    private final BlobServiceClient blobServiceClient;
    private final String profilesPhotos;

    private BlobContainerClient getContainerClient() {
        return blobServiceClient.getBlobContainerClient(profilesPhotos);
    }

    public String upload(UUID profileId, MultipartFile profilePhoto) {
        String profilePhotoName = generateBlobName(profileId, profilePhoto);
        var containerClient = getContainerClient();
        var blob = containerClient.getBlobClient(profilePhotoName);
        uploadPhoto(blob, profilePhoto);
        blob.setTags(Map.of("profileId", profileId.toString()));
        return profilePhotoName;
    }

    public String update(UUID profileId, MultipartFile profilePhoto) {
        delete(profileId);
        return upload(profileId, profilePhoto);
    }

    public Resource load(UUID profileId) {
        var containerClient = getContainerClient();
        var blob = getBlobByProfileId(containerClient, profileId);
        Path pathToTempFile = Path.of(System.getProperty("java.io.tmpdir"), blob.getBlobName());
        blob.downloadToFile(
                pathToTempFile.toString(),
                true
        );
        return new PathResource(pathToTempFile);
    }

    public List<String> delete(UUID profileId) {
        var containerClient = getContainerClient();
        return deleteTaggedBlobs(containerClient, "profileId", profileId.toString());
    }

    private String generateBlobName(UUID profileId, MultipartFile profilePhoto) {
        return "%s.%s".formatted(
                profileId.toString(),
                FilenameUtils.getExtension(profilePhoto.getOriginalFilename())
        );
    }

    private void uploadPhoto(BlobClient blobClient, MultipartFile profilePhoto) {
        try {
            blobClient.upload(profilePhoto.getInputStream());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private List<String> deleteTaggedBlobs(BlobContainerClient containerClient, String key, String value) {
        var taggedBlobs = getTaggedBlobs(containerClient, key, value);
        List<String> deletedBlobs = new LinkedList<>();
        for (TaggedBlobItem blob : taggedBlobs) {
            deletedBlobs.add(blob.getName());
            deleteBlob(containerClient, blob.getName());
        }
        return deletedBlobs;
    }

    private PagedIterable<TaggedBlobItem> getTaggedBlobs(BlobContainerClient containerClient, String key, String value) {
        return containerClient.findBlobsByTags(String.format("%s='%s'", key, value));
    }

    private BlobClient getBlobByProfileId (BlobContainerClient containerClient, UUID profileId) {
        return containerClient.getBlobClient(
                getTaggedBlobs(containerClient, "profileId", profileId.toString())
                        .stream()
                        .findFirst()
                        .orElseThrow(ResourceNotFoundException::new)
                        .getName()
        );
    }

    private void deleteBlob(BlobContainerClient containerClient, String blobName) {
        containerClient.getBlobClient(blobName).delete();
    }
}
