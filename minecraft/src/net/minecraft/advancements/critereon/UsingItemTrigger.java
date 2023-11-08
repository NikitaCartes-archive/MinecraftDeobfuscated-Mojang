package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class UsingItemTrigger extends SimpleCriterionTrigger<UsingItemTrigger.TriggerInstance> {
	@Override
	public Codec<UsingItemTrigger.TriggerInstance> codec() {
		return UsingItemTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<UsingItemTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(UsingItemTrigger.TriggerInstance::player),
						ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item").forGetter(UsingItemTrigger.TriggerInstance::item)
					)
					.apply(instance, UsingItemTrigger.TriggerInstance::new)
		);

		public static Criterion<UsingItemTrigger.TriggerInstance> lookingAt(EntityPredicate.Builder builder, ItemPredicate.Builder builder2) {
			return CriteriaTriggers.USING_ITEM
				.createCriterion(new UsingItemTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(builder)), Optional.of(builder2.build())));
		}

		public boolean matches(ItemStack itemStack) {
			return !this.item.isPresent() || ((ItemPredicate)this.item.get()).matches(itemStack);
		}
	}
}
