package com.pehrs.jmeter.docs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JacksonUtil {

	static public long getLong(ObjectNode node, String name) {
		if(node.has(name)) {
			return node.get(name).asLong();
		}
		return 0l;
	}

	static public double getDouble(ObjectNode node, String name) {
		if(node.has(name)) {
			return node.get(name).asDouble();
		}
		return 0d;
	}

	static public int getInt(ObjectNode node, String name) {
		if(node.has(name)) {
			return node.get(name).asInt();
		}
		return 0;
	}

	static public ObjectNode getOrCreateObject(ObjectNode json, String name) {
		ObjectNode res = (ObjectNode) json.get(name);
		if (res != null) {
			return res;
		}
		res = json.putObject(name);
		return res;
	}

	static public ObjectNode getOrCreateNode4Url(ArrayNode json, String url) {
		return getOrCreateNode4Key(json, "url", url);
	}
	
	static public ObjectNode getOrCreateNode4Id(ArrayNode json, String id) {
		return getOrCreateNode4Key(json, "id", id);
	}

	static public ObjectNode getOrCreateNode4Key(ArrayNode json, String key, String value) {
		for (int i = 0; i < json.size(); i++) {
			JsonNode node = json.get(i);
			String nodeId = node.get(key).asText();
			if (value.equals(nodeId)) {
				return (ObjectNode) node;
			}
		}
		ObjectNode obj = json.addObject();
		obj.put(key, value);
		return obj;
	}

}
