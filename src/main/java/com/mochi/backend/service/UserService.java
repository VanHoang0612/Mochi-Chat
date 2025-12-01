package com.mochi.backend.service;

import com.mochi.backend.dto.user.AddUserRequest;
import com.mochi.backend.dto.user.ChangePasswordRequest;
import com.mochi.backend.dto.user.UserDto;
import com.mochi.backend.enums.ErrorCode;
import com.mochi.backend.exception.AppException;
import com.mochi.backend.mapper.UserMapper;
import com.mochi.backend.model.User;
import com.mochi.backend.repository.UserRepository;
import com.mochi.backend.security.userDetails.CustomUserDetails;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    RoleService roleService;
    PasswordEncoder passwordEncoder;

    public UserDto addUser(
            @RequestPart("data") AddUserRequest addUserRequest,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile
    ) {


        User user = User.builder()
                .username(addUserRequest.getUsername()
                        .trim())
                .email(addUserRequest.getEmail()
                        .trim())
                .firstname(addUserRequest.getFirstname()
                        .trim())
                .lastname(addUserRequest.getLastname()
                        .trim())
                .password(addUserRequest.getPassword()
                        .trim())
                .roles(roleService.getRolesByNames(addUserRequest.getRoles()))
                .enabled(addUserRequest.isEnabled())
                .build();
        return saveUser(user);
    }

    public UserDto saveUser(User user) {
        if (!user.getPassword()
                .matches("^\\$2[aby]\\$.*")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));

        }
        return userMapper.toDto(userRepository.save(user));
    }

    public List<UserDto> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserDto getMe(Authentication authentication) {
        String currentUsername = getCurrentUser(authentication).getUsername();
        User user = findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));
        return userMapper.toDto(user);
    }

    public User getCurrentUser(Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUser();
    }

    public void changePassword(Authentication authentication, ChangePasswordRequest request) {
        String currentUsername = getCurrentUser(authentication).getUsername();
        User user = findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));
        if (passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
        } else {
            throw new AppException(ErrorCode.OLD_PASSWORD_INCORRECT);
        }
    }
}
