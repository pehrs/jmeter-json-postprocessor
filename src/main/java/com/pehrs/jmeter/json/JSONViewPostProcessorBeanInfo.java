package com.pehrs.jmeter.json;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

public class JSONViewPostProcessorBeanInfo extends BeanInfoSupport {

    public JSONViewPostProcessorBeanInfo() {
        super(JSONViewPostProcessor.class);

        PropertyDescriptor p;

        p = property("displaySamplerProperties");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        p.setValue(NOT_OTHER, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.TRUE);

        p = property("displayJMeterVariables");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        p.setValue(NOT_OTHER, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.TRUE);

        p = property("displayJMeterProperties");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        p.setValue(NOT_OTHER, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);

        p = property("displaySystemProperties");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        p.setValue(NOT_OTHER, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);
    }

}
