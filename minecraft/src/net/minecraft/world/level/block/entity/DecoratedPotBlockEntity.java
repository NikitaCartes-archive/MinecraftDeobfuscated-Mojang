package net.minecraft.world.level.block.entity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.ticks.ContainerSingleItem;

public class DecoratedPotBlockEntity extends BlockEntity implements RandomizableContainer, ContainerSingleItem.BlockContainerSingleItem {
	public static final String TAG_SHERDS = "sherds";
	public static final String TAG_ITEM = "item";
	public static final int EVENT_POT_WOBBLES = 1;
	public long wobbleStartedAtTick;
	@Nullable
	public DecoratedPotBlockEntity.WobbleStyle lastWobbleStyle;
	private PotDecorations decorations;
	private ItemStack item = ItemStack.EMPTY;
	@Nullable
	protected ResourceKey<LootTable> lootTable;
	protected long lootTableSeed;

	public DecoratedPotBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.DECORATED_POT, blockPos, blockState);
		this.decorations = PotDecorations.EMPTY;
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		this.decorations.save(compoundTag);
		if (!this.trySaveLootTable(compoundTag) && !this.item.isEmpty()) {
			compoundTag.put("item", this.item.save(provider));
		}
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		this.decorations = PotDecorations.load(compoundTag);
		if (!this.tryLoadLootTable(compoundTag)) {
			if (compoundTag.contains("item", 10)) {
				this.item = (ItemStack)ItemStack.parse(provider, compoundTag.getCompound("item")).orElse(ItemStack.EMPTY);
			} else {
				this.item = ItemStack.EMPTY;
			}
		}
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		return this.saveCustomOnly(provider);
	}

	public Direction getDirection() {
		return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
	}

	public PotDecorations getDecorations() {
		return this.decorations;
	}

	public void setFromItem(ItemStack itemStack) {
		this.applyComponentsFromItemStack(itemStack);
	}

	public ItemStack getPotAsItem() {
		ItemStack itemStack = Items.DECORATED_POT.getDefaultInstance();
		itemStack.applyComponents(this.collectComponents());
		return itemStack;
	}

	public static ItemStack createDecoratedPotItem(PotDecorations potDecorations) {
		ItemStack itemStack = Items.DECORATED_POT.getDefaultInstance();
		itemStack.set(DataComponents.POT_DECORATIONS, potDecorations);
		return itemStack;
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
	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
		super.collectImplicitComponents(builder);
		builder.set(DataComponents.POT_DECORATIONS, this.decorations);
		builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(this.item)));
	}

	@Override
	protected void applyImplicitComponents(BlockEntity.DataComponentInput dataComponentInput) {
		super.applyImplicitComponents(dataComponentInput);
		this.decorations = dataComponentInput.getOrDefault(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY);
		this.item = dataComponentInput.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyOne();
	}

	@Override
	public void removeComponentsFromTag(CompoundTag compoundTag) {
		super.removeComponentsFromTag(compoundTag);
		compoundTag.remove("sherds");
		compoundTag.remove("item");
	}

	@Override
	public ItemStack getTheItem() {
		this.unpackLootTable(null);
		return this.item;
	}

	@Override
	public ItemStack splitTheItem(int i) {
		this.unpackLootTable(null);
		ItemStack itemStack = this.item.split(i);
		if (this.item.isEmpty()) {
			this.item = ItemStack.EMPTY;
		}

		return itemStack;
	}

	@Override
	public void setTheItem(ItemStack itemStack) {
		this.unpackLootTable(null);
		this.item = itemStack;
	}

	@Override
	public BlockEntity getContainerBlockEntity() {
		return this;
	}

	public void wobble(DecoratedPotBlockEntity.WobbleStyle wobbleStyle) {
		if (this.level != null && !this.level.isClientSide()) {
			this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 1, wobbleStyle.ordinal());
		}
	}

	@Override
	public boolean triggerEvent(int i, int j) {
		if (this.level != null && i == 1 && j >= 0 && j < DecoratedPotBlockEntity.WobbleStyle.values().length) {
			this.wobbleStartedAtTick = this.level.getGameTime();
			this.lastWobbleStyle = DecoratedPotBlockEntity.WobbleStyle.values()[j];
			return true;
		} else {
			return super.triggerEvent(i, j);
		}
	}

	public static enum WobbleStyle {
		POSITIVE(7),
		NEGATIVE(10);

		public final int duration;

		private WobbleStyle(final int j) {
			this.duration = j;
		}
	}
}
