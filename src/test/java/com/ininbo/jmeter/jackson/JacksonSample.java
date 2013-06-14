package com.ininbo.jmeter.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


public class JacksonSample {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		String json = "{\"requestId\":\"3f488d97-2820-11e2-abb1-00216a098f78\",\"status\":200,\"statusMessage\":\"\",\"data\":[{\"course\":{\"id\":\"02b42248-2505-11e2-baab-00216a098f78\",\"dateCreated\":\"2012-11-02T15:50:22.000+0000\",\"updated\":\"2012-11-06T08:42:09.000+0000\",\"deleted\":null,\"institutionCourseId\":\"c3\",\"name\":\"ccc\",\"description\":\"cccc\",\"instructorId\":\"instructor1\",\"courseState\":\"Published\",\"allowFreeStudygroups\":false,\"institutionId\":\"i1\",\"courseCredits\":\"cccc\",\"courseStart\":\"2012-11-05T16:45:00+01:00\",\"courseEnd\":\"2012-11-29T17:45:00+01:00\",\"coursePrerequisites\":\"ccc\",\"syllabus\":\"ccc\",\"coursePrice\":\"USD100.00\",\"forumId\":\"02b44959-2505-11e2-baab-00216a098f78\",\"percentOfGradeFromExams\":80,\"percentOfGradeFromAssignments\":20},\"userHasCourse\":null},{\"course\":{\"id\":\"3b2c0d8b-2800-11e2-abb1-00216a098f78\",\"dateCreated\":\"2012-11-06T10:53:43.000+0000\",\"updated\":\"2012-11-06T10:53:43.000+0000\",\"deleted\":null,\"institutionCourseId\":\"i1\",\"name\":\"Sample Institution\",\"description\":\"A little test institution\",\"instructorId\":\"instructor1\",\"courseState\":\"NotAvailable\",\"allowFreeStudygroups\":true,\"institutionId\":\"i1\",\"courseCredits\":\"five points\",\"courseStart\":\"2012-11-05T08:00:00+01:00\",\"courseEnd\":\"2012-11-30T17:00:00+01:00\",\"coursePrerequisites\":\"none\",\"syllabus\":\"A sample syllabus\",\"coursePrice\":\"USD50.00\",\"forumId\":\"3b2c5bac-2800-11e2-abb1-00216a098f78\",\"percentOfGradeFromExams\":70,\"percentOfGradeFromAssignments\":30},\"userHasCourse\":null},{\"course\":{\"id\":\"73037793-24df-11e2-8e35-00216a098f78\",\"dateCreated\":\"2012-11-02T11:21:30.000+0000\",\"updated\":\"2012-11-02T11:23:41.000+0000\",\"deleted\":null,\"institutionCourseId\":\"c2\",\"name\":\"ccc\",\"description\":\"ccc\",\"instructorId\":\"instructor1\",\"courseState\":\"Published\",\"allowFreeStudygroups\":false,\"institutionId\":\"i1\",\"courseCredits\":\"cc\",\"courseStart\":\"2012-11-05T12:15:00+01:00\",\"courseEnd\":\"2012-11-30T12:15:00+01:00\",\"coursePrerequisites\":\"ccc\",\"syllabus\":\"ccc\",\"coursePrice\":\"USD50.00\",\"forumId\":\"7303c5b4-24df-11e2-8e35-00216a098f78\",\"percentOfGradeFromExams\":50,\"percentOfGradeFromAssignments\":50},\"userHasCourse\":null},{\"course\":{\"id\":\"8a80e300-259a-11e2-baab-00216a098f78\",\"dateCreated\":\"2012-11-03T09:40:45.000+0000\",\"updated\":\"2012-11-03T09:40:45.000+0000\",\"deleted\":null,\"institutionCourseId\":\"n2\",\"name\":\"not available\",\"description\":\"nnn\",\"instructorId\":\"instructor1\",\"courseState\":\"NotAvailable\",\"allowFreeStudygroups\":true,\"institutionId\":\"i1\",\"courseCredits\":\"nnn\",\"courseStart\":\"2012-11-05T10:30:00+01:00\",\"courseEnd\":\"2012-12-05T10:30:00+01:00\",\"coursePrerequisites\":\"nnn\",\"syllabus\":\"nn\",\"coursePrice\":\"USD23.00\",\"forumId\":\"8a810a11-259a-11e2-baab-00216a098f78\",\"percentOfGradeFromExams\":80,\"percentOfGradeFromAssignments\":20},\"userHasCourse\":null},{\"course\":{\"id\":\"97e73714-259a-11e2-baab-00216a098f78\",\"dateCreated\":\"2012-11-03T09:41:07.000+0000\",\"updated\":\"2012-11-03T09:41:07.000+0000\",\"deleted\":null,\"institutionCourseId\":\"a4\",\"name\":\"publlished\",\"description\":\"nnn\",\"instructorId\":\"instructor1\",\"courseState\":\"Published\",\"allowFreeStudygroups\":true,\"institutionId\":\"i1\",\"courseCredits\":\"nnn\",\"courseStart\":\"2012-11-05T10:30:00+01:00\",\"courseEnd\":\"2012-12-05T10:30:00+01:00\",\"coursePrerequisites\":\"nnn\",\"syllabus\":\"nn\",\"coursePrice\":\"USD23.00\",\"forumId\":\"97e75e25-259a-11e2-baab-00216a098f78\",\"percentOfGradeFromExams\":80,\"percentOfGradeFromAssignments\":20},\"userHasCourse\":null},{\"course\":{\"id\":\"b3a83d34-27fe-11e2-abb1-00216a098f78\",\"dateCreated\":\"2012-11-06T10:42:46.000+0000\",\"updated\":\"2012-11-06T10:42:46.000+0000\",\"deleted\":null,\"institutionCourseId\":\"i1\",\"name\":\"Sample Institution\",\"description\":\"A little test institution\",\"instructorId\":\"instructor1\",\"courseState\":\"NotAvailable\",\"allowFreeStudygroups\":true,\"institutionId\":\"i1\",\"courseCredits\":\"five points\",\"courseStart\":\"2012-11-05T08:00:00+01:00\",\"courseEnd\":\"2012-11-30T17:00:00+01:00\",\"coursePrerequisites\":\"none\",\"syllabus\":\"A sample syllabus\",\"coursePrice\":\"USD50.00\",\"forumId\":\"b3a88b55-27fe-11e2-abb1-00216a098f78\",\"percentOfGradeFromExams\":70,\"percentOfGradeFromAssignments\":30},\"userHasCourse\":null}]}";
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		JsonNode df = mapper.readValue(json, JsonNode.class);
	    ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
		// System.out.println(df.toString());
		
		System.out.println(writer.writeValueAsString(df));
	}

}
