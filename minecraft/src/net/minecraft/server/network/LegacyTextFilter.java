package net.minecraft.server.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nullable;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.util.GsonHelper;

public class LegacyTextFilter extends ServerTextFilter {
	private static final String ENDPOINT = "v1/chat";
	final URL joinEndpoint;
	final LegacyTextFilter.JoinOrLeaveEncoder joinEncoder;
	final URL leaveEndpoint;
	final LegacyTextFilter.JoinOrLeaveEncoder leaveEncoder;
	private final String authKey;

	private LegacyTextFilter(
		URL uRL,
		ServerTextFilter.MessageEncoder messageEncoder,
		URL uRL2,
		LegacyTextFilter.JoinOrLeaveEncoder joinOrLeaveEncoder,
		URL uRL3,
		LegacyTextFilter.JoinOrLeaveEncoder joinOrLeaveEncoder2,
		String string,
		ServerTextFilter.IgnoreStrategy ignoreStrategy,
		ExecutorService executorService
	) {
		super(uRL, messageEncoder, ignoreStrategy, executorService);
		this.joinEndpoint = uRL2;
		this.joinEncoder = joinOrLeaveEncoder;
		this.leaveEndpoint = uRL3;
		this.leaveEncoder = joinOrLeaveEncoder2;
		this.authKey = string;
	}

	@Nullable
	public static ServerTextFilter createTextFilterFromConfig(String string) {
		try {
			JsonObject jsonObject = GsonHelper.parse(string);
			URI uRI = new URI(GsonHelper.getAsString(jsonObject, "apiServer"));
			String string2 = GsonHelper.getAsString(jsonObject, "apiKey");
			if (string2.isEmpty()) {
				throw new IllegalArgumentException("Missing API key");
			} else {
				int i = GsonHelper.getAsInt(jsonObject, "ruleId", 1);
				String string3 = GsonHelper.getAsString(jsonObject, "serverId", "");
				String string4 = GsonHelper.getAsString(jsonObject, "roomId", "Java:Chat");
				int j = GsonHelper.getAsInt(jsonObject, "hashesToDrop", -1);
				int k = GsonHelper.getAsInt(jsonObject, "maxConcurrentRequests", 7);
				JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "endpoints", null);
				String string5 = getEndpointFromConfig(jsonObject2, "chat", "v1/chat");
				boolean bl = string5.equals("v1/chat");
				URL uRL = uRI.resolve("/" + string5).toURL();
				URL uRL2 = getEndpoint(uRI, jsonObject2, "join", "v1/join");
				URL uRL3 = getEndpoint(uRI, jsonObject2, "leave", "v1/leave");
				LegacyTextFilter.JoinOrLeaveEncoder joinOrLeaveEncoder = gameProfile -> {
					JsonObject jsonObjectx = new JsonObject();
					jsonObjectx.addProperty("server", string3);
					jsonObjectx.addProperty("room", string4);
					jsonObjectx.addProperty("user_id", gameProfile.getId().toString());
					jsonObjectx.addProperty("user_display_name", gameProfile.getName());
					return jsonObjectx;
				};
				ServerTextFilter.MessageEncoder messageEncoder;
				if (bl) {
					messageEncoder = (gameProfile, string3x) -> {
						JsonObject jsonObjectx = new JsonObject();
						jsonObjectx.addProperty("rule", i);
						jsonObjectx.addProperty("server", string3);
						jsonObjectx.addProperty("room", string4);
						jsonObjectx.addProperty("player", gameProfile.getId().toString());
						jsonObjectx.addProperty("player_display_name", gameProfile.getName());
						jsonObjectx.addProperty("text", string3x);
						jsonObjectx.addProperty("language", "*");
						return jsonObjectx;
					};
				} else {
					String string6 = String.valueOf(i);
					messageEncoder = (gameProfile, string4x) -> {
						JsonObject jsonObjectx = new JsonObject();
						jsonObjectx.addProperty("rule_id", string6);
						jsonObjectx.addProperty("category", string3);
						jsonObjectx.addProperty("subcategory", string4);
						jsonObjectx.addProperty("user_id", gameProfile.getId().toString());
						jsonObjectx.addProperty("user_display_name", gameProfile.getName());
						jsonObjectx.addProperty("text", string4x);
						jsonObjectx.addProperty("language", "*");
						return jsonObjectx;
					};
				}

				ServerTextFilter.IgnoreStrategy ignoreStrategy = ServerTextFilter.IgnoreStrategy.select(j);
				ExecutorService executorService = createWorkerPool(k);
				String string7 = Base64.getEncoder().encodeToString(string2.getBytes(StandardCharsets.US_ASCII));
				return new LegacyTextFilter(uRL, messageEncoder, uRL2, joinOrLeaveEncoder, uRL3, joinOrLeaveEncoder, string7, ignoreStrategy, executorService);
			}
		} catch (Exception var20) {
			LOGGER.warn("Failed to parse chat filter config {}", string, var20);
			return null;
		}
	}

	@Override
	public TextFilter createContext(GameProfile gameProfile) {
		return new ServerTextFilter.PlayerContext(gameProfile) {
			@Override
			public void join() {
				LegacyTextFilter.this.processJoinOrLeave(this.profile, LegacyTextFilter.this.joinEndpoint, LegacyTextFilter.this.joinEncoder, this.streamExecutor);
			}

			@Override
			public void leave() {
				LegacyTextFilter.this.processJoinOrLeave(this.profile, LegacyTextFilter.this.leaveEndpoint, LegacyTextFilter.this.leaveEncoder, this.streamExecutor);
			}
		};
	}

	void processJoinOrLeave(GameProfile gameProfile, URL uRL, LegacyTextFilter.JoinOrLeaveEncoder joinOrLeaveEncoder, Executor executor) {
		executor.execute(() -> {
			JsonObject jsonObject = joinOrLeaveEncoder.encode(gameProfile);

			try {
				this.processRequest(jsonObject, uRL);
			} catch (Exception var6) {
				LOGGER.warn("Failed to send join/leave packet to {} for player {}", uRL, gameProfile, var6);
			}
		});
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

	@Override
	protected void setAuthorizationProperty(HttpURLConnection httpURLConnection) {
		httpURLConnection.setRequestProperty("Authorization", "Basic " + this.authKey);
	}

	@Override
	protected FilteredText filterText(String string, ServerTextFilter.IgnoreStrategy ignoreStrategy, JsonObject jsonObject) {
		boolean bl = GsonHelper.getAsBoolean(jsonObject, "response", false);
		if (bl) {
			return FilteredText.passThrough(string);
		} else {
			String string2 = GsonHelper.getAsString(jsonObject, "hashed", null);
			if (string2 == null) {
				return FilteredText.fullyFiltered(string);
			} else {
				JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "hashes");
				FilterMask filterMask = this.parseMask(string, jsonArray, ignoreStrategy);
				return new FilteredText(string, filterMask);
			}
		}
	}

	@FunctionalInterface
	interface JoinOrLeaveEncoder {
		JsonObject encode(GameProfile gameProfile);
	}
}
