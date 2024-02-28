package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetCustomDataFunction extends LootItemConditionalFunction {
	public static final Codec<SetCustomDataFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.and(TagParser.AS_CODEC.fieldOf("tag").forGetter(setCustomDataFunction -> setCustomDataFunction.tag))
				.apply(instance, SetCustomDataFunction::new)
	);
	private final CompoundTag tag;

	private SetCustomDataFunction(List<LootItemCondition> list, CompoundTag compoundTag) {
		super(list);
		this.tag = compoundTag;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_CUSTOM_DATA;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		CustomData.update(DataComponents.CUSTOM_DATA, itemStack, compoundTag -> compoundTag.merge(this.tag));
		return itemStack;
	}

	@Deprecated
	public static LootItemConditionalFunction.Builder<?> setCustomData(CompoundTag compoundTag) {
		return simpleBuilder(list -> new SetCustomDataFunction(list, compoundTag));
	}
}
