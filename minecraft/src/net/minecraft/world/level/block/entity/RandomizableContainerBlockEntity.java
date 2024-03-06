package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.block.state.BlockState;

public abstract class RandomizableContainerBlockEntity extends BaseContainerBlockEntity implements RandomizableContainer {
	@Nullable
	protected ResourceLocation lootTable;
	protected long lootTableSeed = 0L;

	protected RandomizableContainerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
	}

	@Nullable
	@Override
	public ResourceLocation getLootTable() {
		return this.lootTable;
	}

	@Override
	public void setLootTable(@Nullable ResourceLocation resourceLocation) {
		this.lootTable = resourceLocation;
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
	public void applyComponents(DataComponentMap dataComponentMap) {
		super.applyComponents(dataComponentMap);
		SeededContainerLoot seededContainerLoot = dataComponentMap.get(DataComponents.CONTAINER_LOOT);
		if (seededContainerLoot != null) {
			this.lootTable = seededContainerLoot.lootTable();
			this.lootTableSeed = seededContainerLoot.seed();
		}
	}

	@Override
	public void collectComponents(DataComponentMap.Builder builder) {
		super.collectComponents(builder);
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
