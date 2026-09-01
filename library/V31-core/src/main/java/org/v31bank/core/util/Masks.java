/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.v31bank.core.util;

/**
 * Shortens sensitive values so they can be logged.
 * <p>
 * Logs outlive the request that wrote them and travel further: they are shipped to an
 * aggregator, replicated, retained for years, and read by people who have no business
 * seeing an account number. Anything identifying goes through here first, leaving only
 * enough for a human to confirm they are looking at the right record.
 * <p>
 * The mask is a fixed length rather than one character per hidden character, so the
 * masked form does not disclose how long the original was.
 * <p>
 * A value too short to mask safely is hidden completely: revealing the last four digits
 * of a six digit value gives away most of it.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class Masks {

	/**
	 * What replaces the hidden part of a value.
	 */
	public static final String MASK = "****";

	private static final int MIN_HIDDEN = 4;

	private static final int IBAN_PREFIX = 4;

	private static final int IBAN_SUFFIX = 4;

	private Masks() {
	}

	/**
	 * Hide all but the last few characters of a value.
	 * @param value the value to mask, which may be {@code null}
	 * @param visible how many trailing characters to keep
	 * @return the masked value, or {@code null} if the value was
	 */
	public static String tail(String value, int visible) {
		if (value == null) {
			return null;
		}
		if (visible < 0) {
			throw new IllegalArgumentException("visible must not be negative");
		}
		if (value.length() < visible + MIN_HIDDEN) {
			return MASK;
		}
		return MASK + value.substring(value.length() - visible);
	}

	/**
	 * Mask an account or card number down to its last four digits, which is what a
	 * customer is shown to confirm which account is meant.
	 * @param value the number to mask, which may be {@code null}
	 * @return the masked number, for example {@code ****6789}
	 */
	public static String accountNumber(String value) {
		return tail(value, 4);
	}

	/**
	 * Mask a phone number down to its last four digits.
	 * @param value the number to mask, which may be {@code null}
	 * @return the masked number
	 */
	public static String phoneNumber(String value) {
		return tail(value, 4);
	}

	/**
	 * Mask an email address, keeping the first character and the domain so that a support
	 * conversation can confirm the address without the log holding it.
	 * @param value the address to mask, which may be {@code null}
	 * @return the masked address, for example {@code x****@example.com}
	 */
	public static String email(String value) {
		if (value == null) {
			return null;
		}
		int at = value.indexOf('@');
		if (at < 1 || at == value.length() - 1) {
			return MASK;
		}
		return value.charAt(0) + MASK + value.substring(at);
	}

	/**
	 * Shorten an IBAN, keeping the country and check digits it opens with along with its
	 * last four characters, so that two accounts can be told apart at a glance.
	 * <p>
	 * Thirty-four characters of account number make a log line unreadable, and the middle
	 * of them is what identifies the account. This is for logs alone: the middle is also
	 * exactly where a substituted account number hides, so an IBAN a customer is asked to
	 * verify must be shown in full.
	 * @param value the IBAN to shorten, which may be {@code null}
	 * @return the shortened IBAN, for example {@code DE89****3000}
	 */
	public static String iban(String value) {
		if (value == null) {
			return null;
		}
		if (value.length() < IBAN_PREFIX + IBAN_SUFFIX + MIN_HIDDEN) {
			return MASK;
		}
		return value.substring(0, IBAN_PREFIX) + MASK + value.substring(value.length() - IBAN_SUFFIX);
	}

	/**
	 * Hide a value completely, for the ones with no readable part at all — an API key, a
	 * token, a private key, a password.
	 * @param value the value to hide, which may be {@code null}
	 * @return the mask, or {@code null} if the value was
	 */
	public static String secret(String value) {
		return (value != null) ? MASK : null;
	}

}
