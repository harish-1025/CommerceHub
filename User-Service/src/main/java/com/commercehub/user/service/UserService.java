
package com.commercehub.user.service;

import com.commercehub.user.dto.request.UserCreateRequest;
import com.commercehub.user.dto.request.UserUpdateRequest;
import com.commercehub.user.dto.response.UserResponse;
import com.commercehub.user.entity.User;
import com.commercehub.user.enums.UserStatus;
import com.commercehub.user.exception.ResourceAlreadyExistsException;
import com.commercehub.user.exception.ResourceNotFoundException;
import com.commercehub.user.mapper.UserMapper;
import com.commercehub.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    // 1. Register a user
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {

        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException(
                    "A user with this email already exists"
            );
        }

        User user = userMapper.toEntity(request);
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);

        try {
            User savedUser = userRepository.saveAndFlush(user);
            return userMapper.toResponse(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceAlreadyExistsException(
                    "A database constraint prevented user creation"
            );
        }
    }

    // 2. Get a user by ID
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findActiveRecord(id);
        return userMapper.toResponse(user);
    }

    // 3. Get users with pagination and filters
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(
            String name,
            String email,
            UserStatus status,
            Pageable pageable) {

        String nameFilter = normalizeFilter(name);
        String emailFilter = normalizeFilter(email);

        return userRepository.searchUsers(
                nameFilter,
                emailFilter,
                status,
                pageable
        ).map(userMapper::toResponse);
    }

    // 4. Update user details
    @Transactional
    public UserResponse updateUser(
            Long id,
            UserUpdateRequest request) {

        User user = findActiveRecord(id);
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new ResourceAlreadyExistsException(
                    "Another user already uses this email"
            );
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhoneNumber(request.getPhoneNumber());

        try {
            User updatedUser = userRepository.saveAndFlush(user);
            return userMapper.toResponse(updatedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceAlreadyExistsException(
                    "A database constraint prevented the user update"
            );
        }
    }

    // 5. Change account status
    @Transactional
    public UserResponse updateUserStatus(
            Long id,
            UserStatus status) {

        User user = findActiveRecord(id);
        user.setStatus(status);

        User updatedUser = userRepository.saveAndFlush(user);
        return userMapper.toResponse(updatedUser);
    }

    // 6. Soft-delete a user
    @Transactional
    public void deleteUser(Long id) {
        User user = findActiveRecord(id);
        user.setDeleted(true);
        userRepository.saveAndFlush(user);
    }

    private User findActiveRecord(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + id
                ));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
