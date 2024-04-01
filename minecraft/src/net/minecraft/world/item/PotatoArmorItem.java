package net.minecraft.world.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class PotatoArmorItem extends ArmorItem {
	public PotatoArmorItem(Holder<ArmorMaterial> holder, ArmorItem.Type type, Item.Properties properties) {
		super(holder, type, properties);
	}

	@Override
	public SoundEvent getBreakingSound() {
		return SoundEvents.SLIME_SQUISH;
	}

	public static Item getPeelItem(ItemStack itemStack) {
		DyeColor dyeColor = itemStack.get(DataComponents.BASE_COLOR);
		return Items.POTATO_PEELS_MAP.get(dyeColor != null ? dyeColor : DyeColor.WHITE);
	}
}
