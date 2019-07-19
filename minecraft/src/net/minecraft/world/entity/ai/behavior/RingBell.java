package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RingBell extends Behavior<LivingEntity> {
	public RingBell() {
		super(ImmutableMap.of(MemoryModuleType.MEETING_POINT, MemoryStatus.VALUE_PRESENT));
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
		return serverLevel.random.nextFloat() > 0.95F;
	}

	@Override
	protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		Brain<?> brain = livingEntity.getBrain();
		BlockPos blockPos = ((GlobalPos)brain.getMemory(MemoryModuleType.MEETING_POINT).get()).pos();
		if (blockPos.closerThan(new BlockPos(livingEntity), 3.0)) {
			BlockState blockState = serverLevel.getBlockState(blockPos);
			if (blockState.getBlock() == Blocks.BELL) {
				BellBlock bellBlock = (BellBlock)blockState.getBlock();

				for (Direction direction : Direction.Plane.HORIZONTAL) {
					if (bellBlock.onHit(
						serverLevel, blockState, serverLevel.getBlockEntity(blockPos), new BlockHitResult(new Vec3(0.5, 0.5, 0.5), direction, blockPos, false), null, false
					)) {
						break;
					}
				}
			}
		}
	}
}
