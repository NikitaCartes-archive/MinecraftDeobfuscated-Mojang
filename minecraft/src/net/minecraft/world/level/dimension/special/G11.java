package net.minecraft.world.level.dimension.special;

import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.phys.Vec3;

public class G11 extends NormalDimension {
	public G11(Level level, DimensionType dimensionType) {
		super(level, dimensionType);
	}

	@Environment(EnvType.CLIENT)
	private static Vector3f getColor(BlockPos blockPos) {
		double d = (double)blockPos.getX();
		double e = (double)blockPos.getZ();
		double f = Math.sqrt(d * d + e * e);
		float g = (float)Mth.clamp(1.0 - Mth.pct(f, 50.0, 100.0), 0.0, 1.0);
		return new Vector3f(g, g, g);
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

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
		return Vec3.ZERO;
	}
}
