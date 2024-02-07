package net.minecraft.world.level.block.entity;

import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface Hopper extends Container {
	VoxelShape INSIDE = Block.box(2.0, 11.0, 2.0, 14.0, 16.0, 14.0);
	AABB SUCK_AABB = (AABB)Block.box(0.0, 11.0, 0.0, 16.0, 32.0, 16.0).toAabbs().get(0);

	default AABB getSuckAabb() {
		return SUCK_AABB;
	}

	double getLevelX();

	double getLevelY();

	double getLevelZ();
}
