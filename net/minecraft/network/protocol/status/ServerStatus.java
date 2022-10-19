/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import org.jetbrains.annotations.Nullable;

public class ServerStatus {
    public static final int FAVICON_WIDTH = 64;
    public static final int FAVICON_HEIGHT = 64;
    @Nullable
    private Component description;
    @Nullable
    private Players players;
    @Nullable
    private Version version;
    @Nullable
    private String favicon;
    private boolean enforcesSecureChat;

    @Nullable
    public Component getDescription() {
        return this.description;
    }

    public void setDescription(Component component) {
        this.description = component;
    }

    @Nullable
    public Players getPlayers() {
        return this.players;
    }

    public void setPlayers(Players players) {
        this.players = players;
    }

    @Nullable
    public Version getVersion() {
        return this.version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public void setFavicon(String string) {
        this.favicon = string;
    }

    @Nullable
    public String getFavicon() {
        return this.favicon;
    }

    public void setEnforcesSecureChat(boolean bl) {
        this.enforcesSecureChat = bl;
    }

    public boolean enforcesSecureChat() {
        return this.enforcesSecureChat;
    }

    public static class Players {
        private final int maxPlayers;
        private final int numPlayers;
        @Nullable
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

        @Nullable
        public GameProfile[] getSample() {
            return this.sample;
        }

        public void setSample(GameProfile[] gameProfiles) {
            this.sample = gameProfiles;
        }

        public static class Serializer
        implements JsonDeserializer<Players>,
        JsonSerializer<Players> {
            @Override
            public Players deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                JsonArray jsonArray;
                JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "players");
                Players players = new Players(GsonHelper.getAsInt(jsonObject, "max"), GsonHelper.getAsInt(jsonObject, "online"));
                if (GsonHelper.isArrayNode(jsonObject, "sample") && (jsonArray = GsonHelper.getAsJsonArray(jsonObject, "sample")).size() > 0) {
                    GameProfile[] gameProfiles = new GameProfile[jsonArray.size()];
                    for (int i = 0; i < gameProfiles.length; ++i) {
                        JsonObject jsonObject2 = GsonHelper.convertToJsonObject(jsonArray.get(i), "player[" + i + "]");
                        String string = GsonHelper.getAsString(jsonObject2, "id");
                        gameProfiles[i] = new GameProfile(UUID.fromString(string), GsonHelper.getAsString(jsonObject2, "name"));
                    }
                    players.setSample(gameProfiles);
                }
                return players;
            }

            @Override
            public JsonElement serialize(Players players, Type type, JsonSerializationContext jsonSerializationContext) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("max", players.getMaxPlayers());
                jsonObject.addProperty("online", players.getNumPlayers());
                GameProfile[] gameProfiles = players.getSample();
                if (gameProfiles != null && gameProfiles.length > 0) {
                    JsonArray jsonArray = new JsonArray();
                    for (int i = 0; i < gameProfiles.length; ++i) {
                        JsonObject jsonObject2 = new JsonObject();
                        UUID uUID = gameProfiles[i].getId();
                        jsonObject2.addProperty("id", uUID == null ? "" : uUID.toString());
                        jsonObject2.addProperty("name", gameProfiles[i].getName());
                        jsonArray.add(jsonObject2);
                    }
                    jsonObject.add("sample", jsonArray);
                }
                return jsonObject;
            }

            @Override
            public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
                return this.serialize((Players)object, type, jsonSerializationContext);
            }

            @Override
            public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                return this.deserialize(jsonElement, type, jsonDeserializationContext);
            }
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

        public static class Serializer
        implements JsonDeserializer<Version>,
        JsonSerializer<Version> {
            @Override
            public Version deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "version");
                return new Version(GsonHelper.getAsString(jsonObject, "name"), GsonHelper.getAsInt(jsonObject, "protocol"));
            }

            @Override
            public JsonElement serialize(Version version, Type type, JsonSerializationContext jsonSerializationContext) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", version.getName());
                jsonObject.addProperty("protocol", version.getProtocol());
                return jsonObject;
            }

            @Override
            public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
                return this.serialize((Version)object, type, jsonSerializationContext);
            }

            @Override
            public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                return this.deserialize(jsonElement, type, jsonDeserializationContext);
            }
        }
    }

    public static class Serializer
    implements JsonDeserializer<ServerStatus>,
    JsonSerializer<ServerStatus> {
        @Override
        public ServerStatus deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "status");
            ServerStatus serverStatus = new ServerStatus();
            if (jsonObject.has("description")) {
                serverStatus.setDescription((Component)jsonDeserializationContext.deserialize(jsonObject.get("description"), (Type)((Object)Component.class)));
            }
            if (jsonObject.has("players")) {
                serverStatus.setPlayers((Players)jsonDeserializationContext.deserialize(jsonObject.get("players"), (Type)((Object)Players.class)));
            }
            if (jsonObject.has("version")) {
                serverStatus.setVersion((Version)jsonDeserializationContext.deserialize(jsonObject.get("version"), (Type)((Object)Version.class)));
            }
            if (jsonObject.has("favicon")) {
                serverStatus.setFavicon(GsonHelper.getAsString(jsonObject, "favicon"));
            }
            if (jsonObject.has("enforcesSecureChat")) {
                serverStatus.setEnforcesSecureChat(GsonHelper.getAsBoolean(jsonObject, "enforcesSecureChat"));
            }
            return serverStatus;
        }

        @Override
        public JsonElement serialize(ServerStatus serverStatus, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("enforcesSecureChat", serverStatus.enforcesSecureChat());
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

        @Override
        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((ServerStatus)object, type, jsonSerializationContext);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

