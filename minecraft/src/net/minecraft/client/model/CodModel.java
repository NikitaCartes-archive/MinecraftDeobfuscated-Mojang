package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class CodModel<T extends Entity> extends ListModel<T> {
	private final ModelPart body;
	private final ModelPart topFin;
	private final ModelPart head;
	private final ModelPart nose;
	private final ModelPart sideFin0;
	private final ModelPart sideFin1;
	private final ModelPart tailFin;

	public CodModel() {
		this.texWidth = 32;
		this.texHeight = 32;
		int i = 22;
		this.body = new ModelPart(this, 0, 0);
		this.body.addBox(-1.0F, -2.0F, 0.0F, 2.0F, 4.0F, 7.0F);
		this.body.setPos(0.0F, 22.0F, 0.0F);
		this.head = new ModelPart(this, 11, 0);
		this.head.addBox(-1.0F, -2.0F, -3.0F, 2.0F, 4.0F, 3.0F);
		this.head.setPos(0.0F, 22.0F, 0.0F);
		this.nose = new ModelPart(this, 0, 0);
		this.nose.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 1.0F);
		this.nose.setPos(0.0F, 22.0F, -3.0F);
		this.sideFin0 = new ModelPart(this, 22, 1);
		this.sideFin0.addBox(-2.0F, 0.0F, -1.0F, 2.0F, 0.0F, 2.0F);
		this.sideFin0.setPos(-1.0F, 23.0F, 0.0F);
		this.sideFin0.zRot = (float) (-Math.PI / 4);
		this.sideFin1 = new ModelPart(this, 22, 4);
		this.sideFin1.addBox(0.0F, 0.0F, -1.0F, 2.0F, 0.0F, 2.0F);
		this.sideFin1.setPos(1.0F, 23.0F, 0.0F);
		this.sideFin1.zRot = (float) (Math.PI / 4);
		this.tailFin = new ModelPart(this, 22, 3);
		this.tailFin.addBox(0.0F, -2.0F, 0.0F, 0.0F, 4.0F, 4.0F);
		this.tailFin.setPos(0.0F, 22.0F, 7.0F);
		this.topFin = new ModelPart(this, 20, -6);
		this.topFin.addBox(0.0F, -1.0F, -1.0F, 0.0F, 1.0F, 6.0F);
		this.topFin.setPos(0.0F, 20.0F, 0.0F);
	}

	@Override
	public Iterable<ModelPart> parts() {
		return ImmutableList.<ModelPart>of(this.body, this.head, this.nose, this.sideFin0, this.sideFin1, this.tailFin, this.topFin);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
		float l = 1.0F;
		if (!entity.isInWater()) {
			l = 1.5F;
		}

		this.tailFin.yRot = -l * 0.45F * Mth.sin(0.6F * h);
	}
}
