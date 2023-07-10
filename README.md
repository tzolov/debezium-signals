# Signaling and Notifications

The [Debezium signaling](https://debezium.io/documentation/reference/2.3/configuration/signalling.html) mechanism provides a way to modify the behavior of a connector, or to trigger a one-time action, such as initiating an ad hoc snapshot of a table.

The [Debezium notifications](https://debezium.io/documentation/reference/configuration/notification.html) provide a mechanism to obtain status information about the connector. Notifications can be sent to the configured channels.

The Spring [debezium-signals](https://github.com/tzolov/debezium-signals) project implements Spring Boot integrations for the signaling and notifications:

- `SpringNotificationChannel` - Debezium notification integration, that wraps the `io.debezium.pipeline.notification.Notification` signals into Spring Application Events.

- `SpringSignalChannelReader` - Debezium channel integration, that allows wrapping and sending the `io.debezium.pipeline.signal.SignalRecord` signals as Spring Application Events.

To use the `debezium-signals` you need to add the following dependency:

```xml
<!-- Enable Debezium Builder auto-configuration -->
<dependency>
 <groupId>org.springframework.cloud.fn</groupId>
 <artifactId>debezium-autoconfigure</artifactId>
 <version>${debezium.builder.version}</version>
</dependency>

<!-- Enable Spring Debezium Channel and Notificaiton integration -->
<dependency>
 <groupId>org.spring.boot.extension.autoconfigure</groupId>
 <artifactId>debezium-signals</artifactId>
 <version>0.0.2-SNAPSHOT</version>
</dependency>
```

and set the notification and signal channel properties:

```properties
debezium.properties.notification.enabled.channels=SpringNotificationChannel
debezium.properties.signal.enabled.channels=source,SpringSignalChannelReader
```

Then use the standard Spring events to monitor and control the embedded Debezium connectors:

```java
@SpringBootApplication(exclude = { MongoAutoConfiguration.class })
public class CdcDemoApplication implements ApplicationContextAware {

	private ApplicationEventPublisher publisher;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.publisher = applicationContext;
	}

	// Register listener for Debezium's 'Notification' events.
	@EventListener
	public void applicationEventListener(PayloadApplicationEvent<Notification> applicationEvent) {
		System.out.println("APPLICATION EVENT: " + applicationEvent.getPayload());
	}

	// Send Incremental Snapshot as Spring Application Event
	public void scheduleFixedDelayTask() {
		SignalRecord signal = new SignalRecord("'ad-hoc-666", "execute-snapshot",
				"{\"data-collections\": [\"testDB.dbo.orders\", \"testDB.dbo.customers\", \"testDB.dbo.products\"],\"type\":\"incremental\"}",
				null);
		this.publisher.publishEvent(signal);
	}
	// ......
}

```

### References

- [(blog) Debezium signaling and notifications](https://debezium.io/blog/2023/06/27/Debezium-signaling-and-notifications/)
  > Even when using the Kafka signal approach, the incremental snapshot feature still requires the presence and use of the signaling table.

- [Sending signals to a Debezium connector](https://debezium.io/documentation/reference/configuration/signalling.html)
- [Receiving Debezium notifications](https://debezium.io/documentation/reference/configuration/notification.html)
