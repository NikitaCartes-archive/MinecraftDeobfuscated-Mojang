package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class GuardianModel extends ListModel<Guardian> {
	private static final float[] SPIKE_X_ROT = new float[]{1.75F, 0.25F, 0.0F, 0.0F, 0.5F, 0.5F, 0.5F, 0.5F, 1.25F, 0.75F, 0.0F, 0.0F};
	private static final float[] SPIKE_Y_ROT = new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.25F, 1.75F, 1.25F, 0.75F, 0.0F, 0.0F, 0.0F, 0.0F};
	private static final float[] SPIKE_Z_ROT = new float[]{0.0F, 0.0F, 0.25F, 1.75F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.75F, 1.25F};
	private static final float[] SPIKE_X = new float[]{0.0F, 0.0F, 8.0F, -8.0F, -8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F, 8.0F, -8.0F};
	private static final float[] SPIKE_Y = new float[]{-8.0F, -8.0F, -8.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, 8.0F};
	private static final float[] SPIKE_Z = new float[]{8.0F, -8.0F, 0.0F, 0.0F, -8.0F, -8.0F, 8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F};
	private final ModelPart head;
	private final ModelPart eye;
	private final ModelPart[] spikeParts;
	private final ModelPart[] tailParts;

	public GuardianModel() {
		this.texWidth = 64;
		this.texHeight = 64;
		this.spikeParts = new ModelPart[12];
		this.head = new ModelPart(this);
		this.head.texOffs(0, 0).addBox(-6.0F, 10.0F, -8.0F, 12.0F, 12.0F, 16.0F);
		this.head.texOffs(0, 28).addBox(-8.0F, 10.0F, -6.0F, 2.0F, 12.0F, 12.0F);
		this.head.texOffs(0, 28).addBox(6.0F, 10.0F, -6.0F, 2.0F, 12.0F, 12.0F, true);
		this.head.texOffs(16, 40).addBox(-6.0F, 8.0F, -6.0F, 12.0F, 2.0F, 12.0F);
		this.head.texOffs(16, 40).addBox(-6.0F, 22.0F, -6.0F, 12.0F, 2.0F, 12.0F);

		for (int i = 0; i < this.spikeParts.length; i++) {
			this.spikeParts[i] = new ModelPart(this, 0, 0);
			this.spikeParts[i].addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F);
			this.head.addChild(this.spikeParts[i]);
		}

		this.eye = new ModelPart(this, 8, 0);
		this.eye.addBox(-1.0F, 15.0F, 0.0F, 2.0F, 2.0F, 1.0F);
		this.head.addChild(this.eye);
		this.tailParts = new ModelPart[3];
		this.tailParts[0] = new ModelPart(this, 40, 0);
		this.tailParts[0].addBox(-2.0F, 14.0F, 7.0F, 4.0F, 4.0F, 8.0F);
		this.tailParts[1] = new ModelPart(this, 0, 54);
		this.tailParts[1].addBox(0.0F, 14.0F, 0.0F, 3.0F, 3.0F, 7.0F);
		this.tailParts[2] = new ModelPart(this);
		this.tailParts[2].texOffs(41, 32).addBox(0.0F, 14.0F, 0.0F, 2.0F, 2.0F, 6.0F);
		this.tailParts[2].texOffs(25, 19).addBox(1.0F, 10.5F, 3.0F, 1.0F, 9.0F, 9.0F);
		this.head.addChild(this.tailParts[0]);
		this.tailParts[0].addChild(this.tailParts[1]);
		this.tailParts[1].addChild(this.tailParts[2]);
		this.setupSpikes(0.0F, 0.0F);
	}

	@Override
	public Iterable<ModelPart> parts() {
		return ImmutableList.<ModelPart>of(this.head);
	}

	public void setupAnim(Guardian guardian, float f, float g, float h, float i, float j) {
		float k = h - (float)guardian.tickCount;
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.head.xRot = j * (float) (Math.PI / 180.0);
		float l = (1.0F - guardian.getSpikesAnimation(k)) * 0.55F;
		this.setupSpikes(h, l);
		this.eye.z = -8.25F;
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
		this.tailParts[1].x = -1.5F;
		this.tailParts[1].y = 0.5F;
		this.tailParts[1].z = 14.0F;
		this.tailParts[2].yRot = Mth.sin(m) * (float) Math.PI * 0.15F;
		this.tailParts[2].x = 0.5F;
		this.tailParts[2].y = 0.5F;
		this.tailParts[2].z = 6.0F;
	}

	private void setupSpikes(float f, float g) {
		for (int i = 0; i < 12; i++) {
			this.spikeParts[i].xRot = (float) Math.PI * SPIKE_X_ROT[i];
			this.spikeParts[i].yRot = (float) Math.PI * SPIKE_Y_ROT[i];
			this.spikeParts[i].zRot = (float) Math.PI * SPIKE_Z_ROT[i];
			this.spikeParts[i].x = SPIKE_X[i] * (1.0F + Mth.cos(f * 1.5F + (float)i) * 0.01F - g);
			this.spikeParts[i].y = 16.0F + SPIKE_Y[i] * (1.0F + Mth.cos(f * 1.5F + (float)i) * 0.01F - g);
			this.spikeParts[i].z = SPIKE_Z[i] * (1.0F + Mth.cos(f * 1.5F + (float)i) * 0.01F - g);
		}
	}
}
