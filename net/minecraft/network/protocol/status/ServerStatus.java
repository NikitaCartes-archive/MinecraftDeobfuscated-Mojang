/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.status;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

public record ServerStatus(Component description, Optional<Players> players, Optional<Version> version, Optional<Favicon> favicon, boolean enforcesSecureChat) {
    public static final Codec<ServerStatus> CODEC = RecordCodecBuilder.create(instance -> instance.group(ExtraCodecs.COMPONENT.optionalFieldOf("description", CommonComponents.EMPTY).forGetter(ServerStatus::description), Players.CODEC.optionalFieldOf("players").forGetter(ServerStatus::players), Version.CODEC.optionalFieldOf("version").forGetter(ServerStatus::version), Favicon.CODEC.optionalFieldOf("favicon").forGetter(ServerStatus::favicon), Codec.BOOL.optionalFieldOf("enforcesSecureChat", false).forGetter(ServerStatus::enforcesSecureChat)).apply((Applicative<ServerStatus, ?>)instance, ServerStatus::new));

    public record Players(int max, int online, List<GameProfile> sample) {
        private static final Codec<GameProfile> PROFILE_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)UUIDUtil.STRING_CODEC.fieldOf("id")).forGetter(GameProfile::getId), ((MapCodec)Codec.STRING.fieldOf("name")).forGetter(GameProfile::getName)).apply((Applicative<GameProfile, ?>)instance, GameProfile::new));
        public static final Codec<Players> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("max")).forGetter(Players::max), ((MapCodec)Codec.INT.fieldOf("online")).forGetter(Players::online), PROFILE_CODEC.listOf().optionalFieldOf("sample", List.of()).forGetter(Players::sample)).apply((Applicative<Players, ?>)instance, Players::new));
    }

    public record Version(String name, int protocol) {
        public static final Codec<Version> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("name")).forGetter(Version::name), ((MapCodec)Codec.INT.fieldOf("protocol")).forGetter(Version::protocol)).apply((Applicative<Version, ?>)instance, Version::new));

        public static Version current() {
            WorldVersion worldVersion = SharedConstants.getCurrentVersion();
            return new Version(worldVersion.getName(), worldVersion.getProtocolVersion());
        }
    }

    public record Favicon(byte[] iconBytes) {
        public static final int WIDTH = 64;
        public static final int HEIGHT = 64;
        private static final String PREFIX = "data:image/png;base64,";
        public static final Codec<Favicon> CODEC = Codec.STRING.comapFlatMap(string -> {
            if (!string.startsWith(PREFIX)) {
                return DataResult.error("Unknown format");
            }
            try {
                String string2 = string.substring(PREFIX.length()).replaceAll("\n", "");
                byte[] bs = Base64.getDecoder().decode(string2.getBytes(StandardCharsets.UTF_8));
                return DataResult.success(new Favicon(bs));
            } catch (IllegalArgumentException illegalArgumentException) {
                return DataResult.error("Malformed base64 server icon");
            }
        }, favicon -> PREFIX + new String(Base64.getEncoder().encode(favicon.iconBytes), StandardCharsets.UTF_8));
    }
}

