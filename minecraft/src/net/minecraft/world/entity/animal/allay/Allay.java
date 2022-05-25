package net.minecraft.world.entity.animal.allay;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Allay extends PathfinderMob implements InventoryCarrier, VibrationListener.VibrationListenerConfig {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int GAME_EVENT_LISTENER_RANGE = 16;
	private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 1, 1);
	private static final int ANIMATION_DURATION = 5;
	private static final float PATHFINDING_BOUNDING_BOX_PADDING = 0.5F;
	protected static final ImmutableList<SensorType<? extends Sensor<? super Allay>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.NEAREST_ITEMS
	);
	protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.PATH,
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.HURT_BY,
		MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
		MemoryModuleType.LIKED_PLAYER,
		MemoryModuleType.LIKED_NOTEBLOCK_POSITION,
		MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS,
		MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
		MemoryModuleType.IS_PANICKING
	);
	public static final ImmutableList<Float> THROW_SOUND_PITCHES = ImmutableList.of(
		0.5625F, 0.625F, 0.75F, 0.9375F, 1.0F, 1.0F, 1.125F, 1.25F, 1.5F, 1.875F, 2.0F, 2.25F, 2.5F, 3.0F, 3.75F, 4.0F
	);
	private final DynamicGameEventListener<VibrationListener> dynamicGameEventListener;
	private final SimpleContainer inventory = new SimpleContainer(1);
	private float holdingItemAnimationTicks;
	private float holdingItemAnimationTicks0;

	public Allay(EntityType<? extends Allay> entityType, Level level) {
		super(entityType, level);
		this.moveControl = new FlyingMoveControl(this, 20, true);
		this.setCanPickUpLoot(this.canPickUpLoot());
		this.dynamicGameEventListener = new DynamicGameEventListener<>(
			new VibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 16, this, null, 0.0F, 0)
		);
	}

	@Override
	protected Brain.Provider<Allay> brainProvider() {
		return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return AllayAi.makeBrain(this.brainProvider().makeBrain(dynamic));
	}

	@Override
	public Brain<Allay> getBrain() {
		return (Brain<Allay>)super.getBrain();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 20.0)
			.add(Attributes.FLYING_SPEED, 0.1F)
			.add(Attributes.MOVEMENT_SPEED, 0.1F)
			.add(Attributes.ATTACK_DAMAGE, 2.0)
			.add(Attributes.FOLLOW_RANGE, 48.0);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level);
		flyingPathNavigation.setCanOpenDoors(false);
		flyingPathNavigation.setCanFloat(true);
		flyingPathNavigation.setCanPassDoors(true);
		return flyingPathNavigation;
	}

	@Override
	public void travel(Vec3 vec3) {
		if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
			if (this.isInWater()) {
				this.moveRelative(0.02F, vec3);
				this.move(MoverType.SELF, this.getDeltaMovement());
				this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
			} else if (this.isInLava()) {
				this.moveRelative(0.02F, vec3);
				this.move(MoverType.SELF, this.getDeltaMovement());
				this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
			} else {
				this.moveRelative(this.getSpeed(), vec3);
				this.move(MoverType.SELF, this.getDeltaMovement());
				this.setDeltaMovement(this.getDeltaMovement().scale(0.91F));
			}
		}

		this.calculateEntityAnimation(this, false);
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return entityDimensions.height * 0.6F;
	}

	@Override
	public boolean causeFallDamage(float f, float g, DamageSource damageSource) {
		return false;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (damageSource.getEntity() instanceof Player player) {
			Optional<UUID> optional = this.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER);
			if (optional.isPresent() && player.getUUID().equals(optional.get())) {
				return false;
			}
		}

		return super.hurt(damageSource, f);
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
	}

	@Override
	protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return this.hasItemInSlot(EquipmentSlot.MAINHAND) ? SoundEvents.ALLAY_AMBIENT_WITH_ITEM : SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.ALLAY_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ALLAY_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	protected void customServerAiStep() {
		this.level.getProfiler().push("allayBrain");
		this.getBrain().tick((ServerLevel)this.level, this);
		this.level.getProfiler().pop();
		this.level.getProfiler().push("allayActivityUpdate");
		AllayAi.updateActivity(this);
		this.level.getProfiler().pop();
		super.customServerAiStep();
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level.isClientSide && this.isAlive() && this.tickCount % 10 == 0) {
			this.heal(1.0F);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level.isClientSide) {
			this.holdingItemAnimationTicks0 = this.holdingItemAnimationTicks;
			if (this.hasItemInHand()) {
				this.holdingItemAnimationTicks = Mth.clamp(this.holdingItemAnimationTicks + 1.0F, 0.0F, 5.0F);
			} else {
				this.holdingItemAnimationTicks = Mth.clamp(this.holdingItemAnimationTicks - 1.0F, 0.0F, 5.0F);
			}
		} else {
			this.dynamicGameEventListener.getListener().tick(this.level);
		}
	}

	@Override
	public boolean canPickUpLoot() {
		return !this.isOnPickupCooldown() && this.hasItemInHand();
	}

	public boolean hasItemInHand() {
		return !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();
	}

	@Override
	public boolean canTakeItem(ItemStack itemStack) {
		return false;
	}

	private boolean isOnPickupCooldown() {
		return this.getBrain().checkMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryStatus.VALUE_PRESENT);
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		ItemStack itemStack2 = this.getItemInHand(InteractionHand.MAIN_HAND);
		if (itemStack2.isEmpty() && !itemStack.isEmpty()) {
			ItemStack itemStack3 = itemStack.copy();
			itemStack3.setCount(1);
			this.setItemInHand(InteractionHand.MAIN_HAND, itemStack3);
			if (!player.getAbilities().instabuild) {
				itemStack.shrink(1);
			}

			this.level.playSound(player, this, SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.NEUTRAL, 2.0F, 1.0F);
			this.getBrain().setMemory(MemoryModuleType.LIKED_PLAYER, player.getUUID());
			return InteractionResult.SUCCESS;
		} else if (!itemStack2.isEmpty() && interactionHand == InteractionHand.MAIN_HAND && itemStack.isEmpty()) {
			this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
			this.level.playSound(player, this, SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.NEUTRAL, 2.0F, 1.0F);
			this.swing(InteractionHand.MAIN_HAND);

			for (ItemStack itemStack4 : this.getInventory().removeAllItems()) {
				BehaviorUtils.throwItem(this, itemStack4, this.position());
			}

			this.getBrain().eraseMemory(MemoryModuleType.LIKED_PLAYER);
			player.addItem(itemStack2);
			return InteractionResult.SUCCESS;
		} else {
			return super.mobInteract(player, interactionHand);
		}
	}

	@Override
	public SimpleContainer getInventory() {
		return this.inventory;
	}

	@Override
	protected Vec3i getPickupReach() {
		return ITEM_PICKUP_REACH;
	}

	@Override
	public boolean wantsToPickUp(ItemStack itemStack) {
		ItemStack itemStack2 = this.getItemInHand(InteractionHand.MAIN_HAND);
		return !itemStack2.isEmpty() && itemStack2.sameItemStackIgnoreDurability(itemStack) && this.inventory.canAddItem(itemStack);
	}

	@Override
	protected void pickUpItem(ItemEntity itemEntity) {
		InventoryCarrier.pickUpItem(this, this, itemEntity);
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	@Override
	public boolean isFlapping() {
		return !this.isOnGround();
	}

	@Override
	public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> biConsumer) {
		if (this.level instanceof ServerLevel serverLevel) {
			biConsumer.accept(this.dynamicGameEventListener, serverLevel);
		}
	}

	public boolean isFlying() {
		return this.animationSpeed > 0.3F;
	}

	public float getHoldingItemAnimationProgress(float f) {
		return Mth.lerp(f, this.holdingItemAnimationTicks0, this.holdingItemAnimationTicks) / 5.0F;
	}

	@Override
	protected void dropEquipment() {
		super.dropEquipment();
		this.inventory.removeAllItems().forEach(this::spawnAtLocation);
		ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
		if (!itemStack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemStack)) {
			this.spawnAtLocation(itemStack);
			this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		}
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return false;
	}

	@Override
	public boolean shouldListen(ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, GameEvent.Context context) {
		if (this.level != serverLevel || this.isRemoved() || this.isNoAi()) {
			return false;
		} else if (!this.brain.hasMemoryValue(MemoryModuleType.LIKED_NOTEBLOCK_POSITION)) {
			return true;
		} else {
			Optional<GlobalPos> optional = this.brain.getMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
			return optional.isPresent() && ((GlobalPos)optional.get()).dimension() == serverLevel.dimension() && ((GlobalPos)optional.get()).pos().equals(blockPos);
		}
	}

	@Override
	public void onSignalReceive(
		ServerLevel serverLevel,
		GameEventListener gameEventListener,
		BlockPos blockPos,
		GameEvent gameEvent,
		@Nullable Entity entity,
		@Nullable Entity entity2,
		float f
	) {
		if (gameEvent == GameEvent.NOTE_BLOCK_PLAY) {
			AllayAi.hearNoteblock(this, new BlockPos(blockPos));
		}
	}

	@Override
	public TagKey<GameEvent> getListenableEvents() {
		return GameEventTags.ALLAY_CAN_LISTEN;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.put("Inventory", this.inventory.createTag());
		VibrationListener.codec(this)
			.encodeStart(NbtOps.INSTANCE, this.dynamicGameEventListener.getListener())
			.resultOrPartial(LOGGER::error)
			.ifPresent(tag -> compoundTag.put("listener", tag));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.inventory.fromTag(compoundTag.getList("Inventory", 10));
		if (compoundTag.contains("listener", 10)) {
			VibrationListener.codec(this)
				.parse(new Dynamic<>(NbtOps.INSTANCE, compoundTag.getCompound("listener")))
				.resultOrPartial(LOGGER::error)
				.ifPresent(vibrationListener -> this.dynamicGameEventListener.updateListener(vibrationListener, this.level));
		}
	}

	@Override
	protected boolean shouldStayCloseToLeashHolder() {
		return false;
	}

	@Override
	public Iterable<BlockPos> iteratePathfindingStartNodeCandidatePositions() {
		AABB aABB = this.getBoundingBox();
		int i = Mth.floor(aABB.minX - 0.5);
		int j = Mth.floor(aABB.maxX + 0.5);
		int k = Mth.floor(aABB.minZ - 0.5);
		int l = Mth.floor(aABB.maxZ + 0.5);
		int m = Mth.floor(aABB.minY - 0.5);
		int n = Mth.floor(aABB.maxY + 0.5);
		return BlockPos.betweenClosed(i, m, k, j, n, l);
	}
}
