package net.minecraft.world.item;

public class EnchantedGoldenAppleItem extends Item {
	public EnchantedGoldenAppleItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return true;
	}
}
