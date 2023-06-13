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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.connect.runtime.WorkerConfig;
import org.apache.kafka.connect.storage.MemoryOffsetBackingStore;
import org.spring.boot.extension.autoconfigure.debezium.signals.DebeziumSignalsAutoConfiguration.DebeziumContextHolder;

import org.springframework.context.ApplicationContext;
import org.springframework.integration.metadata.MetadataStore;
import org.springframework.util.Assert;

/**
 * @author Christian Tzolov
 */
public class MetadataStoreOffsetBackingStore extends MemoryOffsetBackingStore {

	private MetadataStore metadataStore;

	@Override
	public void configure(WorkerConfig config) {
		super.configure(config);
		ApplicationContext context = DebeziumContextHolder.applicationContext();
		Assert.notNull(context,
				"Application Context not found in the ThreadLocal. Check the DebeziumSignalsAutoConfiguration conditions.");

		this.metadataStore = context.getBean(MetadataStore.class);
		Assert.notNull(this.metadataStore, "No MetadataStore found!");
	}

	@Override
	public synchronized void start() {
		super.start();
		load();
	}

	private void load() {
		if (this.metadataStore.get("keys") != null) {
			String[] keys = this.metadataStore.get("keys").split(",");
			for (String keySting : keys) {
				String valueString = this.metadataStore.get(keySting);
				ByteBuffer key = ByteBuffer.wrap(keySting.getBytes());
				ByteBuffer value = ByteBuffer.wrap(valueString.getBytes());
				this.data.put(key, value);
			}
		}
	}

	@Override
	protected void save() {
		List<String> keys = new ArrayList<>();
		for (Map.Entry<ByteBuffer, ByteBuffer> mapEntry : this.data.entrySet()) {
			byte[] key = (mapEntry.getKey() != null) ? mapEntry.getKey().array() : null;
			byte[] value = (mapEntry.getValue() != null) ? mapEntry.getValue().array() : null;
			if (key != null && value != null) {
				keys.add(new String(key, StandardCharsets.UTF_8));
				this.metadataStore.put(
						new String(key, StandardCharsets.UTF_8),
						new String(value, StandardCharsets.UTF_8));
			}
		}
		this.metadataStore.put("keys", String.join(",", keys));
	}
}