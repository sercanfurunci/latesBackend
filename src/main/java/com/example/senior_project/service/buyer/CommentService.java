package com.example.senior_project.service.buyer;

import com.example.senior_project.dto.CommentRequest;
import com.example.senior_project.model.Comment;
import com.example.senior_project.model.Product;
import com.example.senior_project.model.User;
import com.example.senior_project.repository.CommentRepository;
import com.example.senior_project.repository.ProductRepository;
import com.example.senior_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Comment> getProductComments(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return commentRepository.findByProduct(product);
    }

    @Transactional
    public Comment addComment(CommentRequest request, User user) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setRating(request.getRating());
        comment.setProduct(product);
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setActive(true);

        Comment savedComment = commentRepository.save(comment);
        updateProductRating(product);
        updateSellerRating(product);

        return savedComment;
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Yorum bulunamadı"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bu yorumu silme yetkiniz yok");
        }

        comment.setActive(false);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    private void updateProductRating(Product product) {
        Double averageRating = commentRepository.getAverageRatingByProduct(product);
        product.setAverageRating(averageRating);
        productRepository.save(product);
    }

    @Transactional
    private void updateSellerRating(Product product) {
        User seller = product.getSeller();
        List<Product> sellerProducts = productRepository.findBySeller(seller);

        double totalRating = 0;
        int ratedProductCount = 0;

        for (Product p : sellerProducts) {
            if (p.getAverageRating() != null && p.getAverageRating() > 0) {
                totalRating += p.getAverageRating();
                ratedProductCount++;
            }
        }

        if (ratedProductCount > 0) {
            seller.setSellerRating(totalRating / ratedProductCount);
            userRepository.save(seller);
        }
    }
}