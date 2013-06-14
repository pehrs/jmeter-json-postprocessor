package com.pehrs.jmeter.docs;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DocResultCollector extends AbstractListenerElement implements
		SampleListener, Clearable, Serializable, TestStateListener, Remoteable,
		NoThreadClone {
	private static final String NO_COOKIES = "[no cookies]";

	enum HttpMethod {
		GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, PATCH,
	}

	private static final String POST_DATA = "POST data:\n";

	private static final String PUT_DATA = "PUT data:\n";

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggingManager.getLoggerForClass();

	public static final String DOC_MODEL = "DocResultCollector.doc_model";

	public static final String FILENAME = "filename";
	public static final String ROUTES_FILENAME = "routesFilename";

	private static final String SAVE_CONFIG = "saveConfig";

	private static final Map<String, FileEntry> files = new HashMap<String, FileEntry>();

	private static final Object LOCK = new Object();

	private static int instanceCount; // Keep track of how many instances are
										// active
	// private PrintWriter out;
	FileEntry entry;

	private ObjectMapper mapper = null;

	public DocResultCollector() {
		super();
		log.debug("new DocResultCollector()");
		// setProperty(new TestElementProperty(DOC_MODEL, new MailerModel()));
	}

	public JsonNode parseString2JSon(String jsonStr) throws JsonParseException,
			IOException {
		ObjectMapper mppr = getObjectMapper();
		JsonFactory factory = mppr.getJsonFactory();

		JsonParser jp = factory.createJsonParser(jsonStr.toString());
		JsonNode actualObj = mppr.readTree(jp);
		return actualObj;
	}

	private ObjectMapper getObjectMapper() {
		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		}

		return mapper;
	}

	private static class FileEntry {
		final PrintWriter pw;
		final SampleSaveConfiguration config;
		final ArrayNode json;
		final List<Route> routes;

		FileEntry(PrintWriter _pw, SampleSaveConfiguration _config,
				ArrayNode _node, List<Route> _route) {
			this.pw = _pw;
			this.config = _config;
			this.json = _node;
			this.routes = _route;
		}

		public Route getMatchingRoute(HttpMethod method, URL url) {
			if (routes == null)
				return null;

			String path = url.getPath();
			for (Route route : routes) {
				if (route.method.equals(method)) {
					Matcher match = route.regex.matcher(path);
					if (match.matches()) {
						return route;
					}
				}
			}

			return null;
		}

		public ObjectNode getRouteNode(Route route) {
			ObjectNode obj = getUrlNode(route.method, route.route);			
			
			obj.put("call", route.call);
			obj.put("method", "" + route.method);
			obj.put("regex", "" + route.regex);
			ArrayNode params = obj.putArray("params");
			for (String param : route.params) {
				params.add(param);
			}
			return obj;
		}

		public ObjectNode getUrlNode(HttpMethod method, String url) {
			
			for (int i = 0; i < json.size(); i++) {
				JsonNode node = json.get(i);
				String nodeUrl = node.get("url").asText();
				String nodeMethod = node.get("method").asText();
				if (url.equals(nodeUrl) &&
						(method.toString()).equals(nodeMethod)) {
					return (ObjectNode) node;
				}
			}
			ObjectNode obj = json.addObject();
			obj.put("method", ""+method);
			obj.put("url", ""+url);
			return obj;
		}
	}

	@Override
	public Object clone() {
		log.debug("DocResultCollector.clone()");

		DocResultCollector clone = (DocResultCollector) super.clone();
		clone.setSaveConfig((SampleSaveConfiguration) clone.getSaveConfig()
				.clone());
		// Unfortunately AbstractTestElement does not call super.clone()
		// clone.summariser = this.summariser;
		return clone;
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		log.debug("DocResultCollector.clear()");
		super.clear();
		// setProperty(new TestElementProperty(DOC_MODEL, new MailerModel()));
	}

	/** {@inheritDoc} */
	@Override
	public void sampleOccurred(SampleEvent event) {
		// super.sampleOccurred(e); // sends the result to the visualiser
		// getMailerModel().add(e.getResult(), true);
		log.debug(" ===========> DocResultCollector.sampleOccurred()" + event);
		SampleResult result = event.getResult();
		if (result instanceof HTTPSampleResult 
				&& entry != null) {
			result2json(result, entry);
		}

		if (isSampleWanted(result.isSuccessful())) {
			sendToVisualizer(result);
		}
	}

	private void result2json(SampleResult result, FileEntry entry) {
		// String url = result.getUrlAsString();

		URL url = result.getURL();
		HttpMethod method = getHttpMethod(result);

		ObjectNode node;
		if (entry.routes != null) {
			// Match for a node on route.route
			Route route = entry.getMatchingRoute(method, url);
			if (route != null) {
				node = entry.getRouteNode(route);
			} else {
				log.error("Could not find Route in Entry for method "+method+" and url "+url);
				node = entry.getUrlNode(method, ""+url);
			}
		} else {
			node = entry.getUrlNode(method, ""+url);
		}
		node.put("count", JacksonUtil.getInt(node, "count") + 1);

		long elapsed = result.getEndTime() - result.getStartTime();
		long elapsedMax = JacksonUtil.getLong(node, "elapsedMax");
		node.put("elapsedMax", Math.max(elapsedMax, elapsed));
		long elapsedMin = JacksonUtil.getLong(node, "elapsedMin");
		if (elapsedMin == 0) {
			node.put("elapsedMin", elapsed);
		} else {
			node.put("elapsedMin", Math.min(elapsedMin, elapsed));
		}
		double elapsedAvg = JacksonUtil.getDouble(node, "elapsedAvg");
		if (elapsedAvg == 0d) {
			node.put("elapsedAvg", elapsed);
		} else {
			node.put("elapsedAvg", ((elapsedAvg + (double) elapsed) / 2.0d));
		}

		long latencyMax = JacksonUtil.getLong(node, "latencyMax");
		long latency = result.getLatency();
		node.put("latencyMax", Math.max(latencyMax, latency));
		long latencyMin = JacksonUtil.getLong(node, "latencyMin");
		if (latencyMin == 0) {
			node.put("latencyMin", latency);
		} else {
			node.put("latencyMin", Math.min(latencyMin, latency));
		}
		double latencyAvg = JacksonUtil.getDouble(node, "latencyAvg");
		if (latencyAvg == 0d) {
			node.put("latencyAvg", latency);
		} else {
			node.put("latencyAvg", ((latencyAvg + (double) latency) / 2.0d));
		}

		String reqData = result.getSamplerData();
		String responseCode = result.getResponseCode();

		String key = "" + method + "_" + responseCode;

		ArrayNode samples = node.putArray("samples");
		ObjectNode sample = JacksonUtil.getOrCreateNode4Id(samples, key);

		addRequest(result, sample, method, reqData);
		addResponse(result, sample, responseCode);
	}

	private void addResponse(SampleResult result, ObjectNode node,
			String responseCode) {
		ObjectNode response = node.putObject("response");
		response.put("responseCode", responseCode);
		response.put("responseMessage", result.getResponseMessage());
		response.put("contentType", result.getContentType());
		response.put("headers", toObjectNode(result.getResponseHeaders()));
		String dataType = result.getDataType();
		response.put("dataType", dataType);
		if (dataType.equals("text")) {
			response.put("data", result.getResponseDataAsString());
		}
	}

	private void addRequest(SampleResult result, ObjectNode node,
			HttpMethod method, String reqData) {
		ObjectNode request = node.putObject("request");
		request.put("headers", toObjectNode(result.getRequestHeaders()));
		request.put("method", "" + method);
		switch (method) {
		case GET:
			// No data for GET methods
			break;
		case DELETE:
			// No data for DELETE methods
			break;
		case POST:
			request.put("data", getRequestData(result, POST_DATA));
			break;
		case PUT:
			request.put("data", getRequestData(result, PUT_DATA));
			break;
		default:
			request.put("data", reqData.replace(NO_COOKIES, ""));
			break;
		}
	}

	private String getRequestData(SampleResult result, String delimiter) {
		String reqData = result.getSamplerData();
		int index = reqData.indexOf(delimiter);
		if (index != -1) {
			return reqData.substring(index + delimiter.length()).replace(
					NO_COOKIES, "");
		} else {
			return reqData.replace(NO_COOKIES, "");
		}
	}

	private String prettify(String data) {
		try {
			ObjectMapper mp = getObjectMapper();
			JsonNode json = parseString2JSon(data);
			ObjectWriter writer = mp.writer().withDefaultPrettyPrinter();
			return writer.writeValueAsString(json);
		} catch (JsonParseException e) {
			e.printStackTrace();
			return data;
		} catch (JsonGenerationException e) {
			e.printStackTrace();
			return data;
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return data;
		} catch (IOException e) {
			e.printStackTrace();
			return data;
		}
	}

	private HttpMethod getHttpMethod(SampleResult result) {
		String reqData = result.getSamplerData();

		int firstSpace = reqData.indexOf(" ");
		if (firstSpace == -1) {
			throw new IllegalArgumentException(
					"Could not parse http method from " + reqData);
		}
		String methodStr = reqData.substring(0, firstSpace);
		try {
			return HttpMethod.valueOf(methodStr);
		} catch (IllegalArgumentException e) {
			log.error("Could not parse '"+methodStr+"' to HttpMethod "+HttpMethod.values());
			e.printStackTrace();
			throw e;
		}
	}

	private ObjectNode toObjectNode(String headers) {

		ObjectNode node = getObjectMapper().createObjectNode();

		try {
			BufferedReader in = new BufferedReader(new StringReader(headers));
			for (String line = in.readLine(); line != null; line = in
					.readLine()) {
				String[] parts = line.split(":");
				if (parts != null && parts.length > 1) {
					String header = parts[0].trim();
					String val = parts[1].trim();
					node.put(header, val);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return node;
	}

	private String toJsonArray(String data) {
		StringBuilder out = new StringBuilder();
		out.append("[");

		try {
			BufferedReader in = new BufferedReader(new StringReader(data));
			int index = 0;
			for (String line = in.readLine(); line != null; line = in
					.readLine()) {
				String[] parts = line.split(":");
				if (parts != null && parts.length > 1) {
					String header = parts[0].trim();
					String val = parts[1].trim();
					if (index > 0) {
						out.append(", ");
					}
					out.append("{\"" + header + "\":\"" + val + "\"}");
					index++;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		out.append("]");
		return out.toString();
	}

	private String stringify(String data) {
		return data.replace("\n", "\\n").replace("\r", "")
				.replace("\"", "\\\"").replace("'", "\\'");
	}

	public boolean isSampleWanted(boolean success) {
		log.debug("DocResultCollector.isSampleWanted()");
		// We want all samples...
		return true;
	}

	@Override
	public void testStarted() {
		log.debug("DocResultCollector.testStarted()");
		synchronized (LOCK) {
			instanceCount++;
			try {
				initializeFileOutput();
				if (getVisualizer() != null) {
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	@Override
	public void testStarted(String host) {
		log.debug("DocResultCollector.testStarted() host=" + host);
	}

	@Override
	public void testEnded() {
		log.debug("DocResultCollector.testEnded()");
		synchronized (LOCK) {
			instanceCount--;
			if (instanceCount <= 0) {
				finalizeFileOutput();
			}
		}
	}

	@Override
	public void testEnded(String host) {
		log.debug("DocResultCollector.testStarted() host=" + host);

	}

	@Override
	public void clearData() {
		log.debug("DocResultCollector.clearData()");

	}

	@Override
	public void sampleStarted(SampleEvent e) {
		log.debug("DocResultCollector.clearData() event=" + e);
	}

	@Override
	public void sampleStopped(SampleEvent e) {
		log.debug("DocResultCollector.sampleStopped() event=" + e);
	}

	public String getFilename() {
		return getPropertyAsString(FILENAME);
	}

	public void setFilename(String f) {
		log.debug("DocResultCollector.setFilename() filename=" + f);
		if (!f.endsWith(".js")) {
			f = f + ".js";
		}
		setProperty(FILENAME, f);
	}

	public String getRoutesFilename() {
		return getPropertyAsString(ROUTES_FILENAME);
	}

	public void setRoutesFilename(String fn) {
		log.debug("DocResultCollector.setRoutesFilename() filename=" + fn);
		setProperty(ROUTES_FILENAME, fn);
	}

	/**
	 * @param saveConfig
	 *            The saveConfig to set.
	 */
	public void setSaveConfig(SampleSaveConfiguration saveConfig) {
		log.debug("DocResultCollector.setSaveConfig()");
		getProperty(SAVE_CONFIG).setObjectValue(saveConfig);
	}

	/**
	 * @return Returns the saveConfig.
	 */
	public SampleSaveConfiguration getSaveConfig() {
		log.debug("DocResultCollector.getSaveConfig()");

		try {
			SampleSaveConfiguration saveConfig = (SampleSaveConfiguration) getProperty(
					SAVE_CONFIG).getObjectValue();
			if (saveConfig == null) {
				saveConfig = new SampleSaveConfiguration();
				setSaveConfig(saveConfig);
			}
			return saveConfig;
		} catch (ClassCastException e) {
			setSaveConfig(new SampleSaveConfiguration());
			return getSaveConfig();
		}
	}

	public void loadExistingFile() {
		log.debug("DocResultCollector.loadExistingFile()");
		final Visualizer visualizer = getVisualizer();
		if (visualizer == null) {
			return; // No point reading the file if there's no visualiser
		}
		String filename = getFilename();
		File file = new File(filename);
		if (file.exists()) {
			// ... read the file
		} else {
			GuiPackage.showErrorMessage(
					"Error loading results file - could not open file",
					"Result file loader");
		}
	}

	protected final void sendToVisualizer(SampleResult r) {
		if (getVisualizer() != null) {
			getVisualizer().add(r);
		}
	}

	private void finalizeFileOutput() {
		log.debug("DocResultCollector.finalizeFileOutput()");
		for (Map.Entry<String, DocResultCollector.FileEntry> me : files
				.entrySet()) {
			log.debug("Closing: " + me.getKey());
			FileEntry fe = me.getValue();
			if (fe.routes != null) {
				// Make sure we have all routes in there
				for (Route route : fe.routes) {
					fe.getRouteNode(route);
				}
			}

			try {
				writeFile(fe);
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			fe.pw.close();
			if (fe.pw.checkError()) {
				log.warn("Problem detected during use of " + me.getKey());
			}
		}
		files.clear();
	}

	private void writeFile(FileEntry entry) throws JsonGenerationException,
			JsonMappingException, IOException {
		ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
		entry.pw.println(writer.writeValueAsString(entry.json));
	}

	private void initializeFileOutput() throws IOException {
		String filename = getFilename();
		log.debug("DocResultCollector.initializeFileOutput() filename="
				+ filename);
		if (filename != null) {
			if (entry == null) {
				entry = getEntry(filename, getRoutesFilename(), getSaveConfig());
			}
		}
	}

	private FileEntry getEntry(String filename, String routesFilename,
			SampleSaveConfiguration saveConfig) throws IOException {
		log.debug("DocResultCollector.getFileWriter() filename=" + filename);
		if (filename == null || filename.length() == 0) {
			return null;
		}
		filename = FileServer.resolveBaseRelativeName(filename);
		FileEntry fe = files.get(filename);
		PrintWriter writer;
		if (fe == null) {
			ArrayNode json = readJson(filename);

			// Find the name of the directory containing the file
			// and create it - if there is one
			File pdir = new File(filename).getParentFile();
			if (pdir != null) {
				// returns false if directory already exists, so need to check
				// again
				if (pdir.mkdirs()) {
					log.info("Folder " + pdir.getAbsolutePath()
							+ " was created");
				} // else if might have been created by another process so not a
					// problem
				if (!pdir.exists()) {
					log.warn("Error creating directories for "
							+ pdir.toString());
				}
			}
			writer = new PrintWriter(new OutputStreamWriter(
					new BufferedOutputStream(new FileOutputStream(filename)),
					SaveService.getFileEncoding("UTF-8")), true);
			log.debug("Opened file: " + filename);
			fe = new FileEntry(writer, saveConfig, json,
					getRoutesFile(routesFilename));
			files.put(filename, fe);
		}
		return fe;
	}

	private ArrayNode readJson(String filename) throws IOException {

		File file = new File(filename);
		if (!file.exists()) {
			return getObjectMapper().createArrayNode();
		}

		StringBuilder data = new StringBuilder();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			for (String line = in.readLine(); line != null; line = in
					.readLine()) {
				data.append(line).append("\n");
			}

			ArrayNode json = (ArrayNode) parseString2JSon(data.toString());
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				in.close();
			}
		}

	}

	private static class Route {
		HttpMethod method;
		String route;
		String call;
		List<String> params = new ArrayList<String>();
		Pattern regex;

		@Override
		public String toString() {
			return "Route [method=" + method + ", route=" + route + ", call="
					+ call + ", params=" + params + ", regex=" + regex + "]";
		}

	}

	public static void main(String[] args) throws IOException {
		URL url = new URL(
				"https://test2.dalockr.com/api/0/adm/asset?param=one&p=two");
		// String urlPath =
		// url.getPath()+(url.getQuery()==null?"":"?"+url.getQuery());
		String urlPath = url.getPath();

		log.debug("url=" + url);
		log.debug("url.path=" + url.getPath());
		log.debug("url.query=" + url.getQuery());
		log.debug("url.path+query=" + urlPath);

		String routesFilename = "/media/DEVELOPMENT/dalockr/v2/ws/dalockr2/conf/routes";
		List<Route> routes = getRoutesFile(routesFilename);
		
		log.debug("==============================");
		log.debug("==============================");
		log.debug("==============================");

		for (Route route : routes) {
			Matcher match = route.regex.matcher(urlPath);
			if (match.matches()) {
				log.debug("URL "+url+" MATCH route " + route);
				int index = 1;
				for (String param : route.params) {
					log.debug(" " + param + "=" + match.group(index));
					index++;
				}
			}
		}

	}

	private static List<Route> getRoutesFile(String routesFilename)
			throws FileNotFoundException, IOException {
		if (routesFilename == null) {
			return null;
		}
		if (routesFilename.length() == 0) {
			return null;
		}
		File routesFile = new File(routesFilename);
		if (!routesFile.exists()) {
			return null;
		}

		List<Route> routes = new ArrayList<DocResultCollector.Route>();
		BufferedReader in = new BufferedReader(new FileReader(routesFile));
		for (String line = in.readLine(); line != null; line = in.readLine()) {
			line = line.trim();
			if (line.startsWith("#")) {
				// Comment
				continue;
			}
			if (line.length() == 0) {
				// Empty line
				continue;
			}
			log.debug("ROUTE: " + line);
			Pattern regex = Pattern.compile("([^\\s]+)\\s+([^\\s]+)\\s+(.*)");
			Matcher match = regex.matcher(line);
			if (match.matches()) {
				Route route = new Route();
				route.method = HttpMethod.valueOf(match.group(1));
				route.route = match.group(2);
				route.call = match.group(3);

				String regExString = route.route;
				int start = 0;
				for (int index = route.route.indexOf(":", start); index != -1; index = route.route
						.indexOf(":", start)) {
					int end = route.route.indexOf("/", index);
					String param;
					if (end == -1) {
						// Last
						param = route.route.substring(index);
					} else {
						param = route.route.substring(index, end);
					}
					log.debug("param=" + param);
					route.params.add(param.substring(1));
					regExString = regExString.replace(param, "([^/^?]+)");

					start = index + 1;
				}
				route.regex = Pattern.compile(regExString);

				log.debug("   route=" + route);
				routes.add(route);
			}
		}
		in.close();
		return routes;
	}

}
