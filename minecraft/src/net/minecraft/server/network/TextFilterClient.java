package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.thread.ProcessorMailbox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextFilterClient implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
	private static final ThreadFactory THREAD_FACTORY = runnable -> {
		Thread thread = new Thread(runnable);
		thread.setName("Chat-Filter-Worker-" + WORKER_COUNT.getAndIncrement());
		return thread;
	};
	private final URL chatEndpoint;
	private final URL joinEndpoint;
	private final URL leaveEndpoint;
	private final String authKey;
	private final int ruleId;
	private final String serverId;
	private final TextFilterClient.IgnoreStrategy chatIgnoreStrategy;
	private final ExecutorService workerPool;

	private void processJoinOrLeave(GameProfile gameProfile, URL uRL, Executor executor) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("server", this.serverId);
		jsonObject.addProperty("room", "Chat");
		jsonObject.addProperty("user_id", gameProfile.getId().toString());
		jsonObject.addProperty("user_display_name", gameProfile.getName());
		executor.execute(() -> {
			try {
				this.processRequest(jsonObject, uRL);
			} catch (Exception var5) {
				LOGGER.warn("Failed to send join/leave packet to {} for player {}", uRL, gameProfile, var5);
			}
		});
	}

	private CompletableFuture<Optional<String>> requestMessageProcessing(
		GameProfile gameProfile, String string, TextFilterClient.IgnoreStrategy ignoreStrategy, Executor executor
	) {
		if (string.isEmpty()) {
			return CompletableFuture.completedFuture(Optional.of(""));
		} else {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("rule", this.ruleId);
			jsonObject.addProperty("server", this.serverId);
			jsonObject.addProperty("room", "Chat");
			jsonObject.addProperty("player", gameProfile.getId().toString());
			jsonObject.addProperty("player_display_name", gameProfile.getName());
			jsonObject.addProperty("text", string);
			return CompletableFuture.supplyAsync(() -> {
				try {
					JsonObject jsonObject2 = this.processRequestResponse(jsonObject, this.chatEndpoint);
					boolean bl = GsonHelper.getAsBoolean(jsonObject2, "response", false);
					if (bl) {
						return Optional.of(string);
					} else {
						String string2 = GsonHelper.getAsString(jsonObject2, "hashed", null);
						if (string2 == null) {
							return Optional.empty();
						} else {
							int i = GsonHelper.getAsJsonArray(jsonObject2, "hashes").size();
							return ignoreStrategy.shouldIgnore(string2, i) ? Optional.empty() : Optional.of(string2);
						}
					}
				} catch (Exception var8) {
					LOGGER.warn("Failed to validate message '{}'", string, var8);
					return Optional.empty();
				}
			}, executor);
		}
	}

	public void close() {
		this.workerPool.shutdownNow();
	}

	private void drainStream(InputStream inputStream) throws IOException {
		byte[] bs = new byte[1024];

		while (inputStream.read(bs) != -1) {
		}
	}

	private JsonObject processRequestResponse(JsonObject jsonObject, URL uRL) throws IOException {
		HttpURLConnection httpURLConnection = this.makeRequest(jsonObject, uRL);
		InputStream inputStream = httpURLConnection.getInputStream();
		Throwable var5 = null;

		JsonObject var6;
		try {
			if (httpURLConnection.getResponseCode() != 204) {
				try {
					return Streams.parse(new JsonReader(new InputStreamReader(inputStream))).getAsJsonObject();
				} finally {
					this.drainStream(inputStream);
				}
			}

			var6 = new JsonObject();
		} catch (Throwable var23) {
			var5 = var23;
			throw var23;
		} finally {
			if (inputStream != null) {
				if (var5 != null) {
					try {
						inputStream.close();
					} catch (Throwable var21) {
						var5.addSuppressed(var21);
					}
				} else {
					inputStream.close();
				}
			}
		}

		return var6;
	}

	private void processRequest(JsonObject jsonObject, URL uRL) throws IOException {
		HttpURLConnection httpURLConnection = this.makeRequest(jsonObject, uRL);
		InputStream inputStream = httpURLConnection.getInputStream();
		Throwable var5 = null;

		try {
			this.drainStream(inputStream);
		} catch (Throwable var14) {
			var5 = var14;
			throw var14;
		} finally {
			if (inputStream != null) {
				if (var5 != null) {
					try {
						inputStream.close();
					} catch (Throwable var13) {
						var5.addSuppressed(var13);
					}
				} else {
					inputStream.close();
				}
			}
		}
	}

	private HttpURLConnection makeRequest(JsonObject jsonObject, URL uRL) throws IOException {
		HttpURLConnection httpURLConnection = (HttpURLConnection)uRL.openConnection();
		httpURLConnection.setConnectTimeout(15000);
		httpURLConnection.setReadTimeout(2000);
		httpURLConnection.setUseCaches(false);
		httpURLConnection.setDoOutput(true);
		httpURLConnection.setDoInput(true);
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		httpURLConnection.setRequestProperty("Accept", "application/json");
		httpURLConnection.setRequestProperty("Authorization", "Basic " + this.authKey);
		httpURLConnection.setRequestProperty("User-Agent", "Minecraft server" + SharedConstants.getCurrentVersion().getName());
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(), StandardCharsets.UTF_8);
		Throwable var5 = null;

		try {
			JsonWriter jsonWriter = new JsonWriter(outputStreamWriter);
			Throwable var7 = null;

			try {
				Streams.write(jsonObject, jsonWriter);
			} catch (Throwable var30) {
				var7 = var30;
				throw var30;
			} finally {
				if (jsonWriter != null) {
					if (var7 != null) {
						try {
							jsonWriter.close();
						} catch (Throwable var29) {
							var7.addSuppressed(var29);
						}
					} else {
						jsonWriter.close();
					}
				}
			}
		} catch (Throwable var32) {
			var5 = var32;
			throw var32;
		} finally {
			if (outputStreamWriter != null) {
				if (var5 != null) {
					try {
						outputStreamWriter.close();
					} catch (Throwable var28) {
						var5.addSuppressed(var28);
					}
				} else {
					outputStreamWriter.close();
				}
			}
		}

		int i = httpURLConnection.getResponseCode();
		if (i >= 200 && i < 300) {
			return httpURLConnection;
		} else {
			throw new TextFilterClient.RequestFailedException(i + " " + httpURLConnection.getResponseMessage());
		}
	}

	public TextFilter createContext(GameProfile gameProfile) {
		return new TextFilterClient.PlayerContext(gameProfile);
	}

	@FunctionalInterface
	public interface IgnoreStrategy {
		TextFilterClient.IgnoreStrategy NEVER_IGNORE = (string, i) -> false;
		TextFilterClient.IgnoreStrategy IGNORE_FULLY_FILTERED = (string, i) -> string.length() == i;

		boolean shouldIgnore(String string, int i);
	}

	class PlayerContext implements TextFilter {
		private final GameProfile profile;
		private final Executor streamExecutor;

		private PlayerContext(GameProfile gameProfile) {
			this.profile = gameProfile;
			ProcessorMailbox<Runnable> processorMailbox = ProcessorMailbox.create(TextFilterClient.this.workerPool, "chat stream for " + gameProfile.getName());
			this.streamExecutor = processorMailbox::tell;
		}

		@Override
		public void join() {
			TextFilterClient.this.processJoinOrLeave(this.profile, TextFilterClient.this.joinEndpoint, this.streamExecutor);
		}

		@Override
		public void leave() {
			TextFilterClient.this.processJoinOrLeave(this.profile, TextFilterClient.this.leaveEndpoint, this.streamExecutor);
		}

		@Override
		public CompletableFuture<Optional<List<String>>> processMessageBundle(List<String> list) {
			List<CompletableFuture<Optional<String>>> list2 = (List<CompletableFuture<Optional<String>>>)list.stream()
				.map(string -> TextFilterClient.this.requestMessageProcessing(this.profile, string, TextFilterClient.this.chatIgnoreStrategy, this.streamExecutor))
				.collect(ImmutableList.toImmutableList());
			return Util.sequence(list2)
				.thenApply(listx -> Optional.of(listx.stream().map(optional -> (String)optional.orElse("")).collect(ImmutableList.toImmutableList())))
				.exceptionally(throwable -> Optional.empty());
		}

		@Override
		public CompletableFuture<Optional<String>> processStreamMessage(String string) {
			return TextFilterClient.this.requestMessageProcessing(this.profile, string, TextFilterClient.this.chatIgnoreStrategy, this.streamExecutor);
		}
	}

	public static class RequestFailedException extends RuntimeException {
		private RequestFailedException(String string) {
			super(string);
		}
	}
}
