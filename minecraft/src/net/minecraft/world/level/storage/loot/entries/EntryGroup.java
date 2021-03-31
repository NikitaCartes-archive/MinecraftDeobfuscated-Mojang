package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntryGroup extends CompositeEntryBase {
	EntryGroup(LootPoolEntryContainer[] lootPoolEntryContainers, LootItemCondition[] lootItemConditions) {
		super(lootPoolEntryContainers, lootItemConditions);
	}

	@Override
	public LootPoolEntryType getType() {
		return LootPoolEntries.GROUP;
	}

	@Override
	protected ComposableEntryContainer compose(ComposableEntryContainer[] composableEntryContainers) {
		switch (composableEntryContainers.length) {
			case 0:
				return ALWAYS_TRUE;
			case 1:
				return composableEntryContainers[0];
			case 2:
				ComposableEntryContainer composableEntryContainer = composableEntryContainers[0];
				ComposableEntryContainer composableEntryContainer2 = composableEntryContainers[1];
				return (lootContext, consumer) -> {
					composableEntryContainer.expand(lootContext, consumer);
					composableEntryContainer2.expand(lootContext, consumer);
					return true;
				};
			default:
				return (lootContext, consumer) -> {
					for (ComposableEntryContainer composableEntryContainerx : composableEntryContainers) {
						composableEntryContainerx.expand(lootContext, consumer);
					}

					return true;
				};
		}
	}

	public static EntryGroup.Builder list(LootPoolEntryContainer.Builder<?>... builders) {
		return new EntryGroup.Builder(builders);
	}

	public static class Builder extends LootPoolEntryContainer.Builder<EntryGroup.Builder> {
		private final List<LootPoolEntryContainer> entries = Lists.<LootPoolEntryContainer>newArrayList();

		public Builder(LootPoolEntryContainer.Builder<?>... builders) {
			for (LootPoolEntryContainer.Builder<?> builder : builders) {
				this.entries.add(builder.build());
			}
		}

		protected EntryGroup.Builder getThis() {
			return this;
		}

		@Override
		public EntryGroup.Builder append(LootPoolEntryContainer.Builder<?> builder) {
			this.entries.add(builder.build());
			return this;
		}

		@Override
		public LootPoolEntryContainer build() {
			return new EntryGroup((LootPoolEntryContainer[])this.entries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
		}
	}
}
