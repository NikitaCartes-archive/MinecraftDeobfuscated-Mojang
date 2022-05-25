/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.decoration;

import com.mojang.logging.LogUtils;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ItemFrame
extends HangingEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_ROTATION = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.INT);
    public static final int NUM_ROTATIONS = 8;
    private float dropChance = 1.0f;
    private boolean fixed;

    public ItemFrame(EntityType<? extends ItemFrame> entityType, Level level) {
        super((EntityType<? extends HangingEntity>)entityType, level);
    }

    public ItemFrame(Level level, BlockPos blockPos, Direction direction) {
        this(EntityType.ITEM_FRAME, level, blockPos, direction);
    }

    public ItemFrame(EntityType<? extends ItemFrame> entityType, Level level, BlockPos blockPos, Direction direction) {
        super(entityType, level, blockPos);
        this.setDirection(direction);
    }

    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 0.0f;
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
        this.getEntityData().define(DATA_ROTATION, 0);
    }

    @Override
    protected void setDirection(Direction direction) {
        Validate.notNull(direction);
        this.direction = direction;
        if (direction.getAxis().isHorizontal()) {
            this.setXRot(0.0f);
            this.setYRot(this.direction.get2DDataValue() * 90);
        } else {
            this.setXRot(-90 * direction.getAxisDirection().getStep());
            this.setYRot(0.0f);
        }
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    @Override
    protected void recalculateBoundingBox() {
        if (this.direction == null) {
            return;
        }
        double d = 0.46875;
        double e = (double)this.pos.getX() + 0.5 - (double)this.direction.getStepX() * 0.46875;
        double f = (double)this.pos.getY() + 0.5 - (double)this.direction.getStepY() * 0.46875;
        double g = (double)this.pos.getZ() + 0.5 - (double)this.direction.getStepZ() * 0.46875;
        this.setPosRaw(e, f, g);
        double h = this.getWidth();
        double i = this.getHeight();
        double j = this.getWidth();
        Direction.Axis axis = this.direction.getAxis();
        switch (axis) {
            case X: {
                h = 1.0;
                break;
            }
            case Y: {
                i = 1.0;
                break;
            }
            case Z: {
                j = 1.0;
            }
        }
        this.setBoundingBox(new AABB(e - (h /= 32.0), f - (i /= 32.0), g - (j /= 32.0), e + h, f + i, g + j));
    }

    @Override
    public boolean survives() {
        if (this.fixed) {
            return true;
        }
        if (!this.level.noCollision(this)) {
            return false;
        }
        BlockState blockState = this.level.getBlockState(this.pos.relative(this.direction.getOpposite()));
        if (!(blockState.getMaterial().isSolid() || this.direction.getAxis().isHorizontal() && DiodeBlock.isDiode(blockState))) {
            return false;
        }
        return this.level.getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
    }

    @Override
    public void move(MoverType moverType, Vec3 vec3) {
        if (!this.fixed) {
            super.move(moverType, vec3);
        }
    }

    @Override
    public void push(double d, double e, double f) {
        if (!this.fixed) {
            super.push(d, e, f);
        }
    }

    @Override
    public float getPickRadius() {
        return 0.0f;
    }

    @Override
    public void kill() {
        this.removeFramedMap(this.getItem());
        super.kill();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.fixed) {
            if (damageSource == DamageSource.OUT_OF_WORLD || damageSource.isCreativePlayer()) {
                return super.hurt(damageSource, f);
            }
            return false;
        }
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        if (!damageSource.isExplosion() && !this.getItem().isEmpty()) {
            if (!this.level.isClientSide) {
                this.dropItem(damageSource.getEntity(), false);
                this.playSound(this.getRemoveItemSound(), 1.0f, 1.0f);
            }
            return true;
        }
        return super.hurt(damageSource, f);
    }

    public SoundEvent getRemoveItemSound() {
        return SoundEvents.ITEM_FRAME_REMOVE_ITEM;
    }

    @Override
    public int getWidth() {
        return 12;
    }

    @Override
    public int getHeight() {
        return 12;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        double e = 16.0;
        return d < (e *= 64.0 * ItemFrame.getViewScale()) * e;
    }

    @Override
    public void dropItem(@Nullable Entity entity) {
        this.playSound(this.getBreakSound(), 1.0f, 1.0f);
        this.dropItem(entity, true);
    }

    public SoundEvent getBreakSound() {
        return SoundEvents.ITEM_FRAME_BREAK;
    }

    @Override
    public void playPlacementSound() {
        this.playSound(this.getPlaceSound(), 1.0f, 1.0f);
    }

    public SoundEvent getPlaceSound() {
        return SoundEvents.ITEM_FRAME_PLACE;
    }

    private void dropItem(@Nullable Entity entity, boolean bl) {
        if (this.fixed) {
            return;
        }
        ItemStack itemStack = this.getItem();
        this.setItem(ItemStack.EMPTY);
        if (!this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            if (entity == null) {
                this.removeFramedMap(itemStack);
            }
            return;
        }
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getAbilities().instabuild) {
                this.removeFramedMap(itemStack);
                return;
            }
        }
        if (bl) {
            this.spawnAtLocation(this.getFrameItemStack());
        }
        if (!itemStack.isEmpty()) {
            itemStack = itemStack.copy();
            this.removeFramedMap(itemStack);
            if (this.random.nextFloat() < this.dropChance) {
                this.spawnAtLocation(itemStack);
            }
        }
    }

    private void removeFramedMap(ItemStack itemStack) {
        this.getFramedMapId().ifPresent(i -> {
            MapItemSavedData mapItemSavedData = MapItem.getSavedData(i, this.level);
            if (mapItemSavedData != null) {
                mapItemSavedData.removedFromFrame(this.pos, this.getId());
                mapItemSavedData.setDirty(true);
            }
        });
        itemStack.setEntityRepresentation(null);
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public OptionalInt getFramedMapId() {
        Integer integer;
        ItemStack itemStack = this.getItem();
        if (itemStack.is(Items.FILLED_MAP) && (integer = MapItem.getMapId(itemStack)) != null) {
            return OptionalInt.of(integer);
        }
        return OptionalInt.empty();
    }

    public boolean hasFramedMap() {
        return this.getFramedMapId().isPresent();
    }

    public void setItem(ItemStack itemStack) {
        this.setItem(itemStack, true);
    }

    public void setItem(ItemStack itemStack, boolean bl) {
        if (!itemStack.isEmpty()) {
            itemStack = itemStack.copy();
            itemStack.setCount(1);
        }
        this.onItemChanged(itemStack);
        this.getEntityData().set(DATA_ITEM, itemStack);
        if (!itemStack.isEmpty()) {
            this.playSound(this.getAddItemSound(), 1.0f, 1.0f);
        }
        if (bl && this.pos != null) {
            this.level.updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }
    }

    public SoundEvent getAddItemSound() {
        return SoundEvents.ITEM_FRAME_ADD_ITEM;
    }

    @Override
    public SlotAccess getSlot(int i) {
        if (i == 0) {
            return new SlotAccess(){

                @Override
                public ItemStack get() {
                    return ItemFrame.this.getItem();
                }

                @Override
                public boolean set(ItemStack itemStack) {
                    ItemFrame.this.setItem(itemStack);
                    return true;
                }
            };
        }
        return super.getSlot(i);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (entityDataAccessor.equals(DATA_ITEM)) {
            this.onItemChanged(this.getItem());
        }
    }

    private void onItemChanged(ItemStack itemStack) {
        if (!itemStack.isEmpty() && itemStack.getFrame() != this) {
            itemStack.setEntityRepresentation(this);
        }
        this.recalculateBoundingBox();
    }

    public int getRotation() {
        return this.getEntityData().get(DATA_ROTATION);
    }

    public void setRotation(int i) {
        this.setRotation(i, true);
    }

    private void setRotation(int i, boolean bl) {
        this.getEntityData().set(DATA_ROTATION, i % 8);
        if (bl && this.pos != null) {
            this.level.updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (!this.getItem().isEmpty()) {
            compoundTag.put("Item", this.getItem().save(new CompoundTag()));
            compoundTag.putByte("ItemRotation", (byte)this.getRotation());
            compoundTag.putFloat("ItemDropChance", this.dropChance);
        }
        compoundTag.putByte("Facing", (byte)this.direction.get3DDataValue());
        compoundTag.putBoolean("Invisible", this.isInvisible());
        compoundTag.putBoolean("Fixed", this.fixed);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        CompoundTag compoundTag2 = compoundTag.getCompound("Item");
        if (compoundTag2 != null && !compoundTag2.isEmpty()) {
            ItemStack itemStack2;
            ItemStack itemStack = ItemStack.of(compoundTag2);
            if (itemStack.isEmpty()) {
                LOGGER.warn("Unable to load item from: {}", (Object)compoundTag2);
            }
            if (!(itemStack2 = this.getItem()).isEmpty() && !ItemStack.matches(itemStack, itemStack2)) {
                this.removeFramedMap(itemStack2);
            }
            this.setItem(itemStack, false);
            this.setRotation(compoundTag.getByte("ItemRotation"), false);
            if (compoundTag.contains("ItemDropChance", 99)) {
                this.dropChance = compoundTag.getFloat("ItemDropChance");
            }
        }
        this.setDirection(Direction.from3DDataValue(compoundTag.getByte("Facing")));
        this.setInvisible(compoundTag.getBoolean("Invisible"));
        this.fixed = compoundTag.getBoolean("Fixed");
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        boolean bl2;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        boolean bl = !this.getItem().isEmpty();
        boolean bl3 = bl2 = !itemStack.isEmpty();
        if (this.fixed) {
            return InteractionResult.PASS;
        }
        if (this.level.isClientSide) {
            return bl || bl2 ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!bl) {
            if (bl2 && !this.isRemoved()) {
                MapItemSavedData mapItemSavedData;
                if (itemStack.is(Items.FILLED_MAP) && (mapItemSavedData = MapItem.getSavedData(itemStack, this.level)) != null && mapItemSavedData.isTrackedCountOverLimit(256)) {
                    return InteractionResult.FAIL;
                }
                this.setItem(itemStack);
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
        } else {
            this.playSound(this.getRotateItemSound(), 1.0f, 1.0f);
            this.setRotation(this.getRotation() + 1);
        }
        return InteractionResult.CONSUME;
    }

    public SoundEvent getRotateItemSound() {
        return SoundEvents.ITEM_FRAME_ROTATE_ITEM;
    }

    public int getAnalogOutput() {
        if (this.getItem().isEmpty()) {
            return 0;
        }
        return this.getRotation() % 8 + 1;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, this.direction.get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        this.setDirection(Direction.from3DDataValue(clientboundAddEntityPacket.getData()));
    }

    @Override
    public ItemStack getPickResult() {
        ItemStack itemStack = this.getItem();
        if (itemStack.isEmpty()) {
            return this.getFrameItemStack();
        }
        return itemStack.copy();
    }

    protected ItemStack getFrameItemStack() {
        return new ItemStack(Items.ITEM_FRAME);
    }

    @Override
    public float getVisualRotationYInDegrees() {
        Direction direction = this.getDirection();
        int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
        return Mth.wrapDegrees(180 + direction.get2DDataValue() * 90 + this.getRotation() * 45 + i);
    }
}

