package org.example;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;


@NullMarked
public class DefaultTokenExtractor implements TokenExtractor {

	@Override
	public @Nullable String extractToken(String input) {
		return (input.contains("token") ? "token" : null);
	}
}
