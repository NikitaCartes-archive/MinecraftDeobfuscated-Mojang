package net.minecraft.world.entity.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Chicken extends Animal {
	private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
	public float flap;
	public float flapSpeed;
	public float oFlapSpeed;
	public float oFlap;
	public float flapping = 1.0F;
	private float nextFlap = 1.0F;
	public int eggTime = this.random.nextInt(6000) + 6000;
	public boolean isChickenJockey;

	public Chicken(EntityType<? extends Chicken> entityType, Level level) {
		super(entityType, level);
		this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
		this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, FOOD_ITEMS, false));
		this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return this.isBaby() ? entityDimensions.height * 0.85F : entityDimensions.height * 0.92F;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.25);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		this.oFlap = this.flap;
		this.oFlapSpeed = this.flapSpeed;
		this.flapSpeed = (float)((double)this.flapSpeed + (double)(this.onGround ? -1 : 4) * 0.3);
		this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0F, 1.0F);
		if (!this.onGround && this.flapping < 1.0F) {
			this.flapping = 1.0F;
		}

		this.flapping = (float)((double)this.flapping * 0.9);
		Vec3 vec3 = this.getDeltaMovement();
		if (!this.onGround && vec3.y < 0.0) {
			this.setDeltaMovement(vec3.multiply(1.0, 0.6, 1.0));
		}

		this.flap = this.flap + this.flapping * 2.0F;
		if (!this.level.isClientSide && this.isAlive() && !this.isBaby() && !this.isChickenJockey() && --this.eggTime <= 0) {
			this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
			this.spawnAtLocation(Items.EGG);
			this.eggTime = this.random.nextInt(6000) + 6000;
		}
	}

	@Override
	protected boolean isFlapping() {
		return this.flyDist > this.nextFlap;
	}

	@Override
	protected void onFlap() {
		this.nextFlap = this.flyDist + this.flapSpeed / 2.0F;
	}

	@Override
	public boolean causeFallDamage(float f, float g, DamageSource damageSource) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.CHICKEN_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.CHICKEN_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.CHICKEN_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
	}

	public Chicken getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		return EntityType.CHICKEN.create(serverLevel);
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return FOOD_ITEMS.test(itemStack);
	}

	@Override
	protected int getExperienceReward(Player player) {
		return this.isChickenJockey() ? 10 : super.getExperienceReward(player);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.isChickenJockey = compoundTag.getBoolean("IsChickenJockey");
		if (compoundTag.contains("EggLayTime")) {
			this.eggTime = compoundTag.getInt("EggLayTime");
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("IsChickenJockey", this.isChickenJockey);
		compoundTag.putInt("EggLayTime", this.eggTime);
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return this.isChickenJockey();
	}

	@Override
	public void positionRider(Entity entity) {
		super.positionRider(entity);
		float f = Mth.sin(this.yBodyRot * (float) (Math.PI / 180.0));
		float g = Mth.cos(this.yBodyRot * (float) (Math.PI / 180.0));
		float h = 0.1F;
		float i = 0.0F;
		entity.setPos(this.getX() + (double)(0.1F * f), this.getY(0.5) + entity.getMyRidingOffset() + 0.0, this.getZ() - (double)(0.1F * g));
		if (entity instanceof LivingEntity) {
			((LivingEntity)entity).yBodyRot = this.yBodyRot;
		}
	}

	public boolean isChickenJockey() {
		return this.isChickenJockey;
	}

	public void setChickenJockey(boolean bl) {
		this.isChickenJockey = bl;
	}
}
