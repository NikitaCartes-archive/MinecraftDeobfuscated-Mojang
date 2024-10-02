package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HangingMossBlock;
import net.minecraft.world.level.block.MossyCarpetBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

public class PaleMossDecorator extends TreeDecorator {
	public static final MapCodec<PaleMossDecorator> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.floatRange(0.0F, 1.0F).fieldOf("leaves_probability").forGetter(paleMossDecorator -> paleMossDecorator.leavesProbability),
					Codec.floatRange(0.0F, 1.0F).fieldOf("trunk_probability").forGetter(paleMossDecorator -> paleMossDecorator.trunkProbability),
					Codec.floatRange(0.0F, 1.0F).fieldOf("ground_probability").forGetter(paleMossDecorator -> paleMossDecorator.groundProbability)
				)
				.apply(instance, PaleMossDecorator::new)
	);
	private final float leavesProbability;
	private final float trunkProbability;
	private final float groundProbability;

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.PALE_MOSS;
	}

	public PaleMossDecorator(float f, float g, float h) {
		this.leavesProbability = f;
		this.trunkProbability = g;
		this.groundProbability = h;
	}

	@Override
	public void place(TreeDecorator.Context context) {
		RandomSource randomSource = context.random();
		WorldGenLevel worldGenLevel = (WorldGenLevel)context.level();
		List<BlockPos> list = Util.shuffledCopy(context.logs(), randomSource);
		Mutable<BlockPos> mutable = new MutableObject<>((BlockPos)list.getFirst());
		list.forEach(blockPosx -> {
			if (blockPosx.getY() < mutable.getValue().getY()) {
				mutable.setValue(blockPosx);
			}
		});
		BlockPos blockPos = mutable.getValue();
		if (randomSource.nextFloat() < this.groundProbability) {
			worldGenLevel.registryAccess()
				.lookup(Registries.CONFIGURED_FEATURE)
				.flatMap(registry -> registry.get(VegetationFeatures.PALE_MOSS_PATCH_BONEMEAL))
				.ifPresent(
					reference -> ((ConfiguredFeature)reference.value())
							.place(worldGenLevel, worldGenLevel.getLevel().getChunkSource().getGenerator(), randomSource, blockPos.above())
				);
		}

		context.logs().forEach(blockPosx -> {
			if (randomSource.nextFloat() < this.trunkProbability) {
				BlockPos blockPos2 = blockPosx.below();
				if (context.isAir(blockPos2)) {
					addMossHanger(blockPos2, context);
				}
			}

			if (randomSource.nextFloat() < this.trunkProbability) {
				BlockPos blockPos2 = blockPosx.above();
				if (context.isAir(blockPos2)) {
					MossyCarpetBlock.placeAt((WorldGenLevel)context.level(), blockPos2, context.random(), 3);
				}
			}
		});
		context.leaves().forEach(blockPosx -> {
			if (randomSource.nextFloat() < this.leavesProbability) {
				BlockPos blockPos2 = blockPosx.below();
				if (context.isAir(blockPos2)) {
					addMossHanger(blockPos2, context);
				}
			}
		});
	}

	private static void addMossHanger(BlockPos blockPos, TreeDecorator.Context context) {
		while (context.isAir(blockPos.below()) && !((double)context.random().nextFloat() < 0.5)) {
			context.setBlock(blockPos, Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, Boolean.valueOf(false)));
			blockPos = blockPos.below();
		}

		context.setBlock(blockPos, Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, Boolean.valueOf(true)));
	}
}
