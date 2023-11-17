package jobs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import models.GroupedData;
import models.KafkaMessage;
import operator.GroupByRecordIdAndOrganizationIdProcessFunction;
import schema.KafkaMessageSchema;
import schema.KafkaMessageSinkSchema;

@SuppressWarnings({"unused"})
public class StreamingJob {
	private SourceFunction<Long> source;
	private SinkFunction<Long> sink;
	
	private final String kafkaServers = "kafka:9092";
	private final String inputTopicName = "ipoint.public.entity_field_value";
	private final String consumerGroupId = "entity_group";
	private final String outputTopicName = "windowing_output";

	public StreamingJob(SourceFunction<Long> source, SinkFunction<Long> sink) {
		this.source = source;
		this.sink = sink;
	}

	public StreamingJob() {
	}

	public void execute() throws Exception {
		Configuration conf = new Configuration();
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);

		KafkaSource<KafkaMessage> kafkaSource = KafkaSource.<KafkaMessage>builder().setBootstrapServers(kafkaServers)
				.setTopics(inputTopicName).setGroupId(consumerGroupId)
				.setStartingOffsets(OffsetsInitializer.latest())
				.setValueOnlyDeserializer(new KafkaMessageSchema())
				.build();

		DataStream<KafkaMessage> stream = env
				.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Consumer Source").setParallelism(1);

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
				.<KafkaMessage>forBoundedOutOfOrderness(Duration.ofMillis(100)).withTimestampAssigner(sz)
				.withIdleness(Duration.ofSeconds(10));

//		DataStream<KafkaMessage> watermarkDataStream = stream.assignTimestampsAndWatermarks(watermarkStrategy);
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

		KafkaSink<GroupedData> sink = KafkaSink.<GroupedData>builder().setBootstrapServers(kafkaServers)
				.setRecordSerializer(new KafkaMessageSinkSchema(outputTopicName))
				.setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE).build();

		groupedData.sinkTo(sink);
		env.execute("Kafka Windowing Job");
	}

	public static void main(String[] args) throws Exception {
		StreamingJob job = new StreamingJob();
		job.execute();
	}
}