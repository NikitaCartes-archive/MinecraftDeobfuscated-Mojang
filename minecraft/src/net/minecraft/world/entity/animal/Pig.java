package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
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
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Pig extends Animal implements ItemSteerable, Saddleable {
	private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.INT);
	private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.CARROT, Items.POTATO, Items.BEETROOT);
	private final ItemBasedSteering steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME, DATA_SADDLE_ID);

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

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.25);
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
			this.steering.onSynced();
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
		this.steering.addAdditionalSaveData(compoundTag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.steering.readAdditionalSaveData(compoundTag);
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
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		boolean bl = this.isFood(player.getItemInHand(interactionHand));
		if (!bl && this.isSaddled() && !this.isVehicle() && !player.isSecondaryUseActive()) {
			if (!this.level.isClientSide) {
				player.startRiding(this);
			}

			return InteractionResult.sidedSuccess(this.level.isClientSide);
		} else {
			InteractionResult interactionResult = super.mobInteract(player, interactionHand);
			if (!interactionResult.consumesAction()) {
				ItemStack itemStack = player.getItemInHand(interactionHand);
				return itemStack.getItem() == Items.SADDLE ? itemStack.interactLivingEntity(player, this, interactionHand) : InteractionResult.PASS;
			} else {
				return interactionResult;
			}
		}
	}

	@Override
	public boolean isSaddleable() {
		return this.isAlive() && !this.isBaby();
	}

	@Override
	protected void dropEquipment() {
		super.dropEquipment();
		if (this.isSaddled()) {
			this.spawnAtLocation(Items.SADDLE);
		}
	}

	@Override
	public boolean isSaddled() {
		return this.steering.hasSaddle();
	}

	@Override
	public void equipSaddle(@Nullable SoundSource soundSource) {
		this.steering.setSaddle(true);
		if (soundSource != null) {
			this.level.playSound(null, this, SoundEvents.PIG_SADDLE, soundSource, 0.5F, 1.0F);
		}
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
		Direction direction = this.getMotionDirection();
		if (direction.getAxis() == Direction.Axis.Y) {
			return super.getDismountLocationForPassenger(livingEntity);
		} else {
			int[][] is = DismountHelper.offsetsForDirection(direction);
			BlockPos blockPos = this.blockPosition();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (Pose pose : livingEntity.getDismountPoses()) {
				AABB aABB = livingEntity.getLocalBoundsForPose(pose);

				for (int[] js : is) {
					mutableBlockPos.set(blockPos.getX() + js[0], blockPos.getY(), blockPos.getZ() + js[1]);
					double d = this.level.getBlockFloorHeight(mutableBlockPos);
					if (DismountHelper.isBlockFloorValid(d)) {
						Vec3 vec3 = Vec3.upFromBottomCenterOf(mutableBlockPos, d);
						if (DismountHelper.canDismountTo(this.level, livingEntity, aABB.move(vec3))) {
							livingEntity.setPose(pose);
							return vec3;
						}
					}
				}
			}

			return super.getDismountLocationForPassenger(livingEntity);
		}
	}

	@Override
	public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
		if (serverLevel.getDifficulty() != Difficulty.PEACEFUL) {
			ZombifiedPiglin zombifiedPiglin = EntityType.ZOMBIFIED_PIGLIN.create(serverLevel);
			zombifiedPiglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
			zombifiedPiglin.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
			zombifiedPiglin.setNoAi(this.isNoAi());
			zombifiedPiglin.setBaby(this.isBaby());
			if (this.hasCustomName()) {
				zombifiedPiglin.setCustomName(this.getCustomName());
				zombifiedPiglin.setCustomNameVisible(this.isCustomNameVisible());
			}

			zombifiedPiglin.setPersistenceRequired();
			serverLevel.addFreshEntity(zombifiedPiglin);
			this.remove();
		} else {
			super.thunderHit(serverLevel, lightningBolt);
		}
	}

	@Override
	public void travel(Vec3 vec3) {
		this.travel(this, this.steering, vec3);
	}

	@Override
	public float getSteeringSpeed() {
		return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225F;
	}

	@Override
	public void travelWithInput(Vec3 vec3) {
		super.travel(vec3);
	}

	@Override
	public boolean boost() {
		return this.steering.boost(this.getRandom());
	}

	public Pig getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob) {
		return EntityType.PIG.create(serverLevel);
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return FOOD_ITEMS.test(itemStack);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getLeashOffset() {
		return new Vec3(0.0, (double)(0.6F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
	}
}
