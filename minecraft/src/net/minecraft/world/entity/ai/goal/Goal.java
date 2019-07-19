package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;

public abstract class Goal {
	private final EnumSet<Goal.Flag> flags = EnumSet.noneOf(Goal.Flag.class);

	public abstract boolean canUse();

	public boolean canContinueToUse() {
		return this.canUse();
	}

	public boolean isInterruptable() {
		return true;
	}

	public void start() {
	}

	public void stop() {
	}

	public void tick() {
	}

	public void setFlags(EnumSet<Goal.Flag> enumSet) {
		this.flags.clear();
		this.flags.addAll(enumSet);
	}

	public EnumSet<Goal.Flag> getFlags() {
		return this.flags;
	}

	public static enum Flag {
		MOVE,
		LOOK,
		JUMP,
		TARGET;
	}
}
