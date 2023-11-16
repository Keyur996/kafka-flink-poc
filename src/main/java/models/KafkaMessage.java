package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KafkaMessage {
	public EntityFieldValue before;
	public EntityFieldValue after;
	public KafkaSourceData source;
	public String op;
	public Long ts_ms;
	public String transaction;

	public KafkaMessage() {
		super();
		// TODO Auto-generated constructor stub
	}

	@JsonCreator
	public KafkaMessage(@JsonProperty("before") EntityFieldValue before, @JsonProperty("after") EntityFieldValue after,
			@JsonProperty("source") KafkaSourceData source, @JsonProperty("op") String op,
			@JsonProperty("ts_ms") Long ts_ms, @JsonProperty("transaction") String transaction) {
		super();
		this.before = before;
		this.after = after;
		this.source = source;
		this.op = op;
		this.ts_ms = ts_ms;
		this.transaction = transaction;
	}

	@Override
	public String toString() {
		return "KafkaMessage [before=" + before + ", after=" + after + ", source=" + source + ", op=" + op + ", ts_ms="
				+ ts_ms + ", transaction=" + transaction + "]";
	}

	public EntityFieldValue getBefore() {
		return before;
	}

	public void setBefore(EntityFieldValue before) {
		this.before = before;
	}

	public EntityFieldValue getAfter() {
		return after;
	}

	public void setAfter(EntityFieldValue after) {
		this.after = after;
	}

	public KafkaSourceData getSource() {
		return source;
	}

	public void setSource(KafkaSourceData source) {
		this.source = source;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public Long getTs_ms() {
		return ts_ms;
	}

	public void setTs_ms(Long ts_ms) {
		this.ts_ms = ts_ms;
	}

	public String getTransaction() {
		return transaction;
	}

	public void setTransaction(String transaction) {
		this.transaction = transaction;
	}
}
