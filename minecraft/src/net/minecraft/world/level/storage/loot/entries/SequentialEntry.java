package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SequentialEntry extends CompositeEntryBase {
	SequentialEntry(LootPoolEntryContainer[] lootPoolEntryContainers, LootItemCondition[] lootItemConditions) {
		super(lootPoolEntryContainers, lootItemConditions);
	}

	@Override
	public LootPoolEntryType getType() {
		return LootPoolEntries.SEQUENCE;
	}

	@Override
	protected ComposableEntryContainer compose(ComposableEntryContainer[] composableEntryContainers) {
		switch (composableEntryContainers.length) {
			case 0:
				return ALWAYS_TRUE;
			case 1:
				return composableEntryContainers[0];
			case 2:
				return composableEntryContainers[0].and(composableEntryContainers[1]);
			default:
				return (lootContext, consumer) -> {
					for (ComposableEntryContainer composableEntryContainer : composableEntryContainers) {
						if (!composableEntryContainer.expand(lootContext, consumer)) {
							return false;
						}
					}

					return true;
				};
		}
	}

	public static SequentialEntry.Builder sequential(LootPoolEntryContainer.Builder<?>... builders) {
		return new SequentialEntry.Builder(builders);
	}

	public static class Builder extends LootPoolEntryContainer.Builder<SequentialEntry.Builder> {
		private final List<LootPoolEntryContainer> entries = Lists.<LootPoolEntryContainer>newArrayList();

		public Builder(LootPoolEntryContainer.Builder<?>... builders) {
			for (LootPoolEntryContainer.Builder<?> builder : builders) {
				this.entries.add(builder.build());
			}
		}

		protected SequentialEntry.Builder getThis() {
			return this;
		}

		@Override
		public SequentialEntry.Builder then(LootPoolEntryContainer.Builder<?> builder) {
			this.entries.add(builder.build());
			return this;
		}

		@Override
		public LootPoolEntryContainer build() {
			return new SequentialEntry((LootPoolEntryContainer[])this.entries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
		}
	}
}
