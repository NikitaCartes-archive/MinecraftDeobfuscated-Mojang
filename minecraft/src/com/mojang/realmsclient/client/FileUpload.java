package com.mojang.realmsclient.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.worldupload.RealmsUploadCanceledException;
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
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.User;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class FileUpload {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAX_RETRIES = 5;
	private static final String UPLOAD_PATH = "/upload";
	private final File file;
	private final long realmId;
	private final int slotId;
	private final UploadInfo uploadInfo;
	private final String sessionId;
	private final String username;
	private final String clientVersion;
	private final String worldVersion;
	private final UploadStatus uploadStatus;
	final AtomicBoolean cancelled = new AtomicBoolean(false);
	@Nullable
	private CompletableFuture<UploadResult> uploadTask;
	private final RequestConfig requestConfig = RequestConfig.custom()
		.setSocketTimeout((int)TimeUnit.MINUTES.toMillis(10L))
		.setConnectTimeout((int)TimeUnit.SECONDS.toMillis(15L))
		.build();

	public FileUpload(File file, long l, int i, UploadInfo uploadInfo, User user, String string, String string2, UploadStatus uploadStatus) {
		this.file = file;
		this.realmId = l;
		this.slotId = i;
		this.uploadInfo = uploadInfo;
		this.sessionId = user.getSessionId();
		this.username = user.getName();
		this.clientVersion = string;
		this.worldVersion = string2;
		this.uploadStatus = uploadStatus;
	}

	public UploadResult upload() {
		if (this.uploadTask != null) {
			return new UploadResult.Builder().build();
		} else {
			this.uploadTask = CompletableFuture.supplyAsync(() -> this.requestUpload(0), Util.backgroundExecutor());
			if (this.cancelled.get()) {
				this.cancel();
				return new UploadResult.Builder().build();
			} else {
				return (UploadResult)this.uploadTask.join();
			}
		}
	}

	public void cancel() {
		this.cancelled.set(true);
	}

	private UploadResult requestUpload(int i) {
		UploadResult.Builder builder = new UploadResult.Builder();
		if (this.cancelled.get()) {
			return builder.build();
		} else {
			this.uploadStatus.setTotalBytes(this.file.length());
			HttpPost httpPost = new HttpPost(this.uploadInfo.getUploadEndpoint().resolve("/upload/" + this.realmId + "/" + this.slotId));
			CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();

			UploadResult var8;
			try {
				this.setupRequest(httpPost);
				HttpResponse httpResponse = closeableHttpClient.execute(httpPost);
				long l = this.getRetryDelaySeconds(httpResponse);
				if (!this.shouldRetry(l, i)) {
					this.handleResponse(httpResponse, builder);
					return builder.build();
				}

				var8 = this.retryUploadAfter(l, i);
			} catch (Exception var12) {
				if (!this.cancelled.get()) {
					LOGGER.error("Caught exception while uploading: ", (Throwable)var12);
					return builder.build();
				}

				throw new RealmsUploadCanceledException();
			} finally {
				this.cleanup(httpPost, closeableHttpClient);
			}

			return var8;
		}
	}

	private void cleanup(HttpPost httpPost, @Nullable CloseableHttpClient closeableHttpClient) {
		httpPost.releaseConnection();
		if (closeableHttpClient != null) {
			try {
				closeableHttpClient.close();
			} catch (IOException var4) {
				LOGGER.error("Failed to close Realms upload client");
			}
		}
	}

	private void setupRequest(HttpPost httpPost) throws FileNotFoundException {
		httpPost.setHeader(
			"Cookie",
			"sid="
				+ this.sessionId
				+ ";token="
				+ this.uploadInfo.getToken()
				+ ";user="
				+ this.username
				+ ";version="
				+ this.clientVersion
				+ ";worldVersion="
				+ this.worldVersion
		);
		FileUpload.CustomInputStreamEntity customInputStreamEntity = new FileUpload.CustomInputStreamEntity(
			new FileInputStream(this.file), this.file.length(), this.uploadStatus
		);
		customInputStreamEntity.setContentType("application/octet-stream");
		httpPost.setEntity(customInputStreamEntity);
	}

	private void handleResponse(HttpResponse httpResponse, UploadResult.Builder builder) throws IOException {
		int i = httpResponse.getStatusLine().getStatusCode();
		if (i == 401) {
			LOGGER.debug("Realms server returned 401: {}", httpResponse.getFirstHeader("WWW-Authenticate"));
		}

		builder.withStatusCode(i);
		if (httpResponse.getEntity() != null) {
			String string = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			if (string != null) {
				try {
					JsonParser jsonParser = new JsonParser();
					JsonElement jsonElement = jsonParser.parse(string).getAsJsonObject().get("errorMsg");
					Optional<String> optional = Optional.ofNullable(jsonElement).map(JsonElement::getAsString);
					builder.withErrorMessage((String)optional.orElse(null));
				} catch (Exception var8) {
				}
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
		return (Long)Optional.ofNullable(httpResponse.getFirstHeader("Retry-After")).map(NameValuePair::getValue).map(Long::valueOf).orElse(0L);
	}

	public boolean isFinished() {
		return this.uploadTask.isDone() || this.uploadTask.isCancelled();
	}

	@Environment(EnvType.CLIENT)
	class CustomInputStreamEntity extends InputStreamEntity {
		private final long length;
		private final InputStream content;
		private final UploadStatus uploadStatus;

		public CustomInputStreamEntity(final InputStream inputStream, final long l, final UploadStatus uploadStatus) {
			super(inputStream);
			this.content = inputStream;
			this.length = l;
			this.uploadStatus = uploadStatus;
		}

		@Override
		public void writeTo(OutputStream outputStream) throws IOException {
			Args.notNull(outputStream, "Output stream");
			InputStream inputStream = this.content;

			try {
				byte[] bs = new byte[4096];
				int i;
				if (this.length < 0L) {
					while ((i = inputStream.read(bs)) != -1) {
						if (FileUpload.this.cancelled.get()) {
							throw new RealmsUploadCanceledException();
						}

						outputStream.write(bs, 0, i);
						this.uploadStatus.onWrite((long)i);
					}
				} else {
					long l = this.length;

					while (l > 0L) {
						i = inputStream.read(bs, 0, (int)Math.min(4096L, l));
						if (i == -1) {
							break;
						}

						if (FileUpload.this.cancelled.get()) {
							throw new RealmsUploadCanceledException();
						}

						outputStream.write(bs, 0, i);
						this.uploadStatus.onWrite((long)i);
						l -= (long)i;
						outputStream.flush();
					}
				}
			} catch (Throwable var8) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var7) {
						var8.addSuppressed(var7);
					}
				}

				throw var8;
			}

			if (inputStream != null) {
				inputStream.close();
			}
		}
	}
}
