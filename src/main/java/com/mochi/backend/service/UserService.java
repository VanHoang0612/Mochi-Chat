package com.mochi.backend.service;

import com.mochi.backend.dto.user.AddUserRequest;
import com.mochi.backend.dto.user.ChangePasswordRequest;
import com.mochi.backend.dto.user.UserDto;
import com.mochi.backend.enums.ErrorCode;
import com.mochi.backend.enums.Provider;
import com.mochi.backend.enums.RoleType;
import com.mochi.backend.exception.AppException;
import com.mochi.backend.mapper.UserMapper;
import com.mochi.backend.model.User;
import com.mochi.backend.repository.UserRepository;
import com.mochi.backend.security.userDetails.CustomUserDetails;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
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
                .provider(Provider.LOCAL.name())
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
        if (authentication.getPrincipal() instanceof CustomUserDetails) {

            String currentUsername = getCurrentUser(authentication).getUsername();
            User user = findByUsername(currentUsername)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));
            return userMapper.toDto(user);
        } else if (authentication.getPrincipal() instanceof DefaultOAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            User user = findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));
            return userMapper.toDto(user);
        } else {
            throw new AppException(ErrorCode.ACCOUNT_NOT_EXISTED);
        }
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

    public User findOrCreateGoogleUser(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        log.info("email: {}", email);
        Optional<User> optionalUser = findByEmail(email);

        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            String firstname = (String) attributes.get("given_name");
            String lastname = (String) attributes.get("family_name");
            String avatarUrl = (String) attributes.get("picture");
            String id = (String) attributes.get("sub");

            String username = generateUsernameFromEmail(email);
            log.info(firstname, lastname, email, id, avatarUrl);
            User newUser = User.builder()
                    .email(email)
                    .username(username)
                    .firstname(firstname)
                    .lastname(lastname)
                    .roles(roleService.getRolesByNames(List.of(RoleType.ROLE_USER.name())))
                    .enabled(true)
                    .avatarUrl(avatarUrl)
                    .provider(Provider.GOOGLE.name())
                    .providerId(id)
                    .build();
            return userRepository.save(newUser);
        }
    }

    private String generateUsernameFromEmail(String email) {
        String baseUsername = email.split("@")[0];
        String username;
        do {
            String randomSuffix = UUID.randomUUID()
                    .toString()
                    .substring(0, 8);
            username = baseUsername + "_" + randomSuffix;
        } while (userRepository.existsByUsername(username));
        return username;
    }
}
