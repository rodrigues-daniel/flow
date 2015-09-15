package com.vaadin.tests.application;

import java.util.Properties;

import com.vaadin.server.VaadinRequest;
import com.vaadin.tests.components.AbstractTestUI;
import com.vaadin.ui.Label;

public class DeploymentConfiguration extends AbstractTestUI {

    @Override
    protected void setup(VaadinRequest request) {
        Properties params = getSession().getConfiguration().getInitParameters();
        getLayout().setMargin(true);

        for (Object key : params.keySet()) {
            add(
                    new Label(key + ": " + params.getProperty((String) key)));
        }
    }

    @Override
    protected String getTestDescription() {
        return "Init parameters:";
    }
}