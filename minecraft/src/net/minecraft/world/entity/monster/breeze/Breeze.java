package net.minecraft.world.entity.monster.breeze;

import com.mojang.serialization.Dynamic;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class Breeze extends Monster {
	private static final int SLIDE_PARTICLES_AMOUNT = 20;
	private static final int IDLE_PARTICLES_AMOUNT = 1;
	private static final int JUMP_DUST_PARTICLES_AMOUNT = 20;
	private static final int JUMP_TRAIL_PARTICLES_AMOUNT = 3;
	private static final int JUMP_TRAIL_DURATION_TICKS = 5;
	private static final int JUMP_CIRCLE_DISTANCE_Y = 10;
	private static final float FALL_DISTANCE_SOUND_TRIGGER_THRESHOLD = 3.0F;
	private static final int WHIRL_SOUND_FREQUENCY_MIN = 1;
	private static final int WHIRL_SOUND_FREQUENCY_MAX = 80;
	public AnimationState idle = new AnimationState();
	public AnimationState slide = new AnimationState();
	public AnimationState slideBack = new AnimationState();
	public AnimationState longJump = new AnimationState();
	public AnimationState shoot = new AnimationState();
	public AnimationState inhale = new AnimationState();
	private int jumpTrailStartedTick = 0;
	private int soundTick = 0;
	private static final ProjectileDeflection PROJECTILE_DEFLECTION = (projectile, entity, randomSource) -> {
		entity.level().playLocalSound(entity, SoundEvents.BREEZE_DEFLECT, entity.getSoundSource(), 1.0F, 1.0F);
		ProjectileDeflection.REVERSE.deflect(projectile, entity, randomSource);
	};

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MOVEMENT_SPEED, 0.63F)
			.add(Attributes.MAX_HEALTH, 30.0)
			.add(Attributes.FOLLOW_RANGE, 24.0)
			.add(Attributes.ATTACK_DAMAGE, 3.0);
	}

	public Breeze(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
		this.setPathfindingMalus(PathType.DANGER_TRAPDOOR, -1.0F);
		this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
		this.xpReward = 10;
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return BreezeAi.makeBrain(this.brainProvider().makeBrain(dynamic));
	}

	@Override
	public Brain<Breeze> getBrain() {
		return (Brain<Breeze>)super.getBrain();
	}

	@Override
	protected Brain.Provider<Breeze> brainProvider() {
		return Brain.provider(BreezeAi.MEMORY_TYPES, BreezeAi.SENSOR_TYPES);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (this.level().isClientSide() && DATA_POSE.equals(entityDataAccessor)) {
			this.resetAnimations();
			Pose pose = this.getPose();
			switch (pose) {
				case SHOOTING:
					this.shoot.startIfStopped(this.tickCount);
					break;
				case INHALING:
					this.longJump.startIfStopped(this.tickCount);
					break;
				case SLIDING:
					this.slide.startIfStopped(this.tickCount);
			}
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	private void resetAnimations() {
		this.shoot.stop();
		this.idle.stop();
		this.inhale.stop();
		this.longJump.stop();
	}

	@Override
	public void tick() {
		Pose pose = this.getPose();
		switch (pose) {
			case SHOOTING:
			case INHALING:
			case STANDING:
				this.resetJumpTrail().emitGroundParticles(1 + this.getRandom().nextInt(1));
				break;
			case SLIDING:
				this.emitGroundParticles(20);
				break;
			case LONG_JUMPING:
				this.emitJumpTrailParticles();
		}

		if (pose != Pose.SLIDING && this.slide.isStarted()) {
			this.slideBack.start(this.tickCount);
			this.slide.stop();
		}

		this.soundTick = this.soundTick == 0 ? this.random.nextIntBetweenInclusive(1, 80) : this.soundTick - 1;
		if (this.soundTick == 0) {
			this.playWhirlSound();
		}

		super.tick();
	}

	public Breeze resetJumpTrail() {
		this.jumpTrailStartedTick = 0;
		return this;
	}

	public void emitJumpTrailParticles() {
		if (++this.jumpTrailStartedTick <= 5) {
			BlockState blockState = !this.getInBlockState().isAir() ? this.getInBlockState() : this.getBlockStateOn();
			Vec3 vec3 = this.getDeltaMovement();
			Vec3 vec32 = this.position().add(vec3).add(0.0, 0.1F, 0.0);

			for (int i = 0; i < 3; i++) {
				this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), vec32.x, vec32.y, vec32.z, 0.0, 0.0, 0.0);
			}
		}
	}

	public void emitGroundParticles(int i) {
		if (!this.isPassenger()) {
			Vec3 vec3 = this.getBoundingBox().getCenter();
			Vec3 vec32 = new Vec3(vec3.x, this.position().y, vec3.z);
			BlockState blockState = !this.getInBlockState().isAir() ? this.getInBlockState() : this.getBlockStateOn();
			if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
				for (int j = 0; j < i; j++) {
					this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), vec32.x, vec32.y, vec32.z, 0.0, 0.0, 0.0);
				}
			}
		}
	}

	@Override
	public void playAmbientSound() {
		if (this.getTarget() == null || !this.onGround()) {
			this.level().playLocalSound(this, this.getAmbientSound(), this.getSoundSource(), 1.0F, 1.0F);
		}
	}

	public void playWhirlSound() {
		float f = 0.7F + 0.4F * this.random.nextFloat();
		float g = 0.8F + 0.2F * this.random.nextFloat();
		this.level().playLocalSound(this, SoundEvents.BREEZE_WHIRL, this.getSoundSource(), g, f);
	}

	@Override
	public ProjectileDeflection deflection(Projectile projectile) {
		return projectile.getType() != EntityType.BREEZE_WIND_CHARGE && projectile.getType() != EntityType.WIND_CHARGE
			? PROJECTILE_DEFLECTION
			: ProjectileDeflection.NONE;
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.HOSTILE;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.BREEZE_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.BREEZE_HURT;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return this.onGround() ? SoundEvents.BREEZE_IDLE_GROUND : SoundEvents.BREEZE_IDLE_AIR;
	}

	public Optional<LivingEntity> getHurtBy() {
		return this.getBrain()
			.getMemory(MemoryModuleType.HURT_BY)
			.map(DamageSource::getEntity)
			.filter(entity -> entity instanceof LivingEntity)
			.map(entity -> (LivingEntity)entity);
	}

	public boolean withinInnerCircleRange(Vec3 vec3) {
		Vec3 vec32 = this.blockPosition().getCenter();
		return vec3.closerThan(vec32, 4.0, 10.0);
	}

	@Override
	protected void customServerAiStep() {
		this.level().getProfiler().push("breezeBrain");
		this.getBrain().tick((ServerLevel)this.level(), this);
		this.level().getProfiler().popPush("breezeActivityUpdate");
		BreezeAi.updateActivity(this);
		this.level().getProfiler().pop();
		super.customServerAiStep();
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
		DebugPackets.sendBreezeInfo(this);
	}

	@Override
	public boolean canAttackType(EntityType<?> entityType) {
		return entityType == EntityType.PLAYER || entityType == EntityType.IRON_GOLEM;
	}

	@Override
	public int getMaxHeadYRot() {
		return 30;
	}

	@Override
	public int getHeadRotSpeed() {
		return 25;
	}

	public double getSnoutYPosition() {
		return this.getEyeY() - 0.4;
	}

	@Override
	public boolean isInvulnerableTo(DamageSource damageSource) {
		return damageSource.is(DamageTypeTags.BREEZE_IMMUNE_TO) || damageSource.getEntity() instanceof Breeze || super.isInvulnerableTo(damageSource);
	}

	@Override
	public double getFluidJumpThreshold() {
		return (double)this.getEyeHeight();
	}

	@Override
	public boolean causeFallDamage(float f, float g, DamageSource damageSource) {
		if (f > 3.0F) {
			this.playSound(SoundEvents.BREEZE_LAND, 1.0F, 1.0F);
		}

		return super.causeFallDamage(f, g, damageSource);
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.EVENTS;
	}

	@Nullable
	@Override
	public LivingEntity getTarget() {
		return this.getTargetFromBrain();
	}
}
