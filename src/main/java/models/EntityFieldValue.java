package models;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EntityFieldValue {
	public int id;
	public int organization_id;
	public int branch_id;
	public int entity_field_id;
	public int record_id;
	public String value;
	public Object value_json;
	public int created_by;
	public int updated_by;
	public int deleted_by;
	public Date created_at;
	public Date updated_at;
	public Date deleted_at;

	@JsonCreator
	public EntityFieldValue(@JsonProperty("id") int id, @JsonProperty("organization_id") int organization_id,
			@JsonProperty("branch_id") int branch_id, @JsonProperty("entity_field_id") int entity_field_id,
			@JsonProperty("record_id") int record_id, @JsonProperty("value") String value,
			@JsonProperty("value_json") Object value_json, @JsonProperty("created_by") int created_by,
			@JsonProperty("updated_by") int updated_by, @JsonProperty("deleted_by") int deleted_by,
			@JsonProperty("created_at") Date created_at, @JsonProperty("updated_at") Date updated_at,
			@JsonProperty("deleted_at") Date deleted_at) {
		super();
		this.id = id;
		this.organization_id = organization_id;
		this.branch_id = branch_id;
		this.entity_field_id = entity_field_id;
		this.record_id = record_id;
		this.value = value;
		this.value_json = value_json;
		this.created_by = created_by;
		this.updated_by = updated_by;
		this.deleted_by = deleted_by;
		this.created_at = created_at;
		this.updated_at = updated_at;
		this.deleted_at = deleted_at;
	}

	public EntityFieldValue() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOrganization_id() {
		return organization_id;
	}

	public void setOrganization_id(int organization_id) {
		this.organization_id = organization_id;
	}

	public int getBranch_id() {
		return branch_id;
	}

	public void setBranch_id(int branch_id) {
		this.branch_id = branch_id;
	}

	public int getEntity_field_id() {
		return entity_field_id;
	}

	public void setEntity_field_id(int entity_field_id) {
		this.entity_field_id = entity_field_id;
	}

	public int getRecord_id() {
		return record_id;
	}

	public void setRecord_id(int record_id) {
		this.record_id = record_id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Object getValue_json() {
		return value_json;
	}

	public void setValue_json(Object value_json) {
		this.value_json = value_json;
	}

	public int getCreated_by() {
		return created_by;
	}

	public void setCreated_by(int created_by) {
		this.created_by = created_by;
	}

	public int getUpdated_by() {
		return updated_by;
	}

	public void setUpdated_by(int updated_by) {
		this.updated_by = updated_by;
	}

	public int getDeleted_by() {
		return deleted_by;
	}

	public void setDeleted_by(int deleted_by) {
		this.deleted_by = deleted_by;
	}

	public Date getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

	public Date getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(Date updated_at) {
		this.updated_at = updated_at;
	}

	public Date getDeleted_at() {
		return deleted_at;
	}

	public void setDeleted_at(Date deleted_at) {
		this.deleted_at = deleted_at;
	}

	@Override
	public String toString() {
		return "EntityFieldValue [id=" + id + ", organization_id=" + organization_id + ", branch_id=" + branch_id
				+ ", entity_field_id=" + entity_field_id + ", record_id=" + record_id + ", value=" + value
				+ ", value_json=" + value_json + ", created_by=" + created_by + ", updated_by=" + updated_by
				+ ", deleted_by=" + deleted_by + ", created_at=" + created_at + ", updated_at=" + updated_at
				+ ", deleted_at=" + deleted_at + "]";
	}

}
