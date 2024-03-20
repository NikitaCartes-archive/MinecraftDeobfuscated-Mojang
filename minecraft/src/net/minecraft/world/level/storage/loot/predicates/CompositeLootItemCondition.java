package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public abstract class CompositeLootItemCondition implements LootItemCondition {
	protected final List<LootItemCondition> terms;
	private final Predicate<LootContext> composedPredicate;

	protected CompositeLootItemCondition(List<LootItemCondition> list, Predicate<LootContext> predicate) {
		this.terms = list;
		this.composedPredicate = predicate;
	}

	protected static <T extends CompositeLootItemCondition> Codec<T> createCodec(Function<List<LootItemCondition>, T> function) {
		return RecordCodecBuilder.create(
			instance -> instance.group(
						LootItemConditions.DIRECT_CODEC.listOf().fieldOf("terms").forGetter(compositeLootItemCondition -> compositeLootItemCondition.terms)
					)
					.apply(instance, function)
		);
	}

	protected static <T extends CompositeLootItemCondition> Codec<T> createInlineCodec(Function<List<LootItemCondition>, T> function) {
		return LootItemConditions.DIRECT_CODEC.listOf().xmap(function, compositeLootItemCondition -> compositeLootItemCondition.terms);
	}

	public final boolean test(LootContext lootContext) {
		return this.composedPredicate.test(lootContext);
	}

	@Override
	public void validate(ValidationContext validationContext) {
		LootItemCondition.super.validate(validationContext);

		for (int i = 0; i < this.terms.size(); i++) {
			((LootItemCondition)this.terms.get(i)).validate(validationContext.forChild(".term[" + i + "]"));
		}
	}

	public abstract static class Builder implements LootItemCondition.Builder {
		private final ImmutableList.Builder<LootItemCondition> terms = ImmutableList.builder();

		protected Builder(LootItemCondition.Builder... builders) {
			for (LootItemCondition.Builder builder : builders) {
				this.terms.add(builder.build());
			}
		}

		public void addTerm(LootItemCondition.Builder builder) {
			this.terms.add(builder.build());
		}

		@Override
		public LootItemCondition build() {
			return this.create(this.terms.build());
		}

		protected abstract LootItemCondition create(List<LootItemCondition> list);
	}
}
