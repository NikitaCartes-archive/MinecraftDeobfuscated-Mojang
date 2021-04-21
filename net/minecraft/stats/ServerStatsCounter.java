/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerStatsCounter
extends StatsCounter {
    private static final Logger LOGGER = LogManager.getLogger();
    private final MinecraftServer server;
    private final File file;
    private final Set<Stat<?>> dirty = Sets.newHashSet();

    public ServerStatsCounter(MinecraftServer minecraftServer, File file) {
        this.server = minecraftServer;
        this.file = file;
        if (file.isFile()) {
            try {
                this.parseLocal(minecraftServer.getFixerUpper(), FileUtils.readFileToString(file));
            } catch (IOException iOException) {
                LOGGER.error("Couldn't read statistics file {}", (Object)file, (Object)iOException);
            } catch (JsonParseException jsonParseException) {
                LOGGER.error("Couldn't parse statistics file {}", (Object)file, (Object)jsonParseException);
            }
        }
    }

    public void save() {
        try {
            FileUtils.writeStringToFile(this.file, this.toJson());
        } catch (IOException iOException) {
            LOGGER.error("Couldn't save stats", (Throwable)iOException);
        }
    }

    @Override
    public void setValue(Player player, Stat<?> stat, int i) {
        super.setValue(player, stat, i);
        this.dirty.add(stat);
    }

    private Set<Stat<?>> getDirty() {
        HashSet<Stat<?>> set = Sets.newHashSet(this.dirty);
        this.dirty.clear();
        return set;
    }

    public void parseLocal(DataFixer dataFixer, String string) {
        try (JsonReader jsonReader = new JsonReader(new StringReader(string));){
            jsonReader.setLenient(false);
            JsonElement jsonElement = Streams.parse(jsonReader);
            if (jsonElement.isJsonNull()) {
                LOGGER.error("Unable to parse Stat data from {}", (Object)this.file);
                return;
            }
            CompoundTag compoundTag = ServerStatsCounter.fromJson(jsonElement.getAsJsonObject());
            if (!compoundTag.contains("DataVersion", 99)) {
                compoundTag.putInt("DataVersion", 1343);
            }
            if ((compoundTag = NbtUtils.update(dataFixer, DataFixTypes.STATS, compoundTag, compoundTag.getInt("DataVersion"))).contains("stats", 10)) {
                CompoundTag compoundTag2 = compoundTag.getCompound("stats");
                for (String string2 : compoundTag2.getAllKeys()) {
                    if (!compoundTag2.contains(string2, 10)) continue;
                    Util.ifElse(Registry.STAT_TYPE.getOptional(new ResourceLocation(string2)), statType -> {
                        CompoundTag compoundTag2 = compoundTag2.getCompound(string2);
                        for (String string2 : compoundTag2.getAllKeys()) {
                            if (compoundTag2.contains(string2, 99)) {
                                Util.ifElse(this.getStat((StatType)statType, string2), stat -> this.stats.put(stat, compoundTag2.getInt(string2)), () -> LOGGER.warn("Invalid statistic in {}: Don't know what {} is", (Object)this.file, (Object)string2));
                                continue;
                            }
                            LOGGER.warn("Invalid statistic value in {}: Don't know what {} is for key {}", (Object)this.file, (Object)compoundTag2.get(string2), (Object)string2);
                        }
                    }, () -> LOGGER.warn("Invalid statistic type in {}: Don't know what {} is", (Object)this.file, (Object)string2));
                }
            }
        } catch (JsonParseException | IOException exception) {
            LOGGER.error("Unable to parse Stat data from {}", (Object)this.file, (Object)exception);
        }
    }

    private <T> Optional<Stat<T>> getStat(StatType<T> statType, String string) {
        return Optional.ofNullable(ResourceLocation.tryParse(string)).flatMap(statType.getRegistry()::getOptional).map(statType::get);
    }

    private static CompoundTag fromJson(JsonObject jsonObject) {
        CompoundTag compoundTag = new CompoundTag();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonPrimitive jsonPrimitive;
            JsonElement jsonElement = entry.getValue();
            if (jsonElement.isJsonObject()) {
                compoundTag.put(entry.getKey(), ServerStatsCounter.fromJson(jsonElement.getAsJsonObject()));
                continue;
            }
            if (!jsonElement.isJsonPrimitive() || !(jsonPrimitive = jsonElement.getAsJsonPrimitive()).isNumber()) continue;
            compoundTag.putInt(entry.getKey(), jsonPrimitive.getAsInt());
        }
        return compoundTag;
    }

    protected String toJson() {
        HashMap<StatType, JsonObject> map = Maps.newHashMap();
        for (Object2IntMap.Entry entry : this.stats.object2IntEntrySet()) {
            Stat stat = (Stat)entry.getKey();
            map.computeIfAbsent(stat.getType(), statType -> new JsonObject()).addProperty(ServerStatsCounter.getKey(stat).toString(), entry.getIntValue());
        }
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry entry : map.entrySet()) {
            jsonObject.add(Registry.STAT_TYPE.getKey((StatType<?>)entry.getKey()).toString(), (JsonElement)entry.getValue());
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
        Object2IntOpenHashMap object2IntMap = new Object2IntOpenHashMap();
        for (Stat<?> stat : this.getDirty()) {
            object2IntMap.put(stat, this.getValue(stat));
        }
        serverPlayer.connection.send(new ClientboundAwardStatsPacket(object2IntMap));
    }
}

