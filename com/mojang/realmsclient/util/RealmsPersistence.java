/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;

@Environment(value=EnvType.CLIENT)
public class RealmsPersistence {
    private static final GuardedSerializer GSON = new GuardedSerializer();

    public static RealmsPersistenceData readFile() {
        File file = RealmsPersistence.getPathToData();
        try {
            return GSON.fromJson(FileUtils.readFileToString(file, StandardCharsets.UTF_8), RealmsPersistenceData.class);
        } catch (IOException iOException) {
            return new RealmsPersistenceData();
        }
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
        return new File(Minecraft.getInstance().gameDirectory, "realms_persistence.json");
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

