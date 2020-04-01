package net.minecraft.world.level.dimension.special;

import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;

public class G09 extends NormalDimension {
	private static final Vector3f WHITE = new Vector3f(1.0F, 1.0F, 1.0F);
	private static final Vector3f BLACK = new Vector3f(0.0F, 0.0F, 0.0F);

	public G09(Level level, DimensionType dimensionType) {
		super(level, dimensionType);
	}

	@Environment(EnvType.CLIENT)
	private static Vector3f getColor(BlockPos blockPos) {
		return ((blockPos.getX() ^ blockPos.getY() ^ blockPos.getZ()) & 1) == 0 ? WHITE : BLACK;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vector3f getExtraTint(BlockState blockState, BlockPos blockPos) {
		return getColor(blockPos);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public <T extends LivingEntity> Vector3f getEntityExtraTint(T livingEntity) {
		return getColor(livingEntity.blockPosition());
	}
}
