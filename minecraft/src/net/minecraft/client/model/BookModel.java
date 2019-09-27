package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class BookModel extends Model {
	private final ModelPart leftLid = new ModelPart(64, 32, 0, 0).addBox(-6.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F);
	private final ModelPart rightLid = new ModelPart(64, 32, 16, 0).addBox(0.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F);
	private final ModelPart leftPages;
	private final ModelPart rightPages;
	private final ModelPart flipPage1;
	private final ModelPart flipPage2;
	private final ModelPart seam = new ModelPart(64, 32, 12, 0).addBox(-1.0F, -5.0F, 0.0F, 2.0F, 10.0F, 0.005F);
	private final List<ModelPart> parts;

	public BookModel() {
		this.leftPages = new ModelPart(64, 32, 0, 10).addBox(0.0F, -4.0F, -0.99F, 5.0F, 8.0F, 1.0F);
		this.rightPages = new ModelPart(64, 32, 12, 10).addBox(0.0F, -4.0F, -0.01F, 5.0F, 8.0F, 1.0F);
		this.flipPage1 = new ModelPart(64, 32, 24, 10).addBox(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.005F);
		this.flipPage2 = new ModelPart(64, 32, 24, 10).addBox(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.005F);
		this.parts = ImmutableList.of(this.leftLid, this.rightLid, this.seam, this.leftPages, this.rightPages, this.flipPage1, this.flipPage2);
		this.leftLid.setPos(0.0F, 0.0F, -1.0F);
		this.rightLid.setPos(0.0F, 0.0F, 1.0F);
		this.seam.yRot = (float) (Math.PI / 2);
	}

	public void render(PoseStack poseStack, VertexConsumer vertexConsumer, float f, int i, TextureAtlasSprite textureAtlasSprite) {
		this.parts.forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, f, i, textureAtlasSprite));
	}

	public void setupAnim(float f, float g, float h, float i) {
		float j = (Mth.sin(f * 0.02F) * 0.1F + 1.25F) * i;
		this.leftLid.yRot = (float) Math.PI + j;
		this.rightLid.yRot = -j;
		this.leftPages.yRot = j;
		this.rightPages.yRot = -j;
		this.flipPage1.yRot = j - j * 2.0F * g;
		this.flipPage2.yRot = j - j * 2.0F * h;
		this.leftPages.x = Mth.sin(j);
		this.rightPages.x = Mth.sin(j);
		this.flipPage1.x = Mth.sin(j);
		this.flipPage2.x = Mth.sin(j);
	}
}
