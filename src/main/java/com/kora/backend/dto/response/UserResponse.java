package com.kora.backend.dto.response;

import com.kora.backend.entity.User;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "UserResponse", description = "Public representation of a user. Secrets such as password hashes are never exposed.")
public record UserResponse(
		@Schema(description = "User identifier", example = "61d252f5-d9b2-4760-aee5-972e90083712", requiredMode = Schema.RequiredMode.REQUIRED) String id,

		@Schema(description = "Display name", example = "Ada Lovelace", requiredMode = Schema.RequiredMode.REQUIRED) String name,

		@Schema(description = "Email address", example = "ada@example.com", format = "email", requiredMode = Schema.RequiredMode.REQUIRED) String email,

		@Schema(description = "Whether the email address has been verified", example = "true", requiredMode = Schema.RequiredMode.REQUIRED) boolean emailVerified,

		@Schema(description = "Optional avatar/image URL", example = "https://cdn.example.com/avatars/ada.png", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String image,

		@ArraySchema(schema = @Schema(description = "Assigned role names", example = "ADMIN"), arraySchema = @Schema(description = "Role names assigned to the user")) List<String> roles) {

	public static UserResponse from(User user) {
		List<String> roleNames = user.getRoles().stream()
				.map(role -> role.getName())
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
