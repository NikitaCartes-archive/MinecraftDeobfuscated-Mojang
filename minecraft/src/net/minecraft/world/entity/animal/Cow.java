package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.CelebrateVillagersSurvivedRaid;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Cow extends Animal {
	private int bloatCount = 0;
	public static final EntityDataAccessor<Integer> BLOAT_LEVEL = SynchedEntityData.defineId(Cow.class, EntityDataSerializers.INT);
	private boolean exploded = false;

	public Cow(EntityType<? extends Cow> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
		this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.WHEAT), false));
		this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0, () -> !this.isBaloonCowEnabled()));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	protected boolean isBaloonCowEnabled() {
		return Rules.AIR_BLOCKS.get();
	}

	public int getBloatLevel() {
		return this.entityData.get(BLOAT_LEVEL);
	}

	public void setBloatLevel(int i) {
		this.entityData.set(BLOAT_LEVEL, i);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("BloatLevel", this.getBloatLevel());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setBloatLevel(compoundTag.getInt("BloatLevel"));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(BLOAT_LEVEL, 0);
	}

	@Override
	public void onDonePathing() {
		float f = (float)this.getBloatLevel() / 100.0F;
		if (this.getRandom().nextFloat() < f) {
			this.liftOff();
		}
	}

	private void liftOff() {
		if (this.onGround && !this.level.isClientSide) {
			int i = this.getBloatLevel();
			double d = this.level.getGravity();
			if (d == 0.1) {
				if (this.level.isMoon()) {
					this.addDeltaMovement(new Vec3(0.0, (double)((float)i / 46.0F), 0.0));
				} else {
					this.addDeltaMovement(new Vec3(0.0, (double)((float)i / 6.9F), 0.0));
				}
			} else {
				this.addDeltaMovement(new Vec3(0.0, (double)((float)i / 4.8F), 0.0));
			}

			int j = (int)((float)i * this.random.nextFloat() / 2.0F);
			this.bloatCount = i - j;
		}
	}

	@Override
	public void aiStep() {
		if (this.level instanceof ServerLevel serverLevel) {
			if (this.isBaloonCowEnabled() && this.getY() > 685.0) {
				this.explode();
				return;
			}

			if (this.bloatCount > 0) {
				this.bloatCount--;
				float f = this.getDimensions(Pose.STANDING).width / 2.0F;
				serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 10, (double)f, 0.0, (double)f, 0.1);
				this.playSound(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD.value(), 0.4F, 0.7F + this.random.nextFloat() * 0.6F);
				this.setBloatLevel(this.getBloatLevel() - 1);
			}
		}

		super.aiStep();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		super.onSyncedDataUpdated(entityDataAccessor);
		if (BLOAT_LEVEL.equals(entityDataAccessor)) {
			this.refreshDimensions();
			this.setBoundingBox(this.makeBoundingBox());
		}
	}

	@Override
	public double getPassengersRidingOffset() {
		return (double)this.getDimensions(Pose.STANDING).height - 0.25;
	}

	public float getBloatScale() {
		return 1.0F + (float)this.getBloatLevel() / 40.0F;
	}

	@Nullable
	@Override
	public LivingEntity getControllingPassenger() {
		return this.isBaloonCowEnabled() ? null : super.getControllingPassenger();
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return EntityType.COW.getDimensions().scale(this.getScale() * this.getBloatScale());
	}

	private void explode() {
		this.exploded = true;
		if (!this.level.isClientSide) {
			int[] is = new int[20];

			for (int i = 0; i < 20; i++) {
				int j = 161;
				int k = 179;
				int l = 123;
				j -= (int)(this.random.nextFloat() * 32.0F);
				k -= (int)(this.random.nextFloat() * 22.0F);
				l -= (int)(this.random.nextFloat() * 32.0F);
				is[i] = j << 16 | k << 8 | l;
			}

			ItemStack itemStack = CelebrateVillagersSurvivedRaid.getFirework(0, 2 + this.getBloatLevel() / 12, is);
			FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(this.level, this, this.getX(), this.getEyeY(), this.getZ(), itemStack);
			this.level.addFreshEntity(fireworkRocketEntity);
			this.level.broadcastEntityEvent(fireworkRocketEntity, (byte)17);

			for (Entity entity : this.getPassengers()) {
				if (entity instanceof LivingEntity livingEntity) {
					livingEntity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 400, 1, true, false, false));
					livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 1, true, false, false));
				}

				entity.stopRiding();
			}

			fireworkRocketEntity.discard();
		}

		this.kill();
	}

	@Override
	protected void dropAllDeathLoot(DamageSource damageSource) {
		if (!this.exploded) {
			super.dropAllDeathLoot(damageSource);
		}
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.2F);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.COW_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.COW_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.COW_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.COW_STEP, 0.15F, 1.0F);
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (this.isBaloonCowEnabled() && itemStack.is(Items.AIR_BLOCK)) {
			int i = this.getBloatLevel();
			if (i < 100) {
				this.setBloatLevel(i + 10);
				player.getItemInHand(interactionHand).shrink(1);
				this.playSound(SoundEvents.CAT_HISS, 1.0F, 0.5F);
				return InteractionResult.SUCCESS;
			}
		}

		if (!Rules.MILK_EVERY_MOB.get() && itemStack.is(Items.BUCKET) && !this.isBaby()) {
			player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
			ItemStack itemStack2 = ItemUtils.createFilledResult(itemStack, player, Items.MILK_BUCKET.getDefaultInstance());
			player.setItemInHand(interactionHand, itemStack2);
			return InteractionResult.sidedSuccess(this.level.isClientSide);
		} else if (this.isBaloonCowEnabled() && interactionHand == InteractionHand.MAIN_HAND && this.getPassengers().isEmpty()) {
			player.startRiding(this);
			return InteractionResult.SUCCESS;
		} else {
			return super.mobInteract(player, interactionHand);
		}
	}

	@Nullable
	public Cow getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		return EntityType.COW.create(serverLevel);
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return !this.isBaby() && !this.isBaloonCowEnabled() ? 1.3F : entityDimensions.height * 0.95F;
	}
}
