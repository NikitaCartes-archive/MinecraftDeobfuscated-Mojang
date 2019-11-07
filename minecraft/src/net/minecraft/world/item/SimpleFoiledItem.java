package net.minecraft.world.item;

public class SimpleFoiledItem extends Item {
	public SimpleFoiledItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return true;
	}
}
