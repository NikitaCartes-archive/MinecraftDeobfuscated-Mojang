package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class PufferfishBigModel<T extends Entity> extends ListModel<T> {
	private final ModelPart cube;
	private final ModelPart blueFin0;
	private final ModelPart blueFin1;
	private final ModelPart topFrontFin;
	private final ModelPart topMidFin;
	private final ModelPart topBackFin;
	private final ModelPart sideFrontFin0;
	private final ModelPart sideFrontFin1;
	private final ModelPart bottomFrontFin;
	private final ModelPart bottomBackFin;
	private final ModelPart bottomMidFin;
	private final ModelPart sideBackFin0;
	private final ModelPart sideBackFin1;

	public PufferfishBigModel() {
		this.texWidth = 32;
		this.texHeight = 32;
		int i = 22;
		this.cube = new ModelPart(this, 0, 0);
		this.cube.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F);
		this.cube.setPos(0.0F, 22.0F, 0.0F);
		this.blueFin0 = new ModelPart(this, 24, 0);
		this.blueFin0.addBox(-2.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F);
		this.blueFin0.setPos(-4.0F, 15.0F, -2.0F);
		this.blueFin1 = new ModelPart(this, 24, 3);
		this.blueFin1.addBox(0.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F);
		this.blueFin1.setPos(4.0F, 15.0F, -2.0F);
		this.topFrontFin = new ModelPart(this, 15, 17);
		this.topFrontFin.addBox(-4.0F, -1.0F, 0.0F, 8.0F, 1.0F, 0.0F);
		this.topFrontFin.setPos(0.0F, 14.0F, -4.0F);
		this.topFrontFin.xRot = (float) (Math.PI / 4);
		this.topMidFin = new ModelPart(this, 14, 16);
		this.topMidFin.addBox(-4.0F, -1.0F, 0.0F, 8.0F, 1.0F, 1.0F);
		this.topMidFin.setPos(0.0F, 14.0F, 0.0F);
		this.topBackFin = new ModelPart(this, 23, 18);
		this.topBackFin.addBox(-4.0F, -1.0F, 0.0F, 8.0F, 1.0F, 0.0F);
		this.topBackFin.setPos(0.0F, 14.0F, 4.0F);
		this.topBackFin.xRot = (float) (-Math.PI / 4);
		this.sideFrontFin0 = new ModelPart(this, 5, 17);
		this.sideFrontFin0.addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F);
		this.sideFrontFin0.setPos(-4.0F, 22.0F, -4.0F);
		this.sideFrontFin0.yRot = (float) (-Math.PI / 4);
		this.sideFrontFin1 = new ModelPart(this, 1, 17);
		this.sideFrontFin1.addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F);
		this.sideFrontFin1.setPos(4.0F, 22.0F, -4.0F);
		this.sideFrontFin1.yRot = (float) (Math.PI / 4);
		this.bottomFrontFin = new ModelPart(this, 15, 20);
		this.bottomFrontFin.addBox(-4.0F, 0.0F, 0.0F, 8.0F, 1.0F, 0.0F);
		this.bottomFrontFin.setPos(0.0F, 22.0F, -4.0F);
		this.bottomFrontFin.xRot = (float) (-Math.PI / 4);
		this.bottomMidFin = new ModelPart(this, 15, 20);
		this.bottomMidFin.addBox(-4.0F, 0.0F, 0.0F, 8.0F, 1.0F, 0.0F);
		this.bottomMidFin.setPos(0.0F, 22.0F, 0.0F);
		this.bottomBackFin = new ModelPart(this, 15, 20);
		this.bottomBackFin.addBox(-4.0F, 0.0F, 0.0F, 8.0F, 1.0F, 0.0F);
		this.bottomBackFin.setPos(0.0F, 22.0F, 4.0F);
		this.bottomBackFin.xRot = (float) (Math.PI / 4);
		this.sideBackFin0 = new ModelPart(this, 9, 17);
		this.sideBackFin0.addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F);
		this.sideBackFin0.setPos(-4.0F, 22.0F, 4.0F);
		this.sideBackFin0.yRot = (float) (Math.PI / 4);
		this.sideBackFin1 = new ModelPart(this, 9, 17);
		this.sideBackFin1.addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F);
		this.sideBackFin1.setPos(4.0F, 22.0F, 4.0F);
		this.sideBackFin1.yRot = (float) (-Math.PI / 4);
	}

	@Override
	public Iterable<ModelPart> parts() {
		return ImmutableList.<ModelPart>of(
			this.cube,
			this.blueFin0,
			this.blueFin1,
			this.topFrontFin,
			this.topMidFin,
			this.topBackFin,
			this.sideFrontFin0,
			this.sideBackFin1,
			this.bottomFrontFin,
			this.bottomMidFin,
			this.bottomBackFin,
			this.sideBackFin0,
			this.sideBackFin1
		);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
		this.blueFin0.zRot = -0.2F + 0.4F * Mth.sin(h * 0.2F);
		this.blueFin1.zRot = 0.2F - 0.4F * Mth.sin(h * 0.2F);
	}
}
