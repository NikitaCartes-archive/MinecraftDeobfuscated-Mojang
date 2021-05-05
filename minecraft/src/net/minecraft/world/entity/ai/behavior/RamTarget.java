package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.Vec3;

public class RamTarget<E extends PathfinderMob> extends Behavior<E> {
	public static final int TIME_OUT_DURATION = 200;
	public static final float RAM_SPEED_FORCE_FACTOR = 1.65F;
	private final Function<E, UniformInt> getTimeBetweenRams;
	private final TargetingConditions ramTargeting;
	private final ToIntFunction<E> getDamage;
	private final float speed;
	private final ToDoubleFunction<E> getKnockbackForce;
	private Vec3 ramDirection;
	private final Function<E, SoundEvent> getImpactSound;

	public RamTarget(
		Function<E, UniformInt> function,
		TargetingConditions targetingConditions,
		ToIntFunction<E> toIntFunction,
		float f,
		ToDoubleFunction<E> toDoubleFunction,
		Function<E, SoundEvent> function2
	) {
		super(ImmutableMap.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT, MemoryModuleType.RAM_TARGET, MemoryStatus.VALUE_PRESENT), 200);
		this.getTimeBetweenRams = function;
		this.ramTargeting = targetingConditions;
		this.getDamage = toIntFunction;
		this.speed = f;
		this.getKnockbackForce = toDoubleFunction;
		this.getImpactSound = function2;
		this.ramDirection = Vec3.ZERO;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		return pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.RAM_TARGET);
	}

	protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		return pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.RAM_TARGET);
	}

	protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
		BlockPos blockPos = pathfinderMob.blockPosition();
		Brain<?> brain = pathfinderMob.getBrain();
		Vec3 vec3 = (Vec3)brain.getMemory(MemoryModuleType.RAM_TARGET).get();
		this.ramDirection = new Vec3((double)blockPos.getX() - vec3.x(), 0.0, (double)blockPos.getZ() - vec3.z()).normalize();
		brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speed, 0));
	}

	protected void tick(ServerLevel serverLevel, E pathfinderMob, long l) {
		List<LivingEntity> list = serverLevel.getNearbyEntities(LivingEntity.class, this.ramTargeting, pathfinderMob, pathfinderMob.getBoundingBox());
		Brain<?> brain = pathfinderMob.getBrain();
		if (!list.isEmpty()) {
			LivingEntity livingEntity = (LivingEntity)list.get(0);
			livingEntity.hurt(DamageSource.mobAttack(pathfinderMob), (float)this.getDamage.applyAsInt(pathfinderMob));
			float f = livingEntity.isDamageSourceBlocked(DamageSource.mobAttack(pathfinderMob)) ? 0.5F : 1.0F;
			float g = Mth.clamp(pathfinderMob.getSpeed() * 1.65F, 0.2F, 3.0F);
			livingEntity.knockback((double)(f * g) * this.getKnockbackForce.applyAsDouble(pathfinderMob), this.ramDirection.x(), this.ramDirection.z());
			this.finishRam(serverLevel, pathfinderMob);
			serverLevel.playSound(null, pathfinderMob, (SoundEvent)this.getImpactSound.apply(pathfinderMob), SoundSource.HOSTILE, 1.0F, 1.0F);
		} else {
			Optional<WalkTarget> optional = brain.getMemory(MemoryModuleType.WALK_TARGET);
			Optional<Vec3> optional2 = brain.getMemory(MemoryModuleType.RAM_TARGET);
			boolean bl = !optional.isPresent()
				|| !optional2.isPresent()
				|| ((WalkTarget)optional.get()).getTarget().currentPosition().distanceTo((Vec3)optional2.get()) < 0.25;
			if (bl) {
				this.finishRam(serverLevel, pathfinderMob);
			}
		}
	}

	protected void finishRam(ServerLevel serverLevel, E pathfinderMob) {
		serverLevel.broadcastEntityEvent(pathfinderMob, (byte)59);
		pathfinderMob.getBrain()
			.setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, ((UniformInt)this.getTimeBetweenRams.apply(pathfinderMob)).sample(serverLevel.random));
		pathfinderMob.getBrain().eraseMemory(MemoryModuleType.RAM_TARGET);
	}
}
