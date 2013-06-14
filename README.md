
# com.pehrs.jmeter-json-postprocessor

This is a plugin for JMeter 2.8

## Download JMETER

JMETER can be downloaded from [jmeter.apache.org](http://jmeter.apache.org/download_jmeter.cgi).

## Build and Install the plugin

This plugin enables you to view the formatted JSON responses 

	$ cd ${SRC}/com.pehrs.jmeter-json-postprocessor
	$ mvn package
	$ mvn dependency:copy-dependencies

Unpack and copy the plugin and the dependent jackson jars to the ${JMETERHOME}/lib/ext directory 

	$ cd ${SRC}/system-test/ininbo-jmeter-addon
	$ cp target/jmeter-json-postprocessor-*.jar ${JMETERHOME}/lib/ext
	$ cp target/dependency/jackson-annotations-*.jar ${JMETERHOME}/lib/ext
	$ cp target/dependency/jackson-core-*.jar ${JMETERHOME}/lib/ext
	$ cp target/dependency/jackson-databind-*.jar ${JMETERHOME}/lib/ext


## Using the plugin

Just add the "JSON PostProcessor" as you would with the "Debug PostProcessor"

