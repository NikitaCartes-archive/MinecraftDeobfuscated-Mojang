package net.minecraft.world.level.block.state.predicate;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class BlockMaterialPredicate implements Predicate<BlockState> {
	private static final BlockMaterialPredicate AIR = new BlockMaterialPredicate(Material.AIR) {
		@Override
		public boolean test(@Nullable BlockState blockState) {
			return blockState != null && blockState.isAir();
		}
	};
	private final Material material;

	BlockMaterialPredicate(Material material) {
		this.material = material;
	}

	public static BlockMaterialPredicate forMaterial(Material material) {
		return material == Material.AIR ? AIR : new BlockMaterialPredicate(material);
	}

	public boolean test(@Nullable BlockState blockState) {
		return blockState != null && blockState.getMaterial() == this.material;
	}
}
