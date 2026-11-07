package com.kora.backend.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class HashingUtils {

	private HashingUtils() {
	}

	public static String sha256Hex(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		}
		catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 algorithm is not available", exception);
		}
	}

	public static boolean constantTimeEquals(String left, String right) {
		if (left == null || right == null) {
			return false;
		}

		return MessageDigest.isEqual(
				left.getBytes(StandardCharsets.UTF_8),
				right.getBytes(StandardCharsets.UTF_8));
	}

}
