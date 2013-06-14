package com.pehrs.jmeter.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.util.JMeterUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * This is a variation on the Debug Postprocessor that will prettyprint the
 * response as JSON using jackson.
 * 
 * @author matti
 * 
 */
public class JSONViewPostProcessor extends AbstractTestElement implements
		PostProcessor, TestBean {

	private static final long serialVersionUID = 260L;

	private boolean displaySamplerProperties;

	private boolean displayJMeterVariables;

	private boolean displayJMeterProperties;

	private boolean displaySystemProperties;

	public void process() {
		StringBuilder sb = new StringBuilder(100);
		StringBuilder rd = new StringBuilder(20); // for request Data
		SampleResult sr = new SampleResult();
		sr.setSampleLabel(getName());
		sr.sampleStart();
		JMeterContext threadContext = getThreadContext();
		
		Sampler sampler = threadContext.getPreviousSampler();
		if(sampler instanceof HTTPSampler) {
			HTTPSampler httpSampler = (HTTPSampler)sampler;
		}
		
		if (isDisplaySamplerProperties()) {
			rd.append("SamplerProperties\n");
			sb.append("SamplerProperties:\n");
			formatPropertyIterator(sb, threadContext.getCurrentSampler()
					.propertyIterator());
			sb.append("\n");
		}

		if (isDisplayJMeterVariables()) {
			rd.append("JMeterVariables\n");
			sb.append("JMeterVariables:\n");
			formatSet(sb, threadContext.getVariables().entrySet());
			sb.append("\n");
		}

		if (isDisplayJMeterProperties()) {
			rd.append("JMeterProperties\n");
			sb.append("JMeterProperties:\n");
			formatSet(sb, JMeterUtils.getJMeterProperties().entrySet());
			sb.append("\n");
		}

		if (isDisplaySystemProperties()) {
			rd.append("SystemProperties\n");
			sb.append("SystemProperties:\n");
			formatSet(sb, System.getProperties().entrySet());
			sb.append("\n");
		}
		
		SampleResult previous = threadContext.getPreviousResult();
		String previousData = previous.getResponseDataAsString();

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
			JsonNode df = mapper.readValue(previousData, JsonNode.class);
			ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
			// System.out.println(df.toString());

			sb.append(writer.writeValueAsString(df));
		} catch (JsonParseException e) {
			sb.append(e.getMessage());
		} catch (JsonMappingException e) {
			sb.append(e.getMessage());
		} catch (JsonGenerationException e) {
			sb.append(e.getMessage());
		} catch (IOException e) {
			sb.append(e.getMessage());
		}
		sb.append("\n");

		sr.setResponseData(sb.toString(), null);
		sr.setDataType(SampleResult.TEXT);
		sr.setSamplerData(rd.toString());
		sr.setResponseOK();
		sr.sampleEnd();
		threadContext.getPreviousResult().addSubResult(sr);
	}

	private void formatPropertyIterator(StringBuilder sb, PropertyIterator iter) {
		Map<String, String> map = new HashMap<String, String>();
		while (iter.hasNext()) {
			JMeterProperty item = iter.next();
			map.put(item.getName(), item.getStringValue());
		}
		formatSet(sb, map.entrySet());
	}

	private void formatSet(StringBuilder sb, @SuppressWarnings("rawtypes") Set s) {
		@SuppressWarnings("unchecked")
		ArrayList<Map.Entry<Object, Object>> al = new ArrayList<Map.Entry<Object, Object>>(
				s);
		Collections.sort(al, new Comparator<Map.Entry<Object, Object>>() {
			public int compare(Map.Entry<Object, Object> o1,
					Map.Entry<Object, Object> o2) {
				String m1, m2;
				m1 = (String) o1.getKey();
				m2 = (String) o2.getKey();
				return m1.compareTo(m2);
			}
		});
		for (Map.Entry<Object, Object> me : al) {
			sb.append(me.getKey());
			sb.append("=");
			sb.append(me.getValue());
			sb.append("\n");
		}
	}

	public boolean isDisplayJMeterVariables() {
		return displayJMeterVariables;
	}

	public void setDisplayJMeterVariables(boolean displayJMeterVariables) {
		this.displayJMeterVariables = displayJMeterVariables;
	}

	public boolean isDisplayJMeterProperties() {
		return displayJMeterProperties;
	}

	public void setDisplayJMeterProperties(boolean displayJMeterPropterties) {
		this.displayJMeterProperties = displayJMeterPropterties;
	}

	public boolean isDisplaySamplerProperties() {
		return displaySamplerProperties;
	}

	public void setDisplaySamplerProperties(boolean displaySamplerProperties) {
		this.displaySamplerProperties = displaySamplerProperties;
	}

	public boolean isDisplaySystemProperties() {
		return displaySystemProperties;
	}

	public void setDisplaySystemProperties(boolean displaySystemProperties) {
		this.displaySystemProperties = displaySystemProperties;
	}
}
