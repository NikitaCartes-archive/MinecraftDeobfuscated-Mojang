/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.client.UploadStatus;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.gui.screens.UploadResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class FileUpload {
    private static final Logger LOGGER = LogManager.getLogger();
    private final File file;
    private final long worldId;
    private final int slotId;
    private final UploadInfo uploadInfo;
    private final String sessionId;
    private final String username;
    private final String clientVersion;
    private final UploadStatus uploadStatus;
    private AtomicBoolean cancelled = new AtomicBoolean(false);
    private CompletableFuture<UploadResult> uploadTask;
    private final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout((int)TimeUnit.MINUTES.toMillis(10L)).setConnectTimeout((int)TimeUnit.SECONDS.toMillis(15L)).build();

    public FileUpload(File file, long l, int i, UploadInfo uploadInfo, String string, String string2, String string3, UploadStatus uploadStatus) {
        this.file = file;
        this.worldId = l;
        this.slotId = i;
        this.uploadInfo = uploadInfo;
        this.sessionId = string;
        this.username = string2;
        this.clientVersion = string3;
        this.uploadStatus = uploadStatus;
    }

    public void upload(Consumer<UploadResult> consumer) {
        if (this.uploadTask != null) {
            return;
        }
        this.uploadTask = CompletableFuture.supplyAsync(() -> this.requestUpload(0));
        this.uploadTask.thenAccept((Consumer)consumer);
    }

    public void cancel() {
        this.cancelled.set(true);
        if (this.uploadTask != null) {
            this.uploadTask.cancel(false);
            this.uploadTask = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private UploadResult requestUpload(int i) {
        UploadResult.Builder builder = new UploadResult.Builder();
        if (this.cancelled.get()) {
            return builder.build();
        }
        this.uploadStatus.totalBytes = this.file.length();
        HttpPost httpPost = new HttpPost("http://" + this.uploadInfo.getUploadEndpoint() + ":" + this.uploadInfo.getPort() + "/upload" + "/" + this.worldId + "/" + this.slotId);
        CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
        try {
            this.setupRequest(httpPost);
            CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpPost);
            long l = this.getRetryDelaySeconds(httpResponse);
            if (this.shouldRetry(l, i)) {
                UploadResult uploadResult = this.retryUploadAfter(l, i);
                return uploadResult;
            }
            this.handleResponse(httpResponse, builder);
        } catch (Exception exception) {
            if (!this.cancelled.get()) {
                LOGGER.error("Caught exception while uploading: ", (Throwable)exception);
            }
        } finally {
            this.cleanup(httpPost, closeableHttpClient);
        }
        return builder.build();
    }

    private void cleanup(HttpPost httpPost, CloseableHttpClient closeableHttpClient) {
        httpPost.releaseConnection();
        if (closeableHttpClient != null) {
            try {
                closeableHttpClient.close();
            } catch (IOException iOException) {
                LOGGER.error("Failed to close Realms upload client");
            }
        }
    }

    private void setupRequest(HttpPost httpPost) throws FileNotFoundException {
        httpPost.setHeader("Cookie", "sid=" + this.sessionId + ";token=" + this.uploadInfo.getToken() + ";user=" + this.username + ";version=" + this.clientVersion);
        CustomInputStreamEntity customInputStreamEntity = new CustomInputStreamEntity((InputStream)new FileInputStream(this.file), this.file.length(), this.uploadStatus);
        customInputStreamEntity.setContentType("application/octet-stream");
        httpPost.setEntity(customInputStreamEntity);
    }

    private void handleResponse(HttpResponse httpResponse, UploadResult.Builder builder) throws IOException {
        String string;
        int i = httpResponse.getStatusLine().getStatusCode();
        if (i == 401) {
            LOGGER.debug("Realms server returned 401: " + httpResponse.getFirstHeader("WWW-Authenticate"));
        }
        builder.withStatusCode(i);
        if (httpResponse.getEntity() != null && (string = EntityUtils.toString(httpResponse.getEntity(), "UTF-8")) != null) {
            try {
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(string).getAsJsonObject().get("errorMsg");
                Optional<String> optional = Optional.ofNullable(jsonElement).map(JsonElement::getAsString);
                builder.withErrorMessage(optional.orElse(null));
            } catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private boolean shouldRetry(long l, int i) {
        return l > 0L && i + 1 < 5;
    }

    private UploadResult retryUploadAfter(long l, int i) throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(l).toMillis());
        return this.requestUpload(i + 1);
    }

    private long getRetryDelaySeconds(HttpResponse httpResponse) {
        return Optional.ofNullable(httpResponse.getFirstHeader("Retry-After")).map(Header::getValue).map(Long::valueOf).orElse(0L);
    }

    public boolean isFinished() {
        return this.uploadTask.isDone() || this.uploadTask.isCancelled();
    }

    @Environment(value=EnvType.CLIENT)
    static class CustomInputStreamEntity
    extends InputStreamEntity {
        private final long length;
        private final InputStream content;
        private final UploadStatus uploadStatus;

        public CustomInputStreamEntity(InputStream inputStream, long l, UploadStatus uploadStatus) {
            super(inputStream);
            this.content = inputStream;
            this.length = l;
            this.uploadStatus = uploadStatus;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void writeTo(OutputStream outputStream) throws IOException {
            block7: {
                Args.notNull(outputStream, "Output stream");
                try (InputStream inputStream = this.content;){
                    int i;
                    byte[] bs = new byte[4096];
                    if (this.length < 0L) {
                        int i2;
                        while ((i2 = inputStream.read(bs)) != -1) {
                            outputStream.write(bs, 0, i2);
                            UploadStatus uploadStatus = this.uploadStatus;
                            Long.valueOf(uploadStatus.bytesWritten + (long)i2);
                            uploadStatus.bytesWritten = uploadStatus.bytesWritten;
                        }
                        break block7;
                    }
                    for (long l = this.length; l > 0L; l -= (long)i) {
                        i = inputStream.read(bs, 0, (int)Math.min(4096L, l));
                        if (i == -1) {
                            break;
                        }
                        outputStream.write(bs, 0, i);
                        UploadStatus uploadStatus = this.uploadStatus;
                        Long.valueOf(uploadStatus.bytesWritten + (long)i);
                        uploadStatus.bytesWritten = uploadStatus.bytesWritten;
                        outputStream.flush();
                    }
                }
            }
        }
    }
}

