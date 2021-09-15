package net.minecraft.world.level.levelgen.material;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseChunk;

public class MaterialRuleList implements WorldGenMaterialRule {
	private final List<WorldGenMaterialRule> materialRuleList;

	public MaterialRuleList(List<WorldGenMaterialRule> list) {
		this.materialRuleList = list;
	}

	@Nullable
	@Override
	public BlockState apply(NoiseChunk noiseChunk, int i, int j, int k) {
		for (WorldGenMaterialRule worldGenMaterialRule : this.materialRuleList) {
			BlockState blockState = worldGenMaterialRule.apply(noiseChunk, i, j, k);
			if (blockState != null) {
				return blockState;
			}
		}

		return null;
	}
}
