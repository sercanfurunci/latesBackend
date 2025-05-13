package com.example.senior_project.controller.buyer;

import com.example.senior_project.dto.CommentRequest;
import com.example.senior_project.dto.SellerReplyRequest;
import com.example.senior_project.model.Comment;
import com.example.senior_project.model.User;
import com.example.senior_project.service.buyer.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buyer/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Comment>> getProductComments(@PathVariable Long productId) {
        return ResponseEntity.ok(commentService.getProductComments(productId));
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(commentService.addComment(request, user));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user) {
        commentService.deleteComment(commentId, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{commentId}/reply")
    public ResponseEntity<Comment> addSellerReply(
            @PathVariable Long commentId,
            @RequestBody SellerReplyRequest request,
            @AuthenticationPrincipal User seller) {
        return ResponseEntity.ok(commentService.addSellerReply(commentId, request.getReply(), seller));
    }
}