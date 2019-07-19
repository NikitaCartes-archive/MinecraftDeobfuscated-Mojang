/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import org.apache.commons.io.FileUtils;

@Environment(value=EnvType.CLIENT)
public class RealmsPersistence {
    public static RealmsPersistenceData readFile() {
        File file = new File(Realms.getGameDirectoryPath(), "realms_persistence.json");
        Gson gson = new Gson();
        try {
            return gson.fromJson(FileUtils.readFileToString(file), RealmsPersistenceData.class);
        } catch (IOException iOException) {
            return new RealmsPersistenceData();
        }
    }

    public static void writeFile(RealmsPersistenceData realmsPersistenceData) {
        File file = new File(Realms.getGameDirectoryPath(), "realms_persistence.json");
        Gson gson = new Gson();
        String string = gson.toJson(realmsPersistenceData);
        try {
            FileUtils.writeStringToFile(file, string);
        } catch (IOException iOException) {
            // empty catch block
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class RealmsPersistenceData {
        public String newsLink;
        public boolean hasUnreadNews = false;

        private RealmsPersistenceData() {
        }
    }
}

