package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.tuple.Pair;

public enum BannerPattern {
	BASE("base", "b"),
	SQUARE_BOTTOM_LEFT("square_bottom_left", "bl", "   ", "   ", "#  "),
	SQUARE_BOTTOM_RIGHT("square_bottom_right", "br", "   ", "   ", "  #"),
	SQUARE_TOP_LEFT("square_top_left", "tl", "#  ", "   ", "   "),
	SQUARE_TOP_RIGHT("square_top_right", "tr", "  #", "   ", "   "),
	STRIPE_BOTTOM("stripe_bottom", "bs", "   ", "   ", "###"),
	STRIPE_TOP("stripe_top", "ts", "###", "   ", "   "),
	STRIPE_LEFT("stripe_left", "ls", "#  ", "#  ", "#  "),
	STRIPE_RIGHT("stripe_right", "rs", "  #", "  #", "  #"),
	STRIPE_CENTER("stripe_center", "cs", " # ", " # ", " # "),
	STRIPE_MIDDLE("stripe_middle", "ms", "   ", "###", "   "),
	STRIPE_DOWNRIGHT("stripe_downright", "drs", "#  ", " # ", "  #"),
	STRIPE_DOWNLEFT("stripe_downleft", "dls", "  #", " # ", "#  "),
	STRIPE_SMALL("small_stripes", "ss", "# #", "# #", "   "),
	CROSS("cross", "cr", "# #", " # ", "# #"),
	STRAIGHT_CROSS("straight_cross", "sc", " # ", "###", " # "),
	TRIANGLE_BOTTOM("triangle_bottom", "bt", "   ", " # ", "# #"),
	TRIANGLE_TOP("triangle_top", "tt", "# #", " # ", "   "),
	TRIANGLES_BOTTOM("triangles_bottom", "bts", "   ", "# #", " # "),
	TRIANGLES_TOP("triangles_top", "tts", " # ", "# #", "   "),
	DIAGONAL_LEFT("diagonal_left", "ld", "## ", "#  ", "   "),
	DIAGONAL_RIGHT("diagonal_up_right", "rd", "   ", "  #", " ##"),
	DIAGONAL_LEFT_MIRROR("diagonal_up_left", "lud", "   ", "#  ", "## "),
	DIAGONAL_RIGHT_MIRROR("diagonal_right", "rud", " ##", "  #", "   "),
	CIRCLE_MIDDLE("circle", "mc", "   ", " # ", "   "),
	RHOMBUS_MIDDLE("rhombus", "mr", " # ", "# #", " # "),
	HALF_VERTICAL("half_vertical", "vh", "## ", "## ", "## "),
	HALF_HORIZONTAL("half_horizontal", "hh", "###", "###", "   "),
	HALF_VERTICAL_MIRROR("half_vertical_right", "vhr", " ##", " ##", " ##"),
	HALF_HORIZONTAL_MIRROR("half_horizontal_bottom", "hhb", "   ", "###", "###"),
	BORDER("border", "bo", "###", "# #", "###"),
	CURLY_BORDER("curly_border", "cbo", new ItemStack(Blocks.VINE)),
	GRADIENT("gradient", "gra", "# #", " # ", " # "),
	GRADIENT_UP("gradient_up", "gru", " # ", " # ", "# #"),
	BRICKS("bricks", "bri", new ItemStack(Blocks.BRICKS)),
	GLOBE("globe", "glb"),
	CREEPER("creeper", "cre", new ItemStack(Items.CREEPER_HEAD)),
	SKULL("skull", "sku", new ItemStack(Items.WITHER_SKELETON_SKULL)),
	FLOWER("flower", "flo", new ItemStack(Blocks.OXEYE_DAISY)),
	MOJANG("mojang", "moj", new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));

	public static final int COUNT = values().length;
	public static final int AVAILABLE_PATTERNS = COUNT - 5 - 1;
	private final String filename;
	private final String hashname;
	private final String[] patterns = new String[3];
	private ItemStack patternItem = ItemStack.EMPTY;

	private BannerPattern(String string2, String string3) {
		this.filename = string2;
		this.hashname = string3;
	}

	private BannerPattern(String string2, String string3, ItemStack itemStack) {
		this(string2, string3);
		this.patternItem = itemStack;
	}

	private BannerPattern(String string2, String string3, String string4, String string5, String string6) {
		this(string2, string3);
		this.patterns[0] = string4;
		this.patterns[1] = string5;
		this.patterns[2] = string6;
	}

	@Environment(EnvType.CLIENT)
	public ResourceLocation location() {
		return new ResourceLocation("entity/banner/" + this.getFilename());
	}

	@Environment(EnvType.CLIENT)
	public String getFilename() {
		return this.filename;
	}

	public String getHashname() {
		return this.hashname;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public static BannerPattern byHash(String string) {
		for (BannerPattern bannerPattern : values()) {
			if (bannerPattern.hashname.equals(string)) {
				return bannerPattern;
			}
		}

		return null;
	}

	public static class Builder {
		private final List<Pair<BannerPattern, DyeColor>> patterns = Lists.<Pair<BannerPattern, DyeColor>>newArrayList();

		public BannerPattern.Builder addPattern(BannerPattern bannerPattern, DyeColor dyeColor) {
			this.patterns.add(Pair.of(bannerPattern, dyeColor));
			return this;
		}

		public ListTag toListTag() {
			ListTag listTag = new ListTag();

			for (Pair<BannerPattern, DyeColor> pair : this.patterns) {
				CompoundTag compoundTag = new CompoundTag();
				compoundTag.putString("Pattern", pair.getLeft().hashname);
				compoundTag.putInt("Color", pair.getRight().getId());
				listTag.add(compoundTag);
			}

			return listTag;
		}
	}
}
