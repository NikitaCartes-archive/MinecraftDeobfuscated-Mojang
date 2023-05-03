package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class RecipeCraftedTrigger extends SimpleCriterionTrigger<RecipeCraftedTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("recipe_crafted");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	protected RecipeCraftedTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "recipe_id"));
		ItemPredicate[] itemPredicates = ItemPredicate.fromJsonArray(jsonObject.get("ingredients"));
		return new RecipeCraftedTrigger.TriggerInstance(contextAwarePredicate, resourceLocation, List.of(itemPredicates));
	}

	public void trigger(ServerPlayer serverPlayer, ResourceLocation resourceLocation, List<ItemStack> list) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(resourceLocation, list));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ResourceLocation recipeId;
		private final List<ItemPredicate> predicates;

		public TriggerInstance(ContextAwarePredicate contextAwarePredicate, ResourceLocation resourceLocation, List<ItemPredicate> list) {
			super(RecipeCraftedTrigger.ID, contextAwarePredicate);
			this.recipeId = resourceLocation;
			this.predicates = list;
		}

		public static RecipeCraftedTrigger.TriggerInstance craftedItem(ResourceLocation resourceLocation, List<ItemPredicate> list) {
			return new RecipeCraftedTrigger.TriggerInstance(ContextAwarePredicate.ANY, resourceLocation, list);
		}

		public static RecipeCraftedTrigger.TriggerInstance craftedItem(ResourceLocation resourceLocation) {
			return new RecipeCraftedTrigger.TriggerInstance(ContextAwarePredicate.ANY, resourceLocation, List.of());
		}

		boolean matches(ResourceLocation resourceLocation, List<ItemStack> list) {
			if (!resourceLocation.equals(this.recipeId)) {
				return false;
			} else {
				List<ItemStack> list2 = new ArrayList(list);

				for (ItemPredicate itemPredicate : this.predicates) {
					boolean bl = false;
					Iterator<ItemStack> iterator = list2.iterator();

					while (iterator.hasNext()) {
						if (itemPredicate.matches((ItemStack)iterator.next())) {
							iterator.remove();
							bl = true;
							break;
						}
					}

					if (!bl) {
						return false;
					}
				}

				return true;
			}
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.addProperty("recipe_id", this.recipeId.toString());
			if (this.predicates.size() > 0) {
				JsonArray jsonArray = new JsonArray();

				for (ItemPredicate itemPredicate : this.predicates) {
					jsonArray.add(itemPredicate.serializeToJson());
				}

				jsonObject.add("ingredients", jsonArray);
			}

			return jsonObject;
		}
	}
}
