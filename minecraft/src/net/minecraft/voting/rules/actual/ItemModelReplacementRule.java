package net.minecraft.voting.rules.actual;

import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ItemModelReplacementRule extends ItemReplacementRule {
	private int versionId;

	public ItemModelReplacementRule() {
		super("rule.replace_item_model");
	}

	public Item replace(Item item) {
		ResourceKey<Item> resourceKey = this.get(item);
		return resourceKey != null ? (Item)Objects.requireNonNullElse(BuiltInRegistries.ITEM.get(resourceKey), item) : item;
	}

	@Override
	protected void set(ResourceKey<Item> resourceKey, ResourceKey<Item> resourceKey2) {
		this.versionId++;
		super.set(resourceKey, resourceKey2);
	}

	@Override
	protected void remove(ResourceKey<Item> resourceKey) {
		this.versionId++;
		super.remove(resourceKey);
	}

	public int versionId() {
		return this.versionId;
	}
}
