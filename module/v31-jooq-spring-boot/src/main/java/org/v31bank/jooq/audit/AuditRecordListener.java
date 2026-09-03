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

package org.v31bank.jooq.audit;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordContext;
import org.jooq.RecordListener;

import org.v31bank.core.util.Uuids;

/**
 * Fills in the identifier and the audit columns as records are written. Each column is
 * stamped only where the record declares it, which lets a schema adopt auditing one table
 * at a time.
 * <p>
 * It only sees writes through jOOQ's record API — {@code store}, {@code insert},
 * {@code update}, {@code merge}. A {@code dsl.insertInto(...)} bypasses it entirely, so
 * write through records where the audit trail has to hold.
 * <p>
 * Timestamps come from a {@link Clock} fixed to UTC, so rows written by nodes in
 * different regions order against each other.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class AuditRecordListener implements RecordListener {

	private final AuditorSupplier auditorSupplier;

	private final Clock clock;

	private final boolean assignIdentifiers;

	public AuditRecordListener(AuditorSupplier auditorSupplier, Clock clock, boolean assignIdentifiers) {
		this.auditorSupplier = auditorSupplier;
		this.clock = clock;
		this.assignIdentifiers = assignIdentifiers;
	}

	@Override
	public void insertStart(RecordContext ctx) {
		stampCreation(ctx.record());
	}

	@Override
	public void updateStart(RecordContext ctx) {
		stampModification(ctx.record());
	}

	/**
	 * Stamp an upsert. The creation columns are filled only where the record left them
	 * empty, so a merge landing as an update does not rewrite when the row was created.
	 */
	@Override
	public void mergeStart(RecordContext ctx) {
		Record record = ctx.record();
		stampCreationIfAbsent(record);
		stampModification(record);
	}

	/**
	 * Record who created the row and when, and give it an identifier if it has none. One
	 * the caller already chose is kept, so a retry writes the same row.
	 * @param record the record about to be inserted
	 */
	void stampCreation(Record record) {
		Instant now = Instant.now(this.clock);
		String auditor = currentAuditor();
		if (this.assignIdentifiers) {
			stampIfAbsent(record, AuditColumns.ID, UUID.class, Uuids.timeOrdered());
		}
		stamp(record, AuditColumns.CREATED_BY, String.class, auditor);
		stamp(record, AuditColumns.CREATED_DATE, Instant.class, now);
		stamp(record, AuditColumns.LAST_MODIFIED_BY, String.class, auditor);
		stamp(record, AuditColumns.LAST_MODIFIED_DATE, Instant.class, now);
	}

	/**
	 * Record who last changed the row and when, leaving the creation columns untouched.
	 * @param record the record about to be updated
	 */
	void stampModification(Record record) {
		stamp(record, AuditColumns.LAST_MODIFIED_BY, String.class, currentAuditor());
		stamp(record, AuditColumns.LAST_MODIFIED_DATE, Instant.class, Instant.now(this.clock));
	}

	private void stampCreationIfAbsent(Record record) {
		if (this.assignIdentifiers) {
			stampIfAbsent(record, AuditColumns.ID, UUID.class, Uuids.timeOrdered());
		}
		stampIfAbsent(record, AuditColumns.CREATED_BY, String.class, currentAuditor());
		stampIfAbsent(record, AuditColumns.CREATED_DATE, Instant.class, Instant.now(this.clock));
	}

	private String currentAuditor() {
		return this.auditorSupplier.currentAuditor().orElse(null);
	}

	private static <T> void stamp(Record record, String column, Class<T> type, T value) {
		// Left as it is rather than nulled: an unidentified caller is no reason to
		// erase who acted.
		if (value == null) {
			return;
		}
		Field<T> field = record.field(column, type);
		if (field != null) {
			record.set(field, value);
		}
	}

	private static <T> void stampIfAbsent(Record record, String column, Class<T> type, T value) {
		if (value == null) {
			return;
		}
		Field<T> field = record.field(column, type);
		if (field != null && record.get(field) == null) {
			record.set(field, value);
		}
	}

}
