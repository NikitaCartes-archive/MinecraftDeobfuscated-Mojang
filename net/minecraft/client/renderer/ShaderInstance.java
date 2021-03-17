/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GlslPreprocessor;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ShaderInstance
implements Shader,
AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final AbstractUniform DUMMY_UNIFORM = new AbstractUniform();
    private static ShaderInstance lastAppliedShader;
    private static int lastProgramId;
    private final Map<String, Object> samplerMap = Maps.newHashMap();
    private final List<String> samplerNames = Lists.newArrayList();
    private final List<Integer> samplerLocations = Lists.newArrayList();
    private final List<Uniform> uniforms = Lists.newArrayList();
    private final List<Integer> uniformLocations = Lists.newArrayList();
    private final Map<String, Uniform> uniformMap = Maps.newHashMap();
    private final int programId;
    private final String name;
    private boolean dirty;
    private final BlendMode blend;
    private final List<Integer> attributes;
    private final List<String> attributeNames;
    private final Program vertexProgram;
    private final Program fragmentProgram;
    private final VertexFormat vertexFormat;
    @Nullable
    public final Uniform MODEL_VIEW_MATRIX;
    @Nullable
    public final Uniform PROJECTION_MATRIX;
    @Nullable
    public final Uniform TEXTURE_MATRIX;
    @Nullable
    public final Uniform SCREEN_SIZE;
    @Nullable
    public final Uniform COLOR_MODULATOR;
    @Nullable
    public final Uniform LIGHT0_DIRECTION;
    @Nullable
    public final Uniform LIGHT1_DIRECTION;
    @Nullable
    public final Uniform FOG_START;
    @Nullable
    public final Uniform FOG_END;
    @Nullable
    public final Uniform FOG_COLOR;
    @Nullable
    public final Uniform LINE_WIDTH;
    @Nullable
    public final Uniform GAME_TIME;
    @Nullable
    public final Uniform CHUNK_OFFSET;

    public ShaderInstance(ResourceProvider resourceProvider, String string, VertexFormat vertexFormat) throws IOException {
        this.name = string;
        this.vertexFormat = vertexFormat;
        ResourceLocation resourceLocation = new ResourceLocation("shaders/core/" + string + ".json");
        Resource resource = null;
        try {
            JsonArray jsonArray3;
            JsonArray jsonArray2;
            resource = resourceProvider.getResource(resourceLocation);
            JsonObject jsonObject = GsonHelper.parse(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            String string2 = GsonHelper.getAsString(jsonObject, "vertex");
            String string3 = GsonHelper.getAsString(jsonObject, "fragment");
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "samplers", null);
            if (jsonArray != null) {
                int i = 0;
                for (Object jsonElement : jsonArray) {
                    try {
                        this.parseSamplerNode((JsonElement)jsonElement);
                    } catch (Exception exception) {
                        ChainedJsonException chainedJsonException = ChainedJsonException.forException(exception);
                        chainedJsonException.prependJsonKey("samplers[" + i + "]");
                        throw chainedJsonException;
                    }
                    ++i;
                }
            }
            if ((jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "attributes", null)) != null) {
                int j = 0;
                this.attributes = Lists.newArrayListWithCapacity(jsonArray2.size());
                this.attributeNames = Lists.newArrayListWithCapacity(jsonArray2.size());
                for (JsonElement jsonElement2 : jsonArray2) {
                    try {
                        this.attributeNames.add(GsonHelper.convertToString(jsonElement2, "attribute"));
                    } catch (Exception exception2) {
                        ChainedJsonException chainedJsonException2 = ChainedJsonException.forException(exception2);
                        chainedJsonException2.prependJsonKey("attributes[" + j + "]");
                        throw chainedJsonException2;
                    }
                    ++j;
                }
            } else {
                this.attributes = null;
                this.attributeNames = null;
            }
            if ((jsonArray3 = GsonHelper.getAsJsonArray(jsonObject, "uniforms", null)) != null) {
                int k = 0;
                for (JsonElement jsonElement3 : jsonArray3) {
                    try {
                        this.parseUniformNode(jsonElement3);
                    } catch (Exception exception3) {
                        ChainedJsonException chainedJsonException3 = ChainedJsonException.forException(exception3);
                        chainedJsonException3.prependJsonKey("uniforms[" + k + "]");
                        throw chainedJsonException3;
                    }
                    ++k;
                }
            }
            this.blend = ShaderInstance.parseBlendNode(GsonHelper.getAsJsonObject(jsonObject, "blend", null));
            this.vertexProgram = ShaderInstance.getOrCreate(resourceProvider, Program.Type.VERTEX, string2);
            this.fragmentProgram = ShaderInstance.getOrCreate(resourceProvider, Program.Type.FRAGMENT, string3);
            this.programId = ProgramManager.createProgram();
            if (this.attributeNames != null) {
                int k = 0;
                for (String string4 : vertexFormat.getElementAttributeNames()) {
                    Uniform.glBindAttribLocation(this.programId, k, string4);
                    this.attributes.add(k);
                    ++k;
                }
            }
            ProgramManager.linkShader(this);
            this.updateLocations();
        } catch (Exception exception4) {
            ChainedJsonException chainedJsonException4 = ChainedJsonException.forException(exception4);
            chainedJsonException4.setFilenameAndFlush(resourceLocation.getPath());
            throw chainedJsonException4;
        } finally {
            IOUtils.closeQuietly((Closeable)resource);
        }
        this.markDirty();
        this.MODEL_VIEW_MATRIX = this.getUniform("ModelViewMat");
        this.PROJECTION_MATRIX = this.getUniform("ProjMat");
        this.TEXTURE_MATRIX = this.getUniform("TextureMat");
        this.SCREEN_SIZE = this.getUniform("ScreenSize");
        this.COLOR_MODULATOR = this.getUniform("ColorModulator");
        this.LIGHT0_DIRECTION = this.getUniform("Light0_Direction");
        this.LIGHT1_DIRECTION = this.getUniform("Light1_Direction");
        this.FOG_START = this.getUniform("FogStart");
        this.FOG_END = this.getUniform("FogEnd");
        this.FOG_COLOR = this.getUniform("FogColor");
        this.LINE_WIDTH = this.getUniform("LineWidth");
        this.GAME_TIME = this.getUniform("GameTime");
        this.CHUNK_OFFSET = this.getUniform("ChunkOffset");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Program getOrCreate(final ResourceProvider resourceProvider, Program.Type type, String string) throws IOException {
        Program program2;
        Program program = type.getPrograms().get(string);
        if (program == null) {
            String string2 = "shaders/core/" + string + type.getExtension();
            ResourceLocation resourceLocation = new ResourceLocation(string2);
            Resource resource = resourceProvider.getResource(resourceLocation);
            final String string3 = FileUtil.getFullResourcePath(string2);
            try {
                program2 = Program.compileShader(type, string, resource.getInputStream(), resource.getSourceName(), new GlslPreprocessor(){
                    private final Set<String> importedPaths = Sets.newHashSet();

                    /*
                     * Enabled aggressive block sorting
                     * Enabled unnecessary exception pruning
                     * Enabled aggressive exception aggregation
                     */
                    @Override
                    public String applyImport(boolean bl, String string) {
                        string = FileUtil.normalizeResourcePath((bl ? string3 : "shaders/include/") + string);
                        if (!this.importedPaths.add(string)) {
                            return null;
                        }
                        ResourceLocation resourceLocation = new ResourceLocation(string);
                        try (Resource resource = resourceProvider.getResource(resourceLocation);){
                            String string2 = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                            return string2;
                        } catch (IOException iOException) {
                            LOGGER.error("Could not open GLSL import {}: {}", (Object)string, (Object)iOException.getMessage());
                            return "#error " + iOException.getMessage();
                        }
                    }
                });
            } finally {
                IOUtils.closeQuietly((Closeable)resource);
            }
        } else {
            program2 = program;
        }
        return program2;
    }

    public static BlendMode parseBlendNode(JsonObject jsonObject) {
        if (jsonObject == null) {
            return new BlendMode();
        }
        int i = 32774;
        int j = 1;
        int k = 0;
        int l = 1;
        int m = 0;
        boolean bl = true;
        boolean bl2 = false;
        if (GsonHelper.isStringValue(jsonObject, "func") && (i = BlendMode.stringToBlendFunc(jsonObject.get("func").getAsString())) != 32774) {
            bl = false;
        }
        if (GsonHelper.isStringValue(jsonObject, "srcrgb") && (j = BlendMode.stringToBlendFactor(jsonObject.get("srcrgb").getAsString())) != 1) {
            bl = false;
        }
        if (GsonHelper.isStringValue(jsonObject, "dstrgb") && (k = BlendMode.stringToBlendFactor(jsonObject.get("dstrgb").getAsString())) != 0) {
            bl = false;
        }
        if (GsonHelper.isStringValue(jsonObject, "srcalpha")) {
            l = BlendMode.stringToBlendFactor(jsonObject.get("srcalpha").getAsString());
            if (l != 1) {
                bl = false;
            }
            bl2 = true;
        }
        if (GsonHelper.isStringValue(jsonObject, "dstalpha")) {
            m = BlendMode.stringToBlendFactor(jsonObject.get("dstalpha").getAsString());
            if (m != 0) {
                bl = false;
            }
            bl2 = true;
        }
        if (bl) {
            return new BlendMode();
        }
        if (bl2) {
            return new BlendMode(j, k, l, m, i);
        }
        return new BlendMode(j, k, i);
    }

    @Override
    public void close() {
        for (Uniform uniform : this.uniforms) {
            uniform.close();
        }
        ProgramManager.releaseProgram(this);
    }

    public void clear() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        ProgramManager.glUseProgram(0);
        lastProgramId = -1;
        lastAppliedShader = null;
        int i = GlStateManager._getActiveTexture();
        for (int j = 0; j < this.samplerLocations.size(); ++j) {
            if (this.samplerMap.get(this.samplerNames.get(j)) == null) continue;
            GlStateManager._activeTexture(33984 + j);
            GlStateManager._bindTexture(0);
        }
        GlStateManager._activeTexture(i);
    }

    public void apply() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        this.dirty = false;
        lastAppliedShader = this;
        this.blend.apply();
        if (this.programId != lastProgramId) {
            ProgramManager.glUseProgram(this.programId);
            lastProgramId = this.programId;
        }
        int i = GlStateManager._getActiveTexture();
        for (int j = 0; j < this.samplerLocations.size(); ++j) {
            String string = this.samplerNames.get(j);
            if (this.samplerMap.get(string) == null) continue;
            int k = Uniform.glGetUniformLocation(this.programId, string);
            Uniform.uploadInteger(k, j);
            RenderSystem.activeTexture(33984 + j);
            RenderSystem.enableTexture();
            Object object = this.samplerMap.get(string);
            int l = -1;
            if (object instanceof RenderTarget) {
                l = ((RenderTarget)object).getColorTextureId();
            } else if (object instanceof AbstractTexture) {
                l = ((AbstractTexture)object).getId();
            } else if (object instanceof Integer) {
                l = (Integer)object;
            }
            if (l == -1) continue;
            RenderSystem.bindTexture(l);
        }
        GlStateManager._activeTexture(i);
        for (Uniform uniform : this.uniforms) {
            uniform.upload();
        }
    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

    @Nullable
    public Uniform getUniform(String string) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return this.uniformMap.get(string);
    }

    private void updateLocations() {
        int i;
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        IntArrayList intList = new IntArrayList();
        for (i = 0; i < this.samplerNames.size(); ++i) {
            String string = this.samplerNames.get(i);
            int j = Uniform.glGetUniformLocation(this.programId, string);
            if (j == -1) {
                LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", (Object)this.name, (Object)string);
                this.samplerMap.remove(string);
                intList.add(i);
                continue;
            }
            this.samplerLocations.add(j);
        }
        for (i = intList.size() - 1; i >= 0; --i) {
            int k = intList.getInt(i);
            this.samplerNames.remove(k);
        }
        for (Uniform uniform : this.uniforms) {
            String string2 = uniform.getName();
            int l = Uniform.glGetUniformLocation(this.programId, string2);
            if (l == -1) {
                LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", (Object)this.name, (Object)string2);
                continue;
            }
            this.uniformLocations.add(l);
            uniform.setLocation(l);
            this.uniformMap.put(string2, uniform);
        }
    }

    private void parseSamplerNode(JsonElement jsonElement) {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "sampler");
        String string = GsonHelper.getAsString(jsonObject, "name");
        if (!GsonHelper.isStringValue(jsonObject, "file")) {
            this.samplerMap.put(string, null);
            this.samplerNames.add(string);
            return;
        }
        this.samplerNames.add(string);
    }

    public void setSampler(String string, Object object) {
        this.samplerMap.put(string, object);
        this.markDirty();
    }

    private void parseUniformNode(JsonElement jsonElement) throws ChainedJsonException {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "uniform");
        String string = GsonHelper.getAsString(jsonObject, "name");
        int i = Uniform.getTypeFromString(GsonHelper.getAsString(jsonObject, "type"));
        int j = GsonHelper.getAsInt(jsonObject, "count");
        float[] fs = new float[Math.max(j, 16)];
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");
        if (jsonArray.size() != j && jsonArray.size() > 1) {
            throw new ChainedJsonException("Invalid amount of values specified (expected " + j + ", found " + jsonArray.size() + ")");
        }
        int k = 0;
        for (JsonElement jsonElement2 : jsonArray) {
            try {
                fs[k] = GsonHelper.convertToFloat(jsonElement2, "value");
            } catch (Exception exception) {
                ChainedJsonException chainedJsonException = ChainedJsonException.forException(exception);
                chainedJsonException.prependJsonKey("values[" + k + "]");
                throw chainedJsonException;
            }
            ++k;
        }
        if (j > 1 && jsonArray.size() == 1) {
            while (k < j) {
                fs[k] = fs[0];
                ++k;
            }
        }
        int l = j > 1 && j <= 4 && i < 8 ? j - 1 : 0;
        Uniform uniform = new Uniform(string, i + l, j, this);
        if (i <= 3) {
            uniform.setSafe((int)fs[0], (int)fs[1], (int)fs[2], (int)fs[3]);
        } else if (i <= 7) {
            uniform.setSafe(fs[0], fs[1], fs[2], fs[3]);
        } else {
            uniform.set(fs);
        }
        this.uniforms.add(uniform);
    }

    @Override
    public Program getVertexProgram() {
        return this.vertexProgram;
    }

    @Override
    public Program getFragmentProgram() {
        return this.fragmentProgram;
    }

    @Override
    public void attachToProgram() {
        this.fragmentProgram.attachToShader(this);
        this.vertexProgram.attachToShader(this);
    }

    @Override
    public int getId() {
        return this.programId;
    }

    static {
        lastProgramId = -1;
    }
}

