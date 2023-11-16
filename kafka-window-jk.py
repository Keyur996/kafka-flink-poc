import logging
from pyflink.common import Types, WatermarkStrategy, Time
from pyflink.datastream import StreamExecutionEnvironment,TimeCharacteristic
from pyflink.datastream.connectors.kafka import KafkaSource, KafkaOffsetsInitializer, KafkaSink, KafkaRecordSerializationSchema
from pyflink.datastream.window import TumblingEventTimeWindows, TimeWindow
from pyflink.datastream.functions import ProcessFunction,KeySelector
from pyflink.datastream.connectors.base import DeliveryGuarantee
from pyflink.datastream.formats.json import  JsonRowDeserializationSchema, JsonRowSerializationSchema

from kafka.admin import NewTopic
from kafka.errors import TopicAlreadyExistsError


def GroupByRecordAndOrganization(data):
    print('----1----',data)
    return data

def create_kafka_topic(admin_client, topic_name, num_partitions, replication_factor):
    try:
        # Try to create the topic
        new_topic = NewTopic(
            name=topic_name,
            num_partitions=num_partitions,
            replication_factor=replication_factor
        )
        admin_client.create_topics([new_topic])
        print("Topic {} is created.".format(topic_name))
    except TopicAlreadyExistsError:
        # Handle the case where the topic already exists
        print("Topic {} already exists.".format(topic_name))

def process_data(data):
     print(data)
     print("data jk")
     logging.warning(data)
     logging.warning("data jkk")
     return data

def process_data_key(data):
     return 'test'

def read_and_group_data():
    env = StreamExecutionEnvironment.get_execution_environment()
    
    # /flink-jobs/dependency/
    env.add_jars("file:///flink-jobs/dependency/flink-sql-connector-kafka-3.0.1-1.18.jar")
    env.set_stream_time_characteristic(TimeCharacteristic.EventTime)
    print("Start reading and grouping data from Kafka")
    brokers='kafka:9092'
    # Create a KafkaAdminClient to create a new Kafka topic
    # admin_client = KafkaAdminClient(bootstrap_servers='kafka:9092')

    # # Define the name and configuration of the new topic
    # new_topic_name = 'entity_topic'
    # create_kafka_topic(admin_client, new_topic_name, 1, 1)

    # Initialize a Kafka consumer to read data from the source topic
    type_info = Types.ROW([
        Types.ROW([
            Types.INT(),
            Types.INT(),
            Types.INT(),
            Types.INT(),
            Types.INT(),
            Types.BIG_INT(),
            Types.STRING(),
            Types.STRING(),
            Types.STRING(),
            Types.STRING(),
            Types.STRING(),
            Types.STRING(),
            Types.STRING(),
            Types.STRING()
        ]),
        Types.ROW([
            Types.INT(),
            Types.INT(),
            Types.INT(),
            Types.INT(),
            Types.INT(),
            Types.BIG_INT(),
            Types.STRING(),
            Types.STRING(),
            Types.STRING(),
            Types.STRING(),
            Types.STRING(),
            Types.STRING(),
            Types.STRING(),
            Types.STRING(),
            Types.STRING()
        ]),
        Types.ROW([
            Types.STRING(),
            Types.STRING(),
            Types.STRING(),
            Types.BIG_INT(),
            Types.BOOLEAN(),
            Types.STRING(),
            Types.STRING(),
            Types.STRING(),
            Types.INT(),
            Types.BIG_INT(),
            Types.INT(),
            Types.INT(),
            Types.INT(),
            Types.STRING(),
            Types.BIG_INT()
        ]),
        Types.STRING(),
        Types.BIG_INT(),
        Types.STRING()
    ])
    deserialization_schema = JsonRowDeserializationSchema.Builder().type_info(type_info).build()
    

    kafka_source = KafkaSource.builder() \
        .set_bootstrap_servers(brokers) \
        .set_topics("ipoint.public.entity_field_value") \
        .set_group_id("sd-window-cdc") \
        .set_starting_offsets(KafkaOffsetsInitializer.earliest()) \
        .set_value_only_deserializer(deserialization_schema) \
        .build()

    ds = env.from_source(source=kafka_source, watermark_strategy=WatermarkStrategy.for_monotonous_timestamps(), source_name="Kafka Source")

    def key_selector(data):
        # Check if 'after' is present in the data
        after_data = data.get('after', {})
        
        # Access 'organization_id' and 'record_id' from 'after' data
        organization_id = after_data.get('organization_id', None)
        record_id = after_data.get('record_id', None)
        
        # Check if both 'organization_id' and 'record_id' are present
        if organization_id is not None and record_id is not None:
            return f"{organization_id}_{record_id}"
        else:
            return None  # Return None if either 'organization_id' or 'record_id' is missing

    def filter_data(data):
         print("data jk")
         print(data)
         logging.warning(data)
         return data

    # lambda data: f"{data[0]['after']['organization_id']}_{data[0]['after']['record_id']}"
    ds.map(filter_data).key_by(key_selector, key_type=Types.STRING()) \
        .window(TumblingEventTimeWindows.of(Time.milliseconds(2000))) \
        .process(GroupByRecordAndOrganization)

    
    sink = KafkaSink.builder() \
        .set_bootstrap_servers(brokers) \
        .set_record_serializer(
            KafkaRecordSerializationSchema.builder()
                .set_topic("entity_type")
                .set_value_serialization_schema(JsonRowSerializationSchema.Builder().with_type_info(Types.ROW([
        Types.INT(), Types.STRING()
    ])).build())
                .build()
        ) \
        .set_delivery_guarantee(DeliveryGuarantee.AT_LEAST_ONCE) \
        .build()

    ds.sink_to(sink)

#     kafka_consumer = FlinkKafkaConsumer(
#         topics='ipoint.public.entity_field_value',
#         deserialization_schema=deserialization_schema,
#         properties={'bootstrap.servers': 'kafka:9092', 'group.id': 'entity_group'}
#     )

#     kafka_consumer.set_start_from_earliest()

#     # Add a data source to the Flink environment using the Kafka consumer
#     data_stream = env.add_source(kafka_consumer, source_name="Kafka Source")


#     ds = data_stream \
#         .key_by(lambda x: x[0], key_type=Types.STRING()) \
#         .window(TumblingEventTimeWindows.of(Time.milliseconds(1000))) \
#         .process(GroupByRecordAndOrganization())
    
#     ds.print()

#     # # Send the grouped data to the new topic
#     # ds.add_sink(FlinkKafkaProducer(
#     #     topic=new_topic_name,
#     #     serialization_schema=JsonRowSerializationSchema.Builder().type_info(Types.ROW([
#     #         Types.INT(), Types.STRING()
#     #     ])).build(),
#     #     producer_config={'bootstrap.servers': 'kafka:9092'}
#     # ))

#     sink = KafkaSink.builder() \
#     .set_bootstrap_servers(brokers) \
#     .set_record_serializer(
#         KafkaRecordSerializationSchema.builder()
#             .set_topic("topic-name")
#             .set_value_serialization_schema(SimpleStringSchema())
#             .build()
#     ) \
#     .set_delivery_guarantee(DeliveryGuarantee.AT_LEAST_ONCE) \
#     .build()

# stream.sink_to(sink)

    # Execute the Flink job
    env.execute("Kafka Data Grouping and Writing")

if __name__ == '__main__':
    # logging.basicConfig(stream=sys.stdout, level=logging.WARNING, format="%(message)s")

  
    read_and_group_data()