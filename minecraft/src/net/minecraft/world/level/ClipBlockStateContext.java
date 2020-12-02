package net.minecraft.world.level;

import java.util.function.Predicate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ClipBlockStateContext {
	private final Vec3 from;
	private final Vec3 to;
	private final Predicate<BlockState> block;

	public ClipBlockStateContext(Vec3 vec3, Vec3 vec32, Predicate<BlockState> predicate) {
		this.from = vec3;
		this.to = vec32;
		this.block = predicate;
	}

	public Vec3 getTo() {
		return this.to;
	}

	public Vec3 getFrom() {
		return this.from;
	}

	public Predicate<BlockState> isTargetBlock() {
		return this.block;
	}
}
