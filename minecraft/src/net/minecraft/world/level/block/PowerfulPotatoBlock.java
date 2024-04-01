package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class PowerfulPotatoBlock extends Block {
	public static final MapCodec<PowerfulPotatoBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					BuiltInRegistries.BLOCK.byNameCodec().fieldOf("roots").forGetter(powerfulPotatoBlock -> powerfulPotatoBlock.plant), propertiesCodec()
				)
				.apply(instance, PowerfulPotatoBlock::new)
	);
	public static final IntegerProperty SPROUTS = BlockStateProperties.AGE_3;
	public static final int MAX_SPROUTS = 3;
	private final Block plant;

	@Override
	public MapCodec<PowerfulPotatoBlock> codec() {
		return CODEC;
	}

	protected PowerfulPotatoBlock(Block block, BlockBehaviour.Properties properties) {
		super(properties);
		this.plant = block;
		this.registerDefaultState(this.stateDefinition.any().setValue(SPROUTS, Integer.valueOf(0)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SPROUTS);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState blockState) {
		return (Integer)blockState.getValue(SPROUTS) < 3;
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		int i = (Integer)blockState.getValue(SPROUTS);
		if (i < 3) {
			StrongRootsBlock.GROWTH_DIRECTION.getRandomValue(randomSource).ifPresent(direction -> {
				List<ItemStack> list = StrongRootsBlock.tryPlace(serverLevel, blockPos.relative(direction), randomSource);
				if (list != null) {
					BlockPos blockPos2 = blockPos.above();
					list.forEach(itemStack -> popResource(serverLevel, blockPos2, itemStack));
					serverLevel.setBlock(blockPos, blockState.setValue(SPROUTS, Integer.valueOf(i + 1)), 4);
				}
			});
		}
	}
}
