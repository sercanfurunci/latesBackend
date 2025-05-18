package com.example.senior_project.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "educational_contents", schema = "sc_seniorproject")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class EducationalContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String category; // Örn: "Girişimcilik", "Pazarlama", "Ürün Sunumu"

    private String imageUrl;

    private Integer duration; // dakika cinsinden

    @Column(nullable = false)
    private String videoUrl;

    @ElementCollection
    @CollectionTable(name = "educational_content_resources", schema = "sc_seniorproject", joinColumns = @JoinColumn(name = "educational_content_id"))
    private List<String> resources = new ArrayList<>(); // Ek kaynaklar, linkler

    @ElementCollection
    @CollectionTable(name = "educational_content_video_urls", schema = "sc_seniorproject", joinColumns = @JoinColumn(name = "educational_content_id"))
    private List<String> videoUrls = new ArrayList<>(); // Video içerikleri

    @Column(nullable = false)
    private Integer estimatedDuration; // Dakika cinsinden tahmini süre

    @Column(nullable = false)
    private String difficulty; // "Başlangıç", "Orta", "İleri"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean isPublished;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}