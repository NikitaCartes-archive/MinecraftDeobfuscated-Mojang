package net.minecraft.server.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCertificate;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;

public class PlayerSafetyServiceTextFilter extends ServerTextFilter {
	private final ConfidentialClientApplication client;
	private final ClientCredentialParameters clientParameters;
	private final Set<String> fullyFilteredEvents;
	private final int connectionReadTimeoutMs;

	private PlayerSafetyServiceTextFilter(
		URL uRL,
		ServerTextFilter.MessageEncoder messageEncoder,
		ServerTextFilter.IgnoreStrategy ignoreStrategy,
		ExecutorService executorService,
		ConfidentialClientApplication confidentialClientApplication,
		ClientCredentialParameters clientCredentialParameters,
		Set<String> set,
		int i
	) {
		super(uRL, messageEncoder, ignoreStrategy, executorService);
		this.client = confidentialClientApplication;
		this.clientParameters = clientCredentialParameters;
		this.fullyFilteredEvents = set;
		this.connectionReadTimeoutMs = i;
	}

	@Nullable
	public static ServerTextFilter createTextFilterFromConfig(String string) {
		JsonObject jsonObject = GsonHelper.parse(string);
		URI uRI = URI.create(GsonHelper.getAsString(jsonObject, "apiServer"));
		String string2 = GsonHelper.getAsString(jsonObject, "apiPath");
		String string3 = GsonHelper.getAsString(jsonObject, "scope");
		String string4 = GsonHelper.getAsString(jsonObject, "serverId", "");
		String string5 = GsonHelper.getAsString(jsonObject, "applicationId");
		String string6 = GsonHelper.getAsString(jsonObject, "tenantId");
		String string7 = GsonHelper.getAsString(jsonObject, "roomId", "Java:Chat");
		String string8 = GsonHelper.getAsString(jsonObject, "certificatePath");
		String string9 = GsonHelper.getAsString(jsonObject, "certificatePassword", "");
		int i = GsonHelper.getAsInt(jsonObject, "hashesToDrop", -1);
		int j = GsonHelper.getAsInt(jsonObject, "maxConcurrentRequests", 7);
		JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "fullyFilteredEvents");
		Set<String> set = new HashSet();
		jsonArray.forEach(jsonElement -> set.add(GsonHelper.convertToString(jsonElement, "filteredEvent")));
		int k = GsonHelper.getAsInt(jsonObject, "connectionReadTimeoutMs", 2000);

		URL uRL;
		try {
			uRL = uRI.resolve(string2).toURL();
		} catch (MalformedURLException var26) {
			throw new RuntimeException(var26);
		}

		ServerTextFilter.MessageEncoder messageEncoder = (gameProfile, string3x) -> {
			JsonObject jsonObjectx = new JsonObject();
			jsonObjectx.addProperty("userId", gameProfile.getId().toString());
			jsonObjectx.addProperty("userDisplayName", gameProfile.getName());
			jsonObjectx.addProperty("server", string4);
			jsonObjectx.addProperty("room", string7);
			jsonObjectx.addProperty("area", "JavaChatRealms");
			jsonObjectx.addProperty("data", string3x);
			jsonObjectx.addProperty("language", "*");
			return jsonObjectx;
		};
		ServerTextFilter.IgnoreStrategy ignoreStrategy = ServerTextFilter.IgnoreStrategy.select(i);
		ExecutorService executorService = createWorkerPool(j);

		IClientCertificate iClientCertificate;
		try {
			InputStream inputStream = Files.newInputStream(Path.of(string8));

			try {
				iClientCertificate = ClientCredentialFactory.createFromCertificate(inputStream, string9);
			} catch (Throwable var27) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var24) {
						var27.addSuppressed(var24);
					}
				}

				throw var27;
			}

			if (inputStream != null) {
				inputStream.close();
			}
		} catch (Exception var28) {
			LOGGER.warn("Failed to open certificate file");
			return null;
		}

		ConfidentialClientApplication confidentialClientApplication;
		try {
			confidentialClientApplication = ConfidentialClientApplication.builder(string5, iClientCertificate)
				.sendX5c(true)
				.executorService(executorService)
				.authority(String.format(Locale.ROOT, "https://login.microsoftonline.com/%s/", string6))
				.build();
		} catch (Exception var25) {
			LOGGER.warn("Failed to create confidential client application");
			return null;
		}

		ClientCredentialParameters clientCredentialParameters = ClientCredentialParameters.builder(Set.of(string3)).build();
		return new PlayerSafetyServiceTextFilter(
			uRL, messageEncoder, ignoreStrategy, executorService, confidentialClientApplication, clientCredentialParameters, set, k
		);
	}

	private IAuthenticationResult aquireIAuthenticationResult() {
		return (IAuthenticationResult)this.client.acquireToken(this.clientParameters).join();
	}

	@Override
	protected void setAuthorizationProperty(HttpURLConnection httpURLConnection) {
		IAuthenticationResult iAuthenticationResult = this.aquireIAuthenticationResult();
		httpURLConnection.setRequestProperty("Authorization", "Bearer " + iAuthenticationResult.accessToken());
	}

	@Override
	protected FilteredText filterText(String string, ServerTextFilter.IgnoreStrategy ignoreStrategy, JsonObject jsonObject) {
		JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "result", null);
		if (jsonObject2 == null) {
			return FilteredText.fullyFiltered(string);
		} else {
			boolean bl = GsonHelper.getAsBoolean(jsonObject2, "filtered", true);
			if (!bl) {
				return FilteredText.passThrough(string);
			} else {
				for (JsonElement jsonElement : GsonHelper.getAsJsonArray(jsonObject2, "events", new JsonArray())) {
					JsonObject jsonObject3 = jsonElement.getAsJsonObject();
					String string2 = GsonHelper.getAsString(jsonObject3, "id", "");
					if (this.fullyFilteredEvents.contains(string2)) {
						return FilteredText.fullyFiltered(string);
					}
				}

				JsonArray jsonArray2 = GsonHelper.getAsJsonArray(jsonObject2, "redactedTextIndex", new JsonArray());
				return new FilteredText(string, this.parseMask(string, jsonArray2, ignoreStrategy));
			}
		}
	}

	@Override
	protected int connectionReadTimeout() {
		return this.connectionReadTimeoutMs;
	}
}
