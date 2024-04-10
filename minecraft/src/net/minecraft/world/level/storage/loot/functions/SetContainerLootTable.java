package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerLootTable extends LootItemConditionalFunction {
	public static final MapCodec<SetContainerLootTable> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<ResourceKey<LootTable>, long, Holder<BlockEntityType<?>>>and(
					instance.group(
						ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("name").forGetter(setContainerLootTable -> setContainerLootTable.name),
						Codec.LONG.optionalFieldOf("seed", Long.valueOf(0L)).forGetter(setContainerLootTable -> setContainerLootTable.seed),
						BuiltInRegistries.BLOCK_ENTITY_TYPE.holderByNameCodec().fieldOf("type").forGetter(setContainerLootTable -> setContainerLootTable.type)
					)
				)
				.apply(instance, SetContainerLootTable::new)
	);
	private final ResourceKey<LootTable> name;
	private final long seed;
	private final Holder<BlockEntityType<?>> type;

	private SetContainerLootTable(List<LootItemCondition> list, ResourceKey<LootTable> resourceKey, long l, Holder<BlockEntityType<?>> holder) {
		super(list);
		this.name = resourceKey;
		this.seed = l;
		this.type = holder;
	}

	@Override
	public LootItemFunctionType<SetContainerLootTable> getType() {
		return LootItemFunctions.SET_LOOT_TABLE;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.isEmpty()) {
			return itemStack;
		} else {
			itemStack.set(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(this.name, this.seed));
			return itemStack;
		}
	}

	@Override
	public void validate(ValidationContext validationContext) {
		super.validate(validationContext);
		if (validationContext.resolver().get(Registries.LOOT_TABLE, this.name).isEmpty()) {
			validationContext.reportProblem("Missing loot table used for container: " + this.name.location());
		}
	}

	public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> blockEntityType, ResourceKey<LootTable> resourceKey) {
		return simpleBuilder(list -> new SetContainerLootTable(list, resourceKey, 0L, blockEntityType.builtInRegistryHolder()));
	}

	public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> blockEntityType, ResourceKey<LootTable> resourceKey, long l) {
		return simpleBuilder(list -> new SetContainerLootTable(list, resourceKey, l, blockEntityType.builtInRegistryHolder()));
	}
}
