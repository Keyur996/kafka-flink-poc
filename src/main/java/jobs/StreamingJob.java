package jobs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import models.GroupedData;
import models.KafkaMessage;
import operator.GroupByRecordIdAndOrganizationIdProcessFunction;
import schema.KafkaMessageSchema;
import schema.KafkaMessageSinkSchema;

@SuppressWarnings({ "unused" })
public class StreamingJob {

	private static Properties properties = new Properties();
	private static String inputTopicName;
	private static String consumerGroupId;
	private static String outputTopicNames;
	private static String kafkaServers;

	public StreamingJob() {
		super();
		properties = getProperties();
		inputTopicName = properties.getProperty("inputTopicName");
		consumerGroupId = properties.getProperty("consumerGroupId");
		outputTopicNames = properties.getProperty("outputTopicNames");
		kafkaServers = properties.getProperty("kafkaServers");
	}

	private Properties getProperties() {
		Properties properties = new Properties();
		URL url = StreamingJob.class.getClassLoader().getResource("application.properties");
		try {
			if (url != null) {
				properties.load(url.openStream());
			}
		} catch (FileNotFoundException fie) {
			System.out.println("File Not Found");
			fie.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(properties.getProperty("flink.version"));
		Set<String> keys = properties.stringPropertyNames();
		for (String key : keys) {
			System.out.println(key + " - " + properties.getProperty(key));
		}
		return properties;
	}

	private KafkaSource<KafkaMessage> getKafkaMessageSource(String kafkaServers, String topicName, String groupId) {
		return KafkaSource.<KafkaMessage>builder().setBootstrapServers(kafkaServers).setTopics(topicName)
				.setGroupId(groupId).setStartingOffsets(OffsetsInitializer.latest())
				.setValueOnlyDeserializer(new KafkaMessageSchema()).build();
	}

	private KafkaSink<GroupedData> getOutputSink(String kafkaServers, String topicName) {
		return KafkaSink.<GroupedData>builder().setBootstrapServers(kafkaServers)
				.setRecordSerializer(new KafkaMessageSinkSchema(topicName))
				.setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE).build();
	}

	public void execute() throws Exception {
		Configuration conf = new Configuration();
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);
		env.enableCheckpointing(5000); // Checkpoint every 5 seconds
		env.getCheckpointConfig().setCheckpointTimeout(60000);

		KafkaSource<KafkaMessage> kafkaSource = getKafkaMessageSource(kafkaServers, inputTopicName, consumerGroupId);

		// forBoundedOutOfOrderness below works based on kafka processed/ingestion time
		// not the eventtime. So need to have a separate watermark strategy if you need
		// to process based on event time
		DataStream<KafkaMessage> stream = env.fromSource(kafkaSource,
				WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofMinutes(1)), "Kafka Consumer Source")
				.setParallelism(1);

		// Print the results to the TaskManager logs
		stream.map(message -> {
			System.out.println("Received message: " + message.toString());
			return message;
		});

		SerializableTimestampAssigner<KafkaMessage> sz = new SerializableTimestampAssigner<KafkaMessage>() {
			private static final long serialVersionUID = 1L;

			@Override
			public long extractTimestamp(KafkaMessage message, long l) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					Date date = sdf.parse(String.valueOf(message.ts_ms));
					return date.getTime();
				} catch (ParseException e) {
					return 0;
				}
			}
		};

		WatermarkStrategy<KafkaMessage> watermarkStrategy = WatermarkStrategy
				.<KafkaMessage>forBoundedOutOfOrderness(Duration.ofMillis(1000)).withTimestampAssigner(sz)
				.withIdleness(Duration.ofSeconds(10));

		DataStream<GroupedData> groupedData = stream.assignTimestampsAndWatermarks(watermarkStrategy)
				.keyBy((KeySelector<KafkaMessage, String>) kafkaMessage -> {
					Integer recordId = kafkaMessage.after != null ? Integer.valueOf(kafkaMessage.after.record_id)
							: kafkaMessage.before != null ? Integer.valueOf(kafkaMessage.before.record_id) : null;
					Integer organizationId = kafkaMessage.after != null
							? Integer.valueOf(kafkaMessage.after.organization_id)
							: kafkaMessage.before != null ? Integer.valueOf(kafkaMessage.before.organization_id) : null;
					return recordId == null || organizationId == null ? "defaultKey"
							: recordId.toString() + "_" + organizationId.toString();
				}).window(TumblingProcessingTimeWindows.of(Time.seconds(1)))
				.apply(new GroupByRecordIdAndOrganizationIdProcessFunction());

		KafkaSink<GroupedData> sink = getOutputSink(kafkaServers, outputTopicNames);
		groupedData.sinkTo(sink);
		env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 1000));
		env.execute("Kafka Windowing Job");
	}

	public static void main(String[] args) throws Exception {
		StreamingJob job = new StreamingJob();
		job.execute();
	}
}