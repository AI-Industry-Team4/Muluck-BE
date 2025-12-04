package com.muluck.domain;

import com.muluck.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "plant_folder")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlantFolder extends BaseEntity {

    @Id
    @Column(name = "folder_id", columnDefinition = "BINARY(16)")
    private UUID folderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "folder_name", nullable = false, length = 100)
    private String folderName;

    @OneToMany(mappedBy = "plantFolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Diagnosis> diagnoses = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.folderId == null) {
            this.folderId = UUID.randomUUID();
        }
    }

    // 생성자
    public PlantFolder(User user, String folderName) {
        this.user = user;
        this.folderName = folderName;
    }

    // 비즈니스 메서드
    public void updateFolderName(String folderName) {
        this.folderName = folderName;
    }
}
