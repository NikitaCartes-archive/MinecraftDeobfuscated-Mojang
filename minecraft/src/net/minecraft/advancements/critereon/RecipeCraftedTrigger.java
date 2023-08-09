package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "recipe_id"));
		List<ItemPredicate> list = ItemPredicate.fromJsonArray(jsonObject.get("ingredients"));
		return new RecipeCraftedTrigger.TriggerInstance(optional, resourceLocation, list);
	}

	public void trigger(ServerPlayer serverPlayer, ResourceLocation resourceLocation, List<ItemStack> list) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(resourceLocation, list));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ResourceLocation recipeId;
		private final List<ItemPredicate> predicates;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, ResourceLocation resourceLocation, List<ItemPredicate> list) {
			super(RecipeCraftedTrigger.ID, optional);
			this.recipeId = resourceLocation;
			this.predicates = list;
		}

		public static RecipeCraftedTrigger.TriggerInstance craftedItem(ResourceLocation resourceLocation, List<ItemPredicate.Builder> list) {
			return new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), resourceLocation, list.stream().flatMap(builder -> builder.build().stream()).toList());
		}

		public static RecipeCraftedTrigger.TriggerInstance craftedItem(ResourceLocation resourceLocation) {
			return new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), resourceLocation, List.of());
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
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			jsonObject.addProperty("recipe_id", this.recipeId.toString());
			if (!this.predicates.isEmpty()) {
				jsonObject.add("ingredients", ItemPredicate.serializeToJsonArray(this.predicates));
			}

			return jsonObject;
		}
	}
}
