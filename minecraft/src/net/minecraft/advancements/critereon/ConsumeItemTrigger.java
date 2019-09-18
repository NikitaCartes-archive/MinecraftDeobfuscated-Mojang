package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
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

	public ConsumeItemTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		return new ConsumeItemTrigger.TriggerInstance(ItemPredicate.fromJson(jsonObject.get("item")));
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;

		public TriggerInstance(ItemPredicate itemPredicate) {
			super(ConsumeItemTrigger.ID);
			this.item = itemPredicate;
		}

		public static ConsumeItemTrigger.TriggerInstance usedItem() {
			return new ConsumeItemTrigger.TriggerInstance(ItemPredicate.ANY);
		}

		public static ConsumeItemTrigger.TriggerInstance usedItem(ItemLike itemLike) {
			return new ConsumeItemTrigger.TriggerInstance(
				new ItemPredicate(
					null, itemLike.asItem(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NbtPredicate.ANY
				)
			);
		}

		public boolean matches(ItemStack itemStack) {
			return this.item.matches(itemStack);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("item", this.item.serializeToJson());
			return jsonObject;
		}
	}
}
