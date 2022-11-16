package net.minecraft.client.telemetry;

import com.mojang.authlib.minecraft.TelemetryPropertyContainer;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

@Environment(EnvType.CLIENT)
public record TelemetryProperty<T>(String id, String exportKey, Codec<T> codec, TelemetryProperty.Exporter<T> exporter) {
	private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
	public static final TelemetryProperty<String> USER_ID = string("user_id", "userId");
	public static final TelemetryProperty<String> CLIENT_ID = string("client_id", "clientId");
	public static final TelemetryProperty<UUID> MINECRAFT_SESSION_ID = uuid("minecraft_session_id", "deviceSessionId");
	public static final TelemetryProperty<String> GAME_VERSION = string("game_version", "buildDisplayName");
	public static final TelemetryProperty<String> OPERATING_SYSTEM = string("operating_system", "buildPlatform");
	public static final TelemetryProperty<String> PLATFORM = string("platform", "platform");
	public static final TelemetryProperty<Boolean> CLIENT_MODDED = bool("client_modded", "clientModded");
	public static final TelemetryProperty<UUID> WORLD_SESSION_ID = uuid("world_session_id", "worldSessionId");
	public static final TelemetryProperty<Boolean> SERVER_MODDED = bool("server_modded", "serverModded");
	public static final TelemetryProperty<TelemetryProperty.ServerType> SERVER_TYPE = create(
		"server_type",
		"serverType",
		TelemetryProperty.ServerType.CODEC,
		(telemetryPropertyContainer, string, serverType) -> telemetryPropertyContainer.addProperty(string, serverType.getSerializedName())
	);
	public static final TelemetryProperty<Boolean> OPT_IN = bool("opt_in", "isOptional");
	public static final TelemetryProperty<Instant> EVENT_TIMESTAMP_UTC = create(
		"event_timestamp_utc",
		"eventTimestampUtc",
		ExtraCodecs.INSTANT_ISO8601,
		(telemetryPropertyContainer, string, instant) -> telemetryPropertyContainer.addProperty(string, TIMESTAMP_FORMATTER.format(instant))
	);
	public static final TelemetryProperty<TelemetryProperty.GameMode> GAME_MODE = create(
		"game_mode",
		"playerGameMode",
		TelemetryProperty.GameMode.CODEC,
		(telemetryPropertyContainer, string, gameMode) -> telemetryPropertyContainer.addProperty(string, gameMode.id())
	);
	public static final TelemetryProperty<Integer> SECONDS_SINCE_LOAD = integer("seconds_since_load", "secondsSinceLoad");
	public static final TelemetryProperty<Integer> TICKS_SINCE_LOAD = integer("ticks_since_load", "ticksSinceLoad");
	public static final TelemetryProperty<LongList> FRAME_RATE_SAMPLES = longSamples("frame_rate_samples", "serializedFpsSamples");
	public static final TelemetryProperty<LongList> RENDER_TIME_SAMPLES = longSamples("render_time_samples", "serializedRenderTimeSamples");
	public static final TelemetryProperty<LongList> USED_MEMORY_SAMPLES = longSamples("used_memory_samples", "serializedUsedMemoryKbSamples");
	public static final TelemetryProperty<Integer> NUMBER_OF_SAMPLES = integer("number_of_samples", "numSamples");
	public static final TelemetryProperty<Integer> RENDER_DISTANCE = integer("render_distance", "renderDistance");
	public static final TelemetryProperty<Integer> DEDICATED_MEMORY_KB = integer("dedicated_memory_kb", "dedicatedMemoryKb");
	public static final TelemetryProperty<Integer> WORLD_LOAD_TIME_MS = integer("world_load_time_ms", "worldLoadTimeMs");
	public static final TelemetryProperty<Boolean> NEW_WORLD = bool("new_world", "newWorld");

	public static <T> TelemetryProperty<T> create(String string, String string2, Codec<T> codec, TelemetryProperty.Exporter<T> exporter) {
		return new TelemetryProperty<>(string, string2, codec, exporter);
	}

	public static TelemetryProperty<Boolean> bool(String string, String string2) {
		return create(string, string2, Codec.BOOL, TelemetryPropertyContainer::addProperty);
	}

	public static TelemetryProperty<String> string(String string, String string2) {
		return create(string, string2, Codec.STRING, TelemetryPropertyContainer::addProperty);
	}

	public static TelemetryProperty<Integer> integer(String string, String string2) {
		return create(string, string2, Codec.INT, TelemetryPropertyContainer::addProperty);
	}

	public static TelemetryProperty<UUID> uuid(String string, String string2) {
		return create(
			string, string2, UUIDUtil.STRING_CODEC, (telemetryPropertyContainer, stringx, uUID) -> telemetryPropertyContainer.addProperty(stringx, uUID.toString())
		);
	}

	public static TelemetryProperty<LongList> longSamples(String string, String string2) {
		return create(
			string,
			string2,
			Codec.LONG.listOf().xmap(LongArrayList::new, Function.identity()),
			(telemetryPropertyContainer, stringx, longList) -> telemetryPropertyContainer.addProperty(
					stringx, (String)longList.longStream().mapToObj(String::valueOf).collect(Collectors.joining(";"))
				)
		);
	}

	public void export(TelemetryPropertyMap telemetryPropertyMap, TelemetryPropertyContainer telemetryPropertyContainer) {
		T object = telemetryPropertyMap.get(this);
		if (object != null) {
			this.exporter.apply(telemetryPropertyContainer, this.exportKey, object);
		} else {
			telemetryPropertyContainer.addNullProperty(this.exportKey);
		}
	}

	public MutableComponent title() {
		return Component.translatable("telemetry.property." + this.id + ".title");
	}

	public String toString() {
		return "TelemetryProperty[" + this.id + "]";
	}

	@Environment(EnvType.CLIENT)
	public interface Exporter<T> {
		void apply(TelemetryPropertyContainer telemetryPropertyContainer, String string, T object);
	}

	@Environment(EnvType.CLIENT)
	public static enum GameMode implements StringRepresentable {
		SURVIVAL("survival", 0),
		CREATIVE("creative", 1),
		ADVENTURE("adventure", 2),
		SPECTATOR("spectator", 6),
		HARDCORE("hardcore", 99);

		public static final Codec<TelemetryProperty.GameMode> CODEC = StringRepresentable.fromEnum(TelemetryProperty.GameMode::values);
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
	}

	@Environment(EnvType.CLIENT)
	public static enum ServerType implements StringRepresentable {
		REALM("realm"),
		LOCAL("local"),
		OTHER("server");

		public static final Codec<TelemetryProperty.ServerType> CODEC = StringRepresentable.fromEnum(TelemetryProperty.ServerType::values);
		private final String key;

		private ServerType(String string2) {
			this.key = string2;
		}

		@Override
		public String getSerializedName() {
			return this.key;
		}
	}
}
