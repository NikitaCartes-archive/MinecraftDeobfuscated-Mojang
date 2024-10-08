package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record MatchTool(Optional<ItemPredicate> predicate) implements LootItemCondition {
	public static final MapCodec<MatchTool> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(ItemPredicate.CODEC.optionalFieldOf("predicate").forGetter(MatchTool::predicate)).apply(instance, MatchTool::new)
	);

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.MATCH_TOOL;
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return Set.of(LootContextParams.TOOL);
	}

	public boolean test(LootContext lootContext) {
		ItemStack itemStack = lootContext.getOptionalParameter(LootContextParams.TOOL);
		return itemStack != null && (this.predicate.isEmpty() || ((ItemPredicate)this.predicate.get()).test(itemStack));
	}

	public static LootItemCondition.Builder toolMatches(ItemPredicate.Builder builder) {
		return () -> new MatchTool(Optional.of(builder.build()));
	}
}
