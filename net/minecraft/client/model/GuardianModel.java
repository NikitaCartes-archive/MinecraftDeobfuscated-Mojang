/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class GuardianModel
extends ListModel<Guardian> {
    private static final float[] SPIKE_X_ROT = new float[]{1.75f, 0.25f, 0.0f, 0.0f, 0.5f, 0.5f, 0.5f, 0.5f, 1.25f, 0.75f, 0.0f, 0.0f};
    private static final float[] SPIKE_Y_ROT = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.25f, 1.75f, 1.25f, 0.75f, 0.0f, 0.0f, 0.0f, 0.0f};
    private static final float[] SPIKE_Z_ROT = new float[]{0.0f, 0.0f, 0.25f, 1.75f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.75f, 1.25f};
    private static final float[] SPIKE_X = new float[]{0.0f, 0.0f, 8.0f, -8.0f, -8.0f, 8.0f, 8.0f, -8.0f, 0.0f, 0.0f, 8.0f, -8.0f};
    private static final float[] SPIKE_Y = new float[]{-8.0f, -8.0f, -8.0f, -8.0f, 0.0f, 0.0f, 0.0f, 0.0f, 8.0f, 8.0f, 8.0f, 8.0f};
    private static final float[] SPIKE_Z = new float[]{8.0f, -8.0f, 0.0f, 0.0f, -8.0f, -8.0f, 8.0f, 8.0f, 8.0f, -8.0f, 0.0f, 0.0f};
    private final ModelPart head;
    private final ModelPart eye;
    private final ModelPart[] spikeParts;
    private final ModelPart[] tailParts;

    public GuardianModel() {
        super(RenderType::entityCutoutNoCull);
        this.texWidth = 64;
        this.texHeight = 64;
        this.spikeParts = new ModelPart[12];
        this.head = new ModelPart(this);
        this.head.texOffs(0, 0).addBox(-6.0f, 10.0f, -8.0f, 12.0f, 12.0f, 16.0f);
        this.head.texOffs(0, 28).addBox(-8.0f, 10.0f, -6.0f, 2.0f, 12.0f, 12.0f);
        this.head.texOffs(0, 28).addBox(6.0f, 10.0f, -6.0f, 2.0f, 12.0f, 12.0f, true);
        this.head.texOffs(16, 40).addBox(-6.0f, 8.0f, -6.0f, 12.0f, 2.0f, 12.0f);
        this.head.texOffs(16, 40).addBox(-6.0f, 22.0f, -6.0f, 12.0f, 2.0f, 12.0f);
        for (int i = 0; i < this.spikeParts.length; ++i) {
            this.spikeParts[i] = new ModelPart(this, 0, 0);
            this.spikeParts[i].addBox(-1.0f, -4.5f, -1.0f, 2.0f, 9.0f, 2.0f);
            this.head.addChild(this.spikeParts[i]);
        }
        this.eye = new ModelPart(this, 8, 0);
        this.eye.addBox(-1.0f, 15.0f, 0.0f, 2.0f, 2.0f, 1.0f);
        this.head.addChild(this.eye);
        this.tailParts = new ModelPart[3];
        this.tailParts[0] = new ModelPart(this, 40, 0);
        this.tailParts[0].addBox(-2.0f, 14.0f, 7.0f, 4.0f, 4.0f, 8.0f);
        this.tailParts[1] = new ModelPart(this, 0, 54);
        this.tailParts[1].addBox(0.0f, 14.0f, 0.0f, 3.0f, 3.0f, 7.0f);
        this.tailParts[2] = new ModelPart(this);
        this.tailParts[2].texOffs(41, 32).addBox(0.0f, 14.0f, 0.0f, 2.0f, 2.0f, 6.0f);
        this.tailParts[2].texOffs(25, 19).addBox(1.0f, 10.5f, 3.0f, 1.0f, 9.0f, 9.0f);
        this.head.addChild(this.tailParts[0]);
        this.tailParts[0].addChild(this.tailParts[1]);
        this.tailParts[1].addChild(this.tailParts[2]);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.head);
    }

    @Override
    public void setupAnim(Guardian guardian, float f, float g, float h, float i, float j, float k) {
        float l = h - (float)guardian.tickCount;
        this.head.yRot = i * ((float)Math.PI / 180);
        this.head.xRot = j * ((float)Math.PI / 180);
        float m = (1.0f - guardian.getSpikesAnimation(l)) * 0.55f;
        for (int n = 0; n < 12; ++n) {
            this.spikeParts[n].xRot = (float)Math.PI * SPIKE_X_ROT[n];
            this.spikeParts[n].yRot = (float)Math.PI * SPIKE_Y_ROT[n];
            this.spikeParts[n].zRot = (float)Math.PI * SPIKE_Z_ROT[n];
            this.spikeParts[n].x = SPIKE_X[n] * (1.0f + Mth.cos(h * 1.5f + (float)n) * 0.01f - m);
            this.spikeParts[n].y = 16.0f + SPIKE_Y[n] * (1.0f + Mth.cos(h * 1.5f + (float)n) * 0.01f - m);
            this.spikeParts[n].z = SPIKE_Z[n] * (1.0f + Mth.cos(h * 1.5f + (float)n) * 0.01f - m);
        }
        this.eye.z = -8.25f;
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
        float o = guardian.getTailAnimation(l);
        this.tailParts[0].yRot = Mth.sin(o) * (float)Math.PI * 0.05f;
        this.tailParts[1].yRot = Mth.sin(o) * (float)Math.PI * 0.1f;
        this.tailParts[1].x = -1.5f;
        this.tailParts[1].y = 0.5f;
        this.tailParts[1].z = 14.0f;
        this.tailParts[2].yRot = Mth.sin(o) * (float)Math.PI * 0.15f;
        this.tailParts[2].x = 0.5f;
        this.tailParts[2].y = 0.5f;
        this.tailParts[2].z = 6.0f;
    }
}

