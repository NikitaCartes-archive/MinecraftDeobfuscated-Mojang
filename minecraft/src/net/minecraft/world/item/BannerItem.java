package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.apache.commons.lang3.Validate;

public class BannerItem extends StandingAndWallBlockItem {
	private static final String PATTERN_PREFIX = "block.minecraft.banner.";

	public BannerItem(Block block, Block block2, Item.Properties properties) {
		super(block, block2, properties, Direction.DOWN);
		Validate.isInstanceOf(AbstractBannerBlock.class, block);
		Validate.isInstanceOf(AbstractBannerBlock.class, block2);
	}

	public static void appendHoverTextFromBannerBlockEntityTag(ItemStack itemStack, List<Component> list) {
		BannerPatternLayers bannerPatternLayers = itemStack.get(DataComponents.BANNER_PATTERNS);
		if (bannerPatternLayers != null) {
			for (int i = 0; i < Math.min(bannerPatternLayers.layers().size(), 6); i++) {
				BannerPatternLayers.Layer layer = (BannerPatternLayers.Layer)bannerPatternLayers.layers().get(i);
				layer.pattern()
					.unwrapKey()
					.map(resourceKey -> resourceKey.location().toShortLanguageKey())
					.ifPresent(string -> list.add(Component.translatable("block.minecraft.banner." + string + "." + layer.color().getName()).withStyle(ChatFormatting.GRAY)));
			}
		}
	}

	public DyeColor getColor() {
		return ((AbstractBannerBlock)this.getBlock()).getColor();
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		appendHoverTextFromBannerBlockEntityTag(itemStack, list);
	}
}
