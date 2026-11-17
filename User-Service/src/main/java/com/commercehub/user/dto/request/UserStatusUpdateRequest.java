
package com.commercehub.user.dto.request;

import com.commercehub.user.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public class UserStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
