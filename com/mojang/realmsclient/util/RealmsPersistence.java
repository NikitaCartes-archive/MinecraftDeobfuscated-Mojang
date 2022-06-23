/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util;

import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsPersistence {
    private static final String FILE_NAME = "realms_persistence.json";
    private static final GuardedSerializer GSON = new GuardedSerializer();
    private static final Logger LOGGER = LogUtils.getLogger();

    public RealmsPersistenceData read() {
        return RealmsPersistence.readFile();
    }

    public void save(RealmsPersistenceData realmsPersistenceData) {
        RealmsPersistence.writeFile(realmsPersistenceData);
    }

    public static RealmsPersistenceData readFile() {
        File file = RealmsPersistence.getPathToData();
        try {
            String string = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            RealmsPersistenceData realmsPersistenceData = GSON.fromJson(string, RealmsPersistenceData.class);
            if (realmsPersistenceData != null) {
                return realmsPersistenceData;
            }
        } catch (FileNotFoundException string) {
        } catch (Exception exception) {
            LOGGER.warn("Failed to read Realms storage {}", (Object)file, (Object)exception);
        }
        return new RealmsPersistenceData();
    }

    public static void writeFile(RealmsPersistenceData realmsPersistenceData) {
        File file = RealmsPersistence.getPathToData();
        try {
            FileUtils.writeStringToFile(file, GSON.toJson(realmsPersistenceData), StandardCharsets.UTF_8);
        } catch (IOException iOException) {
            // empty catch block
        }
    }

    private static File getPathToData() {
        return new File(Minecraft.getInstance().gameDirectory, FILE_NAME);
    }

    @Environment(value=EnvType.CLIENT)
    public static class RealmsPersistenceData
    implements ReflectionBasedSerialization {
        @SerializedName(value="newsLink")
        public String newsLink;
        @SerializedName(value="hasUnreadNews")
        public boolean hasUnreadNews;
    }
}

