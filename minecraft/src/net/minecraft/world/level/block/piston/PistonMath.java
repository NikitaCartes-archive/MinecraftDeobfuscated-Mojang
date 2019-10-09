package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class PistonMath {
	public static List<AABB> getEntityMovementAreas(boolean bl, AABB aABB, Direction direction, double d) {
		ArrayList<AABB> arrayList = Lists.newArrayList(getEntityMovementAreaInFront(aABB, direction, d));
		if (bl && direction.getAxis().isHorizontal()) {
			arrayList.add(getEntityMovementAreaOnTop(aABB));
		}

		return arrayList;
	}

	public static AABB getEntityMovementAreaOnTop(AABB aABB) {
		return new AABB(aABB.minX, aABB.maxY, aABB.minZ, aABB.maxX, aABB.maxY + 1.0E-7, aABB.maxZ);
	}

	public static AABB getEntityMovementAreaInFront(AABB aABB, Direction direction, double d) {
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
