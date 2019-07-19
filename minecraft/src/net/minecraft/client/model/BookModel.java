package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class BookModel extends Model {
	private final ModelPart leftLid = new ModelPart(this).texOffs(0, 0).addBox(-6.0F, -5.0F, 0.0F, 6, 10, 0);
	private final ModelPart rightLid = new ModelPart(this).texOffs(16, 0).addBox(0.0F, -5.0F, 0.0F, 6, 10, 0);
	private final ModelPart leftPages;
	private final ModelPart rightPages;
	private final ModelPart flipPage1;
	private final ModelPart flipPage2;
	private final ModelPart seam = new ModelPart(this).texOffs(12, 0).addBox(-1.0F, -5.0F, 0.0F, 2, 10, 0);

	public BookModel() {
		this.leftPages = new ModelPart(this).texOffs(0, 10).addBox(0.0F, -4.0F, -0.99F, 5, 8, 1);
		this.rightPages = new ModelPart(this).texOffs(12, 10).addBox(0.0F, -4.0F, -0.01F, 5, 8, 1);
		this.flipPage1 = new ModelPart(this).texOffs(24, 10).addBox(0.0F, -4.0F, 0.0F, 5, 8, 0);
		this.flipPage2 = new ModelPart(this).texOffs(24, 10).addBox(0.0F, -4.0F, 0.0F, 5, 8, 0);
		this.leftLid.setPos(0.0F, 0.0F, -1.0F);
		this.rightLid.setPos(0.0F, 0.0F, 1.0F);
		this.seam.yRot = (float) (Math.PI / 2);
	}

	public void render(float f, float g, float h, float i, float j, float k) {
		this.setupAnim(f, g, h, i, j, k);
		this.leftLid.render(k);
		this.rightLid.render(k);
		this.seam.render(k);
		this.leftPages.render(k);
		this.rightPages.render(k);
		this.flipPage1.render(k);
		this.flipPage2.render(k);
	}

	private void setupAnim(float f, float g, float h, float i, float j, float k) {
		float l = (Mth.sin(f * 0.02F) * 0.1F + 1.25F) * i;
		this.leftLid.yRot = (float) Math.PI + l;
		this.rightLid.yRot = -l;
		this.leftPages.yRot = l;
		this.rightPages.yRot = -l;
		this.flipPage1.yRot = l - l * 2.0F * g;
		this.flipPage2.yRot = l - l * 2.0F * h;
		this.leftPages.x = Mth.sin(l);
		this.rightPages.x = Mth.sin(l);
		this.flipPage1.x = Mth.sin(l);
		this.flipPage2.x = Mth.sin(l);
	}
}
