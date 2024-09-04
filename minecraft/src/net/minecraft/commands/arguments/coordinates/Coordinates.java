package net.minecraft.commands.arguments.coordinates;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface Coordinates {
	Vec3 getPosition(CommandSourceStack commandSourceStack, boolean bl);

	Vec2 getRotation(CommandSourceStack commandSourceStack, boolean bl);

	default Vec3 getPosition(CommandSourceStack commandSourceStack) {
		return this.getPosition(commandSourceStack, false);
	}

	default Vec2 getRotation(CommandSourceStack commandSourceStack) {
		return this.getRotation(commandSourceStack, false);
	}

	default BlockPos getBlockPos(CommandSourceStack commandSourceStack) {
		return BlockPos.containing(this.getPosition(commandSourceStack, false));
	}

	boolean isXRelative();

	boolean isYRelative();

	boolean isZRelative();
}
