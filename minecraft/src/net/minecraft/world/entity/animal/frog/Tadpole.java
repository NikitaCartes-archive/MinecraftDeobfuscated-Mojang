package net.minecraft.world.entity.animal.frog;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class Tadpole extends AbstractFish {
	@VisibleForTesting
	public static int ticksToBeFrog = Math.abs(-24000);
	public static float HITBOX_WIDTH = 0.4F;
	public static float HITBOX_HEIGHT = 0.3F;
	private int age;
	protected static final ImmutableList<SensorType<? extends Sensor<? super Tadpole>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.FROG_TEMPTATIONS
	);
	protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.PATH,
		MemoryModuleType.NEAREST_VISIBLE_ADULT,
		MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
		MemoryModuleType.IS_TEMPTED,
		MemoryModuleType.TEMPTING_PLAYER,
		MemoryModuleType.BREED_TARGET,
		MemoryModuleType.IS_PANICKING
	);

	public Tadpole(EntityType<? extends AbstractFish> entityType, Level level) {
		super(entityType, level);
		this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
		this.lookControl = new SmoothSwimmingLookControl(this, 10);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new WaterBoundPathNavigation(this, level);
	}

	@Override
	protected Brain.Provider<Tadpole> brainProvider() {
		return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return TadpoleAi.makeBrain(this.brainProvider().makeBrain(dynamic));
	}

	@Override
	public Brain<Tadpole> getBrain() {
		return (Brain<Tadpole>)super.getBrain();
	}

	@Override
	protected SoundEvent getFlopSound() {
		return SoundEvents.TADPOLE_FLOP;
	}

	@Override
	protected void customServerAiStep() {
		this.level().getProfiler().push("tadpoleBrain");
		this.getBrain().tick((ServerLevel)this.level(), this);
		this.level().getProfiler().pop();
		this.level().getProfiler().push("tadpoleActivityUpdate");
		TadpoleAi.updateActivity(this);
		this.level().getProfiler().pop();
		super.customServerAiStep();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 1.0).add(Attributes.MAX_HEALTH, 6.0);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level().isClientSide) {
			this.setAge(this.age + 1);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Age", this.age);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setAge(compoundTag.getInt("Age"));
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return null;
	}

	@Nullable
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.TADPOLE_HURT;
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.TADPOLE_DEATH;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (this.isFood(itemStack)) {
			this.feed(player, itemStack);
			return InteractionResult.sidedSuccess(this.level().isClientSide);
		} else {
			return (InteractionResult)Bucketable.bucketMobPickup(player, interactionHand, this).orElse(super.mobInteract(player, interactionHand));
		}
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	@Override
	public boolean fromBucket() {
		return true;
	}

	@Override
	public void setFromBucket(boolean bl) {
	}

	@Override
	public void saveToBucketTag(ItemStack itemStack) {
		Bucketable.saveDefaultDataToBucketTag(this, itemStack);
		CustomData.update(DataComponents.BUCKET_ENTITY_DATA, itemStack, compoundTag -> compoundTag.putInt("Age", this.getAge()));
	}

	@Override
	public void loadFromBucketTag(CompoundTag compoundTag) {
		Bucketable.loadDefaultDataFromBucketTag(this, compoundTag);
		if (compoundTag.contains("Age")) {
			this.setAge(compoundTag.getInt("Age"));
		}
	}

	@Override
	public ItemStack getBucketItemStack() {
		return new ItemStack(Items.TADPOLE_BUCKET);
	}

	@Override
	public SoundEvent getPickupSound() {
		return SoundEvents.BUCKET_FILL_TADPOLE;
	}

	private boolean isFood(ItemStack itemStack) {
		return itemStack.is(ItemTags.FROG_FOOD);
	}

	private void feed(Player player, ItemStack itemStack) {
		this.usePlayerItem(player, itemStack);
		this.ageUp(AgeableMob.getSpeedUpSecondsWhenFeeding(this.getTicksLeftUntilAdult()));
		this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
	}

	private void usePlayerItem(Player player, ItemStack itemStack) {
		itemStack.consume(1, player);
	}

	private int getAge() {
		return this.age;
	}

	private void ageUp(int i) {
		this.setAge(this.age + i * 20);
	}

	private void setAge(int i) {
		this.age = i;
		if (this.age >= ticksToBeFrog) {
			this.ageUp();
		}
	}

	private void ageUp() {
		if (this.level() instanceof ServerLevel serverLevel) {
			Frog frog = EntityType.FROG.create(this.level());
			if (frog != null) {
				frog.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
				frog.finalizeSpawn(serverLevel, this.level().getCurrentDifficultyAt(frog.blockPosition()), MobSpawnType.CONVERSION, null);
				frog.setNoAi(this.isNoAi());
				if (this.hasCustomName()) {
					frog.setCustomName(this.getCustomName());
					frog.setCustomNameVisible(this.isCustomNameVisible());
				}

				frog.setPersistenceRequired();
				this.playSound(SoundEvents.TADPOLE_GROW_UP, 0.15F, 1.0F);
				serverLevel.addFreshEntityWithPassengers(frog);
				this.discard();
			}
		}
	}

	private int getTicksLeftUntilAdult() {
		return Math.max(0, ticksToBeFrog - this.age);
	}

	@Override
	public boolean shouldDropExperience() {
		return false;
	}
}
