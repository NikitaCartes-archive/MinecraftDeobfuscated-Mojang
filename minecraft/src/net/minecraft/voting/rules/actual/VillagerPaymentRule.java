package net.minecraft.voting.rules.actual;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.voting.rules.FixedOrRandomKeyRule;
import net.minecraft.world.item.Item;

public class VillagerPaymentRule extends FixedOrRandomKeyRule<Item> {
	private static final ResourceKey<Item> NOT_EGG = ResourceKey.create(Registries.ITEM, new ResourceLocation("emerald"));

	public VillagerPaymentRule() {
		super(Registries.ITEM, Component.translatable("rule.payment.seed_reshuffle"), Component.translatable("rule.payment.seed"), NOT_EGG);
	}

	@Override
	protected Component valueDescription(ResourceKey<Item> resourceKey) {
		Item item = BuiltInRegistries.ITEM.get(resourceKey);
		return Component.translatable("rule.payment.item", item.getDescription());
	}
}
