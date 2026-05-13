package com.ulms.service;

import com.ulms.dto.request.LoginRequest;
import com.ulms.dto.response.AuthResponse;
import com.ulms.model.User;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}