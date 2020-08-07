package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ValidateNearbyPoi extends Behavior<LivingEntity> {
	private final MemoryModuleType<GlobalPos> memoryType;
	private final Predicate<PoiType> poiPredicate;

	public ValidateNearbyPoi(PoiType poiType, MemoryModuleType<GlobalPos> memoryModuleType) {
		super(ImmutableMap.of(memoryModuleType, MemoryStatus.VALUE_PRESENT));
		this.poiPredicate = poiType.getPredicate();
		this.memoryType = memoryModuleType;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
		GlobalPos globalPos = (GlobalPos)livingEntity.getBrain().getMemory(this.memoryType).get();
		return serverLevel.dimension() == globalPos.dimension() && globalPos.pos().closerThan(livingEntity.position(), 16.0);
	}

	@Override
	protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		Brain<?> brain = livingEntity.getBrain();
		GlobalPos globalPos = (GlobalPos)brain.getMemory(this.memoryType).get();
		BlockPos blockPos = globalPos.pos();
		ServerLevel serverLevel2 = serverLevel.getServer().getLevel(globalPos.dimension());
		if (serverLevel2 == null || this.poiDoesntExist(serverLevel2, blockPos)) {
			brain.eraseMemory(this.memoryType);
		} else if (this.bedIsOccupied(serverLevel2, blockPos, livingEntity)) {
			brain.eraseMemory(this.memoryType);
			serverLevel.getPoiManager().release(blockPos);
			DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos);
		}
	}

	private boolean bedIsOccupied(ServerLevel serverLevel, BlockPos blockPos, LivingEntity livingEntity) {
		BlockState blockState = serverLevel.getBlockState(blockPos);
		return blockState.getBlock().is(BlockTags.BEDS) && (Boolean)blockState.getValue(BedBlock.OCCUPIED) && !livingEntity.isSleeping();
	}

	private boolean poiDoesntExist(ServerLevel serverLevel, BlockPos blockPos) {
		return !serverLevel.getPoiManager().exists(blockPos, this.poiPredicate);
	}
}
