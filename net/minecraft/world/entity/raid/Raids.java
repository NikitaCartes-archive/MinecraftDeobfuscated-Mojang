/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.raid;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Raids
extends SavedData {
    private static final String RAID_FILE_ID = "raids";
    private final Map<Integer, Raid> raidMap = Maps.newHashMap();
    private final ServerLevel level;
    private int nextAvailableID;
    private int tick;

    public Raids(ServerLevel serverLevel) {
        this.level = serverLevel;
        this.nextAvailableID = 1;
        this.setDirty();
    }

    public Raid get(int i) {
        return this.raidMap.get(i);
    }

    public void tick() {
        ++this.tick;
        Iterator<Raid> iterator = this.raidMap.values().iterator();
        while (iterator.hasNext()) {
            Raid raid = iterator.next();
            if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
                raid.stop();
            }
            if (raid.isStopped()) {
                iterator.remove();
                this.setDirty();
                continue;
            }
            raid.tick();
        }
        if (this.tick % 200 == 0) {
            this.setDirty();
        }
        DebugPackets.sendRaids(this.level, this.raidMap.values());
    }

    public static boolean canJoinRaid(Raider raider, Raid raid) {
        if (raider != null && raid != null && raid.getLevel() != null) {
            return raider.isAlive() && raider.canJoinRaid() && raider.getNoActionTime() <= 2400 && raider.level.dimensionType() == raid.getLevel().dimensionType();
        }
        return false;
    }

    @Nullable
    public Raid createOrExtendRaid(ServerPlayer serverPlayer) {
        BlockPos blockPos3;
        if (serverPlayer.isSpectator()) {
            return null;
        }
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
            return null;
        }
        DimensionType dimensionType = serverPlayer.level.dimensionType();
        if (!dimensionType.hasRaids()) {
            return null;
        }
        BlockPos blockPos = serverPlayer.blockPosition();
        List<PoiRecord> list = this.level.getPoiManager().getInRange(holder -> holder.is(PoiTypeTags.VILLAGE), blockPos, 64, PoiManager.Occupancy.IS_OCCUPIED).toList();
        int i = 0;
        Vec3 vec3 = Vec3.ZERO;
        for (PoiRecord poiRecord : list) {
            BlockPos blockPos2 = poiRecord.getPos();
            vec3 = vec3.add(blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
            ++i;
        }
        if (i > 0) {
            vec3 = vec3.scale(1.0 / (double)i);
            blockPos3 = BlockPos.containing(vec3);
        } else {
            blockPos3 = blockPos;
        }
        Raid raid = this.getOrCreateRaid(serverPlayer.getLevel(), blockPos3);
        boolean bl = false;
        if (!raid.isStarted()) {
            if (!this.raidMap.containsKey(raid.getId())) {
                this.raidMap.put(raid.getId(), raid);
            }
            bl = true;
        } else if (raid.getBadOmenLevel() < raid.getMaxBadOmenLevel()) {
            bl = true;
        } else {
            serverPlayer.removeEffect(MobEffects.BAD_OMEN);
            serverPlayer.connection.send(new ClientboundEntityEventPacket(serverPlayer, 43));
        }
        if (bl) {
            raid.absorbBadOmen(serverPlayer);
            serverPlayer.connection.send(new ClientboundEntityEventPacket(serverPlayer, 43));
            if (!raid.hasFirstWaveSpawned()) {
                serverPlayer.awardStat(Stats.RAID_TRIGGER);
                CriteriaTriggers.BAD_OMEN.trigger(serverPlayer);
            }
        }
        this.setDirty();
        return raid;
    }

    private Raid getOrCreateRaid(ServerLevel serverLevel, BlockPos blockPos) {
        Raid raid = serverLevel.getRaidAt(blockPos);
        return raid != null ? raid : new Raid(this.getUniqueId(), serverLevel, blockPos);
    }

    public static Raids load(ServerLevel serverLevel, CompoundTag compoundTag) {
        Raids raids = new Raids(serverLevel);
        raids.nextAvailableID = compoundTag.getInt("NextAvailableID");
        raids.tick = compoundTag.getInt("Tick");
        ListTag listTag = compoundTag.getList("Raids", 10);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag2 = listTag.getCompound(i);
            Raid raid = new Raid(serverLevel, compoundTag2);
            raids.raidMap.put(raid.getId(), raid);
        }
        return raids;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putInt("NextAvailableID", this.nextAvailableID);
        compoundTag.putInt("Tick", this.tick);
        ListTag listTag = new ListTag();
        for (Raid raid : this.raidMap.values()) {
            CompoundTag compoundTag2 = new CompoundTag();
            raid.save(compoundTag2);
            listTag.add(compoundTag2);
        }
        compoundTag.put("Raids", listTag);
        return compoundTag;
    }

    public static String getFileId(Holder<DimensionType> holder) {
        if (holder.is(BuiltinDimensionTypes.END)) {
            return "raids_end";
        }
        return RAID_FILE_ID;
    }

    private int getUniqueId() {
        return ++this.nextAvailableID;
    }

    @Nullable
    public Raid getNearbyRaid(BlockPos blockPos, int i) {
        Raid raid = null;
        double d = i;
        for (Raid raid2 : this.raidMap.values()) {
            double e = raid2.getCenter().distSqr(blockPos);
            if (!raid2.isActive() || !(e < d)) continue;
            raid = raid2;
            d = e;
        }
        return raid;
    }
}

