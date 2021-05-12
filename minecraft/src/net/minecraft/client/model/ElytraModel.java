package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ElytraModel<T extends LivingEntity> extends AgeableListModel<T> {
	private final ModelPart rightWing;
	private final ModelPart leftWing;

	public ElytraModel(ModelPart modelPart) {
		this.leftWing = modelPart.getChild("left_wing");
		this.rightWing = modelPart.getChild("right_wing");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		CubeDeformation cubeDeformation = new CubeDeformation(1.0F);
		partDefinition.addOrReplaceChild(
			"left_wing",
			CubeListBuilder.create().texOffs(22, 0).addBox(-10.0F, 0.0F, 0.0F, 10.0F, 20.0F, 2.0F, cubeDeformation),
			PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, (float) (Math.PI / 12), 0.0F, (float) (-Math.PI / 12))
		);
		partDefinition.addOrReplaceChild(
			"right_wing",
			CubeListBuilder.create().texOffs(22, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 10.0F, 20.0F, 2.0F, cubeDeformation),
			PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, (float) (Math.PI / 12), 0.0F, (float) (Math.PI / 12))
		);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.<ModelPart>of();
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.<ModelPart>of(this.leftWing, this.rightWing);
	}

	public void setupAnim(T livingEntity, float f, float g, float h, float i, float j) {
		float k = (float) (Math.PI / 12);
		float l = (float) (-Math.PI / 12);
		float m = 0.0F;
		float n = 0.0F;
		if (livingEntity.isFallFlying()) {
			float o = 1.0F;
			Vec3 vec3 = livingEntity.getDeltaMovement();
			if (vec3.y < 0.0) {
				Vec3 vec32 = vec3.normalize();
				o = 1.0F - (float)Math.pow(-vec32.y, 1.5);
			}

			k = o * (float) (Math.PI / 9) + (1.0F - o) * k;
			l = o * (float) (-Math.PI / 2) + (1.0F - o) * l;
		} else if (livingEntity.isCrouching()) {
			k = (float) (Math.PI * 2.0 / 9.0);
			l = (float) (-Math.PI / 4);
			m = 3.0F;
			n = 0.08726646F;
		}

		this.leftWing.y = m;
		if (livingEntity instanceof AbstractClientPlayer abstractClientPlayer) {
			abstractClientPlayer.elytraRotX = (float)((double)abstractClientPlayer.elytraRotX + (double)(k - abstractClientPlayer.elytraRotX) * 0.1);
			abstractClientPlayer.elytraRotY = (float)((double)abstractClientPlayer.elytraRotY + (double)(n - abstractClientPlayer.elytraRotY) * 0.1);
			abstractClientPlayer.elytraRotZ = (float)((double)abstractClientPlayer.elytraRotZ + (double)(l - abstractClientPlayer.elytraRotZ) * 0.1);
			this.leftWing.xRot = abstractClientPlayer.elytraRotX;
			this.leftWing.yRot = abstractClientPlayer.elytraRotY;
			this.leftWing.zRot = abstractClientPlayer.elytraRotZ;
		} else {
			this.leftWing.xRot = k;
			this.leftWing.zRot = l;
			this.leftWing.yRot = n;
		}

		this.rightWing.yRot = -this.leftWing.yRot;
		this.rightWing.y = this.leftWing.y;
		this.rightWing.xRot = this.leftWing.xRot;
		this.rightWing.zRot = -this.leftWing.zRot;
	}
}
