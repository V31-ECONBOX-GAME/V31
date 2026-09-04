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

package org.v31bank.notification.presentation.dto;

import java.time.Instant;
import java.util.UUID;

import org.v31bank.notification.domain.constant.NotificationChannel;
import org.v31bank.notification.domain.constant.NotificationTemplateStatus;
import org.v31bank.notification.domain.model.NotificationTemplate;

public record NotificationTemplateResponse(UUID id, String code, String name, NotificationChannel channel,
		NotificationTemplateStatus status, Instant createdDate, Instant lastModifiedDate) {

	public static NotificationTemplateResponse from(NotificationTemplate notificationTemplate) {
		return new NotificationTemplateResponse(notificationTemplate.getId(), notificationTemplate.getCode(),
				notificationTemplate.getName(), notificationTemplate.getChannel(), notificationTemplate.getStatus(),
				notificationTemplate.getCreatedDate(), notificationTemplate.getLastModifiedDate());
	}

}
