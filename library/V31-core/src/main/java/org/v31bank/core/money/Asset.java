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

package org.v31bank.core.money;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Something the bank can hold a balance of — a currency it takes deposits in, or a unit
 * it books a position in.
 * <p>
 * This exists because {@link java.util.Currency} says less than a bank needs. Its minor
 * unit comes from the JDK's locale data and moves with it, it knows no precision beyond
 * the one a statement is printed at, and it cannot be given a unit ISO 4217 never named.
 * How many decimal places a ledger rounds to cannot depend on which JDK it happens to be
 * running on.
 * <p>
 * The {@link #scale()} is the number of decimal places the asset divides into — 2 for
 * USD, 0 for JPY, 3 for KWD — and it is the whole reason this type is carried around
 * rather than a bare currency code: it decides what {@link Money} is allowed to
 * represent, and rounding to the wrong one either invents value or destroys it.
 * <p>
 * The scale is part of the identity, deliberately. The same code is booked at different
 * precision in different places — a customer balance to the cent, a treasury position or
 * an interest accrual finer than that — and comparing only the code would let two amounts
 * a factor of a hundred apart look like the same asset.
 * <p>
 * The constants below are the assets this library knows by name. One it does not list is
 * constructed directly — {@code new Asset("KWD", 3)} — since which currencies a bank
 * deals in belongs in a service's configuration, not in a shared library that would need
 * releasing every time a market is opened.
 *
 * @param code the ISO 4217 code, upper case, for example {@code USD}
 * @param scale the number of decimal places it divides into
 * @author Xander Wang
 * @since 0.2.0
 */
public record Asset(String code, int scale) implements Comparable<Asset> {

	/**
	 * The largest precision an asset may declare. No currency divides beyond four decimal
	 * places; the headroom above that is for the books that carry an amount finer than
	 * the currency it is denominated in, such as an accrual or an unrounded FX leg.
	 */
	public static final int MAX_SCALE = 12;

	private static final Pattern CODE_PATTERN = Pattern.compile("[A-Z0-9]{2,16}");

	/**
	 * United States dollar.
	 */
	public static final Asset USD = new Asset("USD", 2);

	/**
	 * Euro.
	 */
	public static final Asset EUR = new Asset("EUR", 2);

	/**
	 * Pound sterling.
	 */
	public static final Asset GBP = new Asset("GBP", 2);

	/**
	 * Swiss franc.
	 */
	public static final Asset CHF = new Asset("CHF", 2);

	/**
	 * Singapore dollar.
	 */
	public static final Asset SGD = new Asset("SGD", 2);

	/**
	 * Hong Kong dollar.
	 */
	public static final Asset HKD = new Asset("HKD", 2);

	/**
	 * Chinese yuan renminbi. The code names the currency, not the market it was funded
	 * in, which a booking that has to tell onshore from offshore carries separately.
	 */
	public static final Asset CNY = new Asset("CNY", 2);

	/**
	 * Australian dollar.
	 */
	public static final Asset AUD = new Asset("AUD", 2);

	/**
	 * Canadian dollar.
	 */
	public static final Asset CAD = new Asset("CAD", 2);

	/**
	 * Japanese yen, which has no minor unit: an amount of yen is a whole number.
	 */
	public static final Asset JPY = new Asset("JPY", 0);

	private static final Map<String, Asset> KNOWN = Stream.of(USD, EUR, GBP, CHF, SGD, HKD, CNY, AUD, CAD, JPY)
		.collect(Collectors.toUnmodifiableMap(Asset::code, Function.identity()));

	public Asset {
		Objects.requireNonNull(code, "code must not be null");
		code = code.trim().toUpperCase(Locale.ROOT);
		if (!CODE_PATTERN.matcher(code).matches()) {
			throw new IllegalArgumentException(
					"Asset code '" + code + "' must be 2 to 16 upper case letters or digits");
		}
		if (scale < 0 || scale > MAX_SCALE) {
			throw new IllegalArgumentException(
					"Asset " + code + " declares a scale of " + scale + ", outside 0 to " + MAX_SCALE);
		}
	}

	/**
	 * Return the asset this library knows by the given code, matched case insensitively.
	 * @param code the code to look up
	 * @return the asset
	 * @throws IllegalArgumentException if no asset is known by that code
	 */
	public static Asset of(String code) {
		return find(code).orElseThrow(() -> new IllegalArgumentException("No asset is known by the code '" + code
				+ "'; construct it directly to use one this library does not list"));
	}

	/**
	 * Look up an asset by code without failing when it is not known, for parsing input
	 * that may name anything.
	 * @param code the code to look up, which may be {@code null}
	 * @return the asset, or empty when none is known by that code
	 */
	public static Optional<Asset> find(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(KNOWN.get(code.trim().toUpperCase(Locale.ROOT)));
	}

	/**
	 * Return every asset this library knows by name.
	 * @return the known assets
	 */
	public static Set<Asset> known() {
		return Set.copyOf(KNOWN.values());
	}

	/**
	 * Return the smallest amount of this asset that can exist — one cent, one yen — which
	 * is the granularity every balance and every split has to land on.
	 * @return the minor unit as a fraction of one whole asset
	 */
	public BigDecimal minorUnit() {
		return BigDecimal.ONE.movePointLeft(this.scale);
	}

	@Override
	public int compareTo(Asset other) {
		return this.code.compareTo(other.code);
	}

	@Override
	public String toString() {
		return this.code;
	}

}
