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

package org.v31bank.core;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Generates time-ordered UUIDv7 identifiers.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class Uuids {

	/** The 12-bit {@code rand_a} counter. */
	private static final int MAX_COUNTER = 0xFFF;

	/** Seeds low, leaving 3072 ids in the millisecond. */
	private static final int COUNTER_SEED_MASK = 0x3FF;

	private static final SecureRandom RANDOM = new SecureRandom();

	private static final AtomicReference<State> STATE = new AtomicReference<>(new State(Long.MIN_VALUE, 0));

	private Uuids() {
	}

	public static UUID timeOrdered() {
		State state = nextState();
		long timestamp = state.millis() & 0xFFFFFFFFFFFFL;
		long mostSignificantBits = (timestamp << 16) | (0x7L << 12) | (state.counter() & MAX_COUNTER);
		long leastSignificantBits = (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;
		return new UUID(mostSignificantBits, leastSignificantBits);
	}

	private static State nextState() {
		while (true) {
			State current = STATE.get();
			long millis = System.currentTimeMillis();
			State next;
			if (millis > current.millis()) {
				next = new State(millis, RANDOM.nextInt() & COUNTER_SEED_MASK);
			}
			else if (current.counter() < MAX_COUNTER) {
				next = new State(current.millis(), current.counter() + 1);
			}
			else {
				next = new State(current.millis() + 1, 0);
			}
			if (STATE.compareAndSet(current, next)) {
				return next;
			}
		}
	}

	private record State(long millis, int counter) {

	}

}
