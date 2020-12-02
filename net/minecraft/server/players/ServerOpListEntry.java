/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.server.players.StoredUserEntry;
import org.jetbrains.annotations.Nullable;

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

    @Override
    protected void serialize(JsonObject jsonObject) {
        if (this.getUser() == null) {
            return;
        }
        jsonObject.addProperty("uuid", ((GameProfile)this.getUser()).getId() == null ? "" : ((GameProfile)this.getUser()).getId().toString());
        jsonObject.addProperty("name", ((GameProfile)this.getUser()).getName());
        jsonObject.addProperty("level", this.level);
        jsonObject.addProperty("bypassesPlayerLimit", this.bypassesPlayerLimit);
    }

    @Nullable
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

