package com.example.HNG_Security.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthData {
        private String accessToken;
        private UserResponse user;
    }
}
