package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerLootTable extends LootItemConditionalFunction {
	public static final Codec<SetContainerLootTable> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.<ResourceLocation, long, Holder<BlockEntityType<?>>>and(
					instance.group(
						ResourceLocation.CODEC.fieldOf("name").forGetter(setContainerLootTable -> setContainerLootTable.name),
						ExtraCodecs.strictOptionalField(Codec.LONG, "seed", 0L).forGetter(setContainerLootTable -> setContainerLootTable.seed),
						BuiltInRegistries.BLOCK_ENTITY_TYPE.holderByNameCodec().fieldOf("type").forGetter(setContainerLootTable -> setContainerLootTable.type)
					)
				)
				.apply(instance, SetContainerLootTable::new)
	);
	private final ResourceLocation name;
	private final long seed;
	private final Holder<BlockEntityType<?>> type;

	private SetContainerLootTable(List<LootItemCondition> list, ResourceLocation resourceLocation, long l, Holder<BlockEntityType<?>> holder) {
		super(list);
		this.name = resourceLocation;
		this.seed = l;
		this.type = holder;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_LOOT_TABLE;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.isEmpty()) {
			return itemStack;
		} else {
			CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
			if (compoundTag == null) {
				compoundTag = new CompoundTag();
			}

			compoundTag.putString("LootTable", this.name.toString());
			if (this.seed != 0L) {
				compoundTag.putLong("LootTableSeed", this.seed);
			}

			BlockItem.setBlockEntityData(itemStack, this.type.value(), compoundTag);
			return itemStack;
		}
	}

	@Override
	public void validate(ValidationContext validationContext) {
		super.validate(validationContext);
		LootDataId<LootTable> lootDataId = new LootDataId<>(LootDataType.TABLE, this.name);
		if (validationContext.resolver().getElementOptional(lootDataId).isEmpty()) {
			validationContext.reportProblem("Missing loot table used for container: " + this.name);
		}
	}

	public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> blockEntityType, ResourceLocation resourceLocation) {
		return simpleBuilder(list -> new SetContainerLootTable(list, resourceLocation, 0L, blockEntityType.builtInRegistryHolder()));
	}

	public static LootItemConditionalFunction.Builder<?> withLootTable(BlockEntityType<?> blockEntityType, ResourceLocation resourceLocation, long l) {
		return simpleBuilder(list -> new SetContainerLootTable(list, resourceLocation, l, blockEntityType.builtInRegistryHolder()));
	}
}
