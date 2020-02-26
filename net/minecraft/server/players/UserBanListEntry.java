/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.players.BanListEntry;
import org.jetbrains.annotations.Nullable;

public class UserBanListEntry
extends BanListEntry<GameProfile> {
    public UserBanListEntry(GameProfile gameProfile) {
        this(gameProfile, (Date)null, (String)null, (Date)null, (String)null);
    }

    public UserBanListEntry(GameProfile gameProfile, @Nullable Date date, @Nullable String string, @Nullable Date date2, @Nullable String string2) {
        super(gameProfile, date, string, date2, string2);
    }

    public UserBanListEntry(JsonObject jsonObject) {
        super(UserBanListEntry.createGameProfile(jsonObject), jsonObject);
    }

    @Override
    public Component getDisplayName() {
        GameProfile gameProfile = (GameProfile)this.getUser();
        return new TextComponent(gameProfile.getName() != null ? gameProfile.getName() : Objects.toString(gameProfile.getId(), "(Unknown)"));
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

