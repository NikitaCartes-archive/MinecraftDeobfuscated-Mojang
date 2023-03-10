/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Allay
extends PathfinderMob
implements InventoryCarrier {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int VIBRATION_EVENT_LISTENER_RANGE = 16;
    private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 1, 1);
    private static final int LIFTING_ITEM_ANIMATION_DURATION = 5;
    private static final float DANCING_LOOP_DURATION = 55.0f;
    private static final float SPINNING_ANIMATION_DURATION = 15.0f;
    private static final Ingredient DUPLICATION_ITEM = Ingredient.of(Items.AMETHYST_SHARD);
    private static final int DUPLICATION_COOLDOWN_TICKS = 6000;
    private static final int NUM_OF_DUPLICATION_HEARTS = 3;
    private static final double RIDING_OFFSET = 0.4;
    private static final EntityDataAccessor<Boolean> DATA_DANCING = SynchedEntityData.defineId(Allay.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CAN_DUPLICATE = SynchedEntityData.defineId(Allay.class, EntityDataSerializers.BOOLEAN);
    protected static final ImmutableList<SensorType<? extends Sensor<? super Allay>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.NEAREST_ITEMS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.PATH, MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.HURT_BY, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.LIKED_PLAYER, MemoryModuleType.LIKED_NOTEBLOCK_POSITION, MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleType.IS_PANICKING, new MemoryModuleType[0]);
    public static final ImmutableList<Float> THROW_SOUND_PITCHES = ImmutableList.of(Float.valueOf(0.5625f), Float.valueOf(0.625f), Float.valueOf(0.75f), Float.valueOf(0.9375f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.125f), Float.valueOf(1.25f), Float.valueOf(1.5f), Float.valueOf(1.875f), Float.valueOf(2.0f), Float.valueOf(2.25f), new Float[]{Float.valueOf(2.5f), Float.valueOf(3.0f), Float.valueOf(3.75f), Float.valueOf(4.0f)});
    private final DynamicGameEventListener<VibrationListener> dynamicVibrationListener;
    private final VibrationListener.VibrationListenerConfig vibrationListenerConfig;
    private final DynamicGameEventListener<JukeboxListener> dynamicJukeboxListener;
    private final SimpleContainer inventory = new SimpleContainer(1);
    @Nullable
    private BlockPos jukeboxPos;
    private long duplicationCooldown;
    private float holdingItemAnimationTicks;
    private float holdingItemAnimationTicks0;
    private float dancingAnimationTicks;
    private float spinningAnimationTicks;
    private float spinningAnimationTicks0;

    public Allay(EntityType<? extends Allay> entityType, Level level) {
        super((EntityType<? extends PathfinderMob>)entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setCanPickUpLoot(this.canPickUpLoot());
        EntityPositionSource positionSource = new EntityPositionSource(this, this.getEyeHeight());
        this.vibrationListenerConfig = new AllayVibrationListenerConfig();
        this.dynamicVibrationListener = new DynamicGameEventListener<VibrationListener>(new VibrationListener(positionSource, 16, this.vibrationListenerConfig));
        this.dynamicJukeboxListener = new DynamicGameEventListener<JukeboxListener>(new JukeboxListener(positionSource, GameEvent.JUKEBOX_PLAY.getNotificationRadius()));
    }

    protected Brain.Provider<Allay> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return AllayAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    public Brain<Allay> getBrain() {
        return super.getBrain();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.FLYING_SPEED, 0.1f).add(Attributes.MOVEMENT_SPEED, 0.1f).add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.FOLLOW_RANGE, 48.0);
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
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DANCING, false);
        this.entityData.define(DATA_CAN_DUPLICATE, true);
    }

    @Override
    public void travel(Vec3 vec3) {
        if (this.isControlledByLocalInstance()) {
            if (this.isInWater()) {
                this.moveRelative(0.02f, vec3);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8f));
            } else if (this.isInLava()) {
                this.moveRelative(0.02f, vec3);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
            } else {
                this.moveRelative(this.getSpeed(), vec3);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.91f));
            }
        }
        this.calculateEntityAnimation(false);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return entityDimensions.height * 0.6f;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        Entity entity = damageSource.getEntity();
        if (entity instanceof Player) {
            Player player = (Player)entity;
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
        return 0.4f;
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
            this.heal(1.0f);
        }
        if (this.isDancing() && this.shouldStopDancing() && this.tickCount % 20 == 0) {
            this.setDancing(false);
            this.jukeboxPos = null;
        }
        this.updateDuplicationCooldown();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            this.holdingItemAnimationTicks0 = this.holdingItemAnimationTicks;
            this.holdingItemAnimationTicks = this.hasItemInHand() ? Mth.clamp(this.holdingItemAnimationTicks + 1.0f, 0.0f, 5.0f) : Mth.clamp(this.holdingItemAnimationTicks - 1.0f, 0.0f, 5.0f);
            if (this.isDancing()) {
                this.dancingAnimationTicks += 1.0f;
                this.spinningAnimationTicks0 = this.spinningAnimationTicks;
                this.spinningAnimationTicks = this.isSpinning() ? (this.spinningAnimationTicks += 1.0f) : (this.spinningAnimationTicks -= 1.0f);
                this.spinningAnimationTicks = Mth.clamp(this.spinningAnimationTicks, 0.0f, 15.0f);
            } else {
                this.dancingAnimationTicks = 0.0f;
                this.spinningAnimationTicks = 0.0f;
                this.spinningAnimationTicks0 = 0.0f;
            }
        } else {
            this.dynamicVibrationListener.getListener().tick(this.level);
            if (this.isPanicking()) {
                this.setDancing(false);
            }
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
        if (this.isDancing() && this.isDuplicationItem(itemStack) && this.canDuplicate()) {
            this.duplicateAllay();
            this.level.broadcastEntityEvent(this, (byte)18);
            this.level.playSound(player, this, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 2.0f, 1.0f);
            this.removeInteractionItem(player, itemStack);
            return InteractionResult.SUCCESS;
        }
        if (itemStack2.isEmpty() && !itemStack.isEmpty()) {
            ItemStack itemStack3 = itemStack.copy();
            itemStack3.setCount(1);
            this.setItemInHand(InteractionHand.MAIN_HAND, itemStack3);
            this.removeInteractionItem(player, itemStack);
            this.level.playSound(player, this, SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.NEUTRAL, 2.0f, 1.0f);
            this.getBrain().setMemory(MemoryModuleType.LIKED_PLAYER, player.getUUID());
            return InteractionResult.SUCCESS;
        }
        if (!itemStack2.isEmpty() && interactionHand == InteractionHand.MAIN_HAND && itemStack.isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.level.playSound(player, this, SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.NEUTRAL, 2.0f, 1.0f);
            this.swing(InteractionHand.MAIN_HAND);
            for (ItemStack itemStack4 : this.getInventory().removeAllItems()) {
                BehaviorUtils.throwItem(this, itemStack4, this.position());
            }
            this.getBrain().eraseMemory(MemoryModuleType.LIKED_PLAYER);
            player.addItem(itemStack2);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, interactionHand);
    }

    public void setJukeboxPlaying(BlockPos blockPos, boolean bl) {
        if (bl) {
            if (!this.isDancing()) {
                this.jukeboxPos = blockPos;
                this.setDancing(true);
            }
        } else if (blockPos.equals(this.jukeboxPos) || this.jukeboxPos == null) {
            this.jukeboxPos = null;
            this.setDancing(false);
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
        return !itemStack2.isEmpty() && this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.inventory.canAddItem(itemStack) && this.allayConsidersItemEqual(itemStack2, itemStack);
    }

    private boolean allayConsidersItemEqual(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack.sameItem(itemStack2) && !this.hasNonMatchingPotion(itemStack, itemStack2);
    }

    private boolean hasNonMatchingPotion(ItemStack itemStack, ItemStack itemStack2) {
        boolean bl2;
        boolean bl;
        CompoundTag compoundTag = itemStack.getTag();
        boolean bl3 = bl = compoundTag != null && compoundTag.contains("Potion");
        if (!bl) {
            return false;
        }
        CompoundTag compoundTag2 = itemStack2.getTag();
        boolean bl4 = bl2 = compoundTag2 != null && compoundTag2.contains("Potion");
        if (!bl2) {
            return true;
        }
        Tag tag = compoundTag.get("Potion");
        Tag tag2 = compoundTag2.get("Potion");
        return tag != null && tag2 != null && !tag.equals(tag2);
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
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            biConsumer.accept(this.dynamicVibrationListener, serverLevel);
            biConsumer.accept(this.dynamicJukeboxListener, serverLevel);
        }
    }

    public boolean isDancing() {
        return this.entityData.get(DATA_DANCING);
    }

    public boolean isPanicking() {
        return this.brain.getMemory(MemoryModuleType.IS_PANICKING).isPresent();
    }

    public void setDancing(boolean bl) {
        if (this.level.isClientSide || !this.isEffectiveAi() || bl && this.isPanicking()) {
            return;
        }
        this.entityData.set(DATA_DANCING, bl);
    }

    private boolean shouldStopDancing() {
        return this.jukeboxPos == null || !this.jukeboxPos.closerToCenterThan(this.position(), GameEvent.JUKEBOX_PLAY.getNotificationRadius()) || !this.level.getBlockState(this.jukeboxPos).is(Blocks.JUKEBOX);
    }

    public float getHoldingItemAnimationProgress(float f) {
        return Mth.lerp(f, this.holdingItemAnimationTicks0, this.holdingItemAnimationTicks) / 5.0f;
    }

    public boolean isSpinning() {
        float f = this.dancingAnimationTicks % 55.0f;
        return f < 15.0f;
    }

    public float getSpinningProgress(float f) {
        return Mth.lerp(f, this.spinningAnimationTicks0, this.spinningAnimationTicks) / 15.0f;
    }

    @Override
    public boolean equipmentHasChanged(ItemStack itemStack, ItemStack itemStack2) {
        return !this.allayConsidersItemEqual(itemStack, itemStack2);
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
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        this.writeInventoryToTag(compoundTag);
        VibrationListener.codec(this.vibrationListenerConfig).encodeStart(NbtOps.INSTANCE, this.dynamicVibrationListener.getListener()).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("listener", (Tag)tag));
        compoundTag.putLong("DuplicationCooldown", this.duplicationCooldown);
        compoundTag.putBoolean("CanDuplicate", this.canDuplicate());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.readInventoryFromTag(compoundTag);
        if (compoundTag.contains("listener", 10)) {
            VibrationListener.codec(this.vibrationListenerConfig).parse(new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag.getCompound("listener"))).resultOrPartial(LOGGER::error).ifPresent(vibrationListener -> this.dynamicVibrationListener.updateListener((VibrationListener)vibrationListener, this.level));
        }
        this.duplicationCooldown = compoundTag.getInt("DuplicationCooldown");
        this.entityData.set(DATA_CAN_DUPLICATE, compoundTag.getBoolean("CanDuplicate"));
    }

    @Override
    protected boolean shouldStayCloseToLeashHolder() {
        return false;
    }

    private void updateDuplicationCooldown() {
        if (this.duplicationCooldown > 0L) {
            --this.duplicationCooldown;
        }
        if (!this.level.isClientSide() && this.duplicationCooldown == 0L && !this.canDuplicate()) {
            this.entityData.set(DATA_CAN_DUPLICATE, true);
        }
    }

    private boolean isDuplicationItem(ItemStack itemStack) {
        return DUPLICATION_ITEM.test(itemStack);
    }

    private void duplicateAllay() {
        Allay allay = EntityType.ALLAY.create(this.level);
        if (allay != null) {
            allay.moveTo(this.position());
            allay.setPersistenceRequired();
            allay.resetDuplicationCooldown();
            this.resetDuplicationCooldown();
            this.level.addFreshEntity(allay);
        }
    }

    private void resetDuplicationCooldown() {
        this.duplicationCooldown = 6000L;
        this.entityData.set(DATA_CAN_DUPLICATE, false);
    }

    private boolean canDuplicate() {
        return this.entityData.get(DATA_CAN_DUPLICATE);
    }

    private void removeInteractionItem(Player player, ItemStack itemStack) {
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)this.getEyeHeight() * 0.6, (double)this.getBbWidth() * 0.1);
    }

    @Override
    public double getMyRidingOffset() {
        return 0.4;
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 18) {
            for (int i = 0; i < 3; ++i) {
                this.spawnHeartParticle();
            }
        } else {
            super.handleEntityEvent(b);
        }
    }

    private void spawnHeartParticle() {
        double d = this.random.nextGaussian() * 0.02;
        double e = this.random.nextGaussian() * 0.02;
        double f = this.random.nextGaussian() * 0.02;
        this.level.addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
    }

    class AllayVibrationListenerConfig
    implements VibrationListener.VibrationListenerConfig {
        AllayVibrationListenerConfig() {
        }

        @Override
        public boolean shouldListen(ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, GameEvent.Context context) {
            if (Allay.this.isNoAi()) {
                return false;
            }
            Optional<GlobalPos> optional = Allay.this.getBrain().getMemory(MemoryModuleType.LIKED_NOTEBLOCK_POSITION);
            if (optional.isEmpty()) {
                return true;
            }
            GlobalPos globalPos = optional.get();
            return globalPos.dimension().equals(serverLevel.dimension()) && globalPos.pos().equals(blockPos);
        }

        @Override
        public void onSignalReceive(ServerLevel serverLevel, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity, @Nullable Entity entity2, float f) {
            if (gameEvent == GameEvent.NOTE_BLOCK_PLAY) {
                AllayAi.hearNoteblock(Allay.this, new BlockPos(blockPos));
            }
        }

        @Override
        public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.ALLAY_CAN_LISTEN;
        }
    }

    class JukeboxListener
    implements GameEventListener {
        private final PositionSource listenerSource;
        private final int listenerRadius;

        public JukeboxListener(PositionSource positionSource, int i) {
            this.listenerSource = positionSource;
            this.listenerRadius = i;
        }

        @Override
        public PositionSource getListenerSource() {
            return this.listenerSource;
        }

        @Override
        public int getListenerRadius() {
            return this.listenerRadius;
        }

        @Override
        public boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3) {
            if (gameEvent == GameEvent.JUKEBOX_PLAY) {
                Allay.this.setJukeboxPlaying(BlockPos.containing(vec3), true);
                return true;
            }
            if (gameEvent == GameEvent.JUKEBOX_STOP_PLAY) {
                Allay.this.setJukeboxPlaying(BlockPos.containing(vec3), false);
                return true;
            }
            return false;
        }
    }
}

