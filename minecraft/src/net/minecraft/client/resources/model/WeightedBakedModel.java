package net.minecraft.client.resources.model;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class WeightedBakedModel extends DelegateBakedModel {
	private final SimpleWeightedRandomList<BakedModel> list;

	public WeightedBakedModel(SimpleWeightedRandomList<BakedModel> simpleWeightedRandomList) {
		super((BakedModel)((WeightedEntry.Wrapper)simpleWeightedRandomList.unwrap().getFirst()).data());
		this.list = simpleWeightedRandomList;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
		return (List<BakedQuad>)this.list
			.getRandomValue(randomSource)
			.map(bakedModel -> bakedModel.getQuads(blockState, direction, randomSource))
			.orElse(Collections.emptyList());
	}
}
