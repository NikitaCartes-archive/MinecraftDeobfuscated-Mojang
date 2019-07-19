package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.monster.PigZombie;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Pig extends Animal {
	private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.INT);
	private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.CARROT, Items.POTATO, Items.BEETROOT);
	private boolean boosting;
	private int boostTime;
	private int boostTimeTotal;

	public Pig(EntityType<? extends Pig> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
		this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
		this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, Ingredient.of(Items.CARROT_ON_A_STICK), false));
		this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, false, FOOD_ITEMS));
		this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25);
	}

	@Nullable
	@Override
	public Entity getControllingPassenger() {
		return this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
	}

	@Override
	public boolean canBeControlledByRider() {
		Entity entity = this.getControllingPassenger();
		if (!(entity instanceof Player)) {
			return false;
		} else {
			Player player = (Player)entity;
			return player.getMainHandItem().getItem() == Items.CARROT_ON_A_STICK || player.getOffhandItem().getItem() == Items.CARROT_ON_A_STICK;
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_BOOST_TIME.equals(entityDataAccessor) && this.level.isClientSide) {
			this.boosting = true;
			this.boostTime = 0;
			this.boostTimeTotal = this.entityData.get(DATA_BOOST_TIME);
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_SADDLE_ID, false);
		this.entityData.define(DATA_BOOST_TIME, 0);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("Saddle", this.hasSaddle());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setSaddle(compoundTag.getBoolean("Saddle"));
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.PIG_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.PIG_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PIG_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.PIG_STEP, 0.15F, 1.0F);
	}

	@Override
	public boolean mobInteract(Player player, InteractionHand interactionHand) {
		if (!super.mobInteract(player, interactionHand)) {
			ItemStack itemStack = player.getItemInHand(interactionHand);
			if (itemStack.getItem() == Items.NAME_TAG) {
				itemStack.interactEnemy(player, this, interactionHand);
				return true;
			} else if (this.hasSaddle() && !this.isVehicle()) {
				if (!this.level.isClientSide) {
					player.startRiding(this);
				}

				return true;
			} else if (itemStack.getItem() == Items.SADDLE) {
				itemStack.interactEnemy(player, this, interactionHand);
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	@Override
	protected void dropEquipment() {
		super.dropEquipment();
		if (this.hasSaddle()) {
			this.spawnAtLocation(Items.SADDLE);
		}
	}

	public boolean hasSaddle() {
		return this.entityData.get(DATA_SADDLE_ID);
	}

	public void setSaddle(boolean bl) {
		if (bl) {
			this.entityData.set(DATA_SADDLE_ID, true);
		} else {
			this.entityData.set(DATA_SADDLE_ID, false);
		}
	}

	@Override
	public void thunderHit(LightningBolt lightningBolt) {
		PigZombie pigZombie = EntityType.ZOMBIE_PIGMAN.create(this.level);
		pigZombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
		pigZombie.moveTo(this.x, this.y, this.z, this.yRot, this.xRot);
		pigZombie.setNoAi(this.isNoAi());
		if (this.hasCustomName()) {
			pigZombie.setCustomName(this.getCustomName());
			pigZombie.setCustomNameVisible(this.isCustomNameVisible());
		}

		this.level.addFreshEntity(pigZombie);
		this.remove();
	}

	@Override
	public void travel(Vec3 vec3) {
		if (this.isAlive()) {
			Entity entity = this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
			if (this.isVehicle() && this.canBeControlledByRider()) {
				this.yRot = entity.yRot;
				this.yRotO = this.yRot;
				this.xRot = entity.xRot * 0.5F;
				this.setRot(this.yRot, this.xRot);
				this.yBodyRot = this.yRot;
				this.yHeadRot = this.yRot;
				this.maxUpStep = 1.0F;
				this.flyingSpeed = this.getSpeed() * 0.1F;
				if (this.boosting && this.boostTime++ > this.boostTimeTotal) {
					this.boosting = false;
				}

				if (this.isControlledByLocalInstance()) {
					float f = (float)this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue() * 0.225F;
					if (this.boosting) {
						f += f * 1.15F * Mth.sin((float)this.boostTime / (float)this.boostTimeTotal * (float) Math.PI);
					}

					this.setSpeed(f);
					super.travel(new Vec3(0.0, 0.0, 1.0));
				} else {
					this.setDeltaMovement(Vec3.ZERO);
				}

				this.animationSpeedOld = this.animationSpeed;
				double d = this.x - this.xo;
				double e = this.z - this.zo;
				float g = Mth.sqrt(d * d + e * e) * 4.0F;
				if (g > 1.0F) {
					g = 1.0F;
				}

				this.animationSpeed = this.animationSpeed + (g - this.animationSpeed) * 0.4F;
				this.animationPosition = this.animationPosition + this.animationSpeed;
			} else {
				this.maxUpStep = 0.5F;
				this.flyingSpeed = 0.02F;
				super.travel(vec3);
			}
		}
	}

	public boolean boost() {
		if (this.boosting) {
			return false;
		} else {
			this.boosting = true;
			this.boostTime = 0;
			this.boostTimeTotal = this.getRandom().nextInt(841) + 140;
			this.getEntityData().set(DATA_BOOST_TIME, this.boostTimeTotal);
			return true;
		}
	}

	public Pig getBreedOffspring(AgableMob agableMob) {
		return EntityType.PIG.create(this.level);
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return FOOD_ITEMS.test(itemStack);
	}
}
