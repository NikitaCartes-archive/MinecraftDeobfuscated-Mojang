package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class UsingItemTrigger extends SimpleCriterionTrigger<UsingItemTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("using_item");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public UsingItemTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new UsingItemTrigger.TriggerInstance(composite, itemPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;

		public TriggerInstance(EntityPredicate.Composite composite, ItemPredicate itemPredicate) {
			super(UsingItemTrigger.ID, composite);
			this.item = itemPredicate;
		}

		public static UsingItemTrigger.TriggerInstance lookingAt(EntityPredicate.Builder builder, ItemPredicate.Builder builder2) {
			return new UsingItemTrigger.TriggerInstance(EntityPredicate.Composite.wrap(builder.build()), builder2.build());
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
