package com.hms.webui.client;

/** Raw byte payload for the report/export endpoints, which return files rather than the JSON envelope. */
public record DownloadResult(byte[] bytes, String filename, String contentType) {
}
