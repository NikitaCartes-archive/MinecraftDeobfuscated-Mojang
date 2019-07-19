package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;

@Environment(EnvType.CLIENT)
public class BedModel extends Model {
	private final ModelPart headPiece;
	private final ModelPart footPiece;
	private final ModelPart[] legs = new ModelPart[4];

	public BedModel() {
		this.texWidth = 64;
		this.texHeight = 64;
		this.headPiece = new ModelPart(this, 0, 0);
		this.headPiece.addBox(0.0F, 0.0F, 0.0F, 16, 16, 6, 0.0F);
		this.footPiece = new ModelPart(this, 0, 22);
		this.footPiece.addBox(0.0F, 0.0F, 0.0F, 16, 16, 6, 0.0F);
		this.legs[0] = new ModelPart(this, 50, 0);
		this.legs[1] = new ModelPart(this, 50, 6);
		this.legs[2] = new ModelPart(this, 50, 12);
		this.legs[3] = new ModelPart(this, 50, 18);
		this.legs[0].addBox(0.0F, 6.0F, -16.0F, 3, 3, 3);
		this.legs[1].addBox(0.0F, 6.0F, 0.0F, 3, 3, 3);
		this.legs[2].addBox(-16.0F, 6.0F, -16.0F, 3, 3, 3);
		this.legs[3].addBox(-16.0F, 6.0F, 0.0F, 3, 3, 3);
		this.legs[0].xRot = (float) (Math.PI / 2);
		this.legs[1].xRot = (float) (Math.PI / 2);
		this.legs[2].xRot = (float) (Math.PI / 2);
		this.legs[3].xRot = (float) (Math.PI / 2);
		this.legs[0].zRot = 0.0F;
		this.legs[1].zRot = (float) (Math.PI / 2);
		this.legs[2].zRot = (float) (Math.PI * 3.0 / 2.0);
		this.legs[3].zRot = (float) Math.PI;
	}

	public void render() {
		this.headPiece.render(0.0625F);
		this.footPiece.render(0.0625F);
		this.legs[0].render(0.0625F);
		this.legs[1].render(0.0625F);
		this.legs[2].render(0.0625F);
		this.legs[3].render(0.0625F);
	}

	public void preparePiece(boolean bl) {
		this.headPiece.visible = bl;
		this.footPiece.visible = !bl;
		this.legs[0].visible = !bl;
		this.legs[1].visible = bl;
		this.legs[2].visible = !bl;
		this.legs[3].visible = bl;
	}
}
