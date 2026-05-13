package com.ulms.service;

import com.ulms.dto.request.RegisterRequest;
import com.ulms.model.User;
import com.ulms.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<User> getAllUsers(Pageable pageable);
    User getUserById(Long id);
    User createUser(RegisterRequest request);
    User updateUser(Long id, RegisterRequest request);
    void deleteUser(Long id);
    Page<User> getUsersByRole(UserRole role, Pageable pageable);
}