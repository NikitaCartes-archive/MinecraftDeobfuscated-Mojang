package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

public abstract class RandomizableContainerBlockEntity extends BaseContainerBlockEntity implements RandomizableContainer {
	@Nullable
	protected ResourceKey<LootTable> lootTable;
	protected long lootTableSeed = 0L;

	protected RandomizableContainerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
	}

	@Nullable
	@Override
	public ResourceKey<LootTable> getLootTable() {
		return this.lootTable;
	}

	@Override
	public void setLootTable(@Nullable ResourceKey<LootTable> resourceKey) {
		this.lootTable = resourceKey;
	}

	@Override
	public long getLootTableSeed() {
		return this.lootTableSeed;
	}

	@Override
	public void setLootTableSeed(long l) {
		this.lootTableSeed = l;
	}

	@Override
	public boolean isEmpty() {
		this.unpackLootTable(null);
		return super.isEmpty();
	}

	@Override
	public ItemStack getItem(int i) {
		this.unpackLootTable(null);
		return super.getItem(i);
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		this.unpackLootTable(null);
		return super.removeItem(i, j);
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		this.unpackLootTable(null);
		return super.removeItemNoUpdate(i);
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		this.unpackLootTable(null);
		super.setItem(i, itemStack);
	}

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

	@Override
	protected void applyImplicitComponents(BlockEntity.DataComponentInput dataComponentInput) {
		super.applyImplicitComponents(dataComponentInput);
		SeededContainerLoot seededContainerLoot = dataComponentInput.get(DataComponents.CONTAINER_LOOT);
		if (seededContainerLoot != null) {
			this.lootTable = seededContainerLoot.lootTable();
			this.lootTableSeed = seededContainerLoot.seed();
		}
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
		super.collectImplicitComponents(builder);
		if (this.lootTable != null) {
			builder.set(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(this.lootTable, this.lootTableSeed));
		}
	}

	@Override
	public void removeComponentsFromTag(CompoundTag compoundTag) {
		super.removeComponentsFromTag(compoundTag);
		compoundTag.remove("LootTable");
		compoundTag.remove("LootTableSeed");
	}
}
