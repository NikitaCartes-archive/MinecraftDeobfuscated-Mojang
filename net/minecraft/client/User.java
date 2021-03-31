/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class User {
    private final String name;
    private final String uuid;
    private final String accessToken;
    private final Type type;

    public User(String string, String string2, String string3, String string4) {
        this.name = string;
        this.uuid = string2;
        this.accessToken = string3;
        this.type = Type.byName(string4);
    }

    public String getSessionId() {
        return "token:" + this.accessToken + ":" + this.uuid;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public GameProfile getGameProfile() {
        try {
            UUID uUID = UUIDTypeAdapter.fromString(this.getUuid());
            return new GameProfile(uUID, this.getName());
        } catch (IllegalArgumentException illegalArgumentException) {
            return new GameProfile(null, this.getName());
        }
    }

    public Type getType() {
        return this.type;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        LEGACY("legacy"),
        MOJANG("mojang");

        private static final Map<String, Type> BY_NAME;
        private final String name;

        private Type(String string2) {
            this.name = string2;
        }

        @Nullable
        public static Type byName(String string) {
            return BY_NAME.get(string.toLowerCase(Locale.ROOT));
        }

        static {
            BY_NAME = Arrays.stream(Type.values()).collect(Collectors.toMap(type -> type.name, Function.identity()));
        }
    }
}

