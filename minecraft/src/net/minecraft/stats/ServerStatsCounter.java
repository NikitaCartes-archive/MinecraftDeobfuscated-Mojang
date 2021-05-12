package net.minecraft.stats;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerStatsCounter extends StatsCounter {
	private static final Logger LOGGER = LogManager.getLogger();
	private final MinecraftServer server;
	private final File file;
	private final Set<Stat<?>> dirty = Sets.<Stat<?>>newHashSet();

	public ServerStatsCounter(MinecraftServer minecraftServer, File file) {
		this.server = minecraftServer;
		this.file = file;
		if (file.isFile()) {
			try {
				this.parseLocal(minecraftServer.getFixerUpper(), FileUtils.readFileToString(file));
			} catch (IOException var4) {
				LOGGER.error("Couldn't read statistics file {}", file, var4);
			} catch (JsonParseException var5) {
				LOGGER.error("Couldn't parse statistics file {}", file, var5);
			}
		}
	}

	public void save() {
		try {
			FileUtils.writeStringToFile(this.file, this.toJson());
		} catch (IOException var2) {
			LOGGER.error("Couldn't save stats", (Throwable)var2);
		}
	}

	@Override
	public void setValue(Player player, Stat<?> stat, int i) {
		super.setValue(player, stat, i);
		this.dirty.add(stat);
	}

	private Set<Stat<?>> getDirty() {
		Set<Stat<?>> set = Sets.<Stat<?>>newHashSet(this.dirty);
		this.dirty.clear();
		return set;
	}

	public void parseLocal(DataFixer dataFixer, String string) {
		try {
			JsonReader jsonReader = new JsonReader(new StringReader(string));

			label51: {
				try {
					jsonReader.setLenient(false);
					JsonElement jsonElement = Streams.parse(jsonReader);
					if (!jsonElement.isJsonNull()) {
						CompoundTag compoundTag = fromJson(jsonElement.getAsJsonObject());
						if (!compoundTag.contains("DataVersion", 99)) {
							compoundTag.putInt("DataVersion", 1343);
						}

						compoundTag = NbtUtils.update(dataFixer, DataFixTypes.STATS, compoundTag, compoundTag.getInt("DataVersion"));
						if (!compoundTag.contains("stats", 10)) {
							break label51;
						}

						CompoundTag compoundTag2 = compoundTag.getCompound("stats");
						Iterator var7 = compoundTag2.getAllKeys().iterator();

						while (true) {
							if (!var7.hasNext()) {
								break label51;
							}

							String string2 = (String)var7.next();
							if (compoundTag2.contains(string2, 10)) {
								Util.ifElse(
									Registry.STAT_TYPE.getOptional(new ResourceLocation(string2)),
									statType -> {
										CompoundTag compoundTag2x = compoundTag2.getCompound(string2);

										for (String string2x : compoundTag2x.getAllKeys()) {
											if (compoundTag2x.contains(string2x, 99)) {
												Util.ifElse(
													this.getStat(statType, string2x),
													stat -> this.stats.put(stat, compoundTag2x.getInt(string2x)),
													() -> LOGGER.warn("Invalid statistic in {}: Don't know what {} is", this.file, string2x)
												);
											} else {
												LOGGER.warn("Invalid statistic value in {}: Don't know what {} is for key {}", this.file, compoundTag2x.get(string2x), string2x);
											}
										}
									},
									() -> LOGGER.warn("Invalid statistic type in {}: Don't know what {} is", this.file, string2)
								);
							}
						}
					}

					LOGGER.error("Unable to parse Stat data from {}", this.file);
				} catch (Throwable var10) {
					try {
						jsonReader.close();
					} catch (Throwable var9) {
						var10.addSuppressed(var9);
					}

					throw var10;
				}

				jsonReader.close();
				return;
			}

			jsonReader.close();
		} catch (IOException | JsonParseException var11) {
			LOGGER.error("Unable to parse Stat data from {}", this.file, var11);
		}
	}

	private <T> Optional<Stat<T>> getStat(StatType<T> statType, String string) {
		return Optional.ofNullable(ResourceLocation.tryParse(string)).flatMap(statType.getRegistry()::getOptional).map(statType::get);
	}

	private static CompoundTag fromJson(JsonObject jsonObject) {
		CompoundTag compoundTag = new CompoundTag();

		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			JsonElement jsonElement = (JsonElement)entry.getValue();
			if (jsonElement.isJsonObject()) {
				compoundTag.put((String)entry.getKey(), fromJson(jsonElement.getAsJsonObject()));
			} else if (jsonElement.isJsonPrimitive()) {
				JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
				if (jsonPrimitive.isNumber()) {
					compoundTag.putInt((String)entry.getKey(), jsonPrimitive.getAsInt());
				}
			}
		}

		return compoundTag;
	}

	protected String toJson() {
		Map<StatType<?>, JsonObject> map = Maps.<StatType<?>, JsonObject>newHashMap();

		for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Stat<?>> entry : this.stats.object2IntEntrySet()) {
			Stat<?> stat = (Stat<?>)entry.getKey();
			((JsonObject)map.computeIfAbsent(stat.getType(), statType -> new JsonObject())).addProperty(getKey(stat).toString(), entry.getIntValue());
		}

		JsonObject jsonObject = new JsonObject();

		for (Entry<StatType<?>, JsonObject> entry2 : map.entrySet()) {
			jsonObject.add(Registry.STAT_TYPE.getKey((StatType<?>)entry2.getKey()).toString(), (JsonElement)entry2.getValue());
		}

		JsonObject jsonObject2 = new JsonObject();
		jsonObject2.add("stats", jsonObject);
		jsonObject2.addProperty("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
		return jsonObject2.toString();
	}

	private static <T> ResourceLocation getKey(Stat<T> stat) {
		return stat.getType().getRegistry().getKey(stat.getValue());
	}

	public void markAllDirty() {
		this.dirty.addAll(this.stats.keySet());
	}

	public void sendStats(ServerPlayer serverPlayer) {
		Object2IntMap<Stat<?>> object2IntMap = new Object2IntOpenHashMap<>();

		for (Stat<?> stat : this.getDirty()) {
			object2IntMap.put(stat, this.getValue(stat));
		}

		serverPlayer.connection.send(new ClientboundAwardStatsPacket(object2IntMap));
	}
}
