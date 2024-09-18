package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;

public interface ScheduledTickAccess {
	<T> ScheduledTick<T> createTick(BlockPos blockPos, T object, int i, TickPriority tickPriority);

	<T> ScheduledTick<T> createTick(BlockPos blockPos, T object, int i);

	LevelTickAccess<Block> getBlockTicks();

	default void scheduleTick(BlockPos blockPos, Block block, int i, TickPriority tickPriority) {
		this.getBlockTicks().schedule(this.createTick(blockPos, block, i, tickPriority));
	}

	default void scheduleTick(BlockPos blockPos, Block block, int i) {
		this.getBlockTicks().schedule(this.createTick(blockPos, block, i));
	}

	LevelTickAccess<Fluid> getFluidTicks();

	default void scheduleTick(BlockPos blockPos, Fluid fluid, int i, TickPriority tickPriority) {
		this.getFluidTicks().schedule(this.createTick(blockPos, fluid, i, tickPriority));
	}

	default void scheduleTick(BlockPos blockPos, Fluid fluid, int i) {
		this.getFluidTicks().schedule(this.createTick(blockPos, fluid, i));
	}
}
