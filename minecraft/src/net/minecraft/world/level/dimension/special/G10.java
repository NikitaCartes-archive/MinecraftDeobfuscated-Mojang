package net.minecraft.world.level.dimension.special;

import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;

public class G10 extends NormalDimension {
	public G10(Level level, DimensionType dimensionType) {
		super(level, dimensionType);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void modifyLightmapColor(int i, int j, Vector3f vector3f) {
		vector3f.set(1.0F - vector3f.x(), 1.0F - vector3f.y(), 1.0F - vector3f.z());
	}
}
