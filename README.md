# Signaling and Notifications

The [Debezium signaling](https://debezium.io/documentation/reference/2.3/configuration/signalling.html) mechanism provides a way to modify the behavior of a connector, or to trigger a one-time action, such as initiating an ad hoc snapshot of a table.

The [Debezium notifications](https://debezium.io/documentation/reference/configuration/notification.html) provide a mechanism to obtain status information about the connector. Notifications can be sent to the configured channels.

The [Debezium-Signals](https://github.com/tzolov/debezium-signals) provides a Spring Framework integration for the signaling and notifications mechanisms. It offers:

- `SpringNotificationChannel` - Debezium notification integration, that wraps the `io.debezium.pipeline.notification.Notification` notifications into Spring [Application Events](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events).

- `SpringSignalChannelReader` - Debezium channel integration, that allows sending the `io.debezium.pipeline.signal.SignalRecord` signals as Spring Application Events.

For general guidelines about Spring-Debezium integration follow the [debezium-autoconfigure](https://github.com/spring-cloud/stream-applications/tree/main/functions/common/debezium-autoconfigure) and [Spring Integration with Debezium](https://docs.spring.io/spring-integration/docs/6.2.0-SNAPSHOT/reference/html/debezium.html#debezium) documentations.
Here we will discuss how the `debezium-signals`  helps to expands those integrations.

To enable the `debezium-signals` you need to add the following dependency:

```xml
<dependency>
 <groupId>org.spring.boot.extension.autoconfigure</groupId>
 <artifactId>debezium-signals</artifactId>
 <version>0.0.2-SNAPSHOT</version>
</dependency>
```



and configure the notification and signal channel properties:

```properties
debezium.properties.notification.enabled.channels=SpringNotificationChannel
debezium.properties.signal.enabled.channels=source,SpringSignalChannelReader
```

Then use the standard Spring application events to monitor for Debezium notifications and send Debezium signals:

```java
@SpringBootApplication
public class CdcDemoApplication implements ApplicationContextAware {

	private ApplicationEventPublisher publisher;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.publisher = applicationContext;
	}

	// Send Incremental Snapshot as Spring Application Event
	public void sendDebeziumSignal() {
		SignalRecord signal = new SignalRecord("'ad-hoc-666", "execute-snapshot",
				"{\"data-collections\": [\"testDB.dbo.orders\", \"testDB.dbo.customers\", \"testDB.dbo.products\"],"
				+ "\"type\":\"incremental\"}", null);
		this.publisher.publishEvent(signal);
	}

	// Listener for Debezium's 'Notification' events.
	@EventListener
	public void applicationEventListener(PayloadApplicationEvent<Notification> applicationEvent) {
		System.out.println("APPLICATION EVENT: " + applicationEvent.getPayload());
	}

	// ......
}

```

For a complete example check the [CdcDemoApplication.java](https://github.com/tzolov/spring-debezium-demos/blob/main/src/main/java/com/example/sidebeziumdemo/CdcDemoApplication.java).

### References

- [(blog) Debezium signaling and notifications](https://debezium.io/blog/2023/06/27/Debezium-signaling-and-notifications/)
  > Even when using the Kafka signal approach, the incremental snapshot feature still requires the presence and use of the signaling table.

- [Sending signals to a Debezium connector](https://debezium.io/documentation/reference/configuration/signalling.html)
- [Receiving Debezium notifications](https://debezium.io/documentation/reference/configuration/notification.html)

# Spring Offset Backing Store

The Debezium connectors use the [OffsetBackingStore](https://github.com/apache/kafka/blob/trunk/connect/runtime/src/main/java/org/apache/kafka/connect/storage/OffsetBackingStore.java) abstraction (inherited from Kafka Connect) to keep track of the transactional log offsets, that define how much of the input data has been processed.
Should the connector be restarted, it will use the last recorded offset to know from what point in the input source it should resume reading.

NOTE: Exposing Kafka Connect internals into the Debezium SPI is bad design choice.
Hopefully this will be fixed by [DBZ-1971](https://issues.redhat.com/browse/DBZ-1971).

By default Debezium provides `file` (FileOffsetBackingStore), `kafka` (KafkaOffsetBackingStore) and  `memory` (MemoryOffsetBackingStore) offset backing store implementation.

The `debezium-signals` project adds an additional `MetadataStoreOffsetBackingStore` implementation that leverages the [MetadataStore](https://docs.spring.io/spring-integration/docs/current/reference/html/system-management.html#metadata-store) service.

To use the `MetadataStoreOffsetBackingStore` you need to configure the offset storage property:

```properties
debezium.properties.offset.storage=org.spring.boot.extension.autoconfigure.debezium.signals.MetadataStoreOffsetBackingStore
```

and provide your [MetadataStore](https://docs.spring.io/spring-integration/docs/current/reference/html/system-management.html#metadata-store) instance.
Below we use the `SimpleMetadataStore`, in memory meta store, meant to be used for testing:

```java
import org.springframework.integration.metadata.MetadataStore;
import org.springframework.integration.metadata.SimpleMetadataStore;
...
// Custom Offset Store based on SI MetadataStore.
@Bean
public MetadataStore simpleMetadataStore() {
	return new SimpleMetadataStore();
}
```