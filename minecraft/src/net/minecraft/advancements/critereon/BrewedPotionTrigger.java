package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.alchemy.Potion;

public class BrewedPotionTrigger extends SimpleCriterionTrigger<BrewedPotionTrigger.TriggerInstance> {
	public BrewedPotionTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Potion potion = null;
		if (jsonObject.has("potion")) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "potion"));
			potion = (Potion)BuiltInRegistries.POTION
				.getOptional(resourceLocation)
				.orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + resourceLocation + "'"));
		}

		return new BrewedPotionTrigger.TriggerInstance(optional, potion);
	}

	public void trigger(ServerPlayer serverPlayer, Potion potion) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(potion));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		@Nullable
		private final Potion potion;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, @Nullable Potion potion) {
			super(optional);
			this.potion = potion;
		}

		public static Criterion<BrewedPotionTrigger.TriggerInstance> brewedPotion() {
			return CriteriaTriggers.BREWED_POTION.createCriterion(new BrewedPotionTrigger.TriggerInstance(Optional.empty(), null));
		}

		public boolean matches(Potion potion) {
			return this.potion == null || this.potion == potion;
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			if (this.potion != null) {
				jsonObject.addProperty("potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
			}

			return jsonObject;
		}
	}
}
