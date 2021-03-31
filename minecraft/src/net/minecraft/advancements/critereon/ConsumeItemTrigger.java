package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ConsumeItemTrigger extends SimpleCriterionTrigger<ConsumeItemTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("consume_item");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public ConsumeItemTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		return new ConsumeItemTrigger.TriggerInstance(composite, ItemPredicate.fromJson(jsonObject.get("item")));
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;

		public TriggerInstance(EntityPredicate.Composite composite, ItemPredicate itemPredicate) {
			super(ConsumeItemTrigger.ID, composite);
			this.item = itemPredicate;
		}

		public static ConsumeItemTrigger.TriggerInstance usedItem() {
			return new ConsumeItemTrigger.TriggerInstance(EntityPredicate.Composite.ANY, ItemPredicate.ANY);
		}

		public static ConsumeItemTrigger.TriggerInstance usedItem(ItemPredicate itemPredicate) {
			return new ConsumeItemTrigger.TriggerInstance(EntityPredicate.Composite.ANY, itemPredicate);
		}

		public static ConsumeItemTrigger.TriggerInstance usedItem(ItemLike itemLike) {
			return new ConsumeItemTrigger.TriggerInstance(
				EntityPredicate.Composite.ANY,
				new ItemPredicate(
					null, itemLike.asItem(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NbtPredicate.ANY
				)
			);
		}

		public boolean matches(ItemStack itemStack) {
			return this.item.matches(itemStack);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("item", this.item.serializeToJson());
			return jsonObject;
		}
	}
}
