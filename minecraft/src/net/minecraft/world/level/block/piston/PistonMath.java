package net.minecraft.world.level.block.piston;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class PistonMath {
	public static AABB getMovementArea(AABB aABB, Direction direction, double d) {
		double e = d * (double)direction.getAxisDirection().getStep();
		double f = Math.min(e, 0.0);
		double g = Math.max(e, 0.0);
		switch (direction) {
			case WEST:
				return new AABB(aABB.minX + f, aABB.minY, aABB.minZ, aABB.minX + g, aABB.maxY, aABB.maxZ);
			case EAST:
				return new AABB(aABB.maxX + f, aABB.minY, aABB.minZ, aABB.maxX + g, aABB.maxY, aABB.maxZ);
			case DOWN:
				return new AABB(aABB.minX, aABB.minY + f, aABB.minZ, aABB.maxX, aABB.minY + g, aABB.maxZ);
			case UP:
			default:
				return new AABB(aABB.minX, aABB.maxY + f, aABB.minZ, aABB.maxX, aABB.maxY + g, aABB.maxZ);
			case NORTH:
				return new AABB(aABB.minX, aABB.minY, aABB.minZ + f, aABB.maxX, aABB.maxY, aABB.minZ + g);
			case SOUTH:
				return new AABB(aABB.minX, aABB.minY, aABB.maxZ + f, aABB.maxX, aABB.maxY, aABB.maxZ + g);
		}
	}
}
