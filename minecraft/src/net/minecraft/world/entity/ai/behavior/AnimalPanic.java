package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public class AnimalPanic<E extends PathfinderMob> extends Behavior<E> {
	private static final int PANIC_MIN_DURATION = 100;
	private static final int PANIC_MAX_DURATION = 120;
	private static final int PANIC_DISTANCE_HORIZONTAL = 5;
	private static final int PANIC_DISTANCE_VERTICAL = 4;
	private final float speedMultiplier;
	private final Function<PathfinderMob, TagKey<DamageType>> panicCausingDamageTypes;

	public AnimalPanic(float f) {
		this(f, pathfinderMob -> DamageTypeTags.PANIC_CAUSES);
	}

	public AnimalPanic(float f, Function<PathfinderMob, TagKey<DamageType>> function) {
		super(Map.of(MemoryModuleType.IS_PANICKING, MemoryStatus.REGISTERED, MemoryModuleType.HURT_BY, MemoryStatus.REGISTERED), 100, 120);
		this.speedMultiplier = f;
		this.panicCausingDamageTypes = function;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E pathfinderMob) {
		return (Boolean)pathfinderMob.getBrain()
				.getMemory(MemoryModuleType.HURT_BY)
				.map(damageSource -> damageSource.is((TagKey<DamageType>)this.panicCausingDamageTypes.apply(pathfinderMob)))
				.orElse(false)
			|| pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
	}

	protected boolean canStillUse(ServerLevel serverLevel, E pathfinderMob, long l) {
		return true;
	}

	protected void start(ServerLevel serverLevel, E pathfinderMob, long l) {
		pathfinderMob.getBrain().setMemory(MemoryModuleType.IS_PANICKING, true);
		pathfinderMob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
	}

	protected void stop(ServerLevel serverLevel, E pathfinderMob, long l) {
		Brain<?> brain = pathfinderMob.getBrain();
		brain.eraseMemory(MemoryModuleType.IS_PANICKING);
	}

	protected void tick(ServerLevel serverLevel, E pathfinderMob, long l) {
		if (pathfinderMob.getNavigation().isDone()) {
			Vec3 vec3 = this.getPanicPos(pathfinderMob, serverLevel);
			if (vec3 != null) {
				pathfinderMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speedMultiplier, 0));
			}
		}
	}

	@Nullable
	private Vec3 getPanicPos(E pathfinderMob, ServerLevel serverLevel) {
		if (pathfinderMob.isOnFire()) {
			Optional<Vec3> optional = this.lookForWater(serverLevel, pathfinderMob).map(Vec3::atBottomCenterOf);
			if (optional.isPresent()) {
				return (Vec3)optional.get();
			}
		}

		return LandRandomPos.getPos(pathfinderMob, 5, 4);
	}

	private Optional<BlockPos> lookForWater(BlockGetter blockGetter, Entity entity) {
		BlockPos blockPos = entity.blockPosition();
		if (!blockGetter.getBlockState(blockPos).getCollisionShape(blockGetter, blockPos).isEmpty()) {
			return Optional.empty();
		} else {
			Predicate<BlockPos> predicate;
			if (Mth.ceil(entity.getBbWidth()) == 2) {
				predicate = blockPosx -> BlockPos.squareOutSouthEast(blockPosx).allMatch(blockPosxx -> blockGetter.getFluidState(blockPosxx).is(FluidTags.WATER));
			} else {
				predicate = blockPosx -> blockGetter.getFluidState(blockPosx).is(FluidTags.WATER);
			}

			return BlockPos.findClosestMatch(blockPos, 5, 1, predicate);
		}
	}
}
