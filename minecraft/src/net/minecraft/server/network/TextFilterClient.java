package net.minecraft.server.network;

import com.google.common.base.Strings;
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.thread.ProcessorMailbox;
import org.apache.commons.codec.binary.Base64;
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
	final URL joinEndpoint;
	final URL leaveEndpoint;
	private final String authKey;
	private final int ruleId;
	private final String serverId;
	final TextFilterClient.IgnoreStrategy chatIgnoreStrategy;
	final ExecutorService workerPool;

	private TextFilterClient(URI uRI, String string, int i, String string2, TextFilterClient.IgnoreStrategy ignoreStrategy, int j) throws MalformedURLException {
		this.authKey = string;
		this.ruleId = i;
		this.serverId = string2;
		this.chatIgnoreStrategy = ignoreStrategy;
		this.chatEndpoint = uRI.resolve("/v1/chat").toURL();
		this.joinEndpoint = uRI.resolve("/v1/join").toURL();
		this.leaveEndpoint = uRI.resolve("/v1/leave").toURL();
		this.workerPool = Executors.newFixedThreadPool(j, THREAD_FACTORY);
	}

	@Nullable
	public static TextFilterClient createFromConfig(String string) {
		if (Strings.isNullOrEmpty(string)) {
			return null;
		} else {
			try {
				JsonObject jsonObject = GsonHelper.parse(string);
				URI uRI = new URI(GsonHelper.getAsString(jsonObject, "apiServer"));
				String string2 = GsonHelper.getAsString(jsonObject, "apiKey");
				if (string2.isEmpty()) {
					throw new IllegalArgumentException("Missing API key");
				} else {
					int i = GsonHelper.getAsInt(jsonObject, "ruleId", 1);
					String string3 = GsonHelper.getAsString(jsonObject, "serverId", "");
					int j = GsonHelper.getAsInt(jsonObject, "hashesToDrop", -1);
					int k = GsonHelper.getAsInt(jsonObject, "maxConcurrentRequests", 7);
					TextFilterClient.IgnoreStrategy ignoreStrategy = TextFilterClient.IgnoreStrategy.select(j);
					return new TextFilterClient(uRI, new Base64().encodeToString(string2.getBytes(StandardCharsets.US_ASCII)), i, string3, ignoreStrategy, k);
				}
			} catch (Exception var9) {
				LOGGER.warn("Failed to parse chat filter config {}", string, var9);
				return null;
			}
		}
	}

	void processJoinOrLeave(GameProfile gameProfile, URL uRL, Executor executor) {
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

	CompletableFuture<TextFilter.FilteredText> requestMessageProcessing(
		GameProfile gameProfile, String string, TextFilterClient.IgnoreStrategy ignoreStrategy, Executor executor
	) {
		if (string.isEmpty()) {
			return CompletableFuture.completedFuture(TextFilter.FilteredText.EMPTY);
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
						return TextFilter.FilteredText.passThrough(string);
					} else {
						String string2 = GsonHelper.getAsString(jsonObject2, "hashed", null);
						if (string2 == null) {
							return TextFilter.FilteredText.fullyFiltered(string);
						} else {
							int i = GsonHelper.getAsJsonArray(jsonObject2, "hashes").size();
							return ignoreStrategy.shouldIgnore(string2, i) ? TextFilter.FilteredText.fullyFiltered(string) : new TextFilter.FilteredText(string, string2);
						}
					}
				} catch (Exception var8) {
					LOGGER.warn("Failed to validate message '{}'", string, var8);
					return TextFilter.FilteredText.fullyFiltered(string);
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

		JsonObject var13;
		label74: {
			try {
				if (httpURLConnection.getResponseCode() == 204) {
					var13 = new JsonObject();
					break label74;
				}

				try {
					var13 = Streams.parse(new JsonReader(new InputStreamReader(inputStream))).getAsJsonObject();
				} finally {
					this.drainStream(inputStream);
				}
			} catch (Throwable var12) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var10) {
						var12.addSuppressed(var10);
					}
				}

				throw var12;
			}

			if (inputStream != null) {
				inputStream.close();
			}

			return var13;
		}

		if (inputStream != null) {
			inputStream.close();
		}

		return var13;
	}

	private void processRequest(JsonObject jsonObject, URL uRL) throws IOException {
		HttpURLConnection httpURLConnection = this.makeRequest(jsonObject, uRL);
		InputStream inputStream = httpURLConnection.getInputStream();

		try {
			this.drainStream(inputStream);
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

		try {
			JsonWriter jsonWriter = new JsonWriter(outputStreamWriter);

			try {
				Streams.write(jsonObject, jsonWriter);
			} catch (Throwable var10) {
				try {
					jsonWriter.close();
				} catch (Throwable var9) {
					var10.addSuppressed(var9);
				}

				throw var10;
			}

			jsonWriter.close();
		} catch (Throwable var11) {
			try {
				outputStreamWriter.close();
			} catch (Throwable var8) {
				var11.addSuppressed(var8);
			}

			throw var11;
		}

		outputStreamWriter.close();
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

		static TextFilterClient.IgnoreStrategy ignoreOverThreshold(int i) {
			return (string, j) -> j >= i;
		}

		static TextFilterClient.IgnoreStrategy select(int i) {
			switch (i) {
				case -1:
					return NEVER_IGNORE;
				case 0:
					return IGNORE_FULLY_FILTERED;
				default:
					return ignoreOverThreshold(i);
			}
		}

		boolean shouldIgnore(String string, int i);
	}

	class PlayerContext implements TextFilter {
		private final GameProfile profile;
		private final Executor streamExecutor;

		PlayerContext(GameProfile gameProfile) {
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
		public CompletableFuture<List<TextFilter.FilteredText>> processMessageBundle(List<String> list) {
			List<CompletableFuture<TextFilter.FilteredText>> list2 = (List<CompletableFuture<TextFilter.FilteredText>>)list.stream()
				.map(string -> TextFilterClient.this.requestMessageProcessing(this.profile, string, TextFilterClient.this.chatIgnoreStrategy, this.streamExecutor))
				.collect(ImmutableList.toImmutableList());
			return Util.sequenceFailFast(list2).exceptionally(throwable -> ImmutableList.of());
		}

		@Override
		public CompletableFuture<TextFilter.FilteredText> processStreamMessage(String string) {
			return TextFilterClient.this.requestMessageProcessing(this.profile, string, TextFilterClient.this.chatIgnoreStrategy, this.streamExecutor);
		}
	}

	public static class RequestFailedException extends RuntimeException {
		RequestFailedException(String string) {
			super(string);
		}
	}
}
