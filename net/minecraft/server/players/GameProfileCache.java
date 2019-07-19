/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.players;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

public class GameProfileCache {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private static boolean usesAuthentication;
    private final Map<String, GameProfileInfo> profilesByName = Maps.newHashMap();
    private final Map<UUID, GameProfileInfo> profilesByUUID = Maps.newHashMap();
    private final Deque<GameProfile> profileMRUList = Lists.newLinkedList();
    private final GameProfileRepository profileRepository;
    protected final Gson gson;
    private final File file;
    private static final ParameterizedType GAMEPROFILE_ENTRY_TYPE;

    public GameProfileCache(GameProfileRepository gameProfileRepository, File file) {
        this.profileRepository = gameProfileRepository;
        this.file = file;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(GameProfileInfo.class, new Serializer());
        this.gson = gsonBuilder.create();
        this.load();
    }

    private static GameProfile lookupGameProfile(GameProfileRepository gameProfileRepository, String string) {
        final GameProfile[] gameProfiles = new GameProfile[1];
        ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback(){

            @Override
            public void onProfileLookupSucceeded(GameProfile gameProfile) {
                gameProfiles[0] = gameProfile;
            }

            @Override
            public void onProfileLookupFailed(GameProfile gameProfile, Exception exception) {
                gameProfiles[0] = null;
            }
        };
        gameProfileRepository.findProfilesByNames(new String[]{string}, Agent.MINECRAFT, profileLookupCallback);
        if (!GameProfileCache.usesAuthentication() && gameProfiles[0] == null) {
            UUID uUID = Player.createPlayerUUID(new GameProfile(null, string));
            GameProfile gameProfile = new GameProfile(uUID, string);
            profileLookupCallback.onProfileLookupSucceeded(gameProfile);
        }
        return gameProfiles[0];
    }

    public static void setUsesAuthentication(boolean bl) {
        usesAuthentication = bl;
    }

    private static boolean usesAuthentication() {
        return usesAuthentication;
    }

    public void add(GameProfile gameProfile) {
        this.add(gameProfile, null);
    }

    private void add(GameProfile gameProfile, Date date) {
        UUID uUID = gameProfile.getId();
        if (date == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(2, 1);
            date = calendar.getTime();
        }
        GameProfileInfo gameProfileInfo = new GameProfileInfo(gameProfile, date);
        if (this.profilesByUUID.containsKey(uUID)) {
            GameProfileInfo gameProfileInfo2 = this.profilesByUUID.get(uUID);
            this.profilesByName.remove(gameProfileInfo2.getProfile().getName().toLowerCase(Locale.ROOT));
            this.profileMRUList.remove(gameProfile);
        }
        this.profilesByName.put(gameProfile.getName().toLowerCase(Locale.ROOT), gameProfileInfo);
        this.profilesByUUID.put(uUID, gameProfileInfo);
        this.profileMRUList.addFirst(gameProfile);
        this.save();
    }

    @Nullable
    public GameProfile get(String string) {
        String string2 = string.toLowerCase(Locale.ROOT);
        GameProfileInfo gameProfileInfo = this.profilesByName.get(string2);
        if (gameProfileInfo != null && new Date().getTime() >= gameProfileInfo.expirationDate.getTime()) {
            this.profilesByUUID.remove(gameProfileInfo.getProfile().getId());
            this.profilesByName.remove(gameProfileInfo.getProfile().getName().toLowerCase(Locale.ROOT));
            this.profileMRUList.remove(gameProfileInfo.getProfile());
            gameProfileInfo = null;
        }
        if (gameProfileInfo != null) {
            GameProfile gameProfile = gameProfileInfo.getProfile();
            this.profileMRUList.remove(gameProfile);
            this.profileMRUList.addFirst(gameProfile);
        } else {
            GameProfile gameProfile = GameProfileCache.lookupGameProfile(this.profileRepository, string2);
            if (gameProfile != null) {
                this.add(gameProfile);
                gameProfileInfo = this.profilesByName.get(string2);
            }
        }
        this.save();
        return gameProfileInfo == null ? null : gameProfileInfo.getProfile();
    }

    @Nullable
    public GameProfile get(UUID uUID) {
        GameProfileInfo gameProfileInfo = this.profilesByUUID.get(uUID);
        return gameProfileInfo == null ? null : gameProfileInfo.getProfile();
    }

