package net.minecraft.world.item;

public class TieredItem extends Item {
	private final Tier tier;

	public TieredItem(Tier tier, Item.Properties properties) {
		super(properties.defaultDurability(tier.getUses()));
		this.tier = tier;
	}

	public Tier getTier() {
		return this.tier;
	}

	@Override
	public int getEnchantmentValue() {
		return this.tier.getEnchantmentValue();
	}

	@Override
	public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
		return this.tier.getRepairIngredient().test(itemStack2) || super.isValidRepairItem(itemStack, itemStack2);
	}
}
