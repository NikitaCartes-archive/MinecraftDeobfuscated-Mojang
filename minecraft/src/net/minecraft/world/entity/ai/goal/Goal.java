package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

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

	public boolean requiresUpdateEveryTick() {
		return false;
	}

	public void tick() {
	}

	public void setFlags(EnumSet<Goal.Flag> enumSet) {
		this.flags.clear();
		this.flags.addAll(enumSet);
	}

	public String toString() {
		return this.getClass().getSimpleName();
	}

	public EnumSet<Goal.Flag> getFlags() {
		return this.flags;
	}

	protected int adjustedTickDelay(int i) {
		return this.requiresUpdateEveryTick() ? i : reducedTickDelay(i);
	}

	protected static int reducedTickDelay(int i) {
		return Mth.positiveCeilDiv(i, 2);
	}

	protected static ServerLevel getServerLevel(Entity entity) {
		return (ServerLevel)entity.level();
	}

	protected static ServerLevel getServerLevel(Level level) {
		return (ServerLevel)level;
	}

	public static enum Flag {
		MOVE,
		LOOK,
		JUMP,
		TARGET;
	}
}
