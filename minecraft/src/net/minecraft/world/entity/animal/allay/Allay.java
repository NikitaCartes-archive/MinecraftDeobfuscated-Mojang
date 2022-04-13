package net.minecraft.world.entity.animal.allay;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public class Allay extends PathfinderMob implements InventoryCarrier, GameEventListener {
	private static final boolean USE_V2_MOVE_PARTICLES = false;
	private static final int GAME_EVENT_LISTENER_RANGE = 16;
	private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 1, 1);
	protected static final ImmutableList<SensorType<? extends Sensor<? super Allay>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS
	);
	protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.PATH,
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
		MemoryModuleType.LIKED_PLAYER,
		MemoryModuleType.LIKED_NOTEBLOCK_POSITION,
		MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS,
		MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS
	);
	private final EntityPositionSource entityPositionSource = new EntityPositionSource(this, this.getEyeHeight());
	private final DynamicGameEventListener<Allay> dynamicGameEventListener;
	private final SimpleContainer inventory = new SimpleContainer(1);

	public Allay(EntityType<? extends Allay> entityType, Level level) {
		super(entityType, level);
		this.moveControl = new FlyingMoveControl(this, 20, true);
		this.setCanPickUpLoot(this.canPickUpLoot());
		this.dynamicGameEventListener = new DynamicGameEventListener<>(this);
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
			.add(Attributes.MAX_HEALTH, 10.0)
			.add(Attributes.FLYING_SPEED, 0.6F)
			.add(Attributes.MOVEMENT_SPEED, 0.3F)
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
				float f;
				if (this.onGround) {
					f = this.level.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0, this.getZ())).getBlock().getFriction() * 0.91F;
				} else {
					f = 0.91F;
				}

				float g = Mth.cube(0.6F) * Mth.cube(0.91F) / Mth.cube(f);
				this.moveRelative(this.onGround ? 0.1F * g : 0.02F, vec3);
				this.move(MoverType.SELF, this.getDeltaMovement());
				this.setDeltaMovement(this.getDeltaMovement().scale((double)f));
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
	public boolean canPickUpLoot() {
		return !this.isOnPickupCooldown() && !this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();
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
		return !itemStack2.isEmpty() && itemStack2.sameItemStackIgnoreDurability(itemStack) && this.inventory.canAddItem(itemStack2);
	}

	@Override
	protected void pickUpItem(ItemEntity itemEntity) {
		ItemStack itemStack = itemEntity.getItem();
		if (this.wantsToPickUp(itemStack)) {
			SimpleContainer simpleContainer = this.getInventory();
			boolean bl = simpleContainer.canAddItem(itemStack);
			if (!bl) {
				return;
			}

			this.onItemPickup(itemEntity);
			this.take(itemEntity, itemStack.getCount());
			ItemStack itemStack2 = simpleContainer.addItem(itemStack);
			if (itemStack2.isEmpty()) {
				itemEntity.discard();
			} else {
				itemStack.setCount(itemStack2.getCount());
			}
		}
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
	public PositionSource getListenerSource() {
		return this.entityPositionSource;
	}

	@Override
	public int getListenerRadius() {
		return 16;
	}

	@Override
	public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> biConsumer) {
		if (this.level instanceof ServerLevel serverLevel) {
			biConsumer.accept(this.dynamicGameEventListener, serverLevel);
		}
	}

	@Override
	public boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3) {
		if (gameEvent != GameEvent.NOTE_BLOCK_PLAY) {
			return false;
		} else {
			AllayAi.hearNoteblock(this, new BlockPos(vec3));
			return true;
		}
	}
}
