package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;

public class BannerPatternItem extends Item {
	private final BannerPattern bannerPattern;

	public BannerPatternItem(BannerPattern bannerPattern, Item.Properties properties) {
		super(properties);
		this.bannerPattern = bannerPattern;
	}

	public BannerPattern getBannerPattern() {
		return this.bannerPattern;
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		list.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
	}

	public MutableComponent getDisplayName() {
		return new TranslatableComponent(this.getDescriptionId() + ".desc");
	}
}
