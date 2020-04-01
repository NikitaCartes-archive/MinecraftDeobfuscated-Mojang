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

public class G08 extends NormalDimension {
	private static final Vector3f GREEN = new Vector3f(s(129.0F), s(185.0F), s(0.0F));
	private static final Vector3f YELLOW = new Vector3f(s(255.0F), s(185.0F), s(2.0F));
	private static final Vector3f BLUE = new Vector3f(s(1.0F), s(164.0F), s(239.0F));
	private static final Vector3f RED = new Vector3f(s(244.0F), s(78.0F), s(36.0F));

	private static float s(float f) {
		return f / 255.0F;
	}

	public G08(Level level, DimensionType dimensionType) {
		super(level, dimensionType);
	}

	@Environment(EnvType.CLIENT)
	private static Vector3f getQuadrantColor(BlockPos blockPos) {
		if (blockPos.getX() < 0) {
			return blockPos.getZ() > 0 ? GREEN : YELLOW;
		} else {
			return blockPos.getZ() > 0 ? RED : BLUE;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vector3f getExtraTint(BlockState blockState, BlockPos blockPos) {
		return getQuadrantColor(blockPos);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public <T extends LivingEntity> Vector3f getEntityExtraTint(T livingEntity) {
		return getQuadrantColor(livingEntity.blockPosition());
	}
}
