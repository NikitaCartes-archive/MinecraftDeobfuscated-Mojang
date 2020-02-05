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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
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

@Environment(EnvType.CLIENT)
public class EffectInstance implements Effect, AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final AbstractUniform DUMMY_UNIFORM = new AbstractUniform();
	private static EffectInstance lastAppliedEffect;
	private static int lastProgramId = -1;
	private final Map<String, Object> samplerMap = Maps.<String, Object>newHashMap();
	private final List<String> samplerNames = Lists.<String>newArrayList();
	private final List<Integer> samplerLocations = Lists.<Integer>newArrayList();
	private final List<Uniform> uniforms = Lists.<Uniform>newArrayList();
	private final List<Integer> uniformLocations = Lists.<Integer>newArrayList();
	private final Map<String, Uniform> uniformMap = Maps.<String, Uniform>newHashMap();
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
			resource = resourceManager.getResource(resourceLocation);
			JsonObject jsonObject = GsonHelper.parse(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
			String string2 = GsonHelper.getAsString(jsonObject, "vertex");
			String string3 = GsonHelper.getAsString(jsonObject, "fragment");
			JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "samplers", null);
			if (jsonArray != null) {
				int i = 0;

				for (JsonElement jsonElement : jsonArray) {
					try {
						this.parseSamplerNode(jsonElement);
					} catch (Exception var24) {
						ChainedJsonException chainedJsonException = ChainedJsonException.forException(var24);
						chainedJsonException.prependJsonKey("samplers[" + i + "]");
						throw chainedJsonException;
					}

					i++;
				}
			}

			JsonArray jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "attributes", null);
			if (jsonArray2 != null) {
				int j = 0;
				this.attributes = Lists.<Integer>newArrayListWithCapacity(jsonArray2.size());
				this.attributeNames = Lists.<String>newArrayListWithCapacity(jsonArray2.size());

				for (JsonElement jsonElement2 : jsonArray2) {
					try {
						this.attributeNames.add(GsonHelper.convertToString(jsonElement2, "attribute"));
					} catch (Exception var23) {
						ChainedJsonException chainedJsonException2 = ChainedJsonException.forException(var23);
						chainedJsonException2.prependJsonKey("attributes[" + j + "]");
						throw chainedJsonException2;
					}

					j++;
				}
			} else {
				this.attributes = null;
				this.attributeNames = null;
			}

			JsonArray jsonArray3 = GsonHelper.getAsJsonArray(jsonObject, "uniforms", null);
			if (jsonArray3 != null) {
				int k = 0;

				for (JsonElement jsonElement3 : jsonArray3) {
					try {
						this.parseUniformNode(jsonElement3);
					} catch (Exception var22) {
						ChainedJsonException chainedJsonException3 = ChainedJsonException.forException(var22);
						chainedJsonException3.prependJsonKey("uniforms[" + k + "]");
						throw chainedJsonException3;
					}

					k++;
				}
			}

			this.blend = parseBlendNode(GsonHelper.getAsJsonObject(jsonObject, "blend", null));
			this.vertexProgram = getOrCreate(resourceManager, Program.Type.VERTEX, string2);
			this.fragmentProgram = getOrCreate(resourceManager, Program.Type.FRAGMENT, string3);
			this.programId = ProgramManager.createProgram();
			ProgramManager.linkProgram(this);
			this.updateLocations();
			if (this.attributeNames != null) {
				for (String string4 : this.attributeNames) {
					int l = Uniform.glGetAttribLocation(this.programId, string4);
					this.attributes.add(l);
				}
			}
		} catch (Exception var25) {
			ChainedJsonException chainedJsonException4 = ChainedJsonException.forException(var25);
			chainedJsonException4.setFilenameAndFlush(resourceLocation.getPath());
			throw chainedJsonException4;
		} finally {
			IOUtils.closeQuietly(resource);
		}

		this.markDirty();
	}

	public static Program getOrCreate(ResourceManager resourceManager, Program.Type type, String string) throws IOException {
		Program program = (Program)type.getPrograms().get(string);
		if (program == null) {
			ResourceLocation resourceLocation = new ResourceLocation("shaders/program/" + string + type.getExtension());
			Resource resource = resourceManager.getResource(resourceLocation);

			try {
				program = Program.compileShader(type, string, resource.getInputStream());
			} finally {
				IOUtils.closeQuietly(resource);
			}
		}

		return program;
	}

	public static BlendMode parseBlendNode(JsonObject jsonObject) {
		if (jsonObject == null) {
			return new BlendMode();
		} else {
			int i = 32774;
			int j = 1;
			int k = 0;
			int l = 1;
			int m = 0;
			boolean bl = true;
			boolean bl2 = false;
			if (GsonHelper.isStringValue(jsonObject, "func")) {
				i = BlendMode.stringToBlendFunc(jsonObject.get("func").getAsString());
				if (i != 32774) {
					bl = false;
				}
			}

			if (GsonHelper.isStringValue(jsonObject, "srcrgb")) {
				j = BlendMode.stringToBlendFactor(jsonObject.get("srcrgb").getAsString());
				if (j != 1) {
					bl = false;
				}
			}

			if (GsonHelper.isStringValue(jsonObject, "dstrgb")) {
				k = BlendMode.stringToBlendFactor(jsonObject.get("dstrgb").getAsString());
				if (k != 0) {
					bl = false;
				}
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
			} else {
				return bl2 ? new BlendMode(j, k, l, m, i) : new BlendMode(j, k, i);
			}
		}
	}

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

		for (int i = 0; i < this.samplerLocations.size(); i++) {
			if (this.samplerMap.get(this.samplerNames.get(i)) != null) {
				GlStateManager._activeTexture(33984 + i);
				GlStateManager._bindTexture(0);
			}
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

		for (int i = 0; i < this.samplerLocations.size(); i++) {
			if (this.samplerMap.get(this.samplerNames.get(i)) != null) {
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

				if (j != -1) {
					RenderSystem.bindTexture(j);
					Uniform.uploadInteger(Uniform.glGetUniformLocation(this.programId, (CharSequence)this.samplerNames.get(i)), i);
				}
			}
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
		return (Uniform)this.uniformMap.get(string);
	}

	public AbstractUniform safeGetUniform(String string) {
		RenderSystem.assertThread(RenderSystem::isOnGameThread);
		Uniform uniform = this.getUniform(string);
		return (AbstractUniform)(uniform == null ? DUMMY_UNIFORM : uniform);
	}

	private void updateLocations() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		int i = 0;

		for (int j = 0; i < this.samplerNames.size(); j++) {
			String string = (String)this.samplerNames.get(i);
			int k = Uniform.glGetUniformLocation(this.programId, string);
			if (k == -1) {
				LOGGER.warn("Shader {}could not find sampler named {} in the specified shader program.", this.name, string);
				this.samplerMap.remove(string);
				this.samplerNames.remove(j);
				j--;
			} else {
				this.samplerLocations.add(k);
			}

			i++;
		}

		for (Uniform uniform : this.uniforms) {
			String string = uniform.getName();
			int k = Uniform.glGetUniformLocation(this.programId, string);
			if (k == -1) {
				LOGGER.warn("Could not find uniform named {} in the specified shader program.", string);
			} else {
				this.uniformLocations.add(k);
				uniform.setLocation(k);
				this.uniformMap.put(string, uniform);
			}
		}
	}

	private void parseSamplerNode(JsonElement jsonElement) {
		JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "sampler");
		String string = GsonHelper.getAsString(jsonObject, "name");
		if (!GsonHelper.isStringValue(jsonObject, "file")) {
			this.samplerMap.put(string, null);
			this.samplerNames.add(string);
		} else {
			this.samplerNames.add(string);
		}
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
		} else {
			int k = 0;

			for (JsonElement jsonElement2 : jsonArray) {
				try {
					fs[k] = GsonHelper.convertToFloat(jsonElement2, "value");
				} catch (Exception var13) {
					ChainedJsonException chainedJsonException = ChainedJsonException.forException(var13);
					chainedJsonException.prependJsonKey("values[" + k + "]");
					throw chainedJsonException;
				}

				k++;
			}

			if (j > 1 && jsonArray.size() == 1) {
				while (k < j) {
					fs[k] = fs[0];
					k++;
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
}
