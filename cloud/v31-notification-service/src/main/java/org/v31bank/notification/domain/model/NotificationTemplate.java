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

package org.v31bank.notification.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.v31bank.data.jpa.Audited;
import org.v31bank.notification.domain.constant.NotificationChannel;
import org.v31bank.notification.domain.constant.NotificationTemplateStatus;

@Entity
@Table(name = "notification_template",
		uniqueConstraints = @UniqueConstraint(name = "uk_notification_template_code", columnNames = "code"))
public class NotificationTemplate extends Audited {

	@Column(name = "code", length = 32, nullable = false)
	private String code;

	@Column(name = "name", length = 100, nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "channel", length = 20, nullable = false)
	private NotificationChannel channel;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20, nullable = false)
	private NotificationTemplateStatus status = NotificationTemplateStatus.DRAFT;

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NotificationChannel getChannel() {
		return this.channel;
	}

	public void setChannel(NotificationChannel channel) {
		this.channel = channel;
	}

	public NotificationTemplateStatus getStatus() {
		return this.status;
	}

	public void setStatus(NotificationTemplateStatus status) {
		this.status = status;
	}

}
