package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeCraftedTrigger extends SimpleCriterionTrigger<RecipeCraftedTrigger.TriggerInstance> {
	@Override
	public Codec<RecipeCraftedTrigger.TriggerInstance> codec() {
		return RecipeCraftedTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, ResourceKey<Recipe<?>> resourceKey, List<ItemStack> list) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(resourceKey, list));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Recipe<?>> recipeId, List<ItemPredicate> ingredients)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<RecipeCraftedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(RecipeCraftedTrigger.TriggerInstance::player),
						ResourceKey.codec(Registries.RECIPE).fieldOf("recipe_id").forGetter(RecipeCraftedTrigger.TriggerInstance::recipeId),
						ItemPredicate.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(RecipeCraftedTrigger.TriggerInstance::ingredients)
					)
					.apply(instance, RecipeCraftedTrigger.TriggerInstance::new)
		);

		public static Criterion<RecipeCraftedTrigger.TriggerInstance> craftedItem(ResourceKey<Recipe<?>> resourceKey, List<ItemPredicate.Builder> list) {
			return CriteriaTriggers.RECIPE_CRAFTED
				.createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), resourceKey, list.stream().map(ItemPredicate.Builder::build).toList()));
		}

		public static Criterion<RecipeCraftedTrigger.TriggerInstance> craftedItem(ResourceKey<Recipe<?>> resourceKey) {
			return CriteriaTriggers.RECIPE_CRAFTED.createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), resourceKey, List.of()));
		}

		public static Criterion<RecipeCraftedTrigger.TriggerInstance> crafterCraftedItem(ResourceKey<Recipe<?>> resourceKey) {
			return CriteriaTriggers.CRAFTER_RECIPE_CRAFTED.createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), resourceKey, List.of()));
		}

		boolean matches(ResourceKey<Recipe<?>> resourceKey, List<ItemStack> list) {
			if (resourceKey != this.recipeId) {
				return false;
			} else {
				List<ItemStack> list2 = new ArrayList(list);

				for (ItemPredicate itemPredicate : this.ingredients) {
					boolean bl = false;
					Iterator<ItemStack> iterator = list2.iterator();

					while (iterator.hasNext()) {
						if (itemPredicate.test((ItemStack)iterator.next())) {
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
	}
}
