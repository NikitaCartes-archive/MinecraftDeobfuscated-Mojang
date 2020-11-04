package net.minecraft.client.model.geom.builders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CubeListBuilder {
	private final List<CubeDefinition> cubes = Lists.<CubeDefinition>newArrayList();
	private int xTexOffs;
	private int yTexOffs;
	private boolean mirror;

	public CubeListBuilder texOffs(int i, int j) {
		this.xTexOffs = i;
		this.yTexOffs = j;
		return this;
	}

	public CubeListBuilder mirror() {
		return this.mirror(true);
	}

	public CubeListBuilder mirror(boolean bl) {
		this.mirror = bl;
		return this;
	}

	public CubeListBuilder addBox(String string, float f, float g, float h, int i, int j, int k, CubeDeformation cubeDeformation, int l, int m) {
		this.texOffs(l, m);
		this.cubes
			.add(new CubeDefinition(string, (float)this.xTexOffs, (float)this.yTexOffs, f, g, h, (float)i, (float)j, (float)k, cubeDeformation, this.mirror, 1.0F, 1.0F));
		return this;
	}

	public CubeListBuilder addBox(String string, float f, float g, float h, int i, int j, int k, int l, int m) {
		this.texOffs(l, m);
		this.cubes
			.add(
				new CubeDefinition(string, (float)this.xTexOffs, (float)this.yTexOffs, f, g, h, (float)i, (float)j, (float)k, CubeDeformation.NONE, this.mirror, 1.0F, 1.0F)
			);
		return this;
	}

	public CubeListBuilder addBox(float f, float g, float h, float i, float j, float k) {
		this.cubes.add(new CubeDefinition(null, (float)this.xTexOffs, (float)this.yTexOffs, f, g, h, i, j, k, CubeDeformation.NONE, this.mirror, 1.0F, 1.0F));
		return this;
	}

	public CubeListBuilder addBox(String string, float f, float g, float h, float i, float j, float k) {
		this.cubes.add(new CubeDefinition(string, (float)this.xTexOffs, (float)this.yTexOffs, f, g, h, i, j, k, CubeDeformation.NONE, this.mirror, 1.0F, 1.0F));
		return this;
	}

	public CubeListBuilder addBox(String string, float f, float g, float h, float i, float j, float k, CubeDeformation cubeDeformation) {
		this.cubes.add(new CubeDefinition(string, (float)this.xTexOffs, (float)this.yTexOffs, f, g, h, i, j, k, cubeDeformation, this.mirror, 1.0F, 1.0F));
		return this;
	}

	public CubeListBuilder addBox(float f, float g, float h, float i, float j, float k, boolean bl) {
		this.cubes.add(new CubeDefinition(null, (float)this.xTexOffs, (float)this.yTexOffs, f, g, h, i, j, k, CubeDeformation.NONE, bl, 1.0F, 1.0F));
		return this;
	}

	public CubeListBuilder addBox(float f, float g, float h, float i, float j, float k, CubeDeformation cubeDeformation, float l, float m) {
		this.cubes.add(new CubeDefinition(null, (float)this.xTexOffs, (float)this.yTexOffs, f, g, h, i, j, k, cubeDeformation, this.mirror, l, m));
		return this;
	}

	public CubeListBuilder addBox(float f, float g, float h, float i, float j, float k, CubeDeformation cubeDeformation) {
		this.cubes.add(new CubeDefinition(null, (float)this.xTexOffs, (float)this.yTexOffs, f, g, h, i, j, k, cubeDeformation, this.mirror, 1.0F, 1.0F));
		return this;
	}

	public List<CubeDefinition> getCubes() {
		return ImmutableList.copyOf(this.cubes);
	}

	public static CubeListBuilder create() {
		return new CubeListBuilder();
	}
}
