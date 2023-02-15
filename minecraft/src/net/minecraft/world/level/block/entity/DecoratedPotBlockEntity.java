package net.minecraft.world.level.block.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DecoratedPotBlockEntity extends BlockEntity {
	private static final String TAG_SHARDS = "shards";
	private static final int SHARDS_IN_POT = 4;
	private boolean isBroken = false;
	private final List<Item> shards = Util.make(new ArrayList(4), arrayList -> {
		arrayList.add(Items.BRICK);
		arrayList.add(Items.BRICK);
		arrayList.add(Items.BRICK);
		arrayList.add(Items.BRICK);
	});

	public DecoratedPotBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.DECORATED_POT, blockPos, blockState);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);
		saveShards(this.shards, compoundTag);
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		if (compoundTag.contains("shards", 9)) {
			ListTag listTag = compoundTag.getList("shards", 8);
			this.shards.clear();
			int i = Math.min(4, listTag.size());

			for (int j = 0; j < i; j++) {
				if (listTag.get(j) instanceof StringTag stringTag) {
					this.shards.add(BuiltInRegistries.ITEM.get(new ResourceLocation(stringTag.getAsString())));
				} else {
					this.shards.add(Items.BRICK);
				}
			}

			int jx = 4 - i;

			for (int k = 0; k < jx; k++) {
				this.shards.add(Items.BRICK);
			}
		}
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}

	public static void saveShards(List<Item> list, CompoundTag compoundTag) {
		ListTag listTag = new ListTag();

		for (Item item : list) {
			listTag.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
		}

		compoundTag.put("shards", listTag);
	}

	public ItemStack getItem() {
		ItemStack itemStack = new ItemStack(Blocks.DECORATED_POT);
		CompoundTag compoundTag = new CompoundTag();
		saveShards(this.shards, compoundTag);
		BlockItem.setBlockEntityData(itemStack, BlockEntityType.DECORATED_POT, compoundTag);
		return itemStack;
	}

	public List<Item> getShards() {
		return this.shards;
	}

	public void playerDestroy(Level level, BlockPos blockPos, ItemStack itemStack, Player player) {
		if (player.isCreative()) {
			this.isBroken = true;
		} else {
			if (itemStack.is(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasSilkTouch(itemStack)) {
				List<Item> list = this.getShards();
				NonNullList<ItemStack> nonNullList = NonNullList.createWithCapacity(list.size());
				nonNullList.addAll(0, list.stream().map(Item::getDefaultInstance).toList());
				Containers.dropContents(level, blockPos, nonNullList);
				this.isBroken = true;
				level.playSound(null, blockPos, SoundEvents.DECORATED_POT_SHATTER, SoundSource.PLAYERS, 1.0F, 1.0F);
			}
		}
	}

	public boolean isBroken() {
		return this.isBroken;
	}

	public Direction getDirection() {
		return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
	}

	public void setFromItem(ItemStack itemStack) {
		CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
		if (compoundTag != null) {
			this.load(compoundTag);
		}
	}
}
