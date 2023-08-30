package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record MatchTool(Optional<ItemPredicate> predicate) implements LootItemCondition {
	public static final Codec<MatchTool> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "predicate").forGetter(MatchTool::predicate)).apply(instance, MatchTool::new)
	);

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.MATCH_TOOL;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.TOOL);
	}

	public boolean test(LootContext lootContext) {
		ItemStack itemStack = lootContext.getParamOrNull(LootContextParams.TOOL);
		return itemStack != null && (this.predicate.isEmpty() || ((ItemPredicate)this.predicate.get()).matches(itemStack));
	}

	public static LootItemCondition.Builder toolMatches(ItemPredicate.Builder builder) {
		return () -> new MatchTool(Optional.of(builder.build()));
	}
}
