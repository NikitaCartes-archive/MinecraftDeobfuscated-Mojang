/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.DataVersion;
import org.slf4j.Logger;

public class DetectedVersion
implements WorldVersion {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final WorldVersion BUILT_IN = new DetectedVersion();
    private final String id;
    private final String name;
    private final boolean stable;
    private final DataVersion worldVersion;
    private final int protocolVersion;
    private final int resourcePackVersion;
    private final int dataPackVersion;
    private final Date buildTime;

    private DetectedVersion() {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.name = "1.19.4";
        this.stable = true;
        this.worldVersion = new DataVersion(3337, "main");
        this.protocolVersion = SharedConstants.getProtocolVersion();
        this.resourcePackVersion = 13;
        this.dataPackVersion = 12;
        this.buildTime = new Date();
    }

    private DetectedVersion(JsonObject jsonObject) {
        this.id = GsonHelper.getAsString(jsonObject, "id");
        this.name = GsonHelper.getAsString(jsonObject, "name");
        this.stable = GsonHelper.getAsBoolean(jsonObject, "stable");
        this.worldVersion = new DataVersion(GsonHelper.getAsInt(jsonObject, "world_version"), GsonHelper.getAsString(jsonObject, "series_id", DataVersion.MAIN_SERIES));
        this.protocolVersion = GsonHelper.getAsInt(jsonObject, "protocol_version");
        JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "pack_version");
        this.resourcePackVersion = GsonHelper.getAsInt(jsonObject2, "resource");
        this.dataPackVersion = GsonHelper.getAsInt(jsonObject2, "data");
        this.buildTime = Date.from(ZonedDateTime.parse(GsonHelper.getAsString(jsonObject, "build_time")).toInstant());
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static WorldVersion tryDetectVersion() {
        try (InputStream inputStream = DetectedVersion.class.getResourceAsStream("/version.json");){
            DetectedVersion detectedVersion;
            if (inputStream == null) {
                LOGGER.warn("Missing version information!");
                WorldVersion worldVersion = BUILT_IN;
                return worldVersion;
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
    public DataVersion getDataVersion() {
        return this.worldVersion;
    }

    @Override
    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    @Override
    public int getPackVersion(PackType packType) {
        return packType == PackType.SERVER_DATA ? this.dataPackVersion : this.resourcePackVersion;
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

