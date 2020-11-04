package net.minecraft.world.entity.decoration;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemFrame extends HangingEntity {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.ITEM_STACK);
	private static final EntityDataAccessor<Integer> DATA_ROTATION = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.INT);
	private float dropChance = 1.0F;
	private boolean fixed;

	public ItemFrame(EntityType<? extends ItemFrame> entityType, Level level) {
		super(entityType, level);
	}

	public ItemFrame(Level level, BlockPos blockPos, Direction direction) {
		super(EntityType.ITEM_FRAME, level, blockPos);
		this.setDirection(direction);
	}

	@Override
	protected float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return 0.0F;
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
			this.xRot = 0.0F;
			this.yRot = (float)(this.direction.get2DDataValue() * 90);
		} else {
			this.xRot = (float)(-90 * direction.getAxisDirection().getStep());
			this.yRot = 0.0F;
		}

		this.xRotO = this.xRot;
		this.yRotO = this.yRot;
		this.recalculateBoundingBox();
	}

	@Override
	protected void recalculateBoundingBox() {
		if (this.direction != null) {
			double d = 0.46875;
			double e = (double)this.pos.getX() + 0.5 - (double)this.direction.getStepX() * 0.46875;
			double f = (double)this.pos.getY() + 0.5 - (double)this.direction.getStepY() * 0.46875;
			double g = (double)this.pos.getZ() + 0.5 - (double)this.direction.getStepZ() * 0.46875;
			this.setPosRaw(e, f, g);
			double h = (double)this.getWidth();
			double i = (double)this.getHeight();
			double j = (double)this.getWidth();
			Direction.Axis axis = this.direction.getAxis();
			switch (axis) {
				case X:
					h = 1.0;
					break;
				case Y:
					i = 1.0;
					break;
				case Z:
					j = 1.0;
			}

			h /= 32.0;
			i /= 32.0;
			j /= 32.0;
			this.setBoundingBox(new AABB(e - h, f - i, g - j, e + h, f + i, g + j));
		}
	}

	@Override
	public boolean survives() {
		if (this.fixed) {
			return true;
		} else if (!this.level.noCollision(this)) {
			return false;
		} else {
			BlockState blockState = this.level.getBlockState(this.pos.relative(this.direction.getOpposite()));
			return blockState.getMaterial().isSolid() || this.direction.getAxis().isHorizontal() && DiodeBlock.isDiode(blockState)
				? this.level.getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty()
				: false;
		}
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
		return 0.0F;
	}

	@Override
	public void kill() {
		this.removeFramedMap(this.getItem());
		super.kill();
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.fixed) {
			return damageSource != DamageSource.OUT_OF_WORLD && !damageSource.isCreativePlayer() ? false : super.hurt(damageSource, f);
		} else if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else if (!damageSource.isExplosion() && !this.getItem().isEmpty()) {
			if (!this.level.isClientSide) {
				this.dropItem(damageSource.getEntity(), false);
				this.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F, 1.0F);
			}

			return true;
		} else {
			return super.hurt(damageSource, f);
		}
	}

	@Override
	public int getWidth() {
		return 12;
	}

	@Override
	public int getHeight() {
		return 12;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		double e = 16.0;
		e *= 64.0 * getViewScale();
		return d < e * e;
	}

	@Override
	public void dropItem(@Nullable Entity entity) {
		this.playSound(SoundEvents.ITEM_FRAME_BREAK, 1.0F, 1.0F);
		this.dropItem(entity, true);
	}

	@Override
	public void playPlacementSound() {
		this.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
	}

	private void dropItem(@Nullable Entity entity, boolean bl) {
		if (!this.fixed) {
			ItemStack itemStack = this.getItem();
			this.setItem(ItemStack.EMPTY);
			if (!this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
				if (entity == null) {
					this.removeFramedMap(itemStack);
				}
			} else {
				if (entity instanceof Player) {
					Player player = (Player)entity;
					if (player.getAbilities().instabuild) {
						this.removeFramedMap(itemStack);
						return;
					}
				}

				if (bl) {
					this.spawnAtLocation(Items.ITEM_FRAME);
				}

				if (!itemStack.isEmpty()) {
					itemStack = itemStack.copy();
					this.removeFramedMap(itemStack);
					if (this.random.nextFloat() < this.dropChance) {
						this.spawnAtLocation(itemStack);
					}
				}
			}
		}
	}

	private void removeFramedMap(ItemStack itemStack) {
		if (itemStack.is(Items.FILLED_MAP)) {
			MapItemSavedData mapItemSavedData = MapItem.getOrCreateSavedData(itemStack, this.level);
			mapItemSavedData.removedFromFrame(this.pos, this.getId());
			mapItemSavedData.setDirty(true);
		}

		itemStack.setEntityRepresentation(null);
	}

	public ItemStack getItem() {
		return this.getEntityData().get(DATA_ITEM);
	}

	public void setItem(ItemStack itemStack) {
		this.setItem(itemStack, true);
	}

	public void setItem(ItemStack itemStack, boolean bl) {
		if (!itemStack.isEmpty()) {
			itemStack = itemStack.copy();
			itemStack.setCount(1);
			itemStack.setEntityRepresentation(this);
		}

		this.getEntityData().set(DATA_ITEM, itemStack);
		if (!itemStack.isEmpty()) {
			this.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1.0F, 1.0F);
		}

		if (bl && this.pos != null) {
			this.level.updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
		}
	}

	@Override
	public boolean setSlot(int i, ItemStack itemStack) {
		if (i == 0) {
			this.setItem(itemStack);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (entityDataAccessor.equals(DATA_ITEM)) {
			ItemStack itemStack = this.getItem();
			if (!itemStack.isEmpty() && itemStack.getFrame() != this) {
				itemStack.setEntityRepresentation(this);
			}
		}
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
			ItemStack itemStack = ItemStack.of(compoundTag2);
			if (itemStack.isEmpty()) {
				LOGGER.warn("Unable to load item from: {}", compoundTag2);
			}

			ItemStack itemStack2 = this.getItem();
			if (!itemStack2.isEmpty() && !ItemStack.matches(itemStack, itemStack2)) {
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
		ItemStack itemStack = player.getItemInHand(interactionHand);
		boolean bl = !this.getItem().isEmpty();
		boolean bl2 = !itemStack.isEmpty();
		if (this.fixed) {
			return InteractionResult.PASS;
		} else if (!this.level.isClientSide) {
			if (!bl) {
				if (bl2 && !this.isRemoved()) {
					this.setItem(itemStack);
					if (!player.getAbilities().instabuild) {
						itemStack.shrink(1);
					}
				}
			} else {
				this.playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 1.0F, 1.0F);
				this.setRotation(this.getRotation() + 1);
			}

			return InteractionResult.CONSUME;
		} else {
			return !bl && !bl2 ? InteractionResult.PASS : InteractionResult.SUCCESS;
		}
	}

	public int getAnalogOutput() {
		return this.getItem().isEmpty() ? 0 : this.getRotation() % 8 + 1;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this, this.getType(), this.direction.get3DDataValue(), this.getPos());
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
		super.recreateFromPacket(clientboundAddEntityPacket);
		this.setDirection(Direction.from3DDataValue(clientboundAddEntityPacket.getData()));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ItemStack getPickResult() {
		ItemStack itemStack = this.getItem();
		return itemStack.isEmpty() ? new ItemStack(Items.ITEM_FRAME) : itemStack.copy();
	}
}
