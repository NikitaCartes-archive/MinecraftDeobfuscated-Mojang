package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class GuardianModel extends HierarchicalModel<Guardian> {
	private static final float[] SPIKE_X_ROT = new float[]{1.75F, 0.25F, 0.0F, 0.0F, 0.5F, 0.5F, 0.5F, 0.5F, 1.25F, 0.75F, 0.0F, 0.0F};
	private static final float[] SPIKE_Y_ROT = new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.25F, 1.75F, 1.25F, 0.75F, 0.0F, 0.0F, 0.0F, 0.0F};
	private static final float[] SPIKE_Z_ROT = new float[]{0.0F, 0.0F, 0.25F, 1.75F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.75F, 1.25F};
	private static final float[] SPIKE_X = new float[]{0.0F, 0.0F, 8.0F, -8.0F, -8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F, 8.0F, -8.0F};
	private static final float[] SPIKE_Y = new float[]{-8.0F, -8.0F, -8.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, 8.0F};
	private static final float[] SPIKE_Z = new float[]{8.0F, -8.0F, 0.0F, 0.0F, -8.0F, -8.0F, 8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F};
	private static final String EYE = "eye";
	private static final String TAIL_0 = "tail0";
	private static final String TAIL_1 = "tail1";
	private static final String TAIL_2 = "tail2";
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart eye;
	private final ModelPart[] spikeParts;
	private final ModelPart[] tailParts;

	public GuardianModel(ModelPart modelPart) {
		this.root = modelPart;
		this.spikeParts = new ModelPart[12];
		this.head = modelPart.getChild("head");

		for (int i = 0; i < this.spikeParts.length; i++) {
			this.spikeParts[i] = this.head.getChild(createSpikeName(i));
		}

		this.eye = this.head.getChild("eye");
		this.tailParts = new ModelPart[3];
		this.tailParts[0] = this.head.getChild("tail0");
		this.tailParts[1] = this.tailParts[0].getChild("tail1");
		this.tailParts[2] = this.tailParts[1].getChild("tail2");
	}

	private static String createSpikeName(int i) {
		return "spike" + i;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-6.0F, 10.0F, -8.0F, 12.0F, 12.0F, 16.0F)
				.texOffs(0, 28)
				.addBox(-8.0F, 10.0F, -6.0F, 2.0F, 12.0F, 12.0F)
				.texOffs(0, 28)
				.addBox(6.0F, 10.0F, -6.0F, 2.0F, 12.0F, 12.0F, true)
				.texOffs(16, 40)
				.addBox(-6.0F, 8.0F, -6.0F, 12.0F, 2.0F, 12.0F)
				.texOffs(16, 40)
				.addBox(-6.0F, 22.0F, -6.0F, 12.0F, 2.0F, 12.0F),
			PartPose.ZERO
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F);

		for (int i = 0; i < 12; i++) {
			float f = getSpikeX(i, 0.0F, 0.0F);
			float g = getSpikeY(i, 0.0F, 0.0F);
			float h = getSpikeZ(i, 0.0F, 0.0F);
			float j = (float) Math.PI * SPIKE_X_ROT[i];
			float k = (float) Math.PI * SPIKE_Y_ROT[i];
			float l = (float) Math.PI * SPIKE_Z_ROT[i];
			partDefinition2.addOrReplaceChild(createSpikeName(i), cubeListBuilder, PartPose.offsetAndRotation(f, g, h, j, k, l));
		}

		partDefinition2.addOrReplaceChild(
			"eye", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, 15.0F, 0.0F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 0.0F, -8.25F)
		);
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(
			"tail0", CubeListBuilder.create().texOffs(40, 0).addBox(-2.0F, 14.0F, 7.0F, 4.0F, 4.0F, 8.0F), PartPose.ZERO
		);
		PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild(
			"tail1", CubeListBuilder.create().texOffs(0, 54).addBox(0.0F, 14.0F, 0.0F, 3.0F, 3.0F, 7.0F), PartPose.offset(-1.5F, 0.5F, 14.0F)
		);
		partDefinition4.addOrReplaceChild(
			"tail2",
			CubeListBuilder.create().texOffs(41, 32).addBox(0.0F, 14.0F, 0.0F, 2.0F, 2.0F, 6.0F).texOffs(25, 19).addBox(1.0F, 10.5F, 3.0F, 1.0F, 9.0F, 9.0F),
			PartPose.offset(0.5F, 0.5F, 6.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	public void setupAnim(Guardian guardian, float f, float g, float h, float i, float j) {
		float k = h - (float)guardian.tickCount;
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.head.xRot = j * (float) (Math.PI / 180.0);
		float l = (1.0F - guardian.getSpikesAnimation(k)) * 0.55F;
		this.setupSpikes(h, l);
		Entity entity = Minecraft.getInstance().getCameraEntity();
		if (guardian.hasActiveAttackTarget()) {
			entity = guardian.getActiveAttackTarget();
		}

		if (entity != null) {
			Vec3 vec3 = entity.getEyePosition(0.0F);
			Vec3 vec32 = guardian.getEyePosition(0.0F);
			double d = vec3.y - vec32.y;
			if (d > 0.0) {
				this.eye.y = 0.0F;
			} else {
				this.eye.y = 1.0F;
			}

			Vec3 vec33 = guardian.getViewVector(0.0F);
			vec33 = new Vec3(vec33.x, 0.0, vec33.z);
			Vec3 vec34 = new Vec3(vec32.x - vec3.x, 0.0, vec32.z - vec3.z).normalize().yRot((float) (Math.PI / 2));
			double e = vec33.dot(vec34);
			this.eye.x = Mth.sqrt((float)Math.abs(e)) * 2.0F * (float)Math.signum(e);
		}

		this.eye.visible = true;
		float m = guardian.getTailAnimation(k);
		this.tailParts[0].yRot = Mth.sin(m) * (float) Math.PI * 0.05F;
		this.tailParts[1].yRot = Mth.sin(m) * (float) Math.PI * 0.1F;
		this.tailParts[2].yRot = Mth.sin(m) * (float) Math.PI * 0.15F;
	}

	private void setupSpikes(float f, float g) {
		for (int i = 0; i < 12; i++) {
			this.spikeParts[i].x = getSpikeX(i, f, g);
			this.spikeParts[i].y = getSpikeY(i, f, g);
			this.spikeParts[i].z = getSpikeZ(i, f, g);
		}
	}

	private static float getSpikeOffset(int i, float f, float g) {
		return 1.0F + Mth.cos(f * 1.5F + (float)i) * 0.01F - g;
	}

	private static float getSpikeX(int i, float f, float g) {
		return SPIKE_X[i] * getSpikeOffset(i, f, g);
	}

	private static float getSpikeY(int i, float f, float g) {
		return 16.0F + SPIKE_Y[i] * getSpikeOffset(i, f, g);
	}

	private static float getSpikeZ(int i, float f, float g) {
		return SPIKE_Z[i] * getSpikeOffset(i, f, g);
	}
}
