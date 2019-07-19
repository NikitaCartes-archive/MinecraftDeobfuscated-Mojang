package net.minecraft.world.item;

public class BookItem extends Item {
	public BookItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public boolean isEnchantable(ItemStack itemStack) {
		return itemStack.getCount() == 1;
	}

	@Override
	public int getEnchantmentValue() {
		return 1;
	}
}
