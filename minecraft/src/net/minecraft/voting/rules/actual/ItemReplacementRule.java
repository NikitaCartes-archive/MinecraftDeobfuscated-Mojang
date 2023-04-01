package net.minecraft.voting.rules.actual;

import javax.annotation.Nullable;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.voting.rules.ResourceKeyReplacementRule;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemReplacementRule extends ResourceKeyReplacementRule<Item> {
	private final String descriptionId;

	public ItemReplacementRule(String string) {
		super(Registries.ITEM);
		this.descriptionId = string;
	}

	@Nullable
	public ResourceKey<Item> get(Item item) {
		return (ResourceKey<Item>)this.entries.get(item.builtInRegistryHolder().key());
	}

	public ItemStack adjustItemStack(RegistryAccess registryAccess, ItemStack itemStack) {
		ResourceKey<Item> resourceKey = (ResourceKey<Item>)this.entries.get(itemStack.getItem().builtInRegistryHolder().key());
		if (resourceKey != null) {
			Item item = registryAccess.registryOrThrow(Registries.ITEM).get(resourceKey);
			if (item != null) {
				ItemStack itemStack2 = new ItemStack(item, itemStack.getCount());
				itemStack2.setTag(itemStack.getTag());
				return itemStack2;
			}
		}

		return itemStack;
	}

	protected Component description(ResourceKey<Item> resourceKey, ResourceKey<Item> resourceKey2) {
		Component component = BuiltInRegistries.ITEM.get(resourceKey).getDescription();
		Component component2 = BuiltInRegistries.ITEM.get(resourceKey2).getDescription();
		return Component.translatable(this.descriptionId, component, component2);
	}
}
