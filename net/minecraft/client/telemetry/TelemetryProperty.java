/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.telemetry;

import com.mojang.authlib.minecraft.TelemetryPropertyContainer;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.telemetry.TelemetryPropertyMap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

@Environment(value=EnvType.CLIENT)
public record TelemetryProperty<T>(String id, String exportKey, Codec<T> codec, Exporter<T> exporter) {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
    public static final TelemetryProperty<String> USER_ID = TelemetryProperty.string("user_id", "userId");
    public static final TelemetryProperty<String> CLIENT_ID = TelemetryProperty.string("client_id", "clientId");
    public static final TelemetryProperty<UUID> MINECRAFT_SESSION_ID = TelemetryProperty.uuid("minecraft_session_id", "deviceSessionId");
    public static final TelemetryProperty<String> GAME_VERSION = TelemetryProperty.string("game_version", "buildDisplayName");
    public static final TelemetryProperty<String> OPERATING_SYSTEM = TelemetryProperty.string("operating_system", "buildPlatform");
    public static final TelemetryProperty<String> PLATFORM = TelemetryProperty.string("platform", "platform");
    public static final TelemetryProperty<Boolean> CLIENT_MODDED = TelemetryProperty.bool("client_modded", "clientModded");
    public static final TelemetryProperty<UUID> WORLD_SESSION_ID = TelemetryProperty.uuid("world_session_id", "worldSessionId");
    public static final TelemetryProperty<Boolean> SERVER_MODDED = TelemetryProperty.bool("server_modded", "serverModded");
    public static final TelemetryProperty<ServerType> SERVER_TYPE = TelemetryProperty.create("server_type", "serverType", ServerType.CODEC, (telemetryPropertyContainer, string, serverType) -> telemetryPropertyContainer.addProperty(string, serverType.getSerializedName()));
    public static final TelemetryProperty<Boolean> OPT_IN = TelemetryProperty.bool("opt_in", "isOptional");
    public static final TelemetryProperty<Instant> EVENT_TIMESTAMP_UTC = TelemetryProperty.create("event_timestamp_utc", "eventTimestampUtc", ExtraCodecs.INSTANT_ISO8601, (telemetryPropertyContainer, string, instant) -> telemetryPropertyContainer.addProperty(string, TIMESTAMP_FORMATTER.format((TemporalAccessor)instant)));
    public static final TelemetryProperty<GameMode> GAME_MODE = TelemetryProperty.create("game_mode", "playerGameMode", GameMode.CODEC, (telemetryPropertyContainer, string, gameMode) -> telemetryPropertyContainer.addProperty(string, gameMode.id()));
    public static final TelemetryProperty<Integer> SECONDS_SINCE_LOAD = TelemetryProperty.integer("seconds_since_load", "secondsSinceLoad");
    public static final TelemetryProperty<Integer> TICKS_SINCE_LOAD = TelemetryProperty.integer("ticks_since_load", "ticksSinceLoad");
    public static final TelemetryProperty<LongList> FRAME_RATE_SAMPLES = TelemetryProperty.longSamples("frame_rate_samples", "serializedFpsSamples");
    public static final TelemetryProperty<LongList> RENDER_TIME_SAMPLES = TelemetryProperty.longSamples("render_time_samples", "serializedRenderTimeSamples");
    public static final TelemetryProperty<LongList> USED_MEMORY_SAMPLES = TelemetryProperty.longSamples("used_memory_samples", "serializedUsedMemoryKbSamples");
    public static final TelemetryProperty<Integer> NUMBER_OF_SAMPLES = TelemetryProperty.integer("number_of_samples", "numSamples");
    public static final TelemetryProperty<Integer> RENDER_DISTANCE = TelemetryProperty.integer("render_distance", "renderDistance");
    public static final TelemetryProperty<Integer> DEDICATED_MEMORY_KB = TelemetryProperty.integer("dedicated_memory_kb", "dedicatedMemoryKb");
    public static final TelemetryProperty<Integer> WORLD_LOAD_TIME_MS = TelemetryProperty.integer("world_load_time_ms", "worldLoadTimeMs");
    public static final TelemetryProperty<Boolean> NEW_WORLD = TelemetryProperty.bool("new_world", "newWorld");

    public static <T> TelemetryProperty<T> create(String string, String string2, Codec<T> codec, Exporter<T> exporter) {
        return new TelemetryProperty<T>(string, string2, codec, exporter);
    }

    public static TelemetryProperty<Boolean> bool(String string, String string2) {
        return TelemetryProperty.create(string, string2, Codec.BOOL, TelemetryPropertyContainer::addProperty);
    }

    public static TelemetryProperty<String> string(String string, String string2) {
        return TelemetryProperty.create(string, string2, Codec.STRING, TelemetryPropertyContainer::addProperty);
    }

    public static TelemetryProperty<Integer> integer(String string, String string2) {
        return TelemetryProperty.create(string, string2, Codec.INT, TelemetryPropertyContainer::addProperty);
    }

    public static TelemetryProperty<UUID> uuid(String string2, String string22) {
        return TelemetryProperty.create(string2, string22, UUIDUtil.STRING_CODEC, (telemetryPropertyContainer, string, uUID) -> telemetryPropertyContainer.addProperty(string, uUID.toString()));
    }

    public static TelemetryProperty<LongList> longSamples(String string2, String string22) {
        return TelemetryProperty.create(string2, string22, Codec.LONG.listOf().xmap(LongArrayList::new, Function.identity()), (telemetryPropertyContainer, string, longList) -> telemetryPropertyContainer.addProperty(string, longList.longStream().mapToObj(String::valueOf).collect(Collectors.joining(";"))));
    }

    public void export(TelemetryPropertyMap telemetryPropertyMap, TelemetryPropertyContainer telemetryPropertyContainer) {
        Object object = telemetryPropertyMap.get(this);
        if (object != null) {
            this.exporter.apply(telemetryPropertyContainer, this.exportKey, object);
        } else {
            telemetryPropertyContainer.addNullProperty(this.exportKey);
        }
    }

    public MutableComponent title() {
        return Component.translatable("telemetry.property." + this.id + ".title");
    }

    @Override
    public String toString() {
        return "TelemetryProperty[" + this.id + "]";
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Exporter<T> {
        public void apply(TelemetryPropertyContainer var1, String var2, T var3);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum GameMode implements StringRepresentable
    {
        SURVIVAL("survival", 0),
        CREATIVE("creative", 1),
        ADVENTURE("adventure", 2),
        SPECTATOR("spectator", 6),
        HARDCORE("hardcore", 99);

        public static final Codec<GameMode> CODEC;
        private final String key;
        private final int id;

        private GameMode(String string2, int j) {
            this.key = string2;
            this.id = j;
        }

        public int id() {
            return this.id;
        }

        @Override
        public String getSerializedName() {
            return this.key;
        }

        static {
            CODEC = StringRepresentable.fromEnum(GameMode::values);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ServerType implements StringRepresentable
    {
        REALM("realm"),
        LOCAL("local"),
        OTHER("server");

        public static final Codec<ServerType> CODEC;
        private final String key;

        private ServerType(String string2) {
            this.key = string2;
        }

        @Override
        public String getSerializedName() {
            return this.key;
        }

        static {
            CODEC = StringRepresentable.fromEnum(ServerType::values);
        }
    }
}

