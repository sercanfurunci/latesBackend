package com.example.senior_project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "password",
        "emailVerificationToken", "emailVerificationTokenExpiry", "resetPasswordToken",
        "resetPasswordTokenExpiry", "phoneVerificationCode", "phoneVerificationCodeExpiry",
        "notifications", "addresses", "notificationPreferences" })
@ToString(exclude = { "followers", "following", "addresses", "notifications" })
@EqualsAndHashCode(exclude = { "followers", "following", "addresses", "notifications" })
public class User implements UserDetails {
    private static final Logger log = LoggerFactory.getLogger(User.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType; // SELLER, BUYER, ADMIN

    @Column(length = 1000)
    private String biography;

    @Column(length = 500)
    private String bio;

    private String profilePicture;

    @Column(nullable = false)
    private boolean isActive = true;

    private String resetPasswordToken;

    private LocalDateTime resetPasswordTokenExpiry;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_followers", joinColumns = @JoinColumn(name = "followed_id"), inverseJoinColumns = @JoinColumn(name = "follower_id"))
    @JsonIgnore
    private Set<User> followers = new HashSet<>();

    @ManyToMany(mappedBy = "followers", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> following = new HashSet<>();

    @Column(name = "follower_count")
    private Integer followerCount = 0;

    @Column(name = "following_count")
    private Integer followingCount = 0;

    // Satıcılar için özel alanlar
    private String shopDescription;
    private Double sellerRating;
    private Integer totalSales;
    private Boolean isVerifiedSeller = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Address> addresses = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Notification> notifications = new HashSet<>();

    @Column(name = "notification_preferences")
    @ElementCollection
    @CollectionTable(name = "user_notification_preferences")
    private Set<NotificationType> notificationPreferences = new HashSet<>();

    private String stripeCustomerId; // Ödeme sistemi için

    private String stripeConnectedAccountId; // Satıcılar için ödeme sistemi

    @Column(nullable = false)
    private boolean emailVerified = false;

    private String phoneVerificationCode;

    private LocalDateTime phoneVerificationCodeExpiry;

    private String emailVerificationToken;

    private LocalDateTime emailVerificationTokenExpiry;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = "ROLE_" + userType.name();
        log.debug("User authorities for {}: {}", email, role);
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive && emailVerified;
    }
}
