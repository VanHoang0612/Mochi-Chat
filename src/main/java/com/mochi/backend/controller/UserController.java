package com.mochi.backend.controller;

import com.mochi.backend.dto.api.ApiResponse;
import com.mochi.backend.dto.user.AddUserRequest;
import com.mochi.backend.dto.user.ChangePasswordRequest;
import com.mochi.backend.dto.user.UserDto;
import com.mochi.backend.enums.SuccessCode;
import com.mochi.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<UserDto>> addUser(@RequestBody AddUserRequest request) {
        return ResponseEntity.status(SuccessCode.ADDUSER.getStatus())
                .body(
                        ApiResponse.success(userService.addUser(request, null), SuccessCode.ADDUSER)
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/get-all")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        return ResponseEntity.status(SuccessCode.SUCCESS.getStatus())
                .body(
                        ApiResponse.success(userService.getUsers(), SuccessCode.SUCCESS)
                );
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(Authentication authentication) {
        return ResponseEntity.status(SuccessCode.SUCCESS.getStatus())
                .body(
                        ApiResponse.success(userService.getMe(authentication), SuccessCode.SUCCESS)
                );
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(authentication, request);
        return ResponseEntity.status(SuccessCode.SUCCESS.getStatus())
                .body(
                        ApiResponse.success(null, SuccessCode.SUCCESS)
                );
    }


}
