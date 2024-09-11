package net.minecraft.commands.arguments.coordinates;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface Coordinates {
	Vec3 getPosition(CommandSourceStack commandSourceStack);

	Vec2 getRotation(CommandSourceStack commandSourceStack);

	default BlockPos getBlockPos(CommandSourceStack commandSourceStack) {
		return BlockPos.containing(this.getPosition(commandSourceStack));
	}

	boolean isXRelative();

	boolean isYRelative();

	boolean isZRelative();
}
