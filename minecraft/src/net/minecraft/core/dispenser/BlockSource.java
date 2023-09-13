package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public record BlockSource(ServerLevel level, BlockPos pos, BlockState state, DispenserBlockEntity blockEntity) {
	public Vec3 center() {
		return this.pos.getCenter();
	}
}
