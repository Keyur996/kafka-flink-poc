from pyflink.datastream import StreamExecutionEnvironment
from pyflink.table import StreamTableEnvironment
from pyflink.table import DataTypes

env = StreamExecutionEnvironment.get_execution_environment()
t_env = StreamTableEnvironment.create(env)

# Define a custom schema to handle JSON key and value.
key_value_schema = DataTypes.ROW([DataTypes.FIELD("key", DataTypes.STRING()), DataTypes.FIELD("value", DataTypes.STRING())])

# Connect to the Kafka topic with JSON key and value.
t_env.connect(
    connector='kafka',
    version='universal',
    topic='worker.public.entity_field_value',
    properties={
        'bootstrap.servers': 'localhost:29092',
        'group.id': 'entity_group',
        'auto.offset.reset': 'latest',
        'key.deserializer': 'org.apache.kafka.common.serialization.StringDeserializer',
        'value.deserializer': 'org.apache.kafka.common.serialization.StringDeserializer'
    }
).with_format('json').with_schema(key_value_schema).create_temporary_table('kafka_source')

# Parse the JSON key and value.
t_env.from_path('kafka_source') \
    .select("key, JSON_VALUE, JSON_VALUE.key AS key_field, JSON_VALUE.value AS value_field") \
    .execute_insert('sink_table')

env.execute("Kafka Consumer Job")
