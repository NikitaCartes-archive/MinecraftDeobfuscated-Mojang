package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.monster.warden.Warden;
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
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class DebugPackets {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static void sendGameTestAddMarker(ServerLevel serverLevel, BlockPos blockPos, String string, int i, int j) {
		FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
		friendlyByteBuf.writeBlockPos(blockPos);
		friendlyByteBuf.writeInt(i);
		friendlyByteBuf.writeUtf(string);
		friendlyByteBuf.writeInt(j);
		sendPacketToAllPlayers(serverLevel, friendlyByteBuf, ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_ADD_MARKER);
	}

	public static void sendGameTestClearPacket(ServerLevel serverLevel) {
		FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
		sendPacketToAllPlayers(serverLevel, friendlyByteBuf, ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_CLEAR);
	}

	public static void sendPoiPacketsForChunk(ServerLevel serverLevel, ChunkPos chunkPos) {
	}

	public static void sendPoiAddedPacket(ServerLevel serverLevel, BlockPos blockPos) {
		sendVillageSectionsPacket(serverLevel, blockPos);
	}

	public static void sendPoiRemovedPacket(ServerLevel serverLevel, BlockPos blockPos) {
		sendVillageSectionsPacket(serverLevel, blockPos);
	}

	public static void sendPoiTicketCountPacket(ServerLevel serverLevel, BlockPos blockPos) {
		sendVillageSectionsPacket(serverLevel, blockPos);
	}

	private static void sendVillageSectionsPacket(ServerLevel serverLevel, BlockPos blockPos) {
	}

	public static void sendPathFindingPacket(Level level, Mob mob, @Nullable Path path, float f) {
	}

	public static void sendNeighborsUpdatePacket(Level level, BlockPos blockPos) {
	}

	public static void sendStructurePacket(WorldGenLevel worldGenLevel, StructureStart structureStart) {
	}

	public static void sendGoalSelector(Level level, Mob mob, GoalSelector goalSelector) {
		if (level instanceof ServerLevel) {
			;
		}
	}

	public static void sendRaids(ServerLevel serverLevel, Collection<Raid> collection) {
	}

	public static void sendEntityBrain(LivingEntity livingEntity) {
	}

	public static void sendBeeInfo(Bee bee) {
	}

	public static void sendGameEventInfo(Level level, GameEvent gameEvent, Vec3 vec3) {
	}

	public static void sendGameEventListenerInfo(Level level, GameEventListener gameEventListener) {
	}

	public static void sendHiveInfo(Level level, BlockPos blockPos, BlockState blockState, BeehiveBlockEntity beehiveBlockEntity) {
	}

	private static void writeBrain(LivingEntity livingEntity, FriendlyByteBuf friendlyByteBuf) {
		Brain<?> brain = livingEntity.getBrain();
		long l = livingEntity.level.getGameTime();
		if (livingEntity instanceof InventoryCarrier) {
			Container container = ((InventoryCarrier)livingEntity).getInventory();
			friendlyByteBuf.writeUtf(container.isEmpty() ? "" : container.toString());
		} else {
			friendlyByteBuf.writeUtf("");
		}

		if (brain.hasMemoryValue(MemoryModuleType.PATH)) {
			friendlyByteBuf.writeBoolean(true);
			Path path = (Path)brain.getMemory(MemoryModuleType.PATH).get();
			path.writeToStream(friendlyByteBuf);
		} else {
			friendlyByteBuf.writeBoolean(false);
		}

		if (livingEntity instanceof Villager villager) {
			boolean bl = villager.wantsToSpawnGolem(l);
			friendlyByteBuf.writeBoolean(bl);
		} else {
			friendlyByteBuf.writeBoolean(false);
		}

		if (livingEntity.getType() == EntityType.WARDEN) {
			Warden warden = (Warden)livingEntity;
			friendlyByteBuf.writeInt(warden.getClientAngerLevel());
		} else {
			friendlyByteBuf.writeInt(-1);
		}

		friendlyByteBuf.writeCollection(brain.getActiveActivities(), (friendlyByteBufx, activity) -> friendlyByteBufx.writeUtf(activity.getName()));
		Set<String> set = (Set<String>)brain.getRunningBehaviors().stream().map(Behavior::toString).collect(Collectors.toSet());
		friendlyByteBuf.writeCollection(set, FriendlyByteBuf::writeUtf);
		friendlyByteBuf.writeCollection(getMemoryDescriptions(livingEntity, l), (friendlyByteBufx, string) -> {
			String string2 = StringUtil.truncateStringIfNecessary(string, 255, true);
			friendlyByteBufx.writeUtf(string2);
		});
		if (livingEntity instanceof Villager) {
			Set<BlockPos> set2 = (Set<BlockPos>)Stream.of(MemoryModuleType.JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.MEETING_POINT)
				.map(brain::getMemory)
				.flatMap(Optional::stream)
				.map(GlobalPos::pos)
				.collect(Collectors.toSet());
			friendlyByteBuf.writeCollection(set2, FriendlyByteBuf::writeBlockPos);
		} else {
			friendlyByteBuf.writeVarInt(0);
		}

		if (livingEntity instanceof Villager) {
			Set<BlockPos> set2 = (Set<BlockPos>)Stream.of(MemoryModuleType.POTENTIAL_JOB_SITE)
				.map(brain::getMemory)
				.flatMap(Optional::stream)
				.map(GlobalPos::pos)
				.collect(Collectors.toSet());
			friendlyByteBuf.writeCollection(set2, FriendlyByteBuf::writeBlockPos);
		} else {
			friendlyByteBuf.writeVarInt(0);
		}

		if (livingEntity instanceof Villager) {
			Map<UUID, Object2IntMap<GossipType>> map = ((Villager)livingEntity).getGossips().getGossipEntries();
			List<String> list = Lists.<String>newArrayList();
			map.forEach((uUID, object2IntMap) -> {
				String string = DebugEntityNameGenerator.getEntityName(uUID);
				object2IntMap.forEach((gossipType, integer) -> list.add(string + ": " + gossipType + ": " + integer));
			});
			friendlyByteBuf.writeCollection(list, FriendlyByteBuf::writeUtf);
		} else {
			friendlyByteBuf.writeVarInt(0);
		}
	}

	private static List<String> getMemoryDescriptions(LivingEntity livingEntity, long l) {
		Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> map = livingEntity.getBrain().getMemories();
		List<String> list = Lists.<String>newArrayList();

		for (Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> entry : map.entrySet()) {
			MemoryModuleType<?> memoryModuleType = (MemoryModuleType<?>)entry.getKey();
			Optional<? extends ExpirableValue<?>> optional = (Optional<? extends ExpirableValue<?>>)entry.getValue();
			String string;
			if (optional.isPresent()) {
				ExpirableValue<?> expirableValue = (ExpirableValue<?>)optional.get();
				Object object = expirableValue.getValue();
				if (memoryModuleType == MemoryModuleType.HEARD_BELL_TIME) {
					long m = l - (Long)object;
					string = m + " ticks ago";
				} else if (expirableValue.canExpire()) {
					string = getShortDescription((ServerLevel)livingEntity.level, object) + " (ttl: " + expirableValue.getTimeToLive() + ")";
				} else {
					string = getShortDescription((ServerLevel)livingEntity.level, object);
				}
			} else {
				string = "-";
			}

			list.add(Registry.MEMORY_MODULE_TYPE.getKey(memoryModuleType).getPath() + ": " + string);
		}

		list.sort(String::compareTo);
		return list;
	}

	private static String getShortDescription(ServerLevel serverLevel, @Nullable Object object) {
		if (object == null) {
			return "-";
		} else if (object instanceof UUID) {
			return getShortDescription(serverLevel, serverLevel.getEntity((UUID)object));
		} else if (object instanceof LivingEntity) {
			Entity entity = (Entity)object;
			return DebugEntityNameGenerator.getEntityName(entity);
		} else if (object instanceof Nameable) {
			return ((Nameable)object).getName().getString();
		} else if (object instanceof WalkTarget) {
			return getShortDescription(serverLevel, ((WalkTarget)object).getTarget());
		} else if (object instanceof EntityTracker) {
			return getShortDescription(serverLevel, ((EntityTracker)object).getEntity());
		} else if (object instanceof GlobalPos) {
			return getShortDescription(serverLevel, ((GlobalPos)object).pos());
		} else if (object instanceof BlockPosTracker) {
			return getShortDescription(serverLevel, ((BlockPosTracker)object).currentBlockPosition());
		} else if (object instanceof EntityDamageSource) {
			Entity entity = ((EntityDamageSource)object).getEntity();
			return entity == null ? object.toString() : getShortDescription(serverLevel, entity);
		} else if (!(object instanceof Collection)) {
			return object.toString();
		} else {
			List<String> list = Lists.<String>newArrayList();

			for (Object object2 : (Iterable)object) {
				list.add(getShortDescription(serverLevel, object2));
			}

			return list.toString();
		}
	}

	private static void sendPacketToAllPlayers(ServerLevel serverLevel, FriendlyByteBuf friendlyByteBuf, ResourceLocation resourceLocation) {
		Packet<?> packet = new ClientboundCustomPayloadPacket(resourceLocation, friendlyByteBuf);

		for (Player player : serverLevel.players()) {
			((ServerPlayer)player).connection.send(packet);
		}
	}
}
