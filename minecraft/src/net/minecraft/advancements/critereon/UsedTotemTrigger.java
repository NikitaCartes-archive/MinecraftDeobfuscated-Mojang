package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class UsedTotemTrigger extends SimpleCriterionTrigger<UsedTotemTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("used_totem");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public UsedTotemTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new UsedTotemTrigger.TriggerInstance(itemPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;

		public TriggerInstance(ItemPredicate itemPredicate) {
			super(UsedTotemTrigger.ID);
			this.item = itemPredicate;
		}

		public static UsedTotemTrigger.TriggerInstance usedTotem(ItemLike itemLike) {
			return new UsedTotemTrigger.TriggerInstance(ItemPredicate.Builder.item().of(itemLike).build());
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
