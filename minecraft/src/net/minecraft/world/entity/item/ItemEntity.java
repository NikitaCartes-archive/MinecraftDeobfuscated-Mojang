package net.minecraft.world.entity.item;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class ItemEntity extends Entity {
	private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemEntity.class, EntityDataSerializers.ITEM_STACK);
	private static final int LIFETIME = 6000;
	private static final int INFINITE_PICKUP_DELAY = 32767;
	private static final int INFINITE_LIFETIME = -32768;
	private int age;
	private int pickupDelay;
	private int health = 5;
	@Nullable
	private UUID thrower;
	@Nullable
	private UUID owner;
	public final float bobOffs;

	public ItemEntity(EntityType<? extends ItemEntity> entityType, Level level) {
		super(entityType, level);
		this.bobOffs = this.random.nextFloat() * (float) Math.PI * 2.0F;
		this.setYRot(this.random.nextFloat() * 360.0F);
	}

	public ItemEntity(Level level, double d, double e, double f, ItemStack itemStack) {
		this(level, d, e, f, itemStack, level.random.nextDouble() * 0.2 - 0.1, 0.2, level.random.nextDouble() * 0.2 - 0.1);
	}

	public ItemEntity(Level level, double d, double e, double f, ItemStack itemStack, double g, double h, double i) {
		this(EntityType.ITEM, level);
		this.setPos(d, e, f);
		this.setDeltaMovement(g, h, i);
		this.setItem(itemStack);
	}

	private ItemEntity(ItemEntity itemEntity) {
		super(itemEntity.getType(), itemEntity.level);
		this.setItem(itemEntity.getItem().copy());
		this.copyPosition(itemEntity);
		this.age = itemEntity.age;
		this.bobOffs = itemEntity.bobOffs;
	}

	@Override
	public boolean dampensVibrations() {
		return this.getItem().is(ItemTags.DAMPENS_VIBRATIONS);
	}

	public Entity getThrowingEntity() {
		return Util.mapNullable(this.getThrower(), this.level::getPlayerByUUID);
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	protected void defineSynchedData() {
		this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
	}

	@Override
	public void tick() {
		if (this.getItem().isEmpty()) {
			this.discard();
		} else {
			super.tick();
			if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
				this.pickupDelay--;
			}

			this.xo = this.getX();
			this.yo = this.getY();
			this.zo = this.getZ();
			Vec3 vec3 = this.getDeltaMovement();
			float f = this.getEyeHeight() - 0.11111111F;
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
				this.noPhysics = !this.level.noCollision(this, this.getBoundingBox().deflate(1.0E-7));
				if (this.noPhysics) {
					this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
				}
			}

			if (!this.onGround || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
				this.move(MoverType.SELF, this.getDeltaMovement());
				float g = 0.98F;
				if (this.onGround) {
					g = this.level.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0, this.getZ())).getBlock().getFriction() * 0.98F;
				}

				this.setDeltaMovement(this.getDeltaMovement().multiply((double)g, 0.98, (double)g));
				if (this.onGround) {
					Vec3 vec32 = this.getDeltaMovement();
					if (vec32.y < 0.0) {
						this.setDeltaMovement(vec32.multiply(1.0, -0.5, 1.0));
					}
				}
			}

			boolean bl = Mth.floor(this.xo) != Mth.floor(this.getX()) || Mth.floor(this.yo) != Mth.floor(this.getY()) || Mth.floor(this.zo) != Mth.floor(this.getZ());
			int i = bl ? 2 : 40;
			if (this.tickCount % i == 0 && !this.level.isClientSide && this.isMergable()) {
				this.mergeWithNeighbours();
			}

			if (this.age != -32768) {
				this.age++;
			}

			this.hasImpulse = this.hasImpulse | this.updateInWaterStateAndDoFluidPushing();
			if (!this.level.isClientSide) {
				double d = this.getDeltaMovement().subtract(vec3).lengthSqr();
				if (d > 0.01) {
					this.hasImpulse = true;
				}
			}

			if (!this.level.isClientSide && this.age >= 6000) {
				this.discard();
			}
		}
	}

	private void setUnderwaterMovement() {
		Vec3 vec3 = this.getDeltaMovement();
		this.setDeltaMovement(vec3.x * 0.99F, vec3.y + (double)(vec3.y < 0.06F ? 5.0E-4F : 0.0F), vec3.z * 0.99F);
	}

	private void setUnderLavaMovement() {
		Vec3 vec3 = this.getDeltaMovement();
		this.setDeltaMovement(vec3.x * 0.95F, vec3.y + (double)(vec3.y < 0.06F ? 5.0E-4F : 0.0F), vec3.z * 0.95F);
	}

	private void mergeWithNeighbours() {
		if (this.isMergable()) {
			for (ItemEntity itemEntity : this.level
				.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.5, 0.0, 0.5), itemEntityx -> itemEntityx != this && itemEntityx.isMergable())) {
				if (itemEntity.isMergable()) {
					this.tryToMerge(itemEntity);
					if (this.isRemoved()) {
						break;
					}
				}
			}
		}
	}

	private boolean isMergable() {
		ItemStack itemStack = this.getItem();
		return this.isAlive() && this.pickupDelay != 32767 && this.age != -32768 && this.age < 6000 && itemStack.getCount() < itemStack.getMaxStackSize();
	}

	private void tryToMerge(ItemEntity itemEntity) {
		ItemStack itemStack = this.getItem();
		ItemStack itemStack2 = itemEntity.getItem();
		if (Objects.equals(this.getOwner(), itemEntity.getOwner()) && areMergable(itemStack, itemStack2)) {
			if (itemStack2.getCount() < itemStack.getCount()) {
				merge(this, itemStack, itemEntity, itemStack2);
			} else {
				merge(itemEntity, itemStack2, this, itemStack);
			}
		}
	}

	public static boolean areMergable(ItemStack itemStack, ItemStack itemStack2) {
		if (!itemStack2.is(itemStack.getItem())) {
			return false;
		} else if (itemStack2.getCount() + itemStack.getCount() > itemStack2.getMaxStackSize()) {
			return false;
		} else {
			return itemStack2.hasTag() ^ itemStack.hasTag() ? false : !itemStack2.hasTag() || itemStack2.getTag().equals(itemStack.getTag());
		}
	}

	public static ItemStack merge(ItemStack itemStack, ItemStack itemStack2, int i) {
		int j = Math.min(Math.min(itemStack.getMaxStackSize(), i) - itemStack.getCount(), itemStack2.getCount());
		ItemStack itemStack3 = itemStack.copy();
		itemStack3.grow(j);
		itemStack2.shrink(j);
		return itemStack3;
	}

	private static void merge(ItemEntity itemEntity, ItemStack itemStack, ItemStack itemStack2) {
		ItemStack itemStack3 = merge(itemStack, itemStack2, 64);
		itemEntity.setItem(itemStack3);
	}

	private static void merge(ItemEntity itemEntity, ItemStack itemStack, ItemEntity itemEntity2, ItemStack itemStack2) {
		merge(itemEntity, itemStack, itemStack2);
		itemEntity.pickupDelay = Math.max(itemEntity.pickupDelay, itemEntity2.pickupDelay);
		itemEntity.age = Math.min(itemEntity.age, itemEntity2.age);
		if (itemStack2.isEmpty()) {
			itemEntity2.discard();
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
		} else if (!this.getItem().isEmpty() && this.getItem().is(Items.NETHER_STAR) && damageSource.isExplosion()) {
			return false;
		} else if (!this.getItem().getItem().canBeHurtBy(damageSource)) {
			return false;
		} else if (this.level.isClientSide) {
			return true;
		} else {
			this.markHurt();
			this.health = (int)((float)this.health - f);
			this.gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
			if (this.health <= 0) {
				this.getItem().onDestroyed(this);
				this.discard();
			}

			return true;
		}
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
			this.discard();
		}
	}

	@Override
	public void playerTouch(Player player) {
		if (!this.level.isClientSide) {
			ItemStack itemStack = this.getItem();
			Item item = itemStack.getItem();
			int i = itemStack.getCount();
			if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUUID())) && player.getInventory().add(itemStack)) {
				player.take(this, i);
				if (itemStack.isEmpty()) {
					this.discard();
					itemStack.setCount(i);
				}

				player.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
				player.onItemPickup(this);
			}
		}
	}

	@Override
	public Component getName() {
		Component component = this.getCustomName();
		return (Component)(component != null ? component : Component.translatable(this.getItem().getDescriptionId()));
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Nullable
	@Override
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

	public void setUnlimitedLifetime() {
		this.age = -32768;
	}

	public void setExtendedLifetime() {
		this.age = -6000;
	}

	public void makeFakeItem() {
		this.setNeverPickUp();
		this.age = 5999;
	}

	public float getSpin(float f) {
		return ((float)this.getAge() + f) / 20.0F + this.bobOffs;
	}

	public ItemEntity copy() {
		return new ItemEntity(this);
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.AMBIENT;
	}

	@Override
	public float getVisualRotationYInDegrees() {
		return 180.0F - this.getSpin(0.5F) / (float) (Math.PI * 2) * 360.0F;
	}
}
