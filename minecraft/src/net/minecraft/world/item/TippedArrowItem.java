package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

public class TippedArrowItem extends ArrowItem {
	public TippedArrowItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack getDefaultInstance() {
		return PotionUtils.setPotion(super.getDefaultInstance(), Potions.POISON);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		PotionUtils.addPotionTooltip(itemStack, list, 0.125F, level == null ? 20.0F : level.tickRateManager().tickrate());
	}

	@Override
	public String getDescriptionId(ItemStack itemStack) {
		return PotionUtils.getPotion(itemStack).getName(this.getDescriptionId() + ".effect.");
	}
}
