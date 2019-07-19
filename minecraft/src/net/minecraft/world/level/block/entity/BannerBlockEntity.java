package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BannerBlockEntity extends BlockEntity implements Nameable {
	private Component name;
	private DyeColor baseColor = DyeColor.WHITE;
	private ListTag itemPatterns;
	private boolean receivedData;
	private List<BannerPattern> patterns;
	private List<DyeColor> colors;
	private String textureHashName;

	public BannerBlockEntity() {
		super(BlockEntityType.BANNER);
	}

	public BannerBlockEntity(DyeColor dyeColor) {
		this();
		this.baseColor = dyeColor;
	}

	@Environment(EnvType.CLIENT)
	public void fromItem(ItemStack itemStack, DyeColor dyeColor) {
		this.itemPatterns = null;
		CompoundTag compoundTag = itemStack.getTagElement("BlockEntityTag");
		if (compoundTag != null && compoundTag.contains("Patterns", 9)) {
			this.itemPatterns = compoundTag.getList("Patterns", 10).copy();
		}

		this.baseColor = dyeColor;
		this.patterns = null;
		this.colors = null;
		this.textureHashName = "";
		this.receivedData = true;
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
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		if (this.itemPatterns != null) {
			compoundTag.put("Patterns", this.itemPatterns);
		}

		if (this.name != null) {
			compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
		}

		return compoundTag;
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		if (compoundTag.contains("CustomName", 8)) {
			this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
		}

		if (this.hasLevel()) {
			this.baseColor = ((AbstractBannerBlock)this.getBlockState().getBlock()).getColor();
		} else {
			this.baseColor = null;
		}

		this.itemPatterns = compoundTag.getList("Patterns", 10);
		this.patterns = null;
		this.colors = null;
		this.textureHashName = null;
		this.receivedData = true;
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 6, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.save(new CompoundTag());
	}

	public static int getPatternCount(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTagElement("BlockEntityTag");
		return compoundTag != null && compoundTag.contains("Patterns") ? compoundTag.getList("Patterns", 10).size() : 0;
	}

	@Environment(EnvType.CLIENT)
	public List<BannerPattern> getPatterns() {
		this.createPatternList();
		return this.patterns;
	}

	@Environment(EnvType.CLIENT)
	public List<DyeColor> getColors() {
		this.createPatternList();
		return this.colors;
	}

	@Environment(EnvType.CLIENT)
	public String getTextureHashName() {
		this.createPatternList();
		return this.textureHashName;
	}

	@Environment(EnvType.CLIENT)
	private void createPatternList() {
		if (this.patterns == null || this.colors == null || this.textureHashName == null) {
			if (!this.receivedData) {
				this.textureHashName = "";
			} else {
				this.patterns = Lists.<BannerPattern>newArrayList();
				this.colors = Lists.<DyeColor>newArrayList();
				DyeColor dyeColor = this.getBaseColor(this::getBlockState);
				if (dyeColor == null) {
					this.textureHashName = "banner_missing";
				} else {
					this.patterns.add(BannerPattern.BASE);
					this.colors.add(dyeColor);
					this.textureHashName = "b" + dyeColor.getId();
					if (this.itemPatterns != null) {
						for (int i = 0; i < this.itemPatterns.size(); i++) {
							CompoundTag compoundTag = this.itemPatterns.getCompound(i);
							BannerPattern bannerPattern = BannerPattern.byHash(compoundTag.getString("Pattern"));
							if (bannerPattern != null) {
								this.patterns.add(bannerPattern);
								int j = compoundTag.getInt("Color");
								this.colors.add(DyeColor.byId(j));
								this.textureHashName = this.textureHashName + bannerPattern.getHashname() + j;
							}
						}
					}
				}
			}
		}
	}

	public static void removeLastPattern(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTagElement("BlockEntityTag");
		if (compoundTag != null && compoundTag.contains("Patterns", 9)) {
			ListTag listTag = compoundTag.getList("Patterns", 10);
			if (!listTag.isEmpty()) {
				listTag.remove(listTag.size() - 1);
				if (listTag.isEmpty()) {
					itemStack.removeTagKey("BlockEntityTag");
				}
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public ItemStack getItem(BlockState blockState) {
		ItemStack itemStack = new ItemStack(BannerBlock.byColor(this.getBaseColor(() -> blockState)));
		if (this.itemPatterns != null && !this.itemPatterns.isEmpty()) {
			itemStack.getOrCreateTagElement("BlockEntityTag").put("Patterns", this.itemPatterns.copy());
		}

		if (this.name != null) {
			itemStack.setHoverName(this.name);
		}

		return itemStack;
	}

	public DyeColor getBaseColor(Supplier<BlockState> supplier) {
		if (this.baseColor == null) {
			this.baseColor = ((AbstractBannerBlock)((BlockState)supplier.get()).getBlock()).getColor();
		}

		return this.baseColor;
	}
}
