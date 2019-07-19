package net.minecraft.network.protocol.game;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugPackets {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void sendPoiPacketsForChunk(ServerLevel serverLevel, ChunkPos chunkPos) {
	}

	public static void sendPoiAddedPacket(ServerLevel serverLevel, BlockPos blockPos) {
	}

	public static void sendPoiRemovedPacket(ServerLevel serverLevel, BlockPos blockPos) {
	}

	public static void sendPoiTicketCountPacket(ServerLevel serverLevel, BlockPos blockPos) {
	}

	public static void sendPathFindingPacket(Level level, Mob mob, @Nullable Path path, float f) {
	}

	public static void sendNeighborsUpdatePacket(Level level, BlockPos blockPos) {
	}

	public static void sendStructurePacket(LevelAccessor levelAccessor, StructureStart structureStart) {
	}

	public static void sendGoalSelector(Level level, Mob mob, GoalSelector goalSelector) {
	}

	public static void sendRaids(ServerLevel serverLevel, Collection<Raid> collection) {
	}

	public static void sendEntityBrain(LivingEntity livingEntity) {
	}
}
