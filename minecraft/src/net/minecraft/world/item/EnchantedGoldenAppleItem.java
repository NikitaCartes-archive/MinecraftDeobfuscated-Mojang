package net.minecraft.world.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class EnchantedGoldenAppleItem extends Item {
	public EnchantedGoldenAppleItem(Item.Properties properties) {
		super(properties);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean isFoil(ItemStack itemStack) {
		return true;
	}
}
