package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntryGroup extends CompositeEntryBase {
	public static final MapCodec<EntryGroup> CODEC = createCodec(EntryGroup::new);

	EntryGroup(List<LootPoolEntryContainer> list, List<LootItemCondition> list2) {
		super(list, list2);
	}

	@Override
	public LootPoolEntryType getType() {
		return LootPoolEntries.GROUP;
	}

	@Override
	protected ComposableEntryContainer compose(List<? extends ComposableEntryContainer> list) {
		return switch (list.size()) {
			case 0 -> ALWAYS_TRUE;
			case 1 -> (ComposableEntryContainer)list.get(0);
			case 2 -> {
				ComposableEntryContainer composableEntryContainer = (ComposableEntryContainer)list.get(0);
				ComposableEntryContainer composableEntryContainer2 = (ComposableEntryContainer)list.get(1);
				yield (lootContext, consumer) -> {
					composableEntryContainer.expand(lootContext, consumer);
					composableEntryContainer2.expand(lootContext, consumer);
					return true;
				};
			}
			default -> (lootContext, consumer) -> {
			for (ComposableEntryContainer composableEntryContainerx : list) {
				composableEntryContainerx.expand(lootContext, consumer);
			}

			return true;
		};
		};
	}

	public static EntryGroup.Builder list(LootPoolEntryContainer.Builder<?>... builders) {
		return new EntryGroup.Builder(builders);
	}

	public static class Builder extends LootPoolEntryContainer.Builder<EntryGroup.Builder> {
		private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();

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
			return new EntryGroup(this.entries.build(), this.getConditions());
		}
	}
}
