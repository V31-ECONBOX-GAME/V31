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

package org.v31bank.compliance.infra.persistence.jooq;

import java.io.Serial;
import java.time.Instant;
import java.util.UUID;

import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import org.v31bank.jooq.AuditColumns;

/**
 * The {@code compliance_case} table, as jOOQ sees it.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class ComplianceCaseTable extends TableImpl<ComplianceCaseRecord> {

	public static final ComplianceCaseTable COMPLIANCE_CASE = new ComplianceCaseTable();

	@Serial
	private static final long serialVersionUID = 1L;

	public final TableField<ComplianceCaseRecord, UUID> ID = createField(DSL.name(AuditColumns.ID),
			SQLDataType.UUID.nullable(false), this);

	public final TableField<ComplianceCaseRecord, String> CREATED_BY = createField(DSL.name(AuditColumns.CREATED_BY),
			SQLDataType.VARCHAR(64), this);

	public final TableField<ComplianceCaseRecord, Instant> CREATED_DATE = createField(
			DSL.name(AuditColumns.CREATED_DATE), SQLDataType.INSTANT, this);

	public final TableField<ComplianceCaseRecord, String> LAST_MODIFIED_BY = createField(
			DSL.name(AuditColumns.LAST_MODIFIED_BY), SQLDataType.VARCHAR(64), this);

	public final TableField<ComplianceCaseRecord, Instant> LAST_MODIFIED_DATE = createField(
			DSL.name(AuditColumns.LAST_MODIFIED_DATE), SQLDataType.INSTANT, this);

	public final TableField<ComplianceCaseRecord, String> CASE_NUMBER = createField(DSL.name("case_number"),
			SQLDataType.VARCHAR(32).nullable(false), this);

	public final TableField<ComplianceCaseRecord, UUID> CUSTOMER_ID = createField(DSL.name("customer_id"),
			SQLDataType.UUID.nullable(false), this);

	public final TableField<ComplianceCaseRecord, String> TYPE = createField(DSL.name("type"),
			SQLDataType.VARCHAR(20).nullable(false), this);

	public final TableField<ComplianceCaseRecord, String> STATUS = createField(DSL.name("status"),
			SQLDataType.VARCHAR(20).nullable(false), this);

	public final TableField<ComplianceCaseRecord, String> SUMMARY = createField(DSL.name("summary"),
			SQLDataType.VARCHAR(500), this);

	private final transient UniqueKey<ComplianceCaseRecord> primaryKey = Internal.createUniqueKey(this,
			DSL.name("compliance_case_pkey"), this.ID);

	private ComplianceCaseTable() {
		super(DSL.name("compliance_case"));
	}

	@Override
	public Class<ComplianceCaseRecord> getRecordType() {
		return ComplianceCaseRecord.class;
	}

	@Override
	public UniqueKey<ComplianceCaseRecord> getPrimaryKey() {
		return this.primaryKey;
	}

}
