/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.bridge.game.GameVersion;
import com.mojang.bridge.game.PackType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DetectedVersion
implements GameVersion {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final GameVersion BUILT_IN = new DetectedVersion();
    private final String id;
    private final String name;
    private final boolean stable;
    private final int worldVersion;
    private final int protocolVersion;
    private final int resourcePackVersion;
    private final int dataPackVersion;
    private final Date buildTime;
    private final String releaseTarget;

    private DetectedVersion() {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.name = "21w20a";
        this.stable = false;
        this.worldVersion = 2715;
        this.protocolVersion = SharedConstants.getProtocolVersion();
        this.resourcePackVersion = 7;
        this.dataPackVersion = 7;
        this.buildTime = new Date();
        this.releaseTarget = "1.17";
    }

    private DetectedVersion(JsonObject jsonObject) {
        this.id = GsonHelper.getAsString(jsonObject, "id");
        this.name = GsonHelper.getAsString(jsonObject, "name");
        this.releaseTarget = GsonHelper.getAsString(jsonObject, "release_target");
        this.stable = GsonHelper.getAsBoolean(jsonObject, "stable");
        this.worldVersion = GsonHelper.getAsInt(jsonObject, "world_version");
        this.protocolVersion = GsonHelper.getAsInt(jsonObject, "protocol_version");
        JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "pack_version");
        this.resourcePackVersion = GsonHelper.getAsInt(jsonObject2, "resource");
        this.dataPackVersion = GsonHelper.getAsInt(jsonObject2, "data");
        this.buildTime = Date.from(ZonedDateTime.parse(GsonHelper.getAsString(jsonObject, "build_time")).toInstant());
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static GameVersion tryDetectVersion() {
        try (InputStream inputStream = DetectedVersion.class.getResourceAsStream("/version.json");){
            DetectedVersion detectedVersion;
            if (inputStream == null) {
                LOGGER.warn("Missing version information!");
                GameVersion gameVersion = BUILT_IN;
                return gameVersion;
            }
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);){
                detectedVersion = new DetectedVersion(GsonHelper.parse(inputStreamReader));
            }
            return detectedVersion;
        } catch (JsonParseException | IOException exception) {
            throw new IllegalStateException("Game version information is corrupt", exception);
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getReleaseTarget() {
        return this.releaseTarget;
    }

    @Override
    public int getWorldVersion() {
        return this.worldVersion;
    }

    @Override
    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    @Override
    public int getPackVersion(PackType packType) {
        return packType == PackType.DATA ? this.dataPackVersion : this.resourcePackVersion;
    }

    @Override
    public Date getBuildTime() {
        return this.buildTime;
    }

    @Override
    public boolean isStable() {
        return this.stable;
    }
}

