package net.minecraft.world.level.dimension.special;

import com.mojang.math.Vector3f;
import java.util.function.BiFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.phys.Vec3;

public class G14 extends NormalDimension {
	private final Vector3f selector;
	private final Vec3 fog;

	public G14(Level level, DimensionType dimensionType, Vector3f vector3f) {
		super(level, dimensionType);
		this.selector = vector3f;
		this.fog = new Vec3(vector3f);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vector3f getExtraTint(BlockState blockState, BlockPos blockPos) {
		return this.selector;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public <T extends LivingEntity> Vector3f getEntityExtraTint(T livingEntity) {
		return this.selector;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
		return this.fog;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void modifyLightmapColor(int i, int j, Vector3f vector3f) {
		vector3f.mul(this.selector);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vector3f getSunTint() {
		return this.selector;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vector3f getMoonTint() {
		return this.selector;
	}

	public static BiFunction<Level, DimensionType, ? extends Dimension> create(Vector3f vector3f) {
		return (level, dimensionType) -> new G14(level, dimensionType, vector3f);
	}
}
