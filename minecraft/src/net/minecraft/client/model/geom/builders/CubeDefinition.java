package net.minecraft.client.model.geom.builders;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public final class CubeDefinition {
	@Nullable
	private final String comment;
	private final Vector3f origin;
	private final Vector3f dimensions;
	private final CubeDeformation grow;
	private final boolean mirror;
	private final UVPair texCoord;
	private final UVPair texScale;

	protected CubeDefinition(
		@Nullable String string,
		float f,
		float g,
		float h,
		float i,
		float j,
		float k,
		float l,
		float m,
		CubeDeformation cubeDeformation,
		boolean bl,
		float n,
		float o
	) {
		this.comment = string;
		this.texCoord = new UVPair(f, g);
		this.origin = new Vector3f(h, i, j);
		this.dimensions = new Vector3f(k, l, m);
		this.grow = cubeDeformation;
		this.mirror = bl;
		this.texScale = new UVPair(n, o);
	}

	public ModelPart.Cube bake(int i, int j) {
		return new ModelPart.Cube(
			(int)this.texCoord.u(),
			(int)this.texCoord.v(),
			this.origin.x(),
			this.origin.y(),
			this.origin.z(),
			this.dimensions.x(),
			this.dimensions.y(),
			this.dimensions.z(),
			this.grow.growX,
			this.grow.growY,
			this.grow.growZ,
			this.mirror,
			(float)i * this.texScale.u(),
			(float)j * this.texScale.v()
		);
	}
}
