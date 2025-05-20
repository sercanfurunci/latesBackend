package com.example.senior_project.service.seller;

import com.example.senior_project.model.User;
import java.util.List;

public interface SellerService {
    List<User> getFollowers(User seller);
}