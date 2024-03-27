package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AlternativesEntry extends CompositeEntryBase {
	public static final MapCodec<AlternativesEntry> CODEC = createCodec(AlternativesEntry::new);

	AlternativesEntry(List<LootPoolEntryContainer> list, List<LootItemCondition> list2) {
		super(list, list2);
	}

	@Override
	public LootPoolEntryType getType() {
		return LootPoolEntries.ALTERNATIVES;
	}

	@Override
	protected ComposableEntryContainer compose(List<? extends ComposableEntryContainer> list) {
		return switch (list.size()) {
			case 0 -> ALWAYS_FALSE;
			case 1 -> (ComposableEntryContainer)list.get(0);
			case 2 -> ((ComposableEntryContainer)list.get(0)).or((ComposableEntryContainer)list.get(1));
			default -> (lootContext, consumer) -> {
			for (ComposableEntryContainer composableEntryContainer : list) {
				if (composableEntryContainer.expand(lootContext, consumer)) {
					return true;
				}
			}

			return false;
		};
		};
	}

	@Override
	public void validate(ValidationContext validationContext) {
		super.validate(validationContext);

		for (int i = 0; i < this.children.size() - 1; i++) {
			if (((LootPoolEntryContainer)this.children.get(i)).conditions.isEmpty()) {
				validationContext.reportProblem("Unreachable entry!");
			}
		}
	}

	public static AlternativesEntry.Builder alternatives(LootPoolEntryContainer.Builder<?>... builders) {
		return new AlternativesEntry.Builder(builders);
	}

	public static <E> AlternativesEntry.Builder alternatives(Collection<E> collection, Function<E, LootPoolEntryContainer.Builder<?>> function) {
		return new AlternativesEntry.Builder(
			(LootPoolEntryContainer.Builder<?>[])collection.stream().map(function::apply).toArray(LootPoolEntryContainer.Builder[]::new)
		);
	}

	public static class Builder extends LootPoolEntryContainer.Builder<AlternativesEntry.Builder> {
		private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();

		public Builder(LootPoolEntryContainer.Builder<?>... builders) {
			for (LootPoolEntryContainer.Builder<?> builder : builders) {
				this.entries.add(builder.build());
			}
		}

		protected AlternativesEntry.Builder getThis() {
			return this;
		}

		@Override
		public AlternativesEntry.Builder otherwise(LootPoolEntryContainer.Builder<?> builder) {
			this.entries.add(builder.build());
			return this;
		}

		@Override
		public LootPoolEntryContainer build() {
			return new AlternativesEntry(this.entries.build(), this.getConditions());
		}
	}
}
