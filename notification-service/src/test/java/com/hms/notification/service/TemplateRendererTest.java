package com.hms.notification.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateRendererTest {

    private final TemplateRenderer renderer = new TemplateRenderer();

    @Test
    void substitutesAllPlaceholders() {
        String result = renderer.render("Token {{tokenNumber}} at {{slot}}.",
                Map.of("tokenNumber", "OPD-58-014", "slot", "2026-09-15T10:30:00"));

        assertThat(result).isEqualTo("Token OPD-58-014 at 2026-09-15T10:30:00.");
    }

    @Test
    void leavesUnknownPlaceholdersUntouched() {
        String result = renderer.render("Hello {{name}}, code {{code}}.", Map.of("name", "Asha"));

        assertThat(result).isEqualTo("Hello Asha, code {{code}}.");
    }

    @Test
    void nullVariablesMapIsSafe() {
        String result = renderer.render("Static message.", null);

        assertThat(result).isEqualTo("Static message.");
    }
}
