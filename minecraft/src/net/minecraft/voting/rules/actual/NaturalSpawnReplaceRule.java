package net.minecraft.voting.rules.actual;

import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.MapRule;
import net.minecraft.voting.rules.ResourceKeyReplacementRule;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class NaturalSpawnReplaceRule extends ResourceKeyReplacementRule<EntityType<?>> {
	private static final EnumSet<MobCategory> SENSIBLE_SPAWN_TYPES = EnumSet.of(
		MobCategory.CREATURE, MobCategory.MONSTER, MobCategory.WATER_CREATURE, MobCategory.WATER_AMBIENT
	);

	public NaturalSpawnReplaceRule() {
		super(Registries.ENTITY_TYPE);
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		Registry<EntityType<?>> registry = minecraftServer.registryAccess().registryOrThrow(this.registryName);
		return Stream.generate(() -> registry.getRandom(randomSource))
			.flatMap(Optional::stream)
			.limit(1000L)
			.filter(reference -> SENSIBLE_SPAWN_TYPES.contains(((EntityType)reference.value()).getCategory()))
			.flatMap(
				reference -> {
					ResourceKey<EntityType<?>> resourceKey = reference.key();
					MobCategory mobCategory = ((EntityType)reference.value()).getCategory();
					return Stream.generate(() -> registry.getRandom(randomSource))
						.flatMap(Optional::stream)
						.filter(referencex -> !referencex.is(resourceKey) && ((EntityType)referencex.value()).getCategory() == mobCategory)
						.limit((long)i)
						.map(referencex -> new MapRule.MapRuleChange(resourceKey, referencex.key()));
				}
			)
			.limit((long)i)
			.map(mapRuleChange -> mapRuleChange);
	}

	protected Component description(ResourceKey<EntityType<?>> resourceKey, ResourceKey<EntityType<?>> resourceKey2) {
		Component component = Component.translatable(Util.makeDescriptionId("entity", resourceKey.location()));
		Component component2 = Component.translatable(Util.makeDescriptionId("entity", resourceKey2.location()));
		return Component.translatable("rule.natural_spawn_replace", component, component2);
	}

	@Nullable
	public ResourceKey<EntityType<?>> get(ResourceKey<EntityType<?>> resourceKey) {
		return (ResourceKey<EntityType<?>>)this.entries.get(resourceKey);
	}
}
