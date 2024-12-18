package com.dataincloud.dal.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "posts_photos")
@Getter @Setter
public class PostPhotoJpa {
    @Id
    private Long postId;

    private String name;
    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] data;

    @MapsId
    @OneToOne
    @JoinColumn(name = "id")
    private PostJpa post;
}