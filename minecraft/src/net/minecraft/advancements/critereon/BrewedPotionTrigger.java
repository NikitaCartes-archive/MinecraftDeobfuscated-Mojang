package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.alchemy.Potion;

public class BrewedPotionTrigger extends SimpleCriterionTrigger<BrewedPotionTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("brewed_potion");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public BrewedPotionTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		Potion potion = null;
		if (jsonObject.has("potion")) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "potion"));
			potion = (Potion)BuiltInRegistries.POTION
				.getOptional(resourceLocation)
				.orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + resourceLocation + "'"));
		}

		return new BrewedPotionTrigger.TriggerInstance(contextAwarePredicate, potion);
	}

	public void trigger(ServerPlayer serverPlayer, Potion potion) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(potion));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		@Nullable
		private final Potion potion;

		public TriggerInstance(ContextAwarePredicate contextAwarePredicate, @Nullable Potion potion) {
			super(BrewedPotionTrigger.ID, contextAwarePredicate);
			this.potion = potion;
		}

		public static BrewedPotionTrigger.TriggerInstance brewedPotion() {
			return new BrewedPotionTrigger.TriggerInstance(ContextAwarePredicate.ANY, null);
		}

		public boolean matches(Potion potion) {
			return this.potion == null || this.potion == potion;
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			if (this.potion != null) {
				jsonObject.addProperty("potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
			}

			return jsonObject;
		}
	}
}
