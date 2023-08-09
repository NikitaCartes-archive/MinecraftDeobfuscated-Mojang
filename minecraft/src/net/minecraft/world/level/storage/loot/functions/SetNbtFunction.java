package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetNbtFunction extends LootItemConditionalFunction {
	public static final Codec<SetNbtFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.and(TagParser.AS_CODEC.fieldOf("tag").forGetter(setNbtFunction -> setNbtFunction.tag))
				.apply(instance, SetNbtFunction::new)
	);
	private final CompoundTag tag;

	private SetNbtFunction(List<LootItemCondition> list, CompoundTag compoundTag) {
		super(list);
		this.tag = compoundTag;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_NBT;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		itemStack.getOrCreateTag().merge(this.tag);
		return itemStack;
	}

	@Deprecated
	public static LootItemConditionalFunction.Builder<?> setTag(CompoundTag compoundTag) {
		return simpleBuilder(list -> new SetNbtFunction(list, compoundTag));
	}
}