    private GameProfileInfo getProfileInfo(UUID uUID) {
        GameProfileInfo gameProfileInfo = this.profilesByUUID.get(uUID);
        if (gameProfileInfo != null) {
            GameProfile gameProfile = gameProfileInfo.getProfile();
            this.profileMRUList.remove(gameProfile);
            this.profileMRUList.addFirst(gameProfile);
        }
        return gameProfileInfo;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void load() {
        BufferedReader bufferedReader;
        block5: {
            bufferedReader = null;
            try {
                bufferedReader = Files.newReader(this.file, StandardCharsets.UTF_8);
                List list = (List)GsonHelper.fromJson(this.gson, (Reader)bufferedReader, (Type)GAMEPROFILE_ENTRY_TYPE);
                this.profilesByName.clear();
                this.profilesByUUID.clear();
                this.profileMRUList.clear();
                if (list == null) break block5;
                for (GameProfileInfo gameProfileInfo : Lists.reverse(list)) {
                    if (gameProfileInfo == null) continue;
                    this.add(gameProfileInfo.getProfile(), gameProfileInfo.getExpirationDate());
                }
            } catch (FileNotFoundException fileNotFoundException) {
                IOUtils.closeQuietly(bufferedReader);
            } catch (JsonParseException jsonParseException) {
                IOUtils.closeQuietly(bufferedReader);
            } catch (Throwable throwable) {
                IOUtils.closeQuietly(bufferedReader);
                throw throwable;
            }
        }
        IOUtils.closeQuietly(bufferedReader);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void save() {
        String string = this.gson.toJson(this.getTopMRUProfiles(1000));
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = Files.newWriter(this.file, StandardCharsets.UTF_8);
            bufferedWriter.write(string);
        } catch (FileNotFoundException fileNotFoundException) {
            IOUtils.closeQuietly(bufferedWriter);
            return;
        } catch (IOException iOException) {
            IOUtils.closeQuietly(bufferedWriter);
            return;
        } catch (Throwable throwable) {
            IOUtils.closeQuietly(bufferedWriter);
            throw throwable;
        }
        IOUtils.closeQuietly(bufferedWriter);
    }

    private List<GameProfileInfo> getTopMRUProfiles(int i) {
        ArrayList<GameProfileInfo> list = Lists.newArrayList();
        ArrayList<GameProfile> list2 = Lists.newArrayList(Iterators.limit(this.profileMRUList.iterator(), i));
        for (GameProfile gameProfile : list2) {
            GameProfileInfo gameProfileInfo = this.getProfileInfo(gameProfile.getId());
            if (gameProfileInfo == null) continue;
            list.add(gameProfileInfo);
        }
        return list;
    }

    static {
        GAMEPROFILE_ENTRY_TYPE = new ParameterizedType(){

            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{GameProfileInfo.class};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }

    class GameProfileInfo {
        private final GameProfile profile;
        private final Date expirationDate;

        private GameProfileInfo(GameProfile gameProfile, Date date) {
            this.profile = gameProfile;
            this.expirationDate = date;
        }

        public GameProfile getProfile() {
            return this.profile;
        }

        public Date getExpirationDate() {
            return this.expirationDate;
        }
    }

    class Serializer
    implements JsonDeserializer<GameProfileInfo>,
    JsonSerializer<GameProfileInfo> {
        private Serializer() {
        }

        @Override
        public JsonElement serialize(GameProfileInfo gameProfileInfo, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", gameProfileInfo.getProfile().getName());
            UUID uUID = gameProfileInfo.getProfile().getId();
            jsonObject.addProperty("uuid", uUID == null ? "" : uUID.toString());
            jsonObject.addProperty("expiresOn", DATE_FORMAT.format(gameProfileInfo.getExpirationDate()));
            return jsonObject;
        }

        @Override
        public GameProfileInfo deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonObject()) {
                UUID uUID;
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonElement jsonElement2 = jsonObject.get("name");
                JsonElement jsonElement3 = jsonObject.get("uuid");
                JsonElement jsonElement4 = jsonObject.get("expiresOn");
                if (jsonElement2 == null || jsonElement3 == null) {
                    return null;
                }
                String string = jsonElement3.getAsString();
                String string2 = jsonElement2.getAsString();
                Date date = null;
                if (jsonElement4 != null) {
                    try {
                        date = DATE_FORMAT.parse(jsonElement4.getAsString());
                    } catch (ParseException parseException) {
                        date = null;
                    }
                }
                if (string2 == null || string == null) {
                    return null;
                }
                try {
                    uUID = UUID.fromString(string);
                } catch (Throwable throwable) {
                    return null;
                }
                return new GameProfileInfo(new GameProfile(uUID, string2), date);
            }
            return null;
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((GameProfileInfo)object, type, jsonSerializationContext);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

