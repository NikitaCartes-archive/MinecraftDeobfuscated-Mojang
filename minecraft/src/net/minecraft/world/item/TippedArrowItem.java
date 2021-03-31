package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.Potion;
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
	public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
		if (this.allowdedIn(creativeModeTab)) {
			for (Potion potion : Registry.POTION) {
				if (!potion.getEffects().isEmpty()) {
					nonNullList.add(PotionUtils.setPotion(new ItemStack(this), potion));
				}
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		PotionUtils.addPotionTooltip(itemStack, list, 0.125F);
	}

	@Override
	public String getDescriptionId(ItemStack itemStack) {
		return PotionUtils.getPotion(itemStack).getName(this.getDescriptionId() + ".effect.");
	}
}
