package net.minecraft.client.model.geom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record PartPose(float x, float y, float z, float xRot, float yRot, float zRot, float xScale, float yScale, float zScale) {
	public static final PartPose ZERO = offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

	public static PartPose offset(float f, float g, float h) {
		return offsetAndRotation(f, g, h, 0.0F, 0.0F, 0.0F);
	}

	public static PartPose rotation(float f, float g, float h) {
		return offsetAndRotation(0.0F, 0.0F, 0.0F, f, g, h);
	}

	public static PartPose offsetAndRotation(float f, float g, float h, float i, float j, float k) {
		return new PartPose(f, g, h, i, j, k, 1.0F, 1.0F, 1.0F);
	}

	public PartPose translated(float f, float g, float h) {
		return new PartPose(this.x + f, this.y + g, this.z + h, this.xRot, this.yRot, this.zRot, this.xScale, this.yScale, this.zScale);
	}

	public PartPose withScale(float f) {
		return new PartPose(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot, f, f, f);
	}

	public PartPose scaled(float f) {
		return f == 1.0F ? this : this.scaled(f, f, f);
	}

	public PartPose scaled(float f, float g, float h) {
		return new PartPose(this.x * f, this.y * g, this.z * h, this.xRot, this.yRot, this.zRot, this.xScale * f, this.yScale * g, this.zScale * h);
	}
}
