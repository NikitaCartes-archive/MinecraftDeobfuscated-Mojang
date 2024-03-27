package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RecipeCraftedTrigger extends SimpleCriterionTrigger<RecipeCraftedTrigger.TriggerInstance> {
	@Override
	public Codec<RecipeCraftedTrigger.TriggerInstance> codec() {
		return RecipeCraftedTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, ResourceLocation resourceLocation, List<ItemStack> list) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(resourceLocation, list));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceLocation recipeId, List<ItemPredicate> ingredients)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<RecipeCraftedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(RecipeCraftedTrigger.TriggerInstance::player),
						ResourceLocation.CODEC.fieldOf("recipe_id").forGetter(RecipeCraftedTrigger.TriggerInstance::recipeId),
						ItemPredicate.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(RecipeCraftedTrigger.TriggerInstance::ingredients)
					)
					.apply(instance, RecipeCraftedTrigger.TriggerInstance::new)
		);

		public static Criterion<RecipeCraftedTrigger.TriggerInstance> craftedItem(ResourceLocation resourceLocation, List<ItemPredicate.Builder> list) {
			return CriteriaTriggers.RECIPE_CRAFTED
				.createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), resourceLocation, list.stream().map(ItemPredicate.Builder::build).toList()));
		}

		public static Criterion<RecipeCraftedTrigger.TriggerInstance> craftedItem(ResourceLocation resourceLocation) {
			return CriteriaTriggers.RECIPE_CRAFTED.createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), resourceLocation, List.of()));
		}

		public static Criterion<RecipeCraftedTrigger.TriggerInstance> crafterCraftedItem(ResourceLocation resourceLocation) {
			return CriteriaTriggers.CRAFTER_RECIPE_CRAFTED.createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), resourceLocation, List.of()));
		}

		boolean matches(ResourceLocation resourceLocation, List<ItemStack> list) {
			if (!resourceLocation.equals(this.recipeId)) {
				return false;
			} else {
				List<ItemStack> list2 = new ArrayList(list);

				for (ItemPredicate itemPredicate : this.ingredients) {
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
	}
}
