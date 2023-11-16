package models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GroupedData {
	public String key;
	public List<KafkaMessage> values;

	@JsonCreator
	public GroupedData(@JsonProperty("key") String key, @JsonProperty("values") List<KafkaMessage> values) {
		this.key = key;
		this.values = values;
	}

	public GroupedData() {
		super();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<KafkaMessage> getValues() {
		return values;
	}

	public void setValues(List<KafkaMessage> values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return "GroupedData [key=" + key + ", values=" + values + "]";
	}

}
