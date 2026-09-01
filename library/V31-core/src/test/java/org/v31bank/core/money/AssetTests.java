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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link Asset}.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
class AssetTests {

	@Test
	void knowsWhatEachAssetDividesInto() {
		assertThat(Asset.USD.scale()).isEqualTo(2);
		assertThat(Asset.CNY.scale()).isEqualTo(2);
		assertThat(Asset.JPY.scale()).isZero();
	}

	@Test
	void normalisesTheCode() {
		assertThat(new Asset(" usd ", 2)).isEqualTo(Asset.USD);
		assertThat(Asset.of("usd")).isEqualTo(Asset.USD);
	}

	@Test
	void rejectsCodesThatAreNotCurrencyCodes() {
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Asset("U", 2));
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Asset("EUR-USD", 2));
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Asset("", 2));
	}

	@Test
	void rejectsImpossiblePrecision() {
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Asset("XYZ", -1));
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> new Asset("XYZ", Asset.MAX_SCALE + 1));
	}

	@Test
	void allowsACurrencyItDoesNotList() {
		Asset dinar = new Asset("KWD", 3);
		assertThat(dinar.code()).isEqualTo("KWD");
		assertThat(Asset.known()).doesNotContain(dinar);
	}

	@Test
	void refusesToGuessAtAnUnknownCode() {
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Asset.of("KWD"))
			.withMessageContaining("No asset is known by the code 'KWD'");
		assertThat(Asset.find("KWD")).isEmpty();
		assertThat(Asset.find(null)).isEmpty();
	}

	@Test
	void treatsPrecisionAsPartOfIdentity() {
		assertThat(new Asset("USD", 6)).isNotEqualTo(Asset.USD);
	}

	@Test
	void reportsItsSmallestUnit() {
		assertThat(Asset.USD.minorUnit()).isEqualTo(new BigDecimal("0.01"));
		assertThat(Asset.JPY.minorUnit()).isEqualTo(BigDecimal.ONE);
	}

	@Test
	void readsAsItsCode() {
		assertThat(Asset.USD).hasToString("USD");
	}

}
