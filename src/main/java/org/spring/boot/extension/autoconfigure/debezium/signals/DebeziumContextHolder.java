/*
 * Copyright 2023-2023 the original author or authors.
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

package org.spring.boot.extension.autoconfigure.debezium.signals;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 *
 * @author Christian Tzolov
 */
public class DebeziumContextHolder implements ApplicationContextAware {

	private static ApplicationContext CONTEXT = null;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (CONTEXT == null) {
			CONTEXT = applicationContext;
		}
	}

	static ApplicationContext applicationContext() {
		return CONTEXT;
	}

}
