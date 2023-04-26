package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;

public class FollowTemptation extends Behavior<PathfinderMob> {
	public static final int TEMPTATION_COOLDOWN = 100;
	public static final double CLOSE_ENOUGH_DIST = 2.5;
	private final Function<LivingEntity, Float> speedModifier;
	private final double closeEnoughDistance;

	public FollowTemptation(Function<LivingEntity, Float> function) {
		this(function, 2.5);
	}

	public FollowTemptation(Function<LivingEntity, Float> function, double d) {
		super(Util.make(() -> {
			Builder<MemoryModuleType<?>, MemoryStatus> builder = ImmutableMap.builder();
			builder.put(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED);
			builder.put(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED);
			builder.put(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT);
			builder.put(MemoryModuleType.IS_TEMPTED, MemoryStatus.REGISTERED);
			builder.put(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_PRESENT);
			builder.put(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT);
			builder.put(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT);
			return builder.build();
		}));
		this.speedModifier = function;
		this.closeEnoughDistance = d;
	}

	protected float getSpeedModifier(PathfinderMob pathfinderMob) {
		return (Float)this.speedModifier.apply(pathfinderMob);
	}

	private Optional<Player> getTemptingPlayer(PathfinderMob pathfinderMob) {
		return pathfinderMob.getBrain().getMemory(MemoryModuleType.TEMPTING_PLAYER);
	}

	@Override
	protected boolean timedOut(long l) {
		return false;
	}

	protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		return this.getTemptingPlayer(pathfinderMob).isPresent()
			&& !pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET)
			&& !pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		pathfinderMob.getBrain().setMemory(MemoryModuleType.IS_TEMPTED, true);
	}

	protected void stop(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		Brain<?> brain = pathfinderMob.getBrain();
		brain.setMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, 100);
		brain.setMemory(MemoryModuleType.IS_TEMPTED, false);
		brain.eraseMemory(MemoryModuleType.WALK_TARGET);
		brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
	}

	protected void tick(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		Player player = (Player)this.getTemptingPlayer(pathfinderMob).get();
		Brain<?> brain = pathfinderMob.getBrain();
		brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(player, true));
		if (pathfinderMob.distanceToSqr(player) < this.closeEnoughDistance * this.closeEnoughDistance) {
			brain.eraseMemory(MemoryModuleType.WALK_TARGET);
		} else {
			brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(player, false), this.getSpeedModifier(pathfinderMob), 2));
		}
	}
}
