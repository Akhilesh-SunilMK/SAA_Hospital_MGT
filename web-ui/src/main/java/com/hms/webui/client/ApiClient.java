package com.hms.webui.client;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hms.common.dto.PageResponse;
import com.hms.webui.exception.ApiException;
import com.hms.webui.security.SessionUser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin blocking client over the API gateway. Every business endpoint wraps its payload in the
 * shared {@code ApiResponse} envelope (see hms-common), so this class unwraps that envelope,
 * translating a non-2xx status or {@code success:false} into an {@link ApiException} that the
 * global controller advice turns into a flash message / error page.
 */
@Component
public class ApiClient {

    private static final Logger log = LoggerFactory.getLogger(ApiClient.class);

    private final WebClient webClient;
    private final ObjectMapper mapper;

    public ApiClient(WebClient webClient) {
        this.webClient = webClient;
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public JavaType pageType(Class<?> itemType) {
        return mapper.getTypeFactory().constructParametricType(PageResponse.class, itemType);
    }

    public JavaType listType(Class<?> itemType) {
        return mapper.getTypeFactory().constructCollectionType(List.class, itemType);
    }

    public <T> T get(String path, Class<T> type, SessionUser user) {
        return exchange(HttpMethod.GET, path, null, type == null ? null : mapper.getTypeFactory().constructType(type), user);
    }

    public <T> T get(String path, JavaType type, SessionUser user) {
        return exchange(HttpMethod.GET, path, null, type, user);
    }

    public <T> T post(String path, Object body, Class<T> type, SessionUser user) {
        return exchange(HttpMethod.POST, path, body, type == null ? null : mapper.getTypeFactory().constructType(type), user);
    }

    public <T> T post(String path, Object body, JavaType type, SessionUser user) {
        return exchange(HttpMethod.POST, path, body, type, user);
    }

    public <T> T put(String path, Object body, Class<T> type, SessionUser user) {
        return exchange(HttpMethod.PUT, path, body, type == null ? null : mapper.getTypeFactory().constructType(type), user);
    }

    public <T> T patch(String path, Object body, Class<T> type, SessionUser user) {
        return exchange(HttpMethod.PATCH, path, body, type == null ? null : mapper.getTypeFactory().constructType(type), user);
    }

    public void delete(String path, SessionUser user) {
        exchange(HttpMethod.DELETE, path, null, null, user);
    }

    public DownloadResult download(String path, SessionUser user) {
        WebClient.RequestHeadersSpec<?> req = applyAuth(webClient.get().uri(path), user);
        RawBytes raw;
        try {
            // The body MUST be consumed inside the exchangeToMono callback — reading it
            // afterward (outside the reactive chain) can race the connection being released
            // back to the pool and silently yield an empty body. See exchange() below.
            raw = req.exchangeToMono(resp -> resp.bodyToMono(byte[].class).defaultIfEmpty(new byte[0])
                    .map(bytes -> new RawBytes(resp.statusCode().value(), resp.headers().asHttpHeaders(), bytes)))
                    .block();
        } catch (WebClientException e) {
            throw new ApiException(503, "Could not reach the service: " + e.getMessage(), List.of());
        }
        if (raw == null) {
            throw new ApiException(503, "No response from the service", List.of());
        }
        if (raw.status() < 200 || raw.status() >= 300) {
            String message = raw.bytes().length > 0
                    ? new String(raw.bytes(), StandardCharsets.UTF_8)
                    : "Download failed (HTTP " + raw.status() + ")";
            throw new ApiException(raw.status(), message, List.of());
        }
        String contentType = raw.headers().getFirst(HttpHeaders.CONTENT_TYPE);
        String filename = extractFilename(raw.headers().get(HttpHeaders.CONTENT_DISPOSITION)).orElse("download");
        return new DownloadResult(raw.bytes(), filename, contentType != null ? contentType : "application/octet-stream");
    }

    private record RawText(int status, String body) {}

    private record RawBytes(int status, HttpHeaders headers, byte[] bytes) {}

    private java.util.Optional<String> extractFilename(List<String> contentDisposition) {
        if (contentDisposition == null || contentDisposition.isEmpty()) return java.util.Optional.empty();
        String header = contentDisposition.get(0);
        int idx = header.indexOf("filename=");
        if (idx < 0) return java.util.Optional.empty();
        String name = header.substring(idx + "filename=".length()).replace("\"", "").trim();
        int semi = name.indexOf(';');
        if (semi >= 0) name = name.substring(0, semi);
        return java.util.Optional.of(name);
    }

    private WebClient.RequestHeadersSpec<?> applyAuth(WebClient.RequestHeadersSpec<?> spec, SessionUser user) {
        if (user != null && user.accessToken() != null) {
            return spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken());
        }
        return spec;
    }

    private <T> T exchange(HttpMethod method, String path, Object body, JavaType dataType, SessionUser user) {
        WebClient.RequestBodySpec base = webClient.method(method).uri(path).accept(MediaType.APPLICATION_JSON);
        WebClient.RequestHeadersSpec<?> req;
        if (user != null && user.accessToken() != null) {
            base = base.header(HttpHeaders.AUTHORIZATION, "Bearer " + user.accessToken());
        }
        if (body != null) {
            req = base.contentType(MediaType.APPLICATION_JSON).bodyValue(body);
        } else {
            req = base;
        }

        RawText result;
        try {
            // Consume the body inside the exchangeToMono callback (see the comment on
            // download() above) — this is what was silently losing response bodies before.
            result = req.exchangeToMono(resp -> resp.bodyToMono(String.class).defaultIfEmpty("")
                    .map(text -> new RawText(resp.statusCode().value(), text)))
                    .block();
        } catch (WebClientException e) {
            throw new ApiException(503, "Could not reach the service: " + e.getMessage(), List.of());
        }
        if (result == null) {
            throw new ApiException(503, "No response from the service", List.of());
        }

        int httpStatus = result.status();
        String raw = result.body();

        if (raw == null || raw.isBlank()) {
            if (httpStatus >= 200 && httpStatus < 300) return null;
            throw new ApiException(httpStatus, "Request failed (HTTP " + httpStatus + ")", List.of());
        }

        JsonNode root;
        try {
            root = mapper.readTree(raw);
        } catch (Exception e) {
            if (httpStatus >= 200 && httpStatus < 300) return null;
            throw new ApiException(httpStatus, "Request failed (HTTP " + httpStatus + ")", List.of());
        }

        boolean success = root.path("success").asBoolean(httpStatus < 300);
        if (!success) {
            log.warn("Backend call failed: {} {} -> HTTP {} body={}", method, path, httpStatus, raw);
        }
        String message = root.path("message").isMissingNode() ? null : root.path("message").asText(null);
        List<String> errors = new ArrayList<>();
        if (root.path("errors").isArray()) {
            root.path("errors").forEach(n -> errors.add(n.asText()));
        }

        if (!success) {
            String msg = message != null && !message.isBlank() ? message : "Request failed (HTTP " + httpStatus + ")";
            throw new ApiException(root.path("statusCode").asInt(httpStatus), msg, errors);
        }

        JsonNode dataNode = root.path("data");
        if (dataType == null || dataNode.isMissingNode() || dataNode.isNull()) {
            return null;
        }
        return mapper.convertValue(dataNode, dataType);
    }
}
