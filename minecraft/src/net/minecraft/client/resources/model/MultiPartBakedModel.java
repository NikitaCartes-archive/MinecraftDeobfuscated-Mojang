package net.minecraft.client.resources.model;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class MultiPartBakedModel extends DelegateBakedModel {
	private final List<MultiPartBakedModel.Selector> selectors;
	private final Map<BlockState, BitSet> selectorCache = new Reference2ObjectOpenHashMap<>();

	private static BakedModel getFirstModel(List<MultiPartBakedModel.Selector> list) {
		if (list.isEmpty()) {
			throw new IllegalArgumentException("Model must have at least one selector");
		} else {
			return ((MultiPartBakedModel.Selector)list.getFirst()).model();
		}
	}

	public MultiPartBakedModel(List<MultiPartBakedModel.Selector> list) {
		super(getFirstModel(list));
		this.selectors = list;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
		if (blockState == null) {
			return Collections.emptyList();
		} else {
			BitSet bitSet = (BitSet)this.selectorCache.get(blockState);
			if (bitSet == null) {
				bitSet = new BitSet();

				for (int i = 0; i < this.selectors.size(); i++) {
					if (((MultiPartBakedModel.Selector)this.selectors.get(i)).condition.test(blockState)) {
						bitSet.set(i);
					}
				}

				this.selectorCache.put(blockState, bitSet);
			}

			List<BakedQuad> list = new ArrayList();
			long l = randomSource.nextLong();

			for (int j = 0; j < bitSet.length(); j++) {
				if (bitSet.get(j)) {
					randomSource.setSeed(l);
					list.addAll(((MultiPartBakedModel.Selector)this.selectors.get(j)).model.getQuads(blockState, direction, randomSource));
				}
			}

			return list;
		}
	}

	@Environment(EnvType.CLIENT)
	public static record Selector(Predicate<BlockState> condition, BakedModel model) {
	}
}
