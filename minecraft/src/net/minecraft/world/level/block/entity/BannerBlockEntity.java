package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BannerBlockEntity extends BlockEntity implements Nameable {
	public static final int MAX_PATTERNS = 6;
	public static final String TAG_PATTERNS = "Patterns";
	public static final String TAG_PATTERN = "Pattern";
	public static final String TAG_COLOR = "Color";
	@Nullable
	private Component name;
	private DyeColor baseColor;
	@Nullable
	private ListTag itemPatterns;
	@Nullable
	private List<Pair<BannerPattern, DyeColor>> patterns;

	public BannerBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.BANNER, blockPos, blockState);
		this.baseColor = ((AbstractBannerBlock)blockState.getBlock()).getColor();
	}

	public BannerBlockEntity(BlockPos blockPos, BlockState blockState, DyeColor dyeColor) {
		this(blockPos, blockState);
		this.baseColor = dyeColor;
	}

	@Nullable
	public static ListTag getItemPatterns(ItemStack itemStack) {
		ListTag listTag = null;
		CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
		if (compoundTag != null && compoundTag.contains("Patterns", 9)) {
			listTag = compoundTag.getList("Patterns", 10).copy();
		}

		return listTag;
	}

	public void fromItem(ItemStack itemStack, DyeColor dyeColor) {
		this.baseColor = dyeColor;
		this.fromItem(itemStack);
	}

	public void fromItem(ItemStack itemStack) {
		this.itemPatterns = getItemPatterns(itemStack);
		this.patterns = null;
		this.name = itemStack.hasCustomHoverName() ? itemStack.getHoverName() : null;
	}

	@Override
	public Component getName() {
		return (Component)(this.name != null ? this.name : new TranslatableComponent("block.minecraft.banner"));
	}

	@Nullable
	@Override
	public Component getCustomName() {
		return this.name;
	}

	public void setCustomName(Component component) {
		this.name = component;
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);
		if (this.itemPatterns != null) {
			compoundTag.put("Patterns", this.itemPatterns);
		}

		if (this.name != null) {
			compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
		}
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		if (compoundTag.contains("CustomName", 8)) {
			this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
		}

		this.itemPatterns = compoundTag.getList("Patterns", 10);
		this.patterns = null;
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}

	public static int getPatternCount(ItemStack itemStack) {
		CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
		return compoundTag != null && compoundTag.contains("Patterns") ? compoundTag.getList("Patterns", 10).size() : 0;
	}

	public List<Pair<BannerPattern, DyeColor>> getPatterns() {
		if (this.patterns == null) {
			this.patterns = createPatterns(this.baseColor, this.itemPatterns);
		}

		return this.patterns;
	}

	public static List<Pair<BannerPattern, DyeColor>> createPatterns(DyeColor dyeColor, @Nullable ListTag listTag) {
		List<Pair<BannerPattern, DyeColor>> list = Lists.<Pair<BannerPattern, DyeColor>>newArrayList();
		list.add(Pair.of(BannerPattern.BASE, dyeColor));
		if (listTag != null) {
			for (int i = 0; i < listTag.size(); i++) {
				CompoundTag compoundTag = listTag.getCompound(i);
				BannerPattern bannerPattern = BannerPattern.byHash(compoundTag.getString("Pattern"));
				if (bannerPattern != null) {
					int j = compoundTag.getInt("Color");
					list.add(Pair.of(bannerPattern, DyeColor.byId(j)));
				}
			}
		}

		return list;
	}

	public static void removeLastPattern(ItemStack itemStack) {
		CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
		if (compoundTag != null && compoundTag.contains("Patterns", 9)) {
			ListTag listTag = compoundTag.getList("Patterns", 10);
			if (!listTag.isEmpty()) {
				listTag.remove(listTag.size() - 1);
				if (listTag.isEmpty()) {
					compoundTag.remove("Patterns");
				}

				BlockItem.setBlockEntityData(itemStack, BlockEntityType.BANNER, compoundTag);
			}
		}
	}

	public ItemStack getItem() {
		ItemStack itemStack = new ItemStack(BannerBlock.byColor(this.baseColor));
		if (this.itemPatterns != null && !this.itemPatterns.isEmpty()) {
			CompoundTag compoundTag = new CompoundTag();
			compoundTag.put("Patterns", this.itemPatterns.copy());
			BlockItem.setBlockEntityData(itemStack, this.getType(), compoundTag);
		}

		if (this.name != null) {
			itemStack.setHoverName(this.name);
		}

		return itemStack;
	}

	public DyeColor getBaseColor() {
		return this.baseColor;
	}
}
