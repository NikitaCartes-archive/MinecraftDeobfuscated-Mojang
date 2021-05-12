package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class EnchantedItemTrigger extends SimpleCriterionTrigger<EnchantedItemTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("enchanted_item");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public EnchantedItemTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("levels"));
		return new EnchantedItemTrigger.TriggerInstance(composite, itemPredicate, ints);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, int i) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack, i));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;
		private final MinMaxBounds.Ints levels;

		public TriggerInstance(EntityPredicate.Composite composite, ItemPredicate itemPredicate, MinMaxBounds.Ints ints) {
			super(EnchantedItemTrigger.ID, composite);
			this.item = itemPredicate;
			this.levels = ints;
		}

		public static EnchantedItemTrigger.TriggerInstance enchantedItem() {
			return new EnchantedItemTrigger.TriggerInstance(EntityPredicate.Composite.ANY, ItemPredicate.ANY, MinMaxBounds.Ints.ANY);
		}

		public boolean matches(ItemStack itemStack, int i) {
			return !this.item.matches(itemStack) ? false : this.levels.matches(i);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("item", this.item.serializeToJson());
			jsonObject.add("levels", this.levels.serializeToJson());
			return jsonObject;
		}
	}
}
