package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.ArrayUtils;

public class AlternativesEntry extends CompositeEntryBase {
	AlternativesEntry(LootPoolEntryContainer[] lootPoolEntryContainers, LootItemCondition[] lootItemConditions) {
		super(lootPoolEntryContainers, lootItemConditions);
	}

	@Override
	protected ComposableEntryContainer compose(ComposableEntryContainer[] composableEntryContainers) {
		switch (composableEntryContainers.length) {
			case 0:
				return ALWAYS_FALSE;
			case 1:
				return composableEntryContainers[0];
			case 2:
				return composableEntryContainers[0].or(composableEntryContainers[1]);
			default:
				return (lootContext, consumer) -> {
					for (ComposableEntryContainer composableEntryContainer : composableEntryContainers) {
						if (composableEntryContainer.expand(lootContext, consumer)) {
							return true;
						}
					}

					return false;
				};
		}
	}

	@Override
	public void validate(
		LootTableProblemCollector lootTableProblemCollector,
		Function<ResourceLocation, LootTable> function,
		Set<ResourceLocation> set,
		LootContextParamSet lootContextParamSet
	) {
		super.validate(lootTableProblemCollector, function, set, lootContextParamSet);

		for (int i = 0; i < this.children.length - 1; i++) {
			if (ArrayUtils.isEmpty((Object[])this.children[i].conditions)) {
				lootTableProblemCollector.reportProblem("Unreachable entry!");
			}
		}
	}

	public static AlternativesEntry.Builder alternatives(LootPoolEntryContainer.Builder<?>... builders) {
		return new AlternativesEntry.Builder(builders);
	}

	public static class Builder extends LootPoolEntryContainer.Builder<AlternativesEntry.Builder> {
		private final List<LootPoolEntryContainer> entries = Lists.<LootPoolEntryContainer>newArrayList();

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
			return new AlternativesEntry((LootPoolEntryContainer[])this.entries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
		}
	}
}
