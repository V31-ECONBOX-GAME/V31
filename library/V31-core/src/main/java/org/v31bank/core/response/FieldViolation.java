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

package org.v31bank.core.response;

/**
 * One field of a request that failed validation. The rejected value is deliberately
 * absent, since a response is copied into every proxy log and error tracker on its way
 * back.
 *
 * @param field the path of the offending field, for example {@code lines[0].amount}
 * @param message what is wrong with it, phrased for the person who typed it
 * @author Xander Wang
 * @since 0.2.0
 */
public record FieldViolation(String field, String message) {

}
