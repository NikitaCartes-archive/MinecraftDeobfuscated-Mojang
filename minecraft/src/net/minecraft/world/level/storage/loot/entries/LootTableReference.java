package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
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

public class LootTableReference extends LootPoolSingletonContainer {
	public static final Codec<LootTableReference> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(ResourceLocation.CODEC.fieldOf("name").forGetter(lootTableReference -> lootTableReference.name))
				.<int, int, List<LootItemCondition>, List<LootItemFunction>>and(singletonFields(instance))
				.apply(instance, LootTableReference::new)
	);
	private final ResourceLocation name;

	private LootTableReference(ResourceLocation resourceLocation, int i, int j, List<LootItemCondition> list, List<LootItemFunction> list2) {
		super(i, j, list, list2);
		this.name = resourceLocation;
	}

	@Override
	public LootPoolEntryType getType() {
		return LootPoolEntries.REFERENCE;
	}

	@Override
	public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
		LootTable lootTable = lootContext.getResolver().getLootTable(this.name);
		lootTable.getRandomItemsRaw(lootContext, consumer);
	}

	@Override
	public void validate(ValidationContext validationContext) {
		LootDataId<LootTable> lootDataId = new LootDataId<>(LootDataType.TABLE, this.name);
		if (validationContext.hasVisitedElement(lootDataId)) {
			validationContext.reportProblem("Table " + this.name + " is recursively called");
		} else {
			super.validate(validationContext);
			validationContext.resolver()
				.getElementOptional(lootDataId)
				.ifPresentOrElse(
					lootTable -> lootTable.validate(validationContext.enterElement("->{" + this.name + "}", lootDataId)),
					() -> validationContext.reportProblem("Unknown loot table called " + this.name)
				);
		}
	}

	public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceLocation resourceLocation) {
		return simpleBuilder((i, j, list, list2) -> new LootTableReference(resourceLocation, i, j, list, list2));
	}
}
