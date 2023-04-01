package net.minecraft.voting.rules.actual;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.voting.rules.FixedOrRandomKeyRule;
import net.minecraft.world.item.Item;

public class ChickenEggRule extends FixedOrRandomKeyRule<Item> {
	private static final ResourceKey<Item> EGG = ResourceKey.create(Registries.ITEM, new ResourceLocation("egg"));

	public ChickenEggRule() {
		super(Registries.ITEM, Component.translatable("rule.egg_free.seed_reshuffle"), Component.translatable("rule.egg_free.seed"), EGG);
	}

	@Override
	protected Component valueDescription(ResourceKey<Item> resourceKey) {
		Item item = BuiltInRegistries.ITEM.get(resourceKey);
		return Component.translatable("rule.egg_free.item", item.getDescription());
	}
}
