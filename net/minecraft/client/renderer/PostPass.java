/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import java.io.IOException;
import java.util.List;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(value=EnvType.CLIENT)
public class PostPass
implements AutoCloseable {
    private final EffectInstance effect;
    public final RenderTarget inTarget;
    public final RenderTarget outTarget;
    private final List<IntSupplier> auxAssets = Lists.newArrayList();
    private final List<String> auxNames = Lists.newArrayList();
    private final List<Integer> auxWidths = Lists.newArrayList();
    private final List<Integer> auxHeights = Lists.newArrayList();
    private Matrix4f shaderOrthoMatrix;

    public PostPass(ResourceManager resourceManager, String string, RenderTarget renderTarget, RenderTarget renderTarget2) throws IOException {
        this.effect = new EffectInstance(resourceManager, string);
        this.inTarget = renderTarget;
        this.outTarget = renderTarget2;
    }

    @Override
    public void close() {
        this.effect.close();
    }

    public void addAuxAsset(String string, IntSupplier intSupplier, int i, int j) {
        this.auxNames.add(this.auxNames.size(), string);
        this.auxAssets.add(this.auxAssets.size(), intSupplier);
        this.auxWidths.add(this.auxWidths.size(), i);
        this.auxHeights.add(this.auxHeights.size(), j);
    }

    public void setOrthoMatrix(Matrix4f matrix4f) {
        this.shaderOrthoMatrix = matrix4f;
    }

    public void process(float f) {
        this.inTarget.unbindWrite();
        float g = this.outTarget.width;
        float h = this.outTarget.height;
        RenderSystem.viewport(0, 0, (int)g, (int)h);
        this.effect.setSampler("DiffuseSampler", this.inTarget::getColorTextureId);
        for (int i = 0; i < this.auxAssets.size(); ++i) {
            this.effect.setSampler(this.auxNames.get(i), this.auxAssets.get(i));
            this.effect.safeGetUniform("AuxSize" + i).set(this.auxWidths.get(i).intValue(), this.auxHeights.get(i).intValue());
        }
        this.effect.safeGetUniform("ProjMat").set(this.shaderOrthoMatrix);
        this.effect.safeGetUniform("InSize").set(this.inTarget.width, this.inTarget.height);
        this.effect.safeGetUniform("OutSize").set(g, h);
        this.effect.safeGetUniform("Time").set(f);
        Minecraft minecraft = Minecraft.getInstance();
        this.effect.safeGetUniform("ScreenSize").set(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
        this.effect.apply();
        this.outTarget.clear(Minecraft.ON_OSX);
        this.outTarget.bindWrite(false);
        RenderSystem.depthFunc(519);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(0.0, 0.0, 500.0).endVertex();
        bufferBuilder.vertex(g, 0.0, 500.0).endVertex();
        bufferBuilder.vertex(g, h, 500.0).endVertex();
        bufferBuilder.vertex(0.0, h, 500.0).endVertex();
        bufferBuilder.end();
        BufferUploader._endInternal(bufferBuilder);
        RenderSystem.depthFunc(515);
        this.effect.clear();
        this.outTarget.unbindWrite();
        this.inTarget.unbindRead();
        for (IntSupplier object : this.auxAssets) {
            if (!(object instanceof RenderTarget)) continue;
            ((RenderTarget)((Object)object)).unbindRead();
        }
    }

    public EffectInstance getEffect() {
        return this.effect;
    }
}

