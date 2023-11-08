package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ShotCrossbowTrigger extends SimpleCriterionTrigger<ShotCrossbowTrigger.TriggerInstance> {
	@Override
	public Codec<ShotCrossbowTrigger.TriggerInstance> codec() {
		return ShotCrossbowTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<ShotCrossbowTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(ShotCrossbowTrigger.TriggerInstance::player),
						ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item").forGetter(ShotCrossbowTrigger.TriggerInstance::item)
					)
					.apply(instance, ShotCrossbowTrigger.TriggerInstance::new)
		);

		public static Criterion<ShotCrossbowTrigger.TriggerInstance> shotCrossbow(Optional<ItemPredicate> optional) {
			return CriteriaTriggers.SHOT_CROSSBOW.createCriterion(new ShotCrossbowTrigger.TriggerInstance(Optional.empty(), optional));
		}

		public static Criterion<ShotCrossbowTrigger.TriggerInstance> shotCrossbow(ItemLike itemLike) {
			return CriteriaTriggers.SHOT_CROSSBOW
				.createCriterion(new ShotCrossbowTrigger.TriggerInstance(Optional.empty(), Optional.of(ItemPredicate.Builder.item().of(itemLike).build())));
		}

		public boolean matches(ItemStack itemStack) {
			return this.item.isEmpty() || ((ItemPredicate)this.item.get()).matches(itemStack);
		}
	}
}
