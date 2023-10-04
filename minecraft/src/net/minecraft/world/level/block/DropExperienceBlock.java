package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DropExperienceBlock extends Block {
	public static final MapCodec<DropExperienceBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(IntProvider.codec(0, 10).fieldOf("experience").forGetter(dropExperienceBlock -> dropExperienceBlock.xpRange), propertiesCodec())
				.apply(instance, DropExperienceBlock::new)
	);
	private final IntProvider xpRange;

	@Override
	public MapCodec<? extends DropExperienceBlock> codec() {
		return CODEC;
	}

	public DropExperienceBlock(IntProvider intProvider, BlockBehaviour.Properties properties) {
		super(properties);
		this.xpRange = intProvider;
	}

	@Override
	public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
		super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
		if (bl) {
			this.tryDropExperience(serverLevel, blockPos, itemStack, this.xpRange);
		}
	}
}
