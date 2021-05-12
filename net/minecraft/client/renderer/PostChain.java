/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;

@Environment(value=EnvType.CLIENT)
public class PostChain
implements AutoCloseable {
    private static final String MAIN_RENDER_TARGET = "minecraft:main";
    private final RenderTarget screenTarget;
    private final ResourceManager resourceManager;
    private final String name;
    private final List<PostPass> passes = Lists.newArrayList();
    private final Map<String, RenderTarget> customRenderTargets = Maps.newHashMap();
    private final List<RenderTarget> fullSizedTargets = Lists.newArrayList();
    private Matrix4f shaderOrthoMatrix;
    private int screenWidth;
    private int screenHeight;
    private float time;
    private float lastStamp;

    public PostChain(TextureManager textureManager, ResourceManager resourceManager, RenderTarget renderTarget, ResourceLocation resourceLocation) throws IOException, JsonSyntaxException {
        this.resourceManager = resourceManager;
        this.screenTarget = renderTarget;
        this.time = 0.0f;
        this.lastStamp = 0.0f;
        this.screenWidth = renderTarget.viewWidth;
        this.screenHeight = renderTarget.viewHeight;
        this.name = resourceLocation.toString();
        this.updateOrthoMatrix();
        this.load(textureManager, resourceLocation);
    }

    private void load(TextureManager textureManager, ResourceLocation resourceLocation) throws IOException, JsonSyntaxException {
        Resource resource;
        block11: {
            resource = null;
            try {
                int i;
                JsonArray jsonArray;
                resource = this.resourceManager.getResource(resourceLocation);
                JsonObject jsonObject = GsonHelper.parse(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
                if (GsonHelper.isArrayNode(jsonObject, "targets")) {
                    jsonArray = jsonObject.getAsJsonArray("targets");
                    i = 0;
                    for (JsonElement jsonElement : jsonArray) {
                        try {
                            this.parseTargetNode(jsonElement);
                        } catch (Exception exception) {
                            ChainedJsonException chainedJsonException = ChainedJsonException.forException(exception);
                            chainedJsonException.prependJsonKey("targets[" + i + "]");
                            throw chainedJsonException;
                        }
                        ++i;
                    }
                }
                if (!GsonHelper.isArrayNode(jsonObject, "passes")) break block11;
                jsonArray = jsonObject.getAsJsonArray("passes");
                i = 0;
                for (JsonElement jsonElement : jsonArray) {
                    try {
                        this.parsePassNode(textureManager, jsonElement);
                    } catch (Exception exception) {
                        ChainedJsonException chainedJsonException = ChainedJsonException.forException(exception);
                        chainedJsonException.prependJsonKey("passes[" + i + "]");
                        throw chainedJsonException;
                    }
                    ++i;
                }
            } catch (Exception exception2) {
                try {
                    Object string = resource != null ? " (" + resource.getSourceName() + ")" : "";
                    ChainedJsonException chainedJsonException2 = ChainedJsonException.forException(exception2);
                    chainedJsonException2.setFilenameAndFlush(resourceLocation.getPath() + (String)string);
                    throw chainedJsonException2;
                } catch (Throwable throwable) {
                    IOUtils.closeQuietly(resource);
                    throw throwable;
                }
            }
        }
        IOUtils.closeQuietly((Closeable)resource);
    }

    private void parseTargetNode(JsonElement jsonElement) throws ChainedJsonException {
        if (GsonHelper.isStringValue(jsonElement)) {
            this.addTempTarget(jsonElement.getAsString(), this.screenWidth, this.screenHeight);
        } else {
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "target");
            String string = GsonHelper.getAsString(jsonObject, "name");
            int i = GsonHelper.getAsInt(jsonObject, "width", this.screenWidth);
            int j = GsonHelper.getAsInt(jsonObject, "height", this.screenHeight);
            if (this.customRenderTargets.containsKey(string)) {
                throw new ChainedJsonException(string + " is already defined");
            }
            this.addTempTarget(string, i, j);
        }
    }

    private void parsePassNode(TextureManager textureManager, JsonElement jsonElement) throws IOException {
        JsonArray jsonArray2;
        JsonObject jsonObject;
        block21: {
            jsonObject = GsonHelper.convertToJsonObject(jsonElement, "pass");
            String string = GsonHelper.getAsString(jsonObject, "name");
            String string2 = GsonHelper.getAsString(jsonObject, "intarget");
            String string3 = GsonHelper.getAsString(jsonObject, "outtarget");
            RenderTarget renderTarget = this.getRenderTarget(string2);
            RenderTarget renderTarget2 = this.getRenderTarget(string3);
            if (renderTarget == null) {
                throw new ChainedJsonException("Input target '" + string2 + "' does not exist");
            }
            if (renderTarget2 == null) {
                throw new ChainedJsonException("Output target '" + string3 + "' does not exist");
            }
            PostPass postPass = this.addPass(string, renderTarget, renderTarget2);
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "auxtargets", null);
            if (jsonArray == null) break block21;
            int i = 0;
            for (JsonElement jsonElement2 : jsonArray) {
                block20: {
                    try {
                        RenderTarget renderTarget3;
                        boolean bl;
                        String string4;
                        block22: {
                            String string6;
                            JsonObject jsonObject2 = GsonHelper.convertToJsonObject(jsonElement2, "auxtarget");
                            string4 = GsonHelper.getAsString(jsonObject2, "name");
                            String string5 = GsonHelper.getAsString(jsonObject2, "id");
                            if (string5.endsWith(":depth")) {
                                bl = true;
                                string6 = string5.substring(0, string5.lastIndexOf(58));
                            } else {
                                bl = false;
                                string6 = string5;
                            }
                            renderTarget3 = this.getRenderTarget(string6);
                            if (renderTarget3 != null) break block22;
                            if (bl) {
                                throw new ChainedJsonException("Render target '" + string6 + "' can't be used as depth buffer");
                            }
                            ResourceLocation resourceLocation = new ResourceLocation("textures/effect/" + string6 + ".png");
                            Resource resource = null;
                            try {
                                resource = this.resourceManager.getResource(resourceLocation);
                            } catch (FileNotFoundException fileNotFoundException) {
                                try {
                                    throw new ChainedJsonException("Render target or texture '" + string6 + "' does not exist");
                                } catch (Throwable throwable) {
                                    IOUtils.closeQuietly(resource);
                                    throw throwable;
                                }
                            }
                            IOUtils.closeQuietly((Closeable)resource);
                            RenderSystem.setShaderTexture(0, resourceLocation);
                            textureManager.bindForSetup(resourceLocation);
                            AbstractTexture abstractTexture = textureManager.getTexture(resourceLocation);
                            int j = GsonHelper.getAsInt(jsonObject2, "width");
                            int k = GsonHelper.getAsInt(jsonObject2, "height");
                            boolean bl2 = GsonHelper.getAsBoolean(jsonObject2, "bilinear");
                            if (bl2) {
                                RenderSystem.texParameter(3553, 10241, 9729);
                                RenderSystem.texParameter(3553, 10240, 9729);
                            } else {
                                RenderSystem.texParameter(3553, 10241, 9728);
                                RenderSystem.texParameter(3553, 10240, 9728);
                            }
                            postPass.addAuxAsset(string4, abstractTexture::getId, j, k);
                            break block20;
                        }
                        if (bl) {
                            postPass.addAuxAsset(string4, renderTarget3::getDepthTextureId, renderTarget3.width, renderTarget3.height);
                        } else {
                            postPass.addAuxAsset(string4, renderTarget3::getColorTextureId, renderTarget3.width, renderTarget3.height);
                        }
                    } catch (Exception exception) {
                        ChainedJsonException chainedJsonException = ChainedJsonException.forException(exception);
                        chainedJsonException.prependJsonKey("auxtargets[" + i + "]");
                        throw chainedJsonException;
                    }
                }
                ++i;
            }
        }
        if ((jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "uniforms", null)) != null) {
            int l = 0;
            for (JsonElement jsonElement3 : jsonArray2) {
                try {
                    this.parseUniformNode(jsonElement3);
                } catch (Exception exception2) {
                    ChainedJsonException chainedJsonException2 = ChainedJsonException.forException(exception2);
                    chainedJsonException2.prependJsonKey("uniforms[" + l + "]");
                    throw chainedJsonException2;
                }
                ++l;
            }
        }
    }

    private void parseUniformNode(JsonElement jsonElement) throws ChainedJsonException {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "uniform");
        String string = GsonHelper.getAsString(jsonObject, "name");
        Uniform uniform = this.passes.get(this.passes.size() - 1).getEffect().getUniform(string);
        if (uniform == null) {
            throw new ChainedJsonException("Uniform '" + string + "' does not exist");
        }
        float[] fs = new float[4];
        int i = 0;
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");
        for (JsonElement jsonElement2 : jsonArray) {
            try {
                fs[i] = GsonHelper.convertToFloat(jsonElement2, "value");
            } catch (Exception exception) {
                ChainedJsonException chainedJsonException = ChainedJsonException.forException(exception);
                chainedJsonException.prependJsonKey("values[" + i + "]");
                throw chainedJsonException;
            }
            ++i;
        }
        switch (i) {
            case 0: {
                break;
            }
            case 1: {
                uniform.set(fs[0]);
                break;
            }
            case 2: {
                uniform.set(fs[0], fs[1]);
                break;
            }
            case 3: {
                uniform.set(fs[0], fs[1], fs[2]);
                break;
            }
            case 4: {
                uniform.set(fs[0], fs[1], fs[2], fs[3]);
            }
        }
    }

    public RenderTarget getTempTarget(String string) {
        return this.customRenderTargets.get(string);
    }

    public void addTempTarget(String string, int i, int j) {
        TextureTarget renderTarget = new TextureTarget(i, j, true, Minecraft.ON_OSX);
        renderTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        this.customRenderTargets.put(string, renderTarget);
        if (i == this.screenWidth && j == this.screenHeight) {
            this.fullSizedTargets.add(renderTarget);
        }
    }

    @Override
    public void close() {
        for (RenderTarget renderTarget : this.customRenderTargets.values()) {
            renderTarget.destroyBuffers();
        }
        for (PostPass postPass : this.passes) {
            postPass.close();
        }
        this.passes.clear();
    }

    public PostPass addPass(String string, RenderTarget renderTarget, RenderTarget renderTarget2) throws IOException {
        PostPass postPass = new PostPass(this.resourceManager, string, renderTarget, renderTarget2);
        this.passes.add(this.passes.size(), postPass);
        return postPass;
    }

    private void updateOrthoMatrix() {
        this.shaderOrthoMatrix = Matrix4f.orthographic(0.0f, this.screenTarget.width, this.screenTarget.height, 0.0f, 0.1f, 1000.0f);
    }

    public void resize(int i, int j) {
        this.screenWidth = this.screenTarget.width;
        this.screenHeight = this.screenTarget.height;
        this.updateOrthoMatrix();
        for (PostPass postPass : this.passes) {
            postPass.setOrthoMatrix(this.shaderOrthoMatrix);
        }
        for (RenderTarget renderTarget : this.fullSizedTargets) {
            renderTarget.resize(i, j, Minecraft.ON_OSX);
        }
    }

    public void process(float f) {
        if (f < this.lastStamp) {
            this.time += 1.0f - this.lastStamp;
            this.time += f;
        } else {
            this.time += f - this.lastStamp;
        }
        this.lastStamp = f;
        while (this.time > 20.0f) {
            this.time -= 20.0f;
        }
        for (PostPass postPass : this.passes) {
            postPass.process(this.time / 20.0f);
        }
    }

    public final String getName() {
        return this.name;
    }

    private RenderTarget getRenderTarget(String string) {
        if (string == null) {
            return null;
        }
        if (string.equals(MAIN_RENDER_TARGET)) {
            return this.screenTarget;
        }
        return this.customRenderTargets.get(string);
    }
}

