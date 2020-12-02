package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BarrelBlockEntity extends RandomizableContainerBlockEntity {
	private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
	private ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
		@Override
		protected void onOpen(Level level, BlockPos blockPos, BlockState blockState) {
			BarrelBlockEntity.this.playSound(blockState, SoundEvents.BARREL_OPEN);
			BarrelBlockEntity.this.updateBlockState(blockState, true);
		}

		@Override
		protected void onClose(Level level, BlockPos blockPos, BlockState blockState) {
			BarrelBlockEntity.this.playSound(blockState, SoundEvents.BARREL_CLOSE);
			BarrelBlockEntity.this.updateBlockState(blockState, false);
		}

		@Override
		protected void openerCountChanged(Level level, BlockPos blockPos, BlockState blockState, int i, int j) {
		}

		@Override
		protected boolean isOwnContainer(Player player) {
			if (player.containerMenu instanceof ChestMenu) {
				Container container = ((ChestMenu)player.containerMenu).getContainer();
				return container == BarrelBlockEntity.this;
			} else {
				return false;
			}
		}
	};

	public BarrelBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.BARREL, blockPos, blockState);
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		if (!this.trySaveLootTable(compoundTag)) {
			ContainerHelper.saveAllItems(compoundTag, this.items);
		}

		return compoundTag;
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(compoundTag)) {
			ContainerHelper.loadAllItems(compoundTag, this.items);
		}
	}

	@Override
	public int getContainerSize() {
		return 27;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> nonNullList) {
		this.items = nonNullList;
	}

	@Override
	protected Component getDefaultName() {
		return new TranslatableComponent("container.barrel");
	}

	@Override
	protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return ChestMenu.threeRows(i, inventory, this);
	}

	@Override
	public void startOpen(Player player) {
		if (!player.isSpectator()) {
			this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
		}
	}

	@Override
	public void stopOpen(Player player) {
		if (!player.isSpectator()) {
			this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
		}
	}

	public void recheckOpen() {
		this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
	}

	private void updateBlockState(BlockState blockState, boolean bl) {
		this.level.setBlock(this.getBlockPos(), blockState.setValue(BarrelBlock.OPEN, Boolean.valueOf(bl)), 3);
	}

	private void playSound(BlockState blockState, SoundEvent soundEvent) {
		Vec3i vec3i = ((Direction)blockState.getValue(BarrelBlock.FACING)).getNormal();
		double d = (double)this.worldPosition.getX() + 0.5 + (double)vec3i.getX() / 2.0;
		double e = (double)this.worldPosition.getY() + 0.5 + (double)vec3i.getY() / 2.0;
		double f = (double)this.worldPosition.getZ() + 0.5 + (double)vec3i.getZ() / 2.0;
		this.level.playSound(null, d, e, f, soundEvent, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
	}
}
