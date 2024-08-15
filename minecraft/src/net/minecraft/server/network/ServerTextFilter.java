package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
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
import net.minecraft.network.chat.FilterMask;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringUtil;
import net.minecraft.util.thread.ProcessorMailbox;
import org.slf4j.Logger;

public abstract class ServerTextFilter implements AutoCloseable {
	protected static final Logger LOGGER = LogUtils.getLogger();
	private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
	private static final ThreadFactory THREAD_FACTORY = runnable -> {
		Thread thread = new Thread(runnable);
		thread.setName("Chat-Filter-Worker-" + WORKER_COUNT.getAndIncrement());
		return thread;
	};
	private final URL chatEndpoint;
	private final ServerTextFilter.MessageEncoder chatEncoder;
	final ServerTextFilter.IgnoreStrategy chatIgnoreStrategy;
	final ExecutorService workerPool;

	protected static ExecutorService createWorkerPool(int i) {
		return Executors.newFixedThreadPool(i, THREAD_FACTORY);
	}

	protected ServerTextFilter(
		URL uRL, ServerTextFilter.MessageEncoder messageEncoder, ServerTextFilter.IgnoreStrategy ignoreStrategy, ExecutorService executorService
	) {
		this.chatIgnoreStrategy = ignoreStrategy;
		this.workerPool = executorService;
		this.chatEndpoint = uRL;
		this.chatEncoder = messageEncoder;
	}

	protected static URL getEndpoint(URI uRI, @Nullable JsonObject jsonObject, String string, String string2) throws MalformedURLException {
		String string3 = getEndpointFromConfig(jsonObject, string, string2);
		return uRI.resolve("/" + string3).toURL();
	}

	protected static String getEndpointFromConfig(@Nullable JsonObject jsonObject, String string, String string2) {
		return jsonObject != null ? GsonHelper.getAsString(jsonObject, string, string2) : string2;
	}

	@Nullable
	public static ServerTextFilter createFromConfig(DedicatedServerProperties dedicatedServerProperties) {
		String string = dedicatedServerProperties.textFilteringConfig;
		if (StringUtil.isBlank(string)) {
			return null;
		} else {
			return switch (dedicatedServerProperties.textFilteringVersion) {
				case 0 -> LegacyTextFilter.createTextFilterFromConfig(string);
				case 1 -> PlayerSafetyServiceTextFilter.createTextFilterFromConfig(string);
				default -> {
					LOGGER.warn("Could not create text filter - unsupported text filtering version used");
					yield null;
				}
			};
		}
	}

	protected CompletableFuture<FilteredText> requestMessageProcessing(
		GameProfile gameProfile, String string, ServerTextFilter.IgnoreStrategy ignoreStrategy, Executor executor
	) {
		return string.isEmpty() ? CompletableFuture.completedFuture(FilteredText.EMPTY) : CompletableFuture.supplyAsync(() -> {
			JsonObject jsonObject = this.chatEncoder.encode(gameProfile, string);

			try {
				JsonObject jsonObject2 = this.processRequestResponse(jsonObject, this.chatEndpoint);
				return this.filterText(string, ignoreStrategy, jsonObject2);
			} catch (Exception var6) {
				LOGGER.warn("Failed to validate message '{}'", string, var6);
				return FilteredText.fullyFiltered(string);
			}
		}, executor);
	}

	protected abstract FilteredText filterText(String string, ServerTextFilter.IgnoreStrategy ignoreStrategy, JsonObject jsonObject);

	protected FilterMask parseMask(String string, JsonArray jsonArray, ServerTextFilter.IgnoreStrategy ignoreStrategy) {
		if (jsonArray.isEmpty()) {
			return FilterMask.PASS_THROUGH;
		} else if (ignoreStrategy.shouldIgnore(string, jsonArray.size())) {
			return FilterMask.FULLY_FILTERED;
		} else {
			FilterMask filterMask = new FilterMask(string.length());

			for (int i = 0; i < jsonArray.size(); i++) {
				filterMask.setFiltered(jsonArray.get(i).getAsInt());
			}

			return filterMask;
		}
	}

