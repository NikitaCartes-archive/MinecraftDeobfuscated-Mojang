package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.LubricationComponent;

public class ThrowLubricatedTrigger extends SimpleCriterionTrigger<ThrowLubricatedTrigger.TriggerInstance> {
	@Override
	public Codec<ThrowLubricatedTrigger.TriggerInstance> codec() {
		return ThrowLubricatedTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> itemPredicate, int minLubrication)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<ThrowLubricatedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(ThrowLubricatedTrigger.TriggerInstance::player),
						ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item_predicate").forGetter(ThrowLubricatedTrigger.TriggerInstance::itemPredicate),
						Codec.INT.fieldOf("min_lubrication").forGetter(ThrowLubricatedTrigger.TriggerInstance::minLubrication)
					)
					.apply(instance, ThrowLubricatedTrigger.TriggerInstance::new)
		);

		public static Criterion<ThrowLubricatedTrigger.TriggerInstance> thrownWithAtLeast(int i) {
			return CriteriaTriggers.THROW_LUBRICATED.createCriterion(new ThrowLubricatedTrigger.TriggerInstance(Optional.empty(), Optional.empty(), i));
		}

		public static Criterion<ThrowLubricatedTrigger.TriggerInstance> thrownWithAtLeast(ItemPredicate itemPredicate, int i) {
			return CriteriaTriggers.THROW_LUBRICATED.createCriterion(new ThrowLubricatedTrigger.TriggerInstance(Optional.empty(), Optional.of(itemPredicate), i));
		}

		public boolean matches(ItemStack itemStack) {
			if (this.itemPredicate.isPresent() && !((ItemPredicate)this.itemPredicate.get()).matches(itemStack)) {
				return false;
			} else {
				LubricationComponent lubricationComponent = itemStack.get(DataComponents.LUBRICATION);
				return lubricationComponent == null ? false : lubricationComponent.getLevel() >= this.minLubrication;
			}
		}
	}
}
