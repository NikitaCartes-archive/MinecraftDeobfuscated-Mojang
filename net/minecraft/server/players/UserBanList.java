/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.StoredUserList;
import net.minecraft.server.players.UserBanListEntry;

public class UserBanList
extends StoredUserList<GameProfile, UserBanListEntry> {
    public UserBanList(File file) {
        super(file);
    }

    @Override
    protected StoredUserEntry<GameProfile> createEntry(JsonObject jsonObject) {
        return new UserBanListEntry(jsonObject);
    }

    public boolean isBanned(GameProfile gameProfile) {
        return this.contains(gameProfile);
    }

    @Override
    public String[] getUserList() {
        String[] strings = new String[this.getEntries().size()];
        int i = 0;
        for (StoredUserEntry storedUserEntry : this.getEntries()) {
            strings[i++] = ((GameProfile)storedUserEntry.getUser()).getName();
        }
        return strings;
    }

    @Override
    protected String getKeyForUser(GameProfile gameProfile) {
        return gameProfile.getId().toString();
    }

    @Override
    protected /* synthetic */ String getKeyForUser(Object object) {
        return this.getKeyForUser((GameProfile)object);
    }
}

