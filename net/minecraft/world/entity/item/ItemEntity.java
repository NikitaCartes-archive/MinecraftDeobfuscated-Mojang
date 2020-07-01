/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.item;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ItemEntity
extends Entity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemEntity.class, EntityDataSerializers.ITEM_STACK);
    private int age;
    private int pickupDelay;
    private int health = 5;
    private UUID thrower;
    private UUID owner;
    public final float bobOffs;

    public ItemEntity(EntityType<? extends ItemEntity> entityType, Level level) {
        super(entityType, level);
        this.bobOffs = (float)(Math.random() * Math.PI * 2.0);
    }

    public ItemEntity(Level level, double d, double e, double f) {
        this((EntityType<? extends ItemEntity>)EntityType.ITEM, level);
        this.setPos(d, e, f);
        this.yRot = this.random.nextFloat() * 360.0f;
        this.setDeltaMovement(this.random.nextDouble() * 0.2 - 0.1, 0.2, this.random.nextDouble() * 0.2 - 0.1);
    }

    public ItemEntity(Level level, double d, double e, double f, ItemStack itemStack) {
        this(level, d, e, f);
        this.setItem(itemStack);
    }

    @Environment(value=EnvType.CLIENT)
    private ItemEntity(ItemEntity itemEntity) {
        super(itemEntity.getType(), itemEntity.level);
        this.setItem(itemEntity.getItem().copy());
        this.copyPosition(itemEntity);
        this.age = itemEntity.age;
        this.bobOffs = itemEntity.bobOffs;
    }

    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        double d;
        int i;
        if (this.getItem().isEmpty()) {
            this.remove();
            return;
        }
        super.tick();
        if (this.pickupDelay > 0 && this.pickupDelay != Short.MAX_VALUE) {
            --this.pickupDelay;
        }
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        Vec3 vec3 = this.getDeltaMovement();
        float f = this.getEyeHeight() - 0.11111111f;
        if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > (double)f) {
            this.setUnderwaterMovement();
        } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double)f) {
            this.setUnderLavaMovement();
        } else if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }
        if (this.level.isClientSide) {
            this.noPhysics = false;
        } else {
            boolean bl = this.noPhysics = !this.level.noCollision(this);
            if (this.noPhysics) {
                this.checkInBlock(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
            }
        }
        if (!this.onGround || ItemEntity.getHorizontalDistanceSqr(this.getDeltaMovement()) > (double)1.0E-5f || (this.tickCount + this.getId()) % 4 == 0) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            float g = 0.98f;
            if (this.onGround) {
                g = this.level.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0, this.getZ())).getBlock().getFriction() * 0.98f;
            }
            this.setDeltaMovement(this.getDeltaMovement().multiply(g, 0.98, g));
            if (this.onGround) {
                Vec3 vec32 = this.getDeltaMovement();
                if (vec32.y < 0.0) {
                    this.setDeltaMovement(vec32.multiply(1.0, -0.5, 1.0));
                }
            }
        }
        boolean bl = Mth.floor(this.xo) != Mth.floor(this.getX()) || Mth.floor(this.yo) != Mth.floor(this.getY()) || Mth.floor(this.zo) != Mth.floor(this.getZ());
        int n = i = bl ? 2 : 40;
        if (this.tickCount % i == 0) {
            if (this.level.getFluidState(this.blockPosition()).is(FluidTags.LAVA) && !this.fireImmune()) {
                this.playSound(SoundEvents.GENERIC_BURN, 0.4f, 2.0f + this.random.nextFloat() * 0.4f);
            }
            if (!this.level.isClientSide && this.isMergable()) {
                this.mergeWithNeighbours();
            }
        }
        if (this.age != Short.MIN_VALUE) {
            ++this.age;
        }
        this.hasImpulse |= this.updateInWaterStateAndDoFluidPushing();
        if (!this.level.isClientSide && (d = this.getDeltaMovement().subtract(vec3).lengthSqr()) > 0.01) {
            this.hasImpulse = true;
        }
        if (!this.level.isClientSide && this.age >= 6000) {
            this.remove();
        }
    }

    private void setUnderwaterMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * (double)0.99f, vec3.y + (double)(vec3.y < (double)0.06f ? 5.0E-4f : 0.0f), vec3.z * (double)0.99f);
    }

    private void setUnderLavaMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * (double)0.95f, vec3.y + (double)(vec3.y < (double)0.06f ? 5.0E-4f : 0.0f), vec3.z * (double)0.95f);
    }

    private void mergeWithNeighbours() {
        if (!this.isMergable()) {
            return;
        }
        List<ItemEntity> list = this.level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.5, 0.0, 0.5), itemEntity -> itemEntity != this && itemEntity.isMergable());
        for (ItemEntity itemEntity2 : list) {
            if (!itemEntity2.isMergable()) continue;
            this.tryToMerge(itemEntity2);
            if (!this.removed) continue;
            break;
        }
    }

    private boolean isMergable() {
        ItemStack itemStack = this.getItem();
        return this.isAlive() && this.pickupDelay != Short.MAX_VALUE && this.age != Short.MIN_VALUE && this.age < 6000 && itemStack.getCount() < itemStack.getMaxStackSize();
    }

    private void tryToMerge(ItemEntity itemEntity) {
        ItemStack itemStack = this.getItem();
        ItemStack itemStack2 = itemEntity.getItem();
        if (!Objects.equals(this.getOwner(), itemEntity.getOwner()) || !ItemEntity.areMergable(itemStack, itemStack2)) {
            return;
        }
        if (itemStack2.getCount() < itemStack.getCount()) {
            ItemEntity.merge(this, itemStack, itemEntity, itemStack2);
        } else {
            ItemEntity.merge(itemEntity, itemStack2, this, itemStack);
        }
    }

    public static boolean areMergable(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack2.getItem() != itemStack.getItem()) {
            return false;
        }
        if (itemStack2.getCount() + itemStack.getCount() > itemStack2.getMaxStackSize()) {
            return false;
        }
        if (itemStack2.hasTag() ^ itemStack.hasTag()) {
            return false;
        }
        return !itemStack2.hasTag() || itemStack2.getTag().equals(itemStack.getTag());
    }

    public static ItemStack merge(ItemStack itemStack, ItemStack itemStack2, int i) {
        int j = Math.min(Math.min(itemStack.getMaxStackSize(), i) - itemStack.getCount(), itemStack2.getCount());
        ItemStack itemStack3 = itemStack.copy();
        itemStack3.grow(j);
        itemStack2.shrink(j);
        return itemStack3;
    }

    private static void merge(ItemEntity itemEntity, ItemStack itemStack, ItemStack itemStack2) {
        ItemStack itemStack3 = ItemEntity.merge(itemStack, itemStack2, 64);
        itemEntity.setItem(itemStack3);
    }

    private static void merge(ItemEntity itemEntity, ItemStack itemStack, ItemEntity itemEntity2, ItemStack itemStack2) {
        ItemEntity.merge(itemEntity, itemStack, itemStack2);
        itemEntity.pickupDelay = Math.max(itemEntity.pickupDelay, itemEntity2.pickupDelay);
        itemEntity.age = Math.min(itemEntity.age, itemEntity2.age);
        if (itemStack2.isEmpty()) {
            itemEntity2.remove();
        }
    }

    @Override
    public boolean fireImmune() {
        return this.getItem().getItem().isFireResistant() || super.fireImmune();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        if (!this.getItem().isEmpty() && this.getItem().getItem() == Items.NETHER_STAR && damageSource.isExplosion()) {
            return false;
        }
        if (!this.getItem().getItem().canBeHurtBy(damageSource)) {
            return false;
        }
        this.markHurt();
        this.health = (int)((float)this.health - f);
        if (this.health <= 0) {
            this.remove();
        }
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putShort("Health", (short)this.health);
        compoundTag.putShort("Age", (short)this.age);
        compoundTag.putShort("PickupDelay", (short)this.pickupDelay);
        if (this.getThrower() != null) {
            compoundTag.putUUID("Thrower", this.getThrower());
        }
        if (this.getOwner() != null) {
            compoundTag.putUUID("Owner", this.getOwner());
        }
        if (!this.getItem().isEmpty()) {
            compoundTag.put("Item", this.getItem().save(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.health = compoundTag.getShort("Health");
        this.age = compoundTag.getShort("Age");
        if (compoundTag.contains("PickupDelay")) {
            this.pickupDelay = compoundTag.getShort("PickupDelay");
        }
        if (compoundTag.hasUUID("Owner")) {
            this.owner = compoundTag.getUUID("Owner");
        }
        if (compoundTag.hasUUID("Thrower")) {
            this.thrower = compoundTag.getUUID("Thrower");
        }
        CompoundTag compoundTag2 = compoundTag.getCompound("Item");
        this.setItem(ItemStack.of(compoundTag2));
        if (this.getItem().isEmpty()) {
            this.remove();
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (this.level.isClientSide) {
            return;
        }
        ItemStack itemStack = this.getItem();
        Item item = itemStack.getItem();
        int i = itemStack.getCount();
        if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUUID())) && player.inventory.add(itemStack)) {
            player.take(this, i);
            if (itemStack.isEmpty()) {
                this.remove();
                itemStack.setCount(i);
            }
            player.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
            player.onItemPickup(this);
        }
    }

    @Override
    public Component getName() {
        Component component = this.getCustomName();
        if (component != null) {
            return component;
        }
        return new TranslatableComponent(this.getItem().getDescriptionId());
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    @Nullable
    public Entity changeDimension(ServerLevel serverLevel) {
        Entity entity = super.changeDimension(serverLevel);
        if (!this.level.isClientSide && entity instanceof ItemEntity) {
            ((ItemEntity)entity).mergeWithNeighbours();
        }
        return entity;
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack itemStack) {
        this.getEntityData().set(DATA_ITEM, itemStack);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (DATA_ITEM.equals(entityDataAccessor)) {
            this.getItem().setEntityRepresentation(this);
        }
    }

    @Nullable
    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(@Nullable UUID uUID) {
        this.owner = uUID;
    }

    @Nullable
    public UUID getThrower() {
        return this.thrower;
    }

    public void setThrower(@Nullable UUID uUID) {
        this.thrower = uUID;
    }

    @Environment(value=EnvType.CLIENT)
    public int getAge() {
        return this.age;
    }

    public void setDefaultPickUpDelay() {
        this.pickupDelay = 10;
    }

    public void setNoPickUpDelay() {
        this.pickupDelay = 0;
    }

    public void setNeverPickUp() {
        this.pickupDelay = Short.MAX_VALUE;
    }

    public void setPickUpDelay(int i) {
        this.pickupDelay = i;
    }

    public boolean hasPickUpDelay() {
        return this.pickupDelay > 0;
    }

    public void setExtendedLifetime() {
        this.age = -6000;
    }

    public void makeFakeItem() {
        this.setNeverPickUp();
        this.age = 5999;
    }

    @Environment(value=EnvType.CLIENT)
    public float getSpin(float f) {
        return ((float)this.getAge() + f) / 20.0f + this.bobOffs;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Environment(value=EnvType.CLIENT)
    public ItemEntity copy() {
        return new ItemEntity(this);
    }
}

