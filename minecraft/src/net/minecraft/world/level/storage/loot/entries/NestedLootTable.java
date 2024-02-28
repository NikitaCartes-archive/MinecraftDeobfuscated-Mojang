package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class NestedLootTable extends LootPoolSingletonContainer {
	public static final Codec<NestedLootTable> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(Codec.either(ResourceLocation.CODEC, LootTable.CODEC).fieldOf("value").forGetter(nestedLootTable -> nestedLootTable.contents))
				.<int, int, List<LootItemCondition>, List<LootItemFunction>>and(singletonFields(instance))
				.apply(instance, NestedLootTable::new)
	);
	private final Either<ResourceLocation, LootTable> contents;

	private NestedLootTable(Either<ResourceLocation, LootTable> either, int i, int j, List<LootItemCondition> list, List<LootItemFunction> list2) {
		super(i, j, list, list2);
		this.contents = either;
	}

	@Override
	public LootPoolEntryType getType() {
		return LootPoolEntries.LOOT_TABLE;
	}

	@Override
	public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
		this.contents
			.<LootTable>map(resourceLocation -> lootContext.getResolver().getLootTable(resourceLocation), lootTable -> lootTable)
			.getRandomItemsRaw(lootContext, consumer);
	}

	@Override
	public void validate(ValidationContext validationContext) {
		Optional<ResourceLocation> optional = this.contents.left();
		if (optional.isPresent()) {
			LootDataId<LootTable> lootDataId = new LootDataId<>(LootDataType.TABLE, (ResourceLocation)optional.get());
			if (validationContext.hasVisitedElement(lootDataId)) {
				validationContext.reportProblem("Table " + optional.get() + " is recursively called");
				return;
			}
		}

		super.validate(validationContext);
		this.contents
			.ifLeft(
				resourceLocation -> {
					LootDataId<LootTable> lootDataIdx = new LootDataId<>(LootDataType.TABLE, resourceLocation);
					validationContext.resolver()
						.getElementOptional(lootDataIdx)
						.ifPresentOrElse(
							lootTable -> lootTable.validate(validationContext.enterElement("->{" + resourceLocation + "}", lootDataIdx)),
							() -> validationContext.reportProblem("Unknown loot table called " + resourceLocation)
						);
				}
			)
			.ifRight(lootTable -> lootTable.validate(validationContext.forChild("->{inline}")));
	}

	public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceLocation resourceLocation) {
		return simpleBuilder((i, j, list, list2) -> new NestedLootTable(Either.left(resourceLocation), i, j, list, list2));
	}

	public static LootPoolSingletonContainer.Builder<?> inlineLootTable(LootTable lootTable) {
		return simpleBuilder((i, j, list, list2) -> new NestedLootTable(Either.right(lootTable), i, j, list, list2));
	}
}
