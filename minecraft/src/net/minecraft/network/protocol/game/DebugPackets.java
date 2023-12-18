package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestClearMarkersDebugPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.monster.breeze.Breeze;
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
		sendPacketToAllPlayers(serverLevel, new GameTestAddMarkerDebugPayload(blockPos, i, string, j));
	}

	public static void sendGameTestClearPacket(ServerLevel serverLevel) {
		sendPacketToAllPlayers(serverLevel, new GameTestClearMarkersDebugPayload());
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
	}

	public static void sendRaids(ServerLevel serverLevel, Collection<Raid> collection) {
	}

	public static void sendEntityBrain(LivingEntity livingEntity) {
	}

	public static void sendBeeInfo(Bee bee) {
	}

	public static void sendBreezeInfo(Breeze breeze) {
	}

	public static void sendGameEventInfo(Level level, Holder<GameEvent> holder, Vec3 vec3) {
	}

	public static void sendGameEventListenerInfo(Level level, GameEventListener gameEventListener) {
	}

	public static void sendHiveInfo(Level level, BlockPos blockPos, BlockState blockState, BeehiveBlockEntity beehiveBlockEntity) {
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
					string = getShortDescription((ServerLevel)livingEntity.level(), object) + " (ttl: " + expirableValue.getTimeToLive() + ")";
				} else {
					string = getShortDescription((ServerLevel)livingEntity.level(), object);
				}
			} else {
				string = "-";
			}

			list.add(BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memoryModuleType).getPath() + ": " + string);
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
		} else if (object instanceof DamageSource) {
			Entity entity = ((DamageSource)object).getEntity();
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

	private static void sendPacketToAllPlayers(ServerLevel serverLevel, CustomPacketPayload customPacketPayload) {
		Packet<?> packet = new ClientboundCustomPayloadPacket(customPacketPayload);

		for (ServerPlayer serverPlayer : serverLevel.players()) {
			serverPlayer.connection.send(packet);
		}
	}
}
