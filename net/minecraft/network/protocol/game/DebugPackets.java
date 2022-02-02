/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class DebugPackets {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void sendGameTestAddMarker(ServerLevel serverLevel, BlockPos blockPos, String string, int i, int j) {
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlyByteBuf.writeBlockPos(blockPos);
        friendlyByteBuf.writeInt(i);
        friendlyByteBuf.writeUtf(string);
        friendlyByteBuf.writeInt(j);
        DebugPackets.sendPacketToAllPlayers(serverLevel, friendlyByteBuf, ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_ADD_MARKER);
    }

    public static void sendGameTestClearPacket(ServerLevel serverLevel) {
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
        DebugPackets.sendPacketToAllPlayers(serverLevel, friendlyByteBuf, ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_CLEAR);
    }

    public static void sendPoiPacketsForChunk(ServerLevel serverLevel, ChunkPos chunkPos) {
    }

    public static void sendPoiAddedPacket(ServerLevel serverLevel, BlockPos blockPos) {
        DebugPackets.sendVillageSectionsPacket(serverLevel, blockPos);
    }

    public static void sendPoiRemovedPacket(ServerLevel serverLevel, BlockPos blockPos) {
        DebugPackets.sendVillageSectionsPacket(serverLevel, blockPos);
    }

    public static void sendPoiTicketCountPacket(ServerLevel serverLevel, BlockPos blockPos) {
        DebugPackets.sendVillageSectionsPacket(serverLevel, blockPos);
    }

    private static void sendVillageSectionsPacket(ServerLevel serverLevel, BlockPos blockPos) {
    }

    public static void sendPathFindingPacket(Level level, Mob mob, @Nullable Path path, float f) {
    }

    public static void sendNeighborsUpdatePacket(Level level, BlockPos blockPos) {
    }

    public static void sendStructurePacket(WorldGenLevel worldGenLevel, StructureStart<?> structureStart) {
    }

    public static void sendGoalSelector(Level level, Mob mob, GoalSelector goalSelector) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
    }

    public static void sendRaids(ServerLevel serverLevel, Collection<Raid> collection) {
    }

    public static void sendEntityBrain(LivingEntity livingEntity) {
    }

    public static void sendBeeInfo(Bee bee) {
    }

    public static void sendGameEventInfo(Level level, GameEvent gameEvent, BlockPos blockPos) {
    }

    public static void sendGameEventListenerInfo(Level level, GameEventListener gameEventListener) {
    }

    public static void sendHiveInfo(Level level, BlockPos blockPos, BlockState blockState, BeehiveBlockEntity beehiveBlockEntity) {
    }

    private static void writeBrain(LivingEntity livingEntity, FriendlyByteBuf friendlyByteBuf2) {
        Brain<Path> brain = livingEntity.getBrain();
        long l = livingEntity.level.getGameTime();
        if (livingEntity instanceof InventoryCarrier) {
            Container container = ((InventoryCarrier)((Object)livingEntity)).getInventory();
            friendlyByteBuf2.writeUtf(container.isEmpty() ? "" : container.toString());
        } else {
            friendlyByteBuf2.writeUtf("");
        }
        if (brain.hasMemoryValue(MemoryModuleType.PATH)) {
            friendlyByteBuf2.writeBoolean(true);
            Path path = brain.getMemory(MemoryModuleType.PATH).get();
            path.writeToStream(friendlyByteBuf2);
        } else {
            friendlyByteBuf2.writeBoolean(false);
        }
        if (livingEntity instanceof Villager) {
            Villager villager = (Villager)livingEntity;
            boolean bl = villager.wantsToSpawnGolem(l);
            friendlyByteBuf2.writeBoolean(bl);
        } else {
            friendlyByteBuf2.writeBoolean(false);
        }
        friendlyByteBuf2.writeCollection(brain.getActiveActivities(), (friendlyByteBuf, activity) -> friendlyByteBuf.writeUtf(activity.getName()));
        Set set = brain.getRunningBehaviors().stream().map(Behavior::toString).collect(Collectors.toSet());
        friendlyByteBuf2.writeCollection(set, FriendlyByteBuf::writeUtf);
        friendlyByteBuf2.writeCollection(DebugPackets.getMemoryDescriptions(livingEntity, l), (friendlyByteBuf, string) -> {
            String string2 = StringUtil.truncateStringIfNecessary(string, 255, true);
            friendlyByteBuf.writeUtf(string2);
        });
        if (livingEntity instanceof Villager) {
            Set set2 = Stream.of(MemoryModuleType.JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.MEETING_POINT).map(brain::getMemory).flatMap(Optional::stream).map(GlobalPos::pos).collect(Collectors.toSet());
            friendlyByteBuf2.writeCollection(set2, FriendlyByteBuf::writeBlockPos);
        } else {
            friendlyByteBuf2.writeVarInt(0);
        }
        if (livingEntity instanceof Villager) {
            Set set2 = Stream.of(MemoryModuleType.POTENTIAL_JOB_SITE).map(brain::getMemory).flatMap(Optional::stream).map(GlobalPos::pos).collect(Collectors.toSet());
            friendlyByteBuf2.writeCollection(set2, FriendlyByteBuf::writeBlockPos);
        } else {
            friendlyByteBuf2.writeVarInt(0);
        }
        if (livingEntity instanceof Villager) {
            Map<UUID, Object2IntMap<GossipType>> map = ((Villager)livingEntity).getGossips().getGossipEntries();
            ArrayList list = Lists.newArrayList();
            map.forEach((uUID, object2IntMap) -> {
                String string = DebugEntityNameGenerator.getEntityName(uUID);
                object2IntMap.forEach((gossipType, integer) -> list.add(string + ": " + gossipType + ": " + integer));
            });
            friendlyByteBuf2.writeCollection(list, FriendlyByteBuf::writeUtf);
        } else {
            friendlyByteBuf2.writeVarInt(0);
        }
    }

    private static List<String> getMemoryDescriptions(LivingEntity livingEntity, long l) {
        Map<MemoryModuleType<?>, Optional<ExpirableValue<?>>> map = livingEntity.getBrain().getMemories();
        ArrayList<String> list = Lists.newArrayList();
        for (Map.Entry<MemoryModuleType<?>, Optional<ExpirableValue<?>>> entry : map.entrySet()) {
            Object string;
            MemoryModuleType<?> memoryModuleType = entry.getKey();
            Optional<ExpirableValue<?>> optional = entry.getValue();
            if (optional.isPresent()) {
                ExpirableValue<?> expirableValue = optional.get();
                Object object = expirableValue.getValue();
                if (memoryModuleType == MemoryModuleType.HEARD_BELL_TIME) {
                    long m = l - (Long)object;
                    string = m + " ticks ago";
                } else {
                    string = expirableValue.canExpire() ? DebugPackets.getShortDescription((ServerLevel)livingEntity.level, object) + " (ttl: " + expirableValue.getTimeToLive() + ")" : DebugPackets.getShortDescription((ServerLevel)livingEntity.level, object);
                }
            } else {
                string = "-";
            }
            list.add(Registry.MEMORY_MODULE_TYPE.getKey(memoryModuleType).getPath() + ": " + (String)string);
        }
        list.sort(String::compareTo);
        return list;
    }

    private static String getShortDescription(ServerLevel serverLevel, @Nullable Object object) {
        if (object == null) {
            return "-";
        }
        if (object instanceof UUID) {
            return DebugPackets.getShortDescription(serverLevel, serverLevel.getEntity((UUID)object));
        }
        if (object instanceof LivingEntity) {
            Entity entity = (Entity)object;
            return DebugEntityNameGenerator.getEntityName(entity);
        }
        if (object instanceof Nameable) {
            return ((Nameable)object).getName().getString();
        }
        if (object instanceof WalkTarget) {
            return DebugPackets.getShortDescription(serverLevel, ((WalkTarget)object).getTarget());
        }
        if (object instanceof EntityTracker) {
            return DebugPackets.getShortDescription(serverLevel, ((EntityTracker)object).getEntity());
        }
        if (object instanceof GlobalPos) {
            return DebugPackets.getShortDescription(serverLevel, ((GlobalPos)object).pos());
        }
        if (object instanceof BlockPosTracker) {
            return DebugPackets.getShortDescription(serverLevel, ((BlockPosTracker)object).currentBlockPosition());
        }
        if (object instanceof EntityDamageSource) {
            Entity entity = ((EntityDamageSource)object).getEntity();
            return entity == null ? object.toString() : DebugPackets.getShortDescription(serverLevel, entity);
        }
        if (object instanceof Collection) {
            ArrayList<String> list = Lists.newArrayList();
            for (Object object2 : (Iterable)object) {
                list.add(DebugPackets.getShortDescription(serverLevel, object2));
            }
            return ((Object)list).toString();
        }
        return object.toString();
    }

    private static void sendPacketToAllPlayers(ServerLevel serverLevel, FriendlyByteBuf friendlyByteBuf, ResourceLocation resourceLocation) {
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(resourceLocation, friendlyByteBuf);
        for (Player player : serverLevel.getLevel().players()) {
            ((ServerPlayer)player).connection.send(packet);
        }
    }

    private static /* synthetic */ void method_36163(FriendlyByteBuf friendlyByteBuf, Raid raid) {
        friendlyByteBuf.writeBlockPos(raid.getCenter());
    }

    private static /* synthetic */ void method_36162(FriendlyByteBuf friendlyByteBuf, WrappedGoal wrappedGoal) {
        friendlyByteBuf.writeInt(wrappedGoal.getPriority());
        friendlyByteBuf.writeBoolean(wrappedGoal.isRunning());
        friendlyByteBuf.writeUtf(wrappedGoal.getGoal().getClass().getSimpleName());
    }

    private static /* synthetic */ void method_36155(ServerLevel serverLevel, PoiRecord poiRecord) {
        DebugPackets.sendPoiAddedPacket(serverLevel, poiRecord.getPos());
    }

    private static /* synthetic */ boolean method_36159(PoiType poiType) {
        return true;
    }
}

