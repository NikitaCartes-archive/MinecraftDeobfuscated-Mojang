package net.minecraft.world.level.storage.loot.entries;

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
}
