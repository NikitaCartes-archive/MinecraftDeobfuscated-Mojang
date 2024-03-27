package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record ValueCheckCondition(NumberProvider provider, IntRange range) implements LootItemCondition {
	public static final MapCodec<ValueCheckCondition> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					NumberProviders.CODEC.fieldOf("value").forGetter(ValueCheckCondition::provider), IntRange.CODEC.fieldOf("range").forGetter(ValueCheckCondition::range)
				)
				.apply(instance, ValueCheckCondition::new)
	);

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.VALUE_CHECK;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return Sets.<LootContextParam<?>>union(this.provider.getReferencedContextParams(), this.range.getReferencedContextParams());
	}

	public boolean test(LootContext lootContext) {
		return this.range.test(lootContext, this.provider.getInt(lootContext));
	}

	public static LootItemCondition.Builder hasValue(NumberProvider numberProvider, IntRange intRange) {
		return () -> new ValueCheckCondition(numberProvider, intRange);
	}
}
