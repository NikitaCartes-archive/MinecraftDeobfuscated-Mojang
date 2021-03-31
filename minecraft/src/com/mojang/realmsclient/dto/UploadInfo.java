package com.mojang.realmsclient.dto;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class UploadInfo extends ValueObject {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String DEFAULT_SCHEMA = "http://";
	private static final int DEFAULT_PORT = 8080;
	private static final Pattern URI_SCHEMA_PATTERN = Pattern.compile("^[a-zA-Z][-a-zA-Z0-9+.]+:");
	private final boolean worldClosed;
	@Nullable
	private final String token;
	private final URI uploadEndpoint;

	private UploadInfo(boolean bl, @Nullable String string, URI uRI) {
		this.worldClosed = bl;
		this.token = string;
		this.uploadEndpoint = uRI;
	}

	@Nullable
	public static UploadInfo parse(String string) {
		try {
			JsonParser jsonParser = new JsonParser();
			JsonObject jsonObject = jsonParser.parse(string).getAsJsonObject();
			String string2 = JsonUtils.getStringOr("uploadEndpoint", jsonObject, null);
			if (string2 != null) {
				int i = JsonUtils.getIntOr("port", jsonObject, -1);
				URI uRI = assembleUri(string2, i);
				if (uRI != null) {
					boolean bl = JsonUtils.getBooleanOr("worldClosed", jsonObject, false);
					String string3 = JsonUtils.getStringOr("token", jsonObject, null);
					return new UploadInfo(bl, string3, uRI);
				}
			}
		} catch (Exception var8) {
			LOGGER.error("Could not parse UploadInfo: {}", var8.getMessage());
		}

		return null;
	}

	@Nullable
	@VisibleForTesting
	public static URI assembleUri(String string, int i) {
		Matcher matcher = URI_SCHEMA_PATTERN.matcher(string);
		String string2 = ensureEndpointSchema(string, matcher);

		try {
			URI uRI = new URI(string2);
			int j = selectPortOrDefault(i, uRI.getPort());
			return j != uRI.getPort() ? new URI(uRI.getScheme(), uRI.getUserInfo(), uRI.getHost(), j, uRI.getPath(), uRI.getQuery(), uRI.getFragment()) : uRI;
		} catch (URISyntaxException var6) {
			LOGGER.warn("Failed to parse URI {}", string2, var6);
			return null;
		}
	}

	private static int selectPortOrDefault(int i, int j) {
		if (i != -1) {
			return i;
		} else {
			return j != -1 ? j : 8080;
		}
	}

	private static String ensureEndpointSchema(String string, Matcher matcher) {
		return matcher.find() ? string : "http://" + string;
	}

	public static String createRequest(@Nullable String string) {
		JsonObject jsonObject = new JsonObject();
		if (string != null) {
			jsonObject.addProperty("token", string);
		}

		return jsonObject.toString();
	}

	@Nullable
	public String getToken() {
		return this.token;
	}

	public URI getUploadEndpoint() {
		return this.uploadEndpoint;
	}

	public boolean isWorldClosed() {
		return this.worldClosed;
	}
}
