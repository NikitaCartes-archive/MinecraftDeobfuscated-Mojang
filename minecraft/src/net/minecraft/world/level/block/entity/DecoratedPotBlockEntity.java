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

public class DecoratedPotBlockEntity extends BlockEntity {
	public static final String TAG_SHERDS = "sherds";
	private DecoratedPotBlockEntity.Decorations decorations = DecoratedPotBlockEntity.Decorations.EMPTY;

	public DecoratedPotBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.DECORATED_POT, blockPos, blockState);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);
		this.decorations.save(compoundTag);
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.decorations = DecoratedPotBlockEntity.Decorations.load(compoundTag);
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

	public ItemStack getItem() {
		return createDecoratedPotItem(this.decorations);
	}

	public static ItemStack createDecoratedPotItem(DecoratedPotBlockEntity.Decorations decorations) {
		ItemStack itemStack = Items.DECORATED_POT.getDefaultInstance();
		CompoundTag compoundTag = decorations.save(new CompoundTag());
		BlockItem.setBlockEntityData(itemStack, BlockEntityType.DECORATED_POT, compoundTag);
		return itemStack;
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
}
