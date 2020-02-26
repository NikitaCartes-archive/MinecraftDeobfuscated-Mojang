/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.server.players.StoredUserEntry;

public class ServerOpListEntry
extends StoredUserEntry<GameProfile> {
    private final int level;
    private final boolean bypassesPlayerLimit;

    public ServerOpListEntry(GameProfile gameProfile, int i, boolean bl) {
        super(gameProfile);
        this.level = i;
        this.bypassesPlayerLimit = bl;
    }

    public ServerOpListEntry(JsonObject jsonObject) {
        super(ServerOpListEntry.createGameProfile(jsonObject));
        this.level = jsonObject.has("level") ? jsonObject.get("level").getAsInt() : 0;
        this.bypassesPlayerLimit = jsonObject.has("bypassesPlayerLimit") && jsonObject.get("bypassesPlayerLimit").getAsBoolean();
    }

    public int getLevel() {
        return this.level;
    }

    public boolean getBypassesPlayerLimit() {
        return this.bypassesPlayerLimit;
    }

    private static GameProfile createGameProfile(JsonObject jsonObject) {
        UUID uUID;
        if (!jsonObject.has("uuid") || !jsonObject.has("name")) {
            return null;
        }
        String string = jsonObject.get("uuid").getAsString();
        try {
            uUID = UUID.fromString(string);
        } catch (Throwable throwable) {
            return null;
        }
        return new GameProfile(uUID, jsonObject.get("name").getAsString());
    }
}

