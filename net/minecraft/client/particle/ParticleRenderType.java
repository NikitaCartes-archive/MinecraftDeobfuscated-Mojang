/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

@Environment(value=EnvType.CLIENT)
public interface ParticleRenderType {
    public static final ParticleRenderType TERRAIN_SHEET = new ParticleRenderType(){

        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            Lighting.turnOff();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
            bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
        }

        public String toString() {
            return "TERRAIN_SHEET";
        }
    };
    public static final ParticleRenderType PARTICLE_SHEET_OPAQUE = new ParticleRenderType(){

        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            Lighting.turnOff();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            textureManager.bind(TextureAtlas.LOCATION_PARTICLES);
            bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
        }

        public String toString() {
            return "PARTICLE_SHEET_OPAQUE";
        }
    };
    public static final ParticleRenderType PARTICLE_SHEET_TRANSLUCENT = new ParticleRenderType(){

        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            Lighting.turnOff();
            RenderSystem.depthMask(false);
            textureManager.bind(TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.alphaFunc(516, 0.003921569f);
            bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
        }

        public String toString() {
            return "PARTICLE_SHEET_TRANSLUCENT";
        }
    };
    public static final ParticleRenderType PARTICLE_SHEET_LIT = new ParticleRenderType(){

        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            textureManager.bind(TextureAtlas.LOCATION_PARTICLES);
            Lighting.turnOff();
            bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
        }

        public String toString() {
            return "PARTICLE_SHEET_LIT";
        }
    };
    public static final ParticleRenderType CUSTOM = new ParticleRenderType(){

        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }

        @Override
        public void end(Tesselator tesselator) {
        }

        public String toString() {
            return "CUSTOM";
        }
    };
    public static final ParticleRenderType NO_RENDER = new ParticleRenderType(){

        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
        }

        @Override
        public void end(Tesselator tesselator) {
        }

        public String toString() {
            return "NO_RENDER";
        }
    };

    public void begin(BufferBuilder var1, TextureManager var2);

    public void end(Tesselator var1);
}

