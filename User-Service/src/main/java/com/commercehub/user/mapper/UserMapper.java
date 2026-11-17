
package com.commercehub.user.mapper;

import com.commercehub.user.dto.request.UserCreateRequest;
import com.commercehub.user.dto.response.UserResponse;
import com.commercehub.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserCreateRequest request) {
        return new User(
                request.getFullName(),
                request.getEmail(),
                request.getPhoneNumber()
        );
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
