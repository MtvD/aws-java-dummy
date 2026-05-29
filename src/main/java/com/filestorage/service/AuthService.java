package com.filestorage.service;

import com.filestorage.dto.AuthResponse;
import com.filestorage.dto.LoginRequest;
import com.filestorage.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
