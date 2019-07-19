package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BrewingStandBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, TickableBlockEntity {
	private static final int[] SLOTS_FOR_UP = new int[]{3};
	private static final int[] SLOTS_FOR_DOWN = new int[]{0, 1, 2, 3};
	private static final int[] SLOTS_FOR_SIDES = new int[]{0, 1, 2, 4};
	private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
	private int brewTime;
	private boolean[] lastPotionCount;
	private Item ingredient;
	private int fuel;
	protected final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int i) {
			switch (i) {
				case 0:
					return BrewingStandBlockEntity.this.brewTime;
				case 1:
					return BrewingStandBlockEntity.this.fuel;
				default:
					return 0;
			}
		}

		@Override
		public void set(int i, int j) {
			switch (i) {
				case 0:
					BrewingStandBlockEntity.this.brewTime = j;
					break;
				case 1:
					BrewingStandBlockEntity.this.fuel = j;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}
	};

	public BrewingStandBlockEntity() {
		super(BlockEntityType.BREWING_STAND);
	}

	@Override
	protected Component getDefaultName() {
		return new TranslatableComponent("container.brewing");
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemStack : this.items) {
			if (!itemStack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void tick() {
		ItemStack itemStack = this.items.get(4);
		if (this.fuel <= 0 && itemStack.getItem() == Items.BLAZE_POWDER) {
			this.fuel = 20;
			itemStack.shrink(1);
			this.setChanged();
		}

		boolean bl = this.isBrewable();
		boolean bl2 = this.brewTime > 0;
		ItemStack itemStack2 = this.items.get(3);
		if (bl2) {
			this.brewTime--;
			boolean bl3 = this.brewTime == 0;
			if (bl3 && bl) {
				this.doBrew();
				this.setChanged();
			} else if (!bl) {
				this.brewTime = 0;
				this.setChanged();
			} else if (this.ingredient != itemStack2.getItem()) {
				this.brewTime = 0;
				this.setChanged();
			}
		} else if (bl && this.fuel > 0) {
			this.fuel--;
			this.brewTime = 400;
			this.ingredient = itemStack2.getItem();
			this.setChanged();
		}

		if (!this.level.isClientSide) {
			boolean[] bls = this.getPotionBits();
			if (!Arrays.equals(bls, this.lastPotionCount)) {
				this.lastPotionCount = bls;
				BlockState blockState = this.level.getBlockState(this.getBlockPos());
				if (!(blockState.getBlock() instanceof BrewingStandBlock)) {
					return;
				}

				for (int i = 0; i < BrewingStandBlock.HAS_BOTTLE.length; i++) {
					blockState = blockState.setValue(BrewingStandBlock.HAS_BOTTLE[i], Boolean.valueOf(bls[i]));
				}

				this.level.setBlock(this.worldPosition, blockState, 2);
			}
		}
	}

	public boolean[] getPotionBits() {
		boolean[] bls = new boolean[3];

		for (int i = 0; i < 3; i++) {
			if (!this.items.get(i).isEmpty()) {
				bls[i] = true;
			}
		}

		return bls;
	}

	private boolean isBrewable() {
		ItemStack itemStack = this.items.get(3);
		if (itemStack.isEmpty()) {
			return false;
		} else if (!PotionBrewing.isIngredient(itemStack)) {
			return false;
		} else {
			for (int i = 0; i < 3; i++) {
				ItemStack itemStack2 = this.items.get(i);
				if (!itemStack2.isEmpty() && PotionBrewing.hasMix(itemStack2, itemStack)) {
					return true;
				}
			}

			return false;
		}
	}

	private void doBrew() {
		ItemStack itemStack = this.items.get(3);

		for (int i = 0; i < 3; i++) {
			this.items.set(i, PotionBrewing.mix(itemStack, this.items.get(i)));
		}

		itemStack.shrink(1);
		BlockPos blockPos = this.getBlockPos();
		if (itemStack.getItem().hasCraftingRemainingItem()) {
			ItemStack itemStack2 = new ItemStack(itemStack.getItem().getCraftingRemainingItem());
			if (itemStack.isEmpty()) {
				itemStack = itemStack2;
			} else if (!this.level.isClientSide) {
				Containers.dropItemStack(this.level, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), itemStack2);
			}
		}

		this.items.set(3, itemStack);
		this.level.levelEvent(1035, blockPos, 0);
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compoundTag, this.items);
		this.brewTime = compoundTag.getShort("BrewTime");
		this.fuel = compoundTag.getByte("Fuel");
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		compoundTag.putShort("BrewTime", (short)this.brewTime);
		ContainerHelper.saveAllItems(compoundTag, this.items);
		compoundTag.putByte("Fuel", (byte)this.fuel);
		return compoundTag;
	}

	@Override
	public ItemStack getItem(int i) {
		return i >= 0 && i < this.items.size() ? this.items.get(i) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		return ContainerHelper.removeItem(this.items, i, j);
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		return ContainerHelper.takeItem(this.items, i);
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		if (i >= 0 && i < this.items.size()) {
			this.items.set(i, itemStack);
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return this.level.getBlockEntity(this.worldPosition) != this
			? false
			: !(player.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5) > 64.0);
	}

	@Override
	public boolean canPlaceItem(int i, ItemStack itemStack) {
		if (i == 3) {
			return PotionBrewing.isIngredient(itemStack);
		} else {
			Item item = itemStack.getItem();
			return i == 4
				? item == Items.BLAZE_POWDER
				: (item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.GLASS_BOTTLE) && this.getItem(i).isEmpty();
		}
	}

	@Override
	public int[] getSlotsForFace(Direction direction) {
		if (direction == Direction.UP) {
			return SLOTS_FOR_UP;
		} else {
			return direction == Direction.DOWN ? SLOTS_FOR_DOWN : SLOTS_FOR_SIDES;
		}
	}

	@Override
	public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
		return this.canPlaceItem(i, itemStack);
	}

	@Override
	public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
		return i == 3 ? itemStack.getItem() == Items.GLASS_BOTTLE : true;
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	@Override
	protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return new BrewingStandMenu(i, inventory, this, this.dataAccess);
	}
}