	public void close() {
		this.workerPool.shutdownNow();
	}

	protected void drainStream(InputStream inputStream) throws IOException {
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
					var13 = Streams.parse(new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))).getAsJsonObject();
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

	protected HttpURLConnection makeRequest(JsonObject jsonObject, URL uRL) throws IOException {
		HttpURLConnection httpURLConnection = this.getURLConnection(uRL);
		this.setAuthorizationProperty(httpURLConnection);
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
			throw new ServerTextFilter.RequestFailedException(i + " " + httpURLConnection.getResponseMessage());
		}
	}

	protected abstract void setAuthorizationProperty(HttpURLConnection httpURLConnection);

	protected int connectionReadTimeout() {
		return 2000;
	}

	protected HttpURLConnection getURLConnection(URL uRL) throws IOException {
		HttpURLConnection httpURLConnection = (HttpURLConnection)uRL.openConnection();
		httpURLConnection.setConnectTimeout(15000);
		httpURLConnection.setReadTimeout(this.connectionReadTimeout());
		httpURLConnection.setUseCaches(false);
		httpURLConnection.setDoOutput(true);
		httpURLConnection.setDoInput(true);
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		httpURLConnection.setRequestProperty("Accept", "application/json");
		httpURLConnection.setRequestProperty("User-Agent", "Minecraft server" + SharedConstants.getCurrentVersion().getName());
		return httpURLConnection;
	}

	public TextFilter createContext(GameProfile gameProfile) {
		return new ServerTextFilter.PlayerContext(gameProfile);
	}

	@FunctionalInterface
	public interface IgnoreStrategy {
		ServerTextFilter.IgnoreStrategy NEVER_IGNORE = (string, i) -> false;
		ServerTextFilter.IgnoreStrategy IGNORE_FULLY_FILTERED = (string, i) -> string.length() == i;

		static ServerTextFilter.IgnoreStrategy ignoreOverThreshold(int i) {
			return (string, j) -> j >= i;
		}

		static ServerTextFilter.IgnoreStrategy select(int i) {
			return switch (i) {
				case -1 -> NEVER_IGNORE;
				case 0 -> IGNORE_FULLY_FILTERED;
				default -> ignoreOverThreshold(i);
			};
		}

		boolean shouldIgnore(String string, int i);
	}

	@FunctionalInterface
	protected interface MessageEncoder {
		JsonObject encode(GameProfile gameProfile, String string);
	}

	protected class PlayerContext implements TextFilter {
		protected final GameProfile profile;
		protected final Executor streamExecutor;

		protected PlayerContext(final GameProfile gameProfile) {
			this.profile = gameProfile;
			ProcessorMailbox<Runnable> processorMailbox = ProcessorMailbox.create(ServerTextFilter.this.workerPool, "chat stream for " + gameProfile.getName());
			this.streamExecutor = processorMailbox::tell;
		}

		@Override
		public CompletableFuture<List<FilteredText>> processMessageBundle(List<String> list) {
			List<CompletableFuture<FilteredText>> list2 = (List<CompletableFuture<FilteredText>>)list.stream()
				.map(string -> ServerTextFilter.this.requestMessageProcessing(this.profile, string, ServerTextFilter.this.chatIgnoreStrategy, this.streamExecutor))
				.collect(ImmutableList.toImmutableList());
			return Util.sequenceFailFast(list2).exceptionally(throwable -> ImmutableList.of());
		}

		@Override
		public CompletableFuture<FilteredText> processStreamMessage(String string) {
			return ServerTextFilter.this.requestMessageProcessing(this.profile, string, ServerTextFilter.this.chatIgnoreStrategy, this.streamExecutor);
		}
	}

	protected static class RequestFailedException extends RuntimeException {
		protected RequestFailedException(String string) {
			super(string);
		}
	}
}
