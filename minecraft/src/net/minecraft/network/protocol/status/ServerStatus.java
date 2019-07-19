package net.minecraft.network.protocol.status;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Type;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

public class ServerStatus {
	private Component description;
	private ServerStatus.Players players;
	private ServerStatus.Version version;
	private String favicon;

	public Component getDescription() {
		return this.description;
	}

	public void setDescription(Component component) {
		this.description = component;
	}

	public ServerStatus.Players getPlayers() {
		return this.players;
	}

	public void setPlayers(ServerStatus.Players players) {
		this.players = players;
	}

	public ServerStatus.Version getVersion() {
		return this.version;
	}

	public void setVersion(ServerStatus.Version version) {
		this.version = version;
	}

	public void setFavicon(String string) {
		this.favicon = string;
	}

	public String getFavicon() {
		return this.favicon;
	}

	public static class Players {
		private final int maxPlayers;
		private final int numPlayers;
		private GameProfile[] sample;

		public Players(int i, int j) {
			this.maxPlayers = i;
			this.numPlayers = j;
		}

		public int getMaxPlayers() {
			return this.maxPlayers;
		}

		public int getNumPlayers() {
			return this.numPlayers;
		}

		public GameProfile[] getSample() {
			return this.sample;
		}

		public void setSample(GameProfile[] gameProfiles) {
			this.sample = gameProfiles;
		}

		public static class Serializer implements JsonDeserializer<ServerStatus.Players>, JsonSerializer<ServerStatus.Players> {
			public ServerStatus.Players deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
				JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "players");
				ServerStatus.Players players = new ServerStatus.Players(GsonHelper.getAsInt(jsonObject, "max"), GsonHelper.getAsInt(jsonObject, "online"));
				if (GsonHelper.isArrayNode(jsonObject, "sample")) {
					JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "sample");
					if (jsonArray.size() > 0) {
						GameProfile[] gameProfiles = new GameProfile[jsonArray.size()];

						for (int i = 0; i < gameProfiles.length; i++) {
							JsonObject jsonObject2 = GsonHelper.convertToJsonObject(jsonArray.get(i), "player[" + i + "]");
							String string = GsonHelper.getAsString(jsonObject2, "id");
							gameProfiles[i] = new GameProfile(UUID.fromString(string), GsonHelper.getAsString(jsonObject2, "name"));
						}

						players.setSample(gameProfiles);
					}
				}

				return players;
			}

			public JsonElement serialize(ServerStatus.Players players, Type type, JsonSerializationContext jsonSerializationContext) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("max", players.getMaxPlayers());
				jsonObject.addProperty("online", players.getNumPlayers());
				if (players.getSample() != null && players.getSample().length > 0) {
					JsonArray jsonArray = new JsonArray();

					for (int i = 0; i < players.getSample().length; i++) {
						JsonObject jsonObject2 = new JsonObject();
						UUID uUID = players.getSample()[i].getId();
						jsonObject2.addProperty("id", uUID == null ? "" : uUID.toString());
						jsonObject2.addProperty("name", players.getSample()[i].getName());
						jsonArray.add(jsonObject2);
					}

					jsonObject.add("sample", jsonArray);
				}

				return jsonObject;
			}
		}
	}

	public static class Serializer implements JsonDeserializer<ServerStatus>, JsonSerializer<ServerStatus> {
		public ServerStatus deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "status");
			ServerStatus serverStatus = new ServerStatus();
			if (jsonObject.has("description")) {
				serverStatus.setDescription(jsonDeserializationContext.deserialize(jsonObject.get("description"), Component.class));
			}

			if (jsonObject.has("players")) {
				serverStatus.setPlayers(jsonDeserializationContext.deserialize(jsonObject.get("players"), ServerStatus.Players.class));
			}

			if (jsonObject.has("version")) {
				serverStatus.setVersion(jsonDeserializationContext.deserialize(jsonObject.get("version"), ServerStatus.Version.class));
			}

			if (jsonObject.has("favicon")) {
				serverStatus.setFavicon(GsonHelper.getAsString(jsonObject, "favicon"));
			}

			return serverStatus;
		}

		public JsonElement serialize(ServerStatus serverStatus, Type type, JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = new JsonObject();
			if (serverStatus.getDescription() != null) {
				jsonObject.add("description", jsonSerializationContext.serialize(serverStatus.getDescription()));
			}

			if (serverStatus.getPlayers() != null) {
				jsonObject.add("players", jsonSerializationContext.serialize(serverStatus.getPlayers()));
			}

			if (serverStatus.getVersion() != null) {
				jsonObject.add("version", jsonSerializationContext.serialize(serverStatus.getVersion()));
			}

			if (serverStatus.getFavicon() != null) {
				jsonObject.addProperty("favicon", serverStatus.getFavicon());
			}

			return jsonObject;
		}
	}

	public static class Version {
		private final String name;
		private final int protocol;

		public Version(String string, int i) {
			this.name = string;
			this.protocol = i;
		}

		public String getName() {
			return this.name;
		}

		public int getProtocol() {
			return this.protocol;
		}

		public static class Serializer implements JsonDeserializer<ServerStatus.Version>, JsonSerializer<ServerStatus.Version> {
			public ServerStatus.Version deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
				JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "version");
				return new ServerStatus.Version(GsonHelper.getAsString(jsonObject, "name"), GsonHelper.getAsInt(jsonObject, "protocol"));
			}

			public JsonElement serialize(ServerStatus.Version version, Type type, JsonSerializationContext jsonSerializationContext) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("name", version.getName());
				jsonObject.addProperty("protocol", version.getProtocol());
				return jsonObject;
			}
		}
	}
}
