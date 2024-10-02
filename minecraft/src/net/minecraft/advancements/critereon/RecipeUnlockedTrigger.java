package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeUnlockedTrigger extends SimpleCriterionTrigger<RecipeUnlockedTrigger.TriggerInstance> {
	@Override
	public Codec<RecipeUnlockedTrigger.TriggerInstance> codec() {
		return RecipeUnlockedTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, RecipeHolder<?> recipeHolder) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(recipeHolder));
	}

	public static Criterion<RecipeUnlockedTrigger.TriggerInstance> unlocked(ResourceKey<Recipe<?>> resourceKey) {
		return CriteriaTriggers.RECIPE_UNLOCKED.createCriterion(new RecipeUnlockedTrigger.TriggerInstance(Optional.empty(), resourceKey));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Recipe<?>> recipe) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<RecipeUnlockedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(RecipeUnlockedTrigger.TriggerInstance::player),
						ResourceKey.codec(Registries.RECIPE).fieldOf("recipe").forGetter(RecipeUnlockedTrigger.TriggerInstance::recipe)
					)
					.apply(instance, RecipeUnlockedTrigger.TriggerInstance::new)
		);

		public boolean matches(RecipeHolder<?> recipeHolder) {
			return this.recipe == recipeHolder.id();
		}
	}
}
