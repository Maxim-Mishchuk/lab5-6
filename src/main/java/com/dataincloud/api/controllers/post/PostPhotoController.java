package com.dataincloud.api.controllers.post;

import com.dataincloud.dal.post.PostJpa;
import com.dataincloud.dal.post.PostJpaRepository;
import com.dataincloud.dal.post.PostPhotoJpa;
import com.dataincloud.dal.post.PostPhotoJpaRepository;
import com.dataincloud.services.post.PostService;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.PathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/posts/{postId}/photo")
@Transactional
@RequiredArgsConstructor
public class PostPhotoController {
    private final PostPhotoJpaRepository postPhotoRepository;
    private final PostJpaRepository postRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@PathVariable Long postId, @RequestParam("file") MultipartFile file) {
        PostPhotoJpa newPhoto = new PostPhotoJpa();

        PostJpa post = postRepository.getReferenceById(postId);

        newPhoto.setPost(post);
        newPhoto.setName(
                postId + "." + FilenameUtils.getExtension(file.getOriginalFilename())
        );
        try {
            newPhoto.setData(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        post.setPhoto(newPhoto);
        postPhotoRepository.save(newPhoto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPhoto.getName());
    }

    @GetMapping(produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<PathResource> getByPostId(@PathVariable Long postId) {
        Optional<PostPhotoJpa> photoOpt = postPhotoRepository.findById(postId);
        if (photoOpt.isPresent()) {
            PostPhotoJpa photo = photoOpt.get();

            try {
                // Create a temporary file in the system's temp directory
                String tempDir = System.getProperty("java.io.tmpdir");
                String tempFileName = photo.getName();
                Path tempFilePath = Paths.get(tempDir, tempFileName);

                // Write the photo data to the temporary file
                File tempFile = tempFilePath.toFile();
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(photo.getData());
                }

                // Create a PathResource to send the file
                PathResource resource = new PathResource(tempFilePath);

                return ResponseEntity.ok()
                        .body(resource);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> edit(@PathVariable Long postId, @RequestParam("file") MultipartFile file) {
        Optional<PostPhotoJpa> photoOpt = postPhotoRepository.findById(postId);
        if (photoOpt.isPresent()) {
            PostPhotoJpa photo = photoOpt.get();

            photo.setName(
                    postId + "." + FilenameUtils.getExtension(file.getOriginalFilename())
            );
            try {
                photo.setData(file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            postPhotoRepository.save(photo);
            return ResponseEntity.ok().body("Photo updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Photo not found");
        }
    }

    @DeleteMapping
    public ResponseEntity<String> delete(@PathVariable Long postId) {
        Optional<PostPhotoJpa> photoOpt = postPhotoRepository.findById(postId);
        if (photoOpt.isPresent()) {
            postPhotoRepository.delete(photoOpt.get());
            return ResponseEntity.ok().body("Photo deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Photo not found");
        }
    }


}
