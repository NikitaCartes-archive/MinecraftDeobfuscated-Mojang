package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public class LootTableTrigger extends SimpleCriterionTrigger<LootTableTrigger.TriggerInstance> {
	@Override
	public Codec<LootTableTrigger.TriggerInstance> codec() {
		return LootTableTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, ResourceLocation resourceLocation) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(resourceLocation));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceLocation lootTable) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<LootTableTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(LootTableTrigger.TriggerInstance::player),
						ResourceLocation.CODEC.fieldOf("loot_table").forGetter(LootTableTrigger.TriggerInstance::lootTable)
					)
					.apply(instance, LootTableTrigger.TriggerInstance::new)
		);

		public static Criterion<LootTableTrigger.TriggerInstance> lootTableUsed(ResourceLocation resourceLocation) {
			return CriteriaTriggers.GENERATE_LOOT.createCriterion(new LootTableTrigger.TriggerInstance(Optional.empty(), resourceLocation));
		}

		public boolean matches(ResourceLocation resourceLocation) {
			return this.lootTable.equals(resourceLocation);
		}
	}
}
