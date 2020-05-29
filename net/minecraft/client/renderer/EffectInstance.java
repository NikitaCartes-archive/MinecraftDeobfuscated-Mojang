/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.shaders.Effect;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EffectInstance
implements Effect,
AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final AbstractUniform DUMMY_UNIFORM = new AbstractUniform();
    private static EffectInstance lastAppliedEffect;
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

    public EffectInstance(ResourceManager resourceManager, String string) throws IOException {
        ResourceLocation resourceLocation = new ResourceLocation("shaders/program/" + string + ".json");
        this.name = string;
        Resource resource = null;
        try {
            JsonArray jsonArray3;
            JsonArray jsonArray2;
            resource = resourceManager.getResource(resourceLocation);
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
                for (Object jsonElement2 : jsonArray2) {
                    try {
                        this.attributeNames.add(GsonHelper.convertToString((JsonElement)jsonElement2, "attribute"));
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
            this.blend = EffectInstance.parseBlendNode(GsonHelper.getAsJsonObject(jsonObject, "blend", null));
            this.vertexProgram = EffectInstance.getOrCreate(resourceManager, Program.Type.VERTEX, string2);
            this.fragmentProgram = EffectInstance.getOrCreate(resourceManager, Program.Type.FRAGMENT, string3);
            this.programId = ProgramManager.createProgram();
            ProgramManager.linkProgram(this);
            this.updateLocations();
            if (this.attributeNames != null) {
                for (String string4 : this.attributeNames) {
                    int l = Uniform.glGetAttribLocation(this.programId, string4);
                    this.attributes.add(l);
                }
            }
        } catch (Exception exception4) {
            ChainedJsonException chainedJsonException4 = ChainedJsonException.forException(exception4);
            chainedJsonException4.setFilenameAndFlush(resourceLocation.getPath());
            throw chainedJsonException4;
        } finally {
            IOUtils.closeQuietly((Closeable)resource);
        }
        this.markDirty();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Program getOrCreate(ResourceManager resourceManager, Program.Type type, String string) throws IOException {
        Program program = type.getPrograms().get(string);
        if (program == null) {
            ResourceLocation resourceLocation = new ResourceLocation("shaders/program/" + string + type.getExtension());
            Resource resource = resourceManager.getResource(resourceLocation);
            try {
                program = Program.compileShader(type, string, resource.getInputStream());
            } finally {
                IOUtils.closeQuietly((Closeable)resource);
            }
        }
        return program;
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
        lastAppliedEffect = null;
        for (int i = 0; i < this.samplerLocations.size(); ++i) {
            if (this.samplerMap.get(this.samplerNames.get(i)) == null) continue;
            GlStateManager._activeTexture(33984 + i);
            GlStateManager._bindTexture(0);
        }
    }

    public void apply() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        this.dirty = false;
        lastAppliedEffect = this;
        this.blend.apply();
        if (this.programId != lastProgramId) {
            ProgramManager.glUseProgram(this.programId);
            lastProgramId = this.programId;
        }
        for (int i = 0; i < this.samplerLocations.size(); ++i) {
            if (this.samplerMap.get(this.samplerNames.get(i)) == null) continue;
            RenderSystem.activeTexture(33984 + i);
            RenderSystem.enableTexture();
            Object object = this.samplerMap.get(this.samplerNames.get(i));
            int j = -1;
            if (object instanceof RenderTarget) {
                j = ((RenderTarget)object).colorTextureId;
            } else if (object instanceof AbstractTexture) {
                j = ((AbstractTexture)object).getId();
            } else if (object instanceof Integer) {
                j = (Integer)object;
            }
            if (j == -1) continue;
            RenderSystem.bindTexture(j);
            Uniform.uploadInteger(Uniform.glGetUniformLocation(this.programId, this.samplerNames.get(i)), i);
        }
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

    public AbstractUniform safeGetUniform(String string) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        Uniform uniform = this.getUniform(string);
        return uniform == null ? DUMMY_UNIFORM : uniform;
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
            this.samplerNames.remove(intList.getInt(i));
        }
        for (Uniform uniform : this.uniforms) {
            String string2 = uniform.getName();
            int k = Uniform.glGetUniformLocation(this.programId, string2);
            if (k == -1) {
                LOGGER.warn("Could not find uniform named {} in the specified shader program.", (Object)string2);
                continue;
            }
            this.uniformLocations.add(k);
            uniform.setLocation(k);
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
        if (this.samplerMap.containsKey(string)) {
            this.samplerMap.remove(string);
        }
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
    public int getId() {
        return this.programId;
    }

    static {
        lastProgramId = -1;
    }
}

