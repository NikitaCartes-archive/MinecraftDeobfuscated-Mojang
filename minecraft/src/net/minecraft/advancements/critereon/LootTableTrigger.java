package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootTable;

public class LootTableTrigger extends SimpleCriterionTrigger<LootTableTrigger.TriggerInstance> {
	@Override
	public Codec<LootTableTrigger.TriggerInstance> codec() {
		return LootTableTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, ResourceKey<LootTable> resourceKey) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(resourceKey));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<LootTable> lootTable)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<LootTableTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(LootTableTrigger.TriggerInstance::player),
						ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter(LootTableTrigger.TriggerInstance::lootTable)
					)
					.apply(instance, LootTableTrigger.TriggerInstance::new)
		);

		public static Criterion<LootTableTrigger.TriggerInstance> lootTableUsed(ResourceKey<LootTable> resourceKey) {
			return CriteriaTriggers.GENERATE_LOOT.createCriterion(new LootTableTrigger.TriggerInstance(Optional.empty(), resourceKey));
		}

		public boolean matches(ResourceKey<LootTable> resourceKey) {
			return this.lootTable == resourceKey;
		}
	}
}
