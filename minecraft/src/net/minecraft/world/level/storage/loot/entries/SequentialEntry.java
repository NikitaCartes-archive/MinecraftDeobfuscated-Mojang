package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SequentialEntry extends CompositeEntryBase {
	public static final Codec<SequentialEntry> CODEC = createCodec(SequentialEntry::new);

	SequentialEntry(List<LootPoolEntryContainer> list, List<LootItemCondition> list2) {
		super(list, list2);
	}

	@Override
	public LootPoolEntryType getType() {
		return LootPoolEntries.SEQUENCE;
	}

	@Override
	protected ComposableEntryContainer compose(List<? extends ComposableEntryContainer> list) {
		return switch (list.size()) {
			case 0 -> ALWAYS_TRUE;
			case 1 -> (ComposableEntryContainer)list.get(0);
			case 2 -> ((ComposableEntryContainer)list.get(0)).and((ComposableEntryContainer)list.get(1));
			default -> (lootContext, consumer) -> {
			for (ComposableEntryContainer composableEntryContainer : list) {
				if (!composableEntryContainer.expand(lootContext, consumer)) {
					return false;
				}
			}

			return true;
		};
		};
	}

	public static SequentialEntry.Builder sequential(LootPoolEntryContainer.Builder<?>... builders) {
		return new SequentialEntry.Builder(builders);
	}

	public static class Builder extends LootPoolEntryContainer.Builder<SequentialEntry.Builder> {
		private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();

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
			return new SequentialEntry(this.entries.build(), this.getConditions());
		}
	}
}
