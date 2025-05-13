package com.example.senior_project.repository;

import com.example.senior_project.model.Favorite;
import com.example.senior_project.model.Product;
import com.example.senior_project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserAndProduct(User user, Product product);

    @Transactional
    void deleteByUserAndProduct(User user, Product product);

    List<Favorite> findByUserOrderByCreatedAtDesc(User user);
}