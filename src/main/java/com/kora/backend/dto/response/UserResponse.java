package com.kora.backend.dto.response;

import com.kora.backend.entity.Role;
import com.kora.backend.entity.User;

import java.util.List;

public record UserResponse(
		String id,
		String name,
		String email,
		boolean emailVerified,
		String image,
		List<String> roles
) {

	public static UserResponse from(User user) {
		List<String> roleNames = user.getRoles().stream()
				.map(Role::getName)
				.sorted()
				.toList();

		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.isEmailVerified(),
				user.getImage(),
				roleNames);
	}

}
