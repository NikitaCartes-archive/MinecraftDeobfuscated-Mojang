package net.minecraft.world.entity.animal;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GolemRandomStrollInVillageGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveBackToVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.OfferFlowerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public class IronGolem extends AbstractGolem implements NeutralMob {
	protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(IronGolem.class, EntityDataSerializers.BYTE);
	private static final int IRON_INGOT_HEAL_AMOUNT = 25;
	private int attackAnimationTick;
	private int offerFlowerTick;
	private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
	private int remainingPersistentAngerTime;
	@Nullable
	private UUID persistentAngerTarget;

	public IronGolem(EntityType<? extends IronGolem> entityType, Level level) {
		super(entityType, level);
		this.maxUpStep = 1.0F;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
		this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9, 32.0F));
		this.goalSelector.addGoal(2, new MoveBackToVillageGoal(this, 0.6, false));
		this.goalSelector.addGoal(4, new GolemRandomStrollInVillageGoal(this, 0.6));
		this.goalSelector.addGoal(5, new OfferFlowerGoal(this));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new DefendVillageTargetGoal(this));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Player.class, 10, true, false, this::isAngryAt));
		this.targetSelector
			.addGoal(
				3, new NearestAttackableTargetGoal(this, Mob.class, 5, false, false, livingEntity -> livingEntity instanceof Enemy && !(livingEntity instanceof Creeper))
			);
		this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<>(this, false));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_FLAGS_ID, (byte)0);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 100.0)
			.add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
			.add(Attributes.ATTACK_DAMAGE, 15.0);
	}

	@Override
	protected int decreaseAirSupply(int i) {
		return i;
	}

	@Override
	protected void doPush(Entity entity) {
		if (entity instanceof Enemy && !(entity instanceof Creeper) && this.getRandom().nextInt(20) == 0) {
			this.setTarget((LivingEntity)entity);
		}

		super.doPush(entity);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.attackAnimationTick > 0) {
			this.attackAnimationTick--;
		}

		if (this.offerFlowerTick > 0) {
			this.offerFlowerTick--;
		}

		if (this.getDeltaMovement().horizontalDistanceSqr() > 2.5000003E-7F && this.random.nextInt(5) == 0) {
			int i = Mth.floor(this.getX());
			int j = Mth.floor(this.getY() - 0.2F);
			int k = Mth.floor(this.getZ());
			BlockState blockState = this.level.getBlockState(new BlockPos(i, j, k));
			if (!blockState.isAir()) {
				this.level
					.addParticle(
						new BlockParticleOption(ParticleTypes.BLOCK, blockState),
						this.getX() + ((double)this.random.nextFloat() - 0.5) * (double)this.getBbWidth(),
						this.getY() + 0.1,
						this.getZ() + ((double)this.random.nextFloat() - 0.5) * (double)this.getBbWidth(),
						4.0 * ((double)this.random.nextFloat() - 0.5),
						0.5,
						((double)this.random.nextFloat() - 0.5) * 4.0
					);
			}
		}

		if (!this.level.isClientSide) {
			this.updatePersistentAnger((ServerLevel)this.level, true);
		}
	}

	@Override
	public boolean canAttackType(EntityType<?> entityType) {
		if (this.isPlayerCreated() && entityType == EntityType.PLAYER) {
			return false;
		} else {
			return entityType == EntityType.CREEPER ? false : super.canAttackType(entityType);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("PlayerCreated", this.isPlayerCreated());
		this.addPersistentAngerSaveData(compoundTag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setPlayerCreated(compoundTag.getBoolean("PlayerCreated"));
		this.readPersistentAngerSaveData(this.level, compoundTag);
	}

	@Override
	public void startPersistentAngerTimer() {
		this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
	}

	@Override
	public void setRemainingPersistentAngerTime(int i) {
		this.remainingPersistentAngerTime = i;
	}

	@Override
	public int getRemainingPersistentAngerTime() {
		return this.remainingPersistentAngerTime;
	}

	@Override
	public void setPersistentAngerTarget(@Nullable UUID uUID) {
		this.persistentAngerTarget = uUID;
	}

	@Nullable
	@Override
	public UUID getPersistentAngerTarget() {
		return this.persistentAngerTarget;
	}

	private float getAttackDamage() {
		return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		this.attackAnimationTick = 10;
		this.level.broadcastEntityEvent(this, (byte)4);
		float f = this.getAttackDamage();
		float g = (int)f > 0 ? f / 2.0F + (float)this.random.nextInt((int)f) : f;
		boolean bl = entity.hurt(DamageSource.mobAttack(this), g);
		if (bl) {
			entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, 0.4F, 0.0));
			this.doEnchantDamageEffects(this, entity);
		}

		this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
		return bl;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		IronGolem.Crackiness crackiness = this.getCrackiness();
		boolean bl = super.hurt(damageSource, f);
		if (bl && this.getCrackiness() != crackiness) {
			this.playSound(SoundEvents.IRON_GOLEM_DAMAGE, 1.0F, 1.0F);
		}

		return bl;
	}

	public IronGolem.Crackiness getCrackiness() {
		return IronGolem.Crackiness.byFraction(this.getHealth() / this.getMaxHealth());
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 4) {
			this.attackAnimationTick = 10;
			this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
		} else if (b == 11) {
			this.offerFlowerTick = 400;
		} else if (b == 34) {
			this.offerFlowerTick = 0;
		} else {
			super.handleEntityEvent(b);
		}
	}

	public int getAttackAnimationTick() {
		return this.attackAnimationTick;
	}

	public void offerFlower(boolean bl) {
		if (bl) {
			this.offerFlowerTick = 400;
			this.level.broadcastEntityEvent(this, (byte)11);
		} else {
			this.offerFlowerTick = 0;
			this.level.broadcastEntityEvent(this, (byte)34);
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.IRON_GOLEM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.IRON_GOLEM_DEATH;
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (!itemStack.is(Items.IRON_INGOT)) {
			return InteractionResult.PASS;
		} else {
			float f = this.getHealth();
			this.heal(25.0F);
			if (this.getHealth() == f) {
				return InteractionResult.PASS;
			} else {
				float g = 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
				this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, g);
				this.gameEvent(GameEvent.MOB_INTERACT, this.eyeBlockPosition());
				if (!player.getAbilities().instabuild) {
					itemStack.shrink(1);
				}

				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}
		}
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
	}

	public int getOfferFlowerTick() {
		return this.offerFlowerTick;
	}

	public boolean isPlayerCreated() {
		return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
	}

	public void setPlayerCreated(boolean bl) {
		byte b = this.entityData.get(DATA_FLAGS_ID);
		if (bl) {
			this.entityData.set(DATA_FLAGS_ID, (byte)(b | 1));
		} else {
			this.entityData.set(DATA_FLAGS_ID, (byte)(b & -2));
		}
	}

	@Override
	public void die(DamageSource damageSource) {
		super.die(damageSource);
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader levelReader) {
		BlockPos blockPos = this.blockPosition();
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState = levelReader.getBlockState(blockPos2);
		if (!blockState.entityCanStandOn(levelReader, blockPos2, this)) {
			return false;
		} else {
			for (int i = 1; i < 3; i++) {
				BlockPos blockPos3 = blockPos.above(i);
				BlockState blockState2 = levelReader.getBlockState(blockPos3);
				if (!NaturalSpawner.isValidEmptySpawnBlock(levelReader, blockPos3, blockState2, blockState2.getFluidState(), EntityType.IRON_GOLEM)) {
					return false;
				}
			}

			return NaturalSpawner.isValidEmptySpawnBlock(
					levelReader, blockPos, levelReader.getBlockState(blockPos), Fluids.EMPTY.defaultFluidState(), EntityType.IRON_GOLEM
				)
				&& levelReader.isUnobstructed(this);
		}
	}

	@Override
	public Vec3 getLeashOffset() {
		return new Vec3(0.0, (double)(0.875F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
	}

	public static enum Crackiness {
		NONE(1.0F),
		LOW(0.75F),
		MEDIUM(0.5F),
		HIGH(0.25F);

		private static final List<IronGolem.Crackiness> BY_DAMAGE = (List<IronGolem.Crackiness>)Stream.of(values())
			.sorted(Comparator.comparingDouble(crackiness -> (double)crackiness.fraction))
			.collect(ImmutableList.toImmutableList());
		private final float fraction;

		private Crackiness(float f) {
			this.fraction = f;
		}

		public static IronGolem.Crackiness byFraction(float f) {
			for (IronGolem.Crackiness crackiness : BY_DAMAGE) {
				if (f < crackiness.fraction) {
					return crackiness;
				}
			}

			return NONE;
		}
	}
}
