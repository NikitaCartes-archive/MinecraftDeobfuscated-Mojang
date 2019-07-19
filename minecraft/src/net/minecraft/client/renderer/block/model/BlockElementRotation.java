package net.minecraft.client.renderer.block.model;

import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;

@Environment(EnvType.CLIENT)
public class BlockElementRotation {
	public final Vector3f origin;
	public final Direction.Axis axis;
	public final float angle;
	public final boolean rescale;

	public BlockElementRotation(Vector3f vector3f, Direction.Axis axis, float f, boolean bl) {
		this.origin = vector3f;
		this.axis = axis;
		this.angle = f;
		this.rescale = bl;
	}
}
