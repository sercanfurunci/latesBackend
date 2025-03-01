package com.example.senior_project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "search_history")
@NoArgsConstructor
@AllArgsConstructor

@Builder

public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String searchTerm;

    @Column(nullable = false)
    private LocalDateTime searchDate;

    @PrePersist
    protected void onCreate() {
        searchDate = LocalDateTime.now();
    }
} 