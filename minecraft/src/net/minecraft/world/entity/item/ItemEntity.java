package net.minecraft.world.entity.item;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

public class ItemEntity extends Entity {
	private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemEntity.class, EntityDataSerializers.ITEM_STACK);
	private int age;
	private int pickupDelay;
	private int health = 5;
	private UUID thrower;
	private UUID owner;
	public final float bobOffs = (float)(Math.random() * Math.PI * 2.0);

	public ItemEntity(EntityType<? extends ItemEntity> entityType, Level level) {
		super(entityType, level);
	}

	public ItemEntity(Level level, double d, double e, double f) {
		this(EntityType.ITEM, level);
		this.setPos(d, e, f);
		this.yRot = this.random.nextFloat() * 360.0F;
		this.setDeltaMovement(this.random.nextDouble() * 0.2 - 0.1, 0.2, this.random.nextDouble() * 0.2 - 0.1);
	}

	public ItemEntity(Level level, double d, double e, double f, ItemStack itemStack) {
		this(level, d, e, f);
		this.setItem(itemStack);
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
		if (this.getItem().isEmpty()) {
			this.remove();
		} else {
			super.tick();
			if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
				this.pickupDelay--;
			}

			this.xo = this.getX();
			this.yo = this.getY();
			this.zo = this.getZ();
			Vec3 vec3 = this.getDeltaMovement();
			if (this.isUnderLiquid(FluidTags.WATER)) {
				this.setUnderwaterMovement();
			} else if (!this.isNoGravity()) {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
			}

			if (this.level.isClientSide) {
				this.noPhysics = false;
			} else {
				this.noPhysics = !this.level.noCollision(this);
				if (this.noPhysics) {
					this.checkInBlock(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
				}
			}

			if (!this.onGround || getHorizontalDistanceSqr(this.getDeltaMovement()) > 1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
				this.move(MoverType.SELF, this.getDeltaMovement());
				float f = 0.98F;
				if (this.onGround) {
					f = this.level.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0, this.getZ())).getBlock().getFriction() * 0.98F;
				}

				this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 0.98, (double)f));
				if (this.onGround) {
					this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, -0.5, 1.0));
				}
			}

			boolean bl = Mth.floor(this.xo) != Mth.floor(this.getX()) || Mth.floor(this.yo) != Mth.floor(this.getY()) || Mth.floor(this.zo) != Mth.floor(this.getZ());
			int i = bl ? 2 : 40;
			if (this.tickCount % i == 0) {
				if (this.level.getFluidState(new BlockPos(this)).is(FluidTags.LAVA)) {
					this.setDeltaMovement(
						(double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F), 0.2F, (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F)
					);
					this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
				}

				if (!this.level.isClientSide && this.isMergable()) {
					this.mergeWithNeighbours();
				}
			}

			if (this.age != -32768) {
				this.age++;
			}

			this.hasImpulse = this.hasImpulse | this.updateInWaterState();
			if (!this.level.isClientSide) {
				double d = this.getDeltaMovement().subtract(vec3).lengthSqr();
				if (d > 0.01) {
					this.hasImpulse = true;
				}
			}

			if (!this.level.isClientSide && this.age >= 6000) {
				this.remove();
			}
		}
	}

	private void setUnderwaterMovement() {
		Vec3 vec3 = this.getDeltaMovement();
		this.setDeltaMovement(vec3.x * 0.99F, vec3.y + (double)(vec3.y < 0.06F ? 5.0E-4F : 0.0F), vec3.z * 0.99F);
	}

	private void mergeWithNeighbours() {
		List<ItemEntity> list = this.level
			.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.5, 0.0, 0.5), itemEntityx -> itemEntityx != this && itemEntityx.isMergable());
		if (!list.isEmpty()) {
			for (ItemEntity itemEntity : list) {
				if (!this.isMergable()) {
					return;
				}

				this.merge(itemEntity);
			}
		}
	}

	private boolean isMergable() {
		ItemStack itemStack = this.getItem();
		return this.isAlive() && this.pickupDelay != 32767 && this.age != -32768 && this.age < 6000 && itemStack.getCount() < itemStack.getMaxStackSize();
	}

	private void merge(ItemEntity itemEntity) {
		ItemStack itemStack = this.getItem();
		ItemStack itemStack2 = itemEntity.getItem();
		if (itemStack2.getItem() == itemStack.getItem()) {
			if (itemStack2.getCount() + itemStack.getCount() <= itemStack2.getMaxStackSize()) {
				if (!(itemStack2.hasTag() ^ itemStack.hasTag())) {
					if (!itemStack2.hasTag() || itemStack2.getTag().equals(itemStack.getTag())) {
						if (itemStack2.getCount() < itemStack.getCount()) {
							merge(this, itemStack, itemEntity, itemStack2);
						} else {
							merge(itemEntity, itemStack2, this, itemStack);
						}
					}
				}
			}
		}
	}

	private static void merge(ItemEntity itemEntity, ItemStack itemStack, ItemEntity itemEntity2, ItemStack itemStack2) {
		int i = Math.min(itemStack.getMaxStackSize() - itemStack.getCount(), itemStack2.getCount());
		ItemStack itemStack3 = itemStack.copy();
		itemStack3.grow(i);
		itemEntity.setItem(itemStack3);
		itemStack2.shrink(i);
		itemEntity2.setItem(itemStack2);
		itemEntity.pickupDelay = Math.max(itemEntity.pickupDelay, itemEntity2.pickupDelay);
		itemEntity.age = Math.min(itemEntity.age, itemEntity2.age);
		if (itemStack2.isEmpty()) {
			itemEntity2.remove();
		}
	}

	@Override
	protected void burn(int i) {
		this.hurt(DamageSource.IN_FIRE, (float)i);
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else if (!this.getItem().isEmpty() && this.getItem().getItem() == Items.NETHER_STAR && damageSource.isExplosion()) {
			return false;
		} else {
			this.markHurt();
			this.health = (int)((float)this.health - f);
			if (this.health <= 0) {
				this.remove();
			}

			return false;
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.putShort("Health", (short)this.health);
		compoundTag.putShort("Age", (short)this.age);
		compoundTag.putShort("PickupDelay", (short)this.pickupDelay);
		if (this.getThrower() != null) {
			compoundTag.put("Thrower", NbtUtils.createUUIDTag(this.getThrower()));
		}

		if (this.getOwner() != null) {
			compoundTag.put("Owner", NbtUtils.createUUIDTag(this.getOwner()));
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

		if (compoundTag.contains("Owner", 10)) {
			this.owner = NbtUtils.loadUUIDTag(compoundTag.getCompound("Owner"));
		}

		if (compoundTag.contains("Thrower", 10)) {
			this.thrower = NbtUtils.loadUUIDTag(compoundTag.getCompound("Thrower"));
		}

		CompoundTag compoundTag2 = compoundTag.getCompound("Item");
		this.setItem(ItemStack.of(compoundTag2));
		if (this.getItem().isEmpty()) {
			this.remove();
		}
	}

	@Override
	public void playerTouch(Player player) {
		if (!this.level.isClientSide) {
			ItemStack itemStack = this.getItem();
			Item item = itemStack.getItem();
			int i = itemStack.getCount();
			if (this.pickupDelay == 0 && (this.owner == null || 6000 - this.age <= 200 || this.owner.equals(player.getUUID())) && player.inventory.add(itemStack)) {
				player.take(this, i);
				if (itemStack.isEmpty()) {
					this.remove();
					itemStack.setCount(i);
				}

				player.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
			}
		}
	}

	@Override
	public Component getName() {
		Component component = this.getCustomName();
		return (Component)(component != null ? component : new TranslatableComponent(this.getItem().getDescriptionId()));
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Nullable
	@Override
	public Entity changeDimension(DimensionType dimensionType) {
		Entity entity = super.changeDimension(dimensionType);
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

	@Environment(EnvType.CLIENT)
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
		this.pickupDelay = 32767;
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

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}
}
