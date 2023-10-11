package net.minecraft.world.level.block.entity;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.ticks.ContainerSingleItem;

public class DecoratedPotBlockEntity extends BlockEntity implements ContainerSingleItem {
	public static final String TAG_SHERDS = "sherds";
	public static final String TAG_ITEM = "item";
	public static final int EVENT_POT_WOBBLES = 1;
	public long wobbleStartedAtTick;
	@Nullable
	public DecoratedPotBlockEntity.WobbleStyle lastWobbleStyle;
	private DecoratedPotBlockEntity.Decorations decorations;
	private ItemStack item = ItemStack.EMPTY;

	public DecoratedPotBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.DECORATED_POT, blockPos, blockState);
		this.decorations = DecoratedPotBlockEntity.Decorations.EMPTY;
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);
		this.decorations.save(compoundTag);
		if (!this.item.isEmpty()) {
			compoundTag.put("item", this.item.save(new CompoundTag()));
		}
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.decorations = DecoratedPotBlockEntity.Decorations.load(compoundTag);
		if (compoundTag.contains("item", 10)) {
			this.item = ItemStack.of(compoundTag.getCompound("item"));
		} else {
			this.item = ItemStack.EMPTY;
		}
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}

	public Direction getDirection() {
		return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
	}

	public DecoratedPotBlockEntity.Decorations getDecorations() {
		return this.decorations;
	}

	public void setFromItem(ItemStack itemStack) {
		this.decorations = DecoratedPotBlockEntity.Decorations.load(BlockItem.getBlockEntityData(itemStack));
	}

	public ItemStack getPotAsItem() {
		return createDecoratedPotItem(this.decorations);
	}

	public static ItemStack createDecoratedPotItem(DecoratedPotBlockEntity.Decorations decorations) {
		ItemStack itemStack = Items.DECORATED_POT.getDefaultInstance();
		CompoundTag compoundTag = decorations.save(new CompoundTag());
		BlockItem.setBlockEntityData(itemStack, BlockEntityType.DECORATED_POT, compoundTag);
		return itemStack;
	}

	@Override
	public ItemStack getTheItem() {
		return this.item;
	}

	@Override
	public ItemStack splitTheItem(int i) {
		ItemStack itemStack = this.item.split(i);
		if (this.item.isEmpty()) {
			this.item = ItemStack.EMPTY;
		}

		return itemStack;
	}

	@Override
	public void setTheItem(ItemStack itemStack) {
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

	public static record Decorations(Item back, Item left, Item right, Item front) {
		public static final DecoratedPotBlockEntity.Decorations EMPTY = new DecoratedPotBlockEntity.Decorations(Items.BRICK, Items.BRICK, Items.BRICK, Items.BRICK);

		public CompoundTag save(CompoundTag compoundTag) {
			if (this.equals(EMPTY)) {
				return compoundTag;
			} else {
				ListTag listTag = new ListTag();
				this.sorted().forEach(item -> listTag.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString())));
				compoundTag.put("sherds", listTag);
				return compoundTag;
			}
		}

		public Stream<Item> sorted() {
			return Stream.of(this.back, this.left, this.right, this.front);
		}

		public static DecoratedPotBlockEntity.Decorations load(@Nullable CompoundTag compoundTag) {
			if (compoundTag != null && compoundTag.contains("sherds", 9)) {
				ListTag listTag = compoundTag.getList("sherds", 8);
				return new DecoratedPotBlockEntity.Decorations(itemFromTag(listTag, 0), itemFromTag(listTag, 1), itemFromTag(listTag, 2), itemFromTag(listTag, 3));
			} else {
				return EMPTY;
			}
		}

		private static Item itemFromTag(ListTag listTag, int i) {
			if (i >= listTag.size()) {
				return Items.BRICK;
			} else {
				Tag tag = listTag.get(i);
				return BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getAsString()));
			}
		}
	}

	public static enum WobbleStyle {
		POSITIVE(7),
		NEGATIVE(10);

		public final int duration;

		private WobbleStyle(int j) {
			this.duration = j;
		}
	}
}
