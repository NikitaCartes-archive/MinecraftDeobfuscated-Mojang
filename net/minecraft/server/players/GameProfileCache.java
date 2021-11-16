/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.players;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class GameProfileCache {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int GAMEPROFILES_MRU_LIMIT = 1000;
    private static final int GAMEPROFILES_EXPIRATION_MONTHS = 1;
    private static boolean usesAuthentication;
    private final Map<String, GameProfileInfo> profilesByName = Maps.newConcurrentMap();
    private final Map<UUID, GameProfileInfo> profilesByUUID = Maps.newConcurrentMap();
    private final Map<String, CompletableFuture<Optional<GameProfile>>> requests = Maps.newConcurrentMap();
    private final GameProfileRepository profileRepository;
    private final Gson gson = new GsonBuilder().create();
    private final File file;
    private final AtomicLong operationCount = new AtomicLong();
    @Nullable
    private Executor executor;

    public GameProfileCache(GameProfileRepository gameProfileRepository, File file) {
        this.profileRepository = gameProfileRepository;
        this.file = file;
        Lists.reverse(this.load()).forEach(this::safeAdd);
    }

    private void safeAdd(GameProfileInfo gameProfileInfo) {
        UUID uUID;
        GameProfile gameProfile = gameProfileInfo.getProfile();
        gameProfileInfo.setLastAccess(this.getNextOperation());
        String string = gameProfile.getName();
        if (string != null) {
            this.profilesByName.put(string.toLowerCase(Locale.ROOT), gameProfileInfo);
        }
        if ((uUID = gameProfile.getId()) != null) {
            this.profilesByUUID.put(uUID, gameProfileInfo);
        }
    }

    private static Optional<GameProfile> lookupGameProfile(GameProfileRepository gameProfileRepository, String string) {
        final AtomicReference atomicReference = new AtomicReference();
        ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback(){

            @Override
            public void onProfileLookupSucceeded(GameProfile gameProfile) {
                atomicReference.set(gameProfile);
            }

            @Override
            public void onProfileLookupFailed(GameProfile gameProfile, Exception exception) {
                atomicReference.set(null);
            }
        };
        gameProfileRepository.findProfilesByNames(new String[]{string}, Agent.MINECRAFT, profileLookupCallback);
        GameProfile gameProfile = (GameProfile)atomicReference.get();
        if (!GameProfileCache.usesAuthentication() && gameProfile == null) {
            UUID uUID = Player.createPlayerUUID(new GameProfile(null, string));
            return Optional.of(new GameProfile(uUID, string));
        }
        return Optional.ofNullable(gameProfile);
    }

    public static void setUsesAuthentication(boolean bl) {
        usesAuthentication = bl;
    }

    private static boolean usesAuthentication() {
        return usesAuthentication;
    }

    public void add(GameProfile gameProfile) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(2, 1);
        Date date = calendar.getTime();
        GameProfileInfo gameProfileInfo = new GameProfileInfo(gameProfile, date);
        this.safeAdd(gameProfileInfo);
        this.save();
    }

    private long getNextOperation() {
        return this.operationCount.incrementAndGet();
    }

    public Optional<GameProfile> get(String string) {
        Optional<GameProfile> optional;
        String string2 = string.toLowerCase(Locale.ROOT);
        GameProfileInfo gameProfileInfo = this.profilesByName.get(string2);
        boolean bl = false;
        if (gameProfileInfo != null && new Date().getTime() >= gameProfileInfo.expirationDate.getTime()) {
            this.profilesByUUID.remove(gameProfileInfo.getProfile().getId());
            this.profilesByName.remove(gameProfileInfo.getProfile().getName().toLowerCase(Locale.ROOT));
            bl = true;
            gameProfileInfo = null;
        }
        if (gameProfileInfo != null) {
            gameProfileInfo.setLastAccess(this.getNextOperation());
            optional = Optional.of(gameProfileInfo.getProfile());
        } else {
            optional = GameProfileCache.lookupGameProfile(this.profileRepository, string2);
            if (optional.isPresent()) {
                this.add(optional.get());
                bl = false;
            }
        }
        if (bl) {
            this.save();
        }
        return optional;
    }

    public void getAsync(String string, Consumer<Optional<GameProfile>> consumer) {
        if (this.executor == null) {
            throw new IllegalStateException("No executor");
        }
        CompletableFuture<Optional<GameProfile>> completableFuture = this.requests.get(string);
        if (completableFuture != null) {
            this.requests.put(string, (CompletableFuture<Optional<GameProfile>>)completableFuture.whenCompleteAsync((optional, throwable) -> consumer.accept((Optional<GameProfile>)optional), this.executor));
        } else {
            this.requests.put(string, (CompletableFuture<Optional<GameProfile>>)((CompletableFuture)CompletableFuture.supplyAsync(() -> this.get(string), Util.backgroundExecutor()).whenCompleteAsync((optional, throwable) -> this.requests.remove(string), this.executor)).whenCompleteAsync((optional, throwable) -> consumer.accept((Optional<GameProfile>)optional), this.executor));
        }
    }

    public Optional<GameProfile> get(UUID uUID) {
        GameProfileInfo gameProfileInfo = this.profilesByUUID.get(uUID);
        if (gameProfileInfo == null) {
            return Optional.empty();
        }
        gameProfileInfo.setLastAccess(this.getNextOperation());
        return Optional.of(gameProfileInfo.getProfile());
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void clearExecutor() {
        this.executor = null;
    }

    private static DateFormat createDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public List<GameProfileInfo> load() {
        ArrayList<GameProfileInfo> list = Lists.newArrayList();
        try (BufferedReader reader2222 = Files.newReader(this.file, StandardCharsets.UTF_8);){
            JsonArray jsonArray = this.gson.fromJson((Reader)reader2222, JsonArray.class);
            if (jsonArray == null) {
                ArrayList<GameProfileInfo> arrayList = list;
                return arrayList;
            }
            DateFormat dateFormat = GameProfileCache.createDateFormat();
            jsonArray.forEach(jsonElement -> GameProfileCache.readGameProfile(jsonElement, dateFormat).ifPresent(list::add));
            return list;
        } catch (FileNotFoundException reader2222) {
            return list;
        } catch (JsonParseException | IOException exception) {
            LOGGER.warn("Failed to load profile cache {}", (Object)this.file, (Object)exception);
        }
        return list;
    }

    public void save() {
        JsonArray jsonArray = new JsonArray();
        DateFormat dateFormat = GameProfileCache.createDateFormat();
        this.getTopMRUProfiles(1000).forEach(gameProfileInfo -> jsonArray.add(GameProfileCache.writeGameProfile(gameProfileInfo, dateFormat)));
        String string = this.gson.toJson(jsonArray);
        try (BufferedWriter writer = Files.newWriter(this.file, StandardCharsets.UTF_8);){
            writer.write(string);
        } catch (IOException iOException) {
            // empty catch block
        }
    }

    private Stream<GameProfileInfo> getTopMRUProfiles(int i) {
        return ImmutableList.copyOf(this.profilesByUUID.values()).stream().sorted(Comparator.comparing(GameProfileInfo::getLastAccess).reversed()).limit(i);
    }

    private static JsonElement writeGameProfile(GameProfileInfo gameProfileInfo, DateFormat dateFormat) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", gameProfileInfo.getProfile().getName());
        UUID uUID = gameProfileInfo.getProfile().getId();
        jsonObject.addProperty("uuid", uUID == null ? "" : uUID.toString());
        jsonObject.addProperty("expiresOn", dateFormat.format(gameProfileInfo.getExpirationDate()));
        return jsonObject;
    }

    private static Optional<GameProfileInfo> readGameProfile(JsonElement jsonElement, DateFormat dateFormat) {
        if (jsonElement.isJsonObject()) {
            UUID uUID;
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonElement jsonElement2 = jsonObject.get("name");
            JsonElement jsonElement3 = jsonObject.get("uuid");
            JsonElement jsonElement4 = jsonObject.get("expiresOn");
            if (jsonElement2 == null || jsonElement3 == null) {
                return Optional.empty();
            }
            String string = jsonElement3.getAsString();
            String string2 = jsonElement2.getAsString();
            Date date = null;
            if (jsonElement4 != null) {
                try {
                    date = dateFormat.parse(jsonElement4.getAsString());
                } catch (ParseException parseException) {
                    // empty catch block
                }
            }
            if (string2 == null || string == null || date == null) {
                return Optional.empty();
            }
            try {
                uUID = UUID.fromString(string);
            } catch (Throwable throwable) {
                return Optional.empty();
            }
            return Optional.of(new GameProfileInfo(new GameProfile(uUID, string2), date));
        }
        return Optional.empty();
    }

    static class GameProfileInfo {
        private final GameProfile profile;
        final Date expirationDate;
        private volatile long lastAccess;

        GameProfileInfo(GameProfile gameProfile, Date date) {
            this.profile = gameProfile;
            this.expirationDate = date;
        }

        public GameProfile getProfile() {
            return this.profile;
        }

        public Date getExpirationDate() {
            return this.expirationDate;
        }

        public void setLastAccess(long l) {
            this.lastAccess = l;
        }

        public long getLastAccess() {
            return this.lastAccess;
        }
    }
}

