package net.minecraft.world.item.trading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record ItemCost(Holder<Item> item, int count, DataComponentPredicate components, ItemStack itemStack) {
	public static final Codec<ItemCost> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemCost::item),
					ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(ItemCost::count),
					DataComponentPredicate.CODEC.optionalFieldOf("components", DataComponentPredicate.EMPTY).forGetter(ItemCost::components)
				)
				.apply(instance, ItemCost::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemCost> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.holderRegistry(Registries.ITEM),
		ItemCost::item,
		ByteBufCodecs.VAR_INT,
		ItemCost::count,
		DataComponentPredicate.STREAM_CODEC,
		ItemCost::components,
		ItemCost::new
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ItemCost>> OPTIONAL_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs::optional);

	public ItemCost(ItemLike itemLike) {
		this(itemLike, 1);
	}

	public ItemCost(ItemLike itemLike, int i) {
		this(itemLike.asItem().builtInRegistryHolder(), i, DataComponentPredicate.EMPTY);
	}

	public ItemCost(Holder<Item> holder, int i, DataComponentPredicate dataComponentPredicate) {
		this(holder, i, dataComponentPredicate, createStack(holder, i, dataComponentPredicate));
	}

	public ItemCost withComponents(UnaryOperator<DataComponentPredicate.Builder> unaryOperator) {
		return new ItemCost(this.item, this.count, ((DataComponentPredicate.Builder)unaryOperator.apply(DataComponentPredicate.builder())).build());
	}

	private static ItemStack createStack(Holder<Item> holder, int i, DataComponentPredicate dataComponentPredicate) {
		return new ItemStack(holder, i, dataComponentPredicate.asPatch());
	}

	public boolean test(ItemStack itemStack) {
		return itemStack.is(this.item) && this.components.test((DataComponentHolder)itemStack);
	}
}
