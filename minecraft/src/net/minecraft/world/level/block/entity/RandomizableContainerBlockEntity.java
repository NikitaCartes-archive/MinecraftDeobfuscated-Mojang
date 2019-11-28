package net.minecraft.world.level.block.entity;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public abstract class RandomizableContainerBlockEntity extends BaseContainerBlockEntity {
	@Nullable
	protected ResourceLocation lootTable;
	protected long lootTableSeed;

	protected RandomizableContainerBlockEntity(BlockEntityType<?> blockEntityType) {
		super(blockEntityType);
	}

	public static void setLootTable(BlockGetter blockGetter, Random random, BlockPos blockPos, ResourceLocation resourceLocation) {
		BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
		if (blockEntity instanceof RandomizableContainerBlockEntity) {
			((RandomizableContainerBlockEntity)blockEntity).setLootTable(resourceLocation, random.nextLong());
		}
	}

	protected boolean tryLoadLootTable(CompoundTag compoundTag) {
		if (compoundTag.contains("LootTable", 8)) {
			this.lootTable = new ResourceLocation(compoundTag.getString("LootTable"));
			this.lootTableSeed = compoundTag.getLong("LootTableSeed");
			return true;
		} else {
			return false;
		}
	}

	protected boolean trySaveLootTable(CompoundTag compoundTag) {
		if (this.lootTable == null) {
			return false;
		} else {
			compoundTag.putString("LootTable", this.lootTable.toString());
			if (this.lootTableSeed != 0L) {
				compoundTag.putLong("LootTableSeed", this.lootTableSeed);
			}

			return true;
		}
	}

	public void unpackLootTable(@Nullable Player player) {
		if (this.lootTable != null && this.level.getServer() != null) {
			LootTable lootTable = this.level.getServer().getLootTables().get(this.lootTable);
			this.lootTable = null;
			LootContext.Builder builder = new LootContext.Builder((ServerLevel)this.level)
				.withParameter(LootContextParams.BLOCK_POS, new BlockPos(this.worldPosition))
				.withOptionalRandomSeed(this.lootTableSeed);
			if (player != null) {
				builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
			}

			lootTable.fill(this, builder.create(LootContextParamSets.CHEST));
		}
	}

	public void setLootTable(ResourceLocation resourceLocation, long l) {
		this.lootTable = resourceLocation;
		this.lootTableSeed = l;
	}

	@Override
	public boolean isEmpty() {
		this.unpackLootTable(null);
		return this.getItems().stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getItem(int i) {
		this.unpackLootTable(null);
		return this.getItems().get(i);
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		this.unpackLootTable(null);
		ItemStack itemStack = ContainerHelper.removeItem(this.getItems(), i, j);
		if (!itemStack.isEmpty()) {
			this.setChanged();
		}

		return itemStack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		this.unpackLootTable(null);
		return ContainerHelper.takeItem(this.getItems(), i);
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		this.unpackLootTable(null);
		this.getItems().set(i, itemStack);
		if (itemStack.getCount() > this.getMaxStackSize()) {
			itemStack.setCount(this.getMaxStackSize());
		}

		this.setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return this.level.getBlockEntity(this.worldPosition) != this
			? false
			: !(player.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5) > 64.0);
	}

	@Override
	public void clearContent() {
		this.getItems().clear();
	}

	protected abstract NonNullList<ItemStack> getItems();

	protected abstract void setItems(NonNullList<ItemStack> nonNullList);

	@Override
	public boolean canOpen(Player player) {
		return super.canOpen(player) && (this.lootTable == null || !player.isSpectator());
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
		if (this.canOpen(player)) {
			this.unpackLootTable(inventory.player);
			return this.createMenu(i, inventory);
		} else {
			return null;
		}
	}
}
