package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KafkaSourceData {
	public String version;
	public String connector;
	public String name;
	public String ts_ms;
	public String snapshot;
	public String db;
	public String sequence;
	public String schema;
	public String table;
	public String txId;
	public String lsn;
	public String xmin;

	@JsonCreator
	public KafkaSourceData(@JsonProperty("version") String version, @JsonProperty("connector") String connector,
			@JsonProperty("name") String name, @JsonProperty("ts_ms") String ts_ms,
			@JsonProperty("snapshot") String snapshot, @JsonProperty("db") String db,
			@JsonProperty("sequence") String sequence, @JsonProperty("schema") String schema,
			@JsonProperty("table") String table, @JsonProperty("txId") String txId, @JsonProperty("lsn") String lsn,
			@JsonProperty("xmin") String xmin) {
		super();
		this.version = version;
		this.connector = connector;
		this.name = name;
		this.ts_ms = ts_ms;
		this.snapshot = snapshot;
		this.db = db;
		this.sequence = sequence;
		this.schema = schema;
		this.table = table;
		this.txId = txId;
		this.lsn = lsn;
		this.xmin = xmin;
	}

	public KafkaSourceData() {
		super();
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getConnector() {
		return connector;
	}

	public void setConnector(String connector) {
		this.connector = connector;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTs_ms() {
		return ts_ms;
	}

	public void setTs_ms(String ts_ms) {
		this.ts_ms = ts_ms;
	}

	public String getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public String getLsn() {
		return lsn;
	}

	public void setLsn(String lsn) {
		this.lsn = lsn;
	}

	public String getXmin() {
		return xmin;
	}

	public void setXmin(String xmin) {
		this.xmin = xmin;
	}

	@Override
	public String toString() {
		return "KafkaSourceData [version=" + version + ", connector=" + connector + ", name=" + name + ", ts_ms="
				+ ts_ms + ", snapshot=" + snapshot + ", db=" + db + ", sequence=" + sequence + ", schema=" + schema
				+ ", table=" + table + ", txId=" + txId + ", lsn=" + lsn + ", xmin=" + xmin + "]";
	}

}
