/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Objects;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.StoredUserList;

public class ServerOpList
extends StoredUserList<GameProfile, ServerOpListEntry> {
    public ServerOpList(File file) {
        super(file);
    }

    @Override
    protected StoredUserEntry<GameProfile> createEntry(JsonObject jsonObject) {
        return new ServerOpListEntry(jsonObject);
    }

    @Override
    public String[] getUserList() {
        return (String[])this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(GameProfile::getName).toArray(String[]::new);
    }

    public boolean canBypassPlayerLimit(GameProfile gameProfile) {
        ServerOpListEntry serverOpListEntry = (ServerOpListEntry)this.get(gameProfile);
        if (serverOpListEntry != null) {
            return serverOpListEntry.getBypassesPlayerLimit();
        }
        return false;
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

