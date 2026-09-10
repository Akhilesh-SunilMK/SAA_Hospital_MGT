package com.hms.notification.service;

import org.springframework.stereotype.Component;

import java.util.Map;

/** FR-NT-03: templated messages with {{variable}} substitution — no templating engine needed. */
@Component
public class TemplateRenderer {

    public String render(String template, Map<String, String> variables) {
        if (template == null) {
            return "";
        }
        String result = template;
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                result = result.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
            }
        }
        return result;
    }
}
