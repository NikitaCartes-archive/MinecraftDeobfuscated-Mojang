/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
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

@Environment(value=EnvType.CLIENT)
public class GuardianModel
extends HierarchicalModel<Guardian> {
    private static final float[] SPIKE_X_ROT = new float[]{1.75f, 0.25f, 0.0f, 0.0f, 0.5f, 0.5f, 0.5f, 0.5f, 1.25f, 0.75f, 0.0f, 0.0f};
    private static final float[] SPIKE_Y_ROT = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.25f, 1.75f, 1.25f, 0.75f, 0.0f, 0.0f, 0.0f, 0.0f};
    private static final float[] SPIKE_Z_ROT = new float[]{0.0f, 0.0f, 0.25f, 1.75f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.75f, 1.25f};
    private static final float[] SPIKE_X = new float[]{0.0f, 0.0f, 8.0f, -8.0f, -8.0f, 8.0f, 8.0f, -8.0f, 0.0f, 0.0f, 8.0f, -8.0f};
    private static final float[] SPIKE_Y = new float[]{-8.0f, -8.0f, -8.0f, -8.0f, 0.0f, 0.0f, 0.0f, 0.0f, 8.0f, 8.0f, 8.0f, 8.0f};
    private static final float[] SPIKE_Z = new float[]{8.0f, -8.0f, 0.0f, 0.0f, -8.0f, -8.0f, 8.0f, 8.0f, 8.0f, -8.0f, 0.0f, 0.0f};
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart eye;
    private final ModelPart[] spikeParts;
    private final ModelPart[] tailParts;

    public GuardianModel(ModelPart modelPart) {
        this.root = modelPart;
        this.spikeParts = new ModelPart[12];
        this.head = modelPart.getChild("head");
        for (int i = 0; i < this.spikeParts.length; ++i) {
            this.spikeParts[i] = this.head.getChild(GuardianModel.createSpikeName(i));
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
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, 10.0f, -8.0f, 12.0f, 12.0f, 16.0f).texOffs(0, 28).addBox(-8.0f, 10.0f, -6.0f, 2.0f, 12.0f, 12.0f).texOffs(0, 28).addBox(6.0f, 10.0f, -6.0f, 2.0f, 12.0f, 12.0f, true).texOffs(16, 40).addBox(-6.0f, 8.0f, -6.0f, 12.0f, 2.0f, 12.0f).texOffs(16, 40).addBox(-6.0f, 22.0f, -6.0f, 12.0f, 2.0f, 12.0f), PartPose.ZERO);
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f);
        for (int i = 0; i < 12; ++i) {
            float f = GuardianModel.getSpikeX(i, 0.0f, 0.0f);
            float g = GuardianModel.getSpikeY(i, 0.0f, 0.0f);
            float h = GuardianModel.getSpikeZ(i, 0.0f, 0.0f);
            float j = (float)Math.PI * SPIKE_X_ROT[i];
            float k = (float)Math.PI * SPIKE_Y_ROT[i];
            float l = (float)Math.PI * SPIKE_Z_ROT[i];
            partDefinition2.addOrReplaceChild(GuardianModel.createSpikeName(i), cubeListBuilder, PartPose.offsetAndRotation(f, g, h, j, k, l));
        }
        partDefinition2.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0f, 15.0f, 0.0f, 2.0f, 2.0f, 1.0f), PartPose.offset(0.0f, 0.0f, -8.25f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("tail0", CubeListBuilder.create().texOffs(40, 0).addBox(-2.0f, 14.0f, 7.0f, 4.0f, 4.0f, 8.0f), PartPose.ZERO);
        PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 54).addBox(0.0f, 14.0f, 0.0f, 3.0f, 3.0f, 7.0f), PartPose.offset(-1.5f, 0.5f, 14.0f));
        partDefinition4.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(41, 32).addBox(0.0f, 14.0f, 0.0f, 2.0f, 2.0f, 6.0f).texOffs(25, 19).addBox(1.0f, 10.5f, 3.0f, 1.0f, 9.0f, 9.0f), PartPose.offset(0.5f, 0.5f, 6.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(Guardian guardian, float f, float g, float h, float i, float j) {
        float k = h - (float)guardian.tickCount;
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        float l = (1.0f - guardian.getSpikesAnimation(k)) * 0.55f;
        this.setupSpikes(h, l);
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (guardian.hasActiveAttackTarget()) {
            entity = guardian.getActiveAttackTarget();
        }
        if (entity != null) {
            Vec3 vec3 = entity.getEyePosition(0.0f);
            Vec3 vec32 = guardian.getEyePosition(0.0f);
            double d = vec3.y - vec32.y;
            this.eye.y = d > 0.0 ? 0.0f : 1.0f;
            Vec3 vec33 = guardian.getViewVector(0.0f);
            vec33 = new Vec3(vec33.x, 0.0, vec33.z);
            Vec3 vec34 = new Vec3(vec32.x - vec3.x, 0.0, vec32.z - vec3.z).normalize().yRot(1.5707964f);
            double e = vec33.dot(vec34);
            this.eye.x = Mth.sqrt((float)Math.abs(e)) * 2.0f * (float)Math.signum(e);
        }
        this.eye.visible = true;
        float m = guardian.getTailAnimation(k);
        this.tailParts[0].yRot = Mth.sin(m) * (float)Math.PI * 0.05f;
        this.tailParts[1].yRot = Mth.sin(m) * (float)Math.PI * 0.1f;
        this.tailParts[2].yRot = Mth.sin(m) * (float)Math.PI * 0.15f;
    }

    private void setupSpikes(float f, float g) {
        for (int i = 0; i < 12; ++i) {
            this.spikeParts[i].x = GuardianModel.getSpikeX(i, f, g);
            this.spikeParts[i].y = GuardianModel.getSpikeY(i, f, g);
            this.spikeParts[i].z = GuardianModel.getSpikeZ(i, f, g);
        }
    }

    private static float getSpikeOffset(int i, float f, float g) {
        return 1.0f + Mth.cos(f * 1.5f + (float)i) * 0.01f - g;
    }

    private static float getSpikeX(int i, float f, float g) {
        return SPIKE_X[i] * GuardianModel.getSpikeOffset(i, f, g);
    }

    private static float getSpikeY(int i, float f, float g) {
        return 16.0f + SPIKE_Y[i] * GuardianModel.getSpikeOffset(i, f, g);
    }

    private static float getSpikeZ(int i, float f, float g) {
        return SPIKE_Z[i] * GuardianModel.getSpikeOffset(i, f, g);
    }
}

