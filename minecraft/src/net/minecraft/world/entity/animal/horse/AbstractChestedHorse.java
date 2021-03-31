package net.minecraft.world.entity.animal.horse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public abstract class AbstractChestedHorse extends AbstractHorse {
	private static final EntityDataAccessor<Boolean> DATA_ID_CHEST = SynchedEntityData.defineId(AbstractChestedHorse.class, EntityDataSerializers.BOOLEAN);
	public static final int INV_CHEST_COUNT = 15;

	protected AbstractChestedHorse(EntityType<? extends AbstractChestedHorse> entityType, Level level) {
		super(entityType, level);
		this.canGallop = false;
	}

	@Override
	protected void randomizeAttributes() {
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)this.generateRandomMaxHealth());
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_ID_CHEST, false);
	}

	public static AttributeSupplier.Builder createBaseChestedHorseAttributes() {
		return createBaseHorseAttributes().add(Attributes.MOVEMENT_SPEED, 0.175F).add(Attributes.JUMP_STRENGTH, 0.5);
	}

	public boolean hasChest() {
		return this.entityData.get(DATA_ID_CHEST);
	}

	public void setChest(boolean bl) {
		this.entityData.set(DATA_ID_CHEST, bl);
	}

	@Override
	protected int getInventorySize() {
		return this.hasChest() ? 17 : super.getInventorySize();
	}

	@Override
	public double getPassengersRidingOffset() {
		return super.getPassengersRidingOffset() - 0.25;
	}

	@Override
	protected void dropEquipment() {
		super.dropEquipment();
		if (this.hasChest()) {
			if (!this.level.isClientSide) {
				this.spawnAtLocation(Blocks.CHEST);
			}

			this.setChest(false);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("ChestedHorse", this.hasChest());
		if (this.hasChest()) {
			ListTag listTag = new ListTag();

			for (int i = 2; i < this.inventory.getContainerSize(); i++) {
				ItemStack itemStack = this.inventory.getItem(i);
				if (!itemStack.isEmpty()) {
					CompoundTag compoundTag2 = new CompoundTag();
					compoundTag2.putByte("Slot", (byte)i);
					itemStack.save(compoundTag2);
					listTag.add(compoundTag2);
				}
			}

			compoundTag.put("Items", listTag);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setChest(compoundTag.getBoolean("ChestedHorse"));
		this.createInventory();
		if (this.hasChest()) {
			ListTag listTag = compoundTag.getList("Items", 10);

			for (int i = 0; i < listTag.size(); i++) {
				CompoundTag compoundTag2 = listTag.getCompound(i);
				int j = compoundTag2.getByte("Slot") & 255;
				if (j >= 2 && j < this.inventory.getContainerSize()) {
					this.inventory.setItem(j, ItemStack.of(compoundTag2));
				}
			}
		}

		this.updateContainerEquipment();
	}

	@Override
	public SlotAccess getSlot(int i) {
		return i == 499 ? new SlotAccess() {
			@Override
			public ItemStack get() {
				return AbstractChestedHorse.this.hasChest() ? new ItemStack(Items.CHEST) : ItemStack.EMPTY;
			}

			@Override
			public boolean set(ItemStack itemStack) {
				if (itemStack.isEmpty()) {
					if (AbstractChestedHorse.this.hasChest()) {
						AbstractChestedHorse.this.setChest(false);
						AbstractChestedHorse.this.createInventory();
					}

					return true;
				} else if (itemStack.is(Items.CHEST)) {
					if (!AbstractChestedHorse.this.hasChest()) {
						AbstractChestedHorse.this.setChest(true);
						AbstractChestedHorse.this.createInventory();
					}

					return true;
				} else {
					return false;
				}
			}
		} : super.getSlot(i);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (!this.isBaby()) {
			if (this.isTamed() && player.isSecondaryUseActive()) {
				this.openInventory(player);
				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}

			if (this.isVehicle()) {
				return super.mobInteract(player, interactionHand);
			}
		}

		if (!itemStack.isEmpty()) {
			if (this.isFood(itemStack)) {
				return this.fedFood(player, itemStack);
			}

			if (!this.isTamed()) {
				this.makeMad();
				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}

			if (!this.hasChest() && itemStack.is(Blocks.CHEST.asItem())) {
				this.setChest(true);
				this.playChestEquipsSound();
				if (!player.getAbilities().instabuild) {
					itemStack.shrink(1);
				}

				this.createInventory();
				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}

			if (!this.isBaby() && !this.isSaddled() && itemStack.is(Items.SADDLE)) {
				this.openInventory(player);
				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}
		}

		if (this.isBaby()) {
			return super.mobInteract(player, interactionHand);
		} else {
			this.doPlayerRide(player);
			return InteractionResult.sidedSuccess(this.level.isClientSide);
		}
	}

	protected void playChestEquipsSound() {
		this.playSound(SoundEvents.DONKEY_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
	}

	public int getInventoryColumns() {
		return 5;
	}
}
