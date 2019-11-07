package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;

@Environment(EnvType.CLIENT)
public class WitherBossModel<T extends WitherBoss> extends ListModel<T> {
	private final ModelPart[] upperBodyParts;
	private final ModelPart[] heads;
	private final ImmutableList<ModelPart> parts;

	public WitherBossModel(float f) {
		this.texWidth = 64;
		this.texHeight = 64;
		this.upperBodyParts = new ModelPart[3];
		this.upperBodyParts[0] = new ModelPart(this, 0, 16);
		this.upperBodyParts[0].addBox(-10.0F, 3.9F, -0.5F, 20.0F, 3.0F, 3.0F, f);
		this.upperBodyParts[1] = new ModelPart(this).setTexSize(this.texWidth, this.texHeight);
		this.upperBodyParts[1].setPos(-2.0F, 6.9F, -0.5F);
		this.upperBodyParts[1].texOffs(0, 22).addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, f);
		this.upperBodyParts[1].texOffs(24, 22).addBox(-4.0F, 1.5F, 0.5F, 11.0F, 2.0F, 2.0F, f);
		this.upperBodyParts[1].texOffs(24, 22).addBox(-4.0F, 4.0F, 0.5F, 11.0F, 2.0F, 2.0F, f);
		this.upperBodyParts[1].texOffs(24, 22).addBox(-4.0F, 6.5F, 0.5F, 11.0F, 2.0F, 2.0F, f);
		this.upperBodyParts[2] = new ModelPart(this, 12, 22);
		this.upperBodyParts[2].addBox(0.0F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F, f);
		this.heads = new ModelPart[3];
		this.heads[0] = new ModelPart(this, 0, 0);
		this.heads[0].addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, f);
		this.heads[1] = new ModelPart(this, 32, 0);
		this.heads[1].addBox(-4.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, f);
		this.heads[1].x = -8.0F;
		this.heads[1].y = 4.0F;
		this.heads[2] = new ModelPart(this, 32, 0);
		this.heads[2].addBox(-4.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, f);
		this.heads[2].x = 10.0F;
		this.heads[2].y = 4.0F;
		Builder<ModelPart> builder = ImmutableList.builder();
		builder.addAll(Arrays.asList(this.heads));
		builder.addAll(Arrays.asList(this.upperBodyParts));
		this.parts = builder.build();
	}

	public ImmutableList<ModelPart> parts() {
		return this.parts;
	}

	public void setupAnim(T witherBoss, float f, float g, float h, float i, float j) {
		float k = Mth.cos(h * 0.1F);
		this.upperBodyParts[1].xRot = (0.065F + 0.05F * k) * (float) Math.PI;
		this.upperBodyParts[2].setPos(-2.0F, 6.9F + Mth.cos(this.upperBodyParts[1].xRot) * 10.0F, -0.5F + Mth.sin(this.upperBodyParts[1].xRot) * 10.0F);
		this.upperBodyParts[2].xRot = (0.265F + 0.1F * k) * (float) Math.PI;
		this.heads[0].yRot = i * (float) (Math.PI / 180.0);
		this.heads[0].xRot = j * (float) (Math.PI / 180.0);
	}

	public void prepareMobModel(T witherBoss, float f, float g, float h) {
		for (int i = 1; i < 3; i++) {
			this.heads[i].yRot = (witherBoss.getHeadYRot(i - 1) - witherBoss.yBodyRot) * (float) (Math.PI / 180.0);
			this.heads[i].xRot = witherBoss.getHeadXRot(i - 1) * (float) (Math.PI / 180.0);
		}
	}
}
