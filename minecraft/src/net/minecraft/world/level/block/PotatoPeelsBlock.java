package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PotatoPeelsBlock extends Block {
	public static final MapCodec<PotatoPeelsBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(propertiesCodec(), DyeColor.CODEC.fieldOf("color").forGetter(potatoPeelsBlock -> potatoPeelsBlock.color))
				.apply(instance, PotatoPeelsBlock::new)
	);
	private final DyeColor color;

	public PotatoPeelsBlock(BlockBehaviour.Properties properties, DyeColor dyeColor) {
		super(properties);
		this.color = dyeColor;
	}

	@Override
	protected MapCodec<PotatoPeelsBlock> codec() {
		return CODEC;
	}

	public DyeColor getColor() {
		return this.color;
	}

	public Item getPeelsItem() {
		return Items.POTATO_PEELS_MAP.get(this.getColor());
	}
}
