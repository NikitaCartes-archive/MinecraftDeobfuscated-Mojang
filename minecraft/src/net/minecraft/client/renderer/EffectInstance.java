package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.shaders.Effect;
import com.mojang.blaze3d.shaders.EffectProgram;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class EffectInstance implements Effect, AutoCloseable {
	private static final String EFFECT_SHADER_PATH = "shaders/program/";
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final AbstractUniform DUMMY_UNIFORM = new AbstractUniform();
	private static final boolean ALWAYS_REAPPLY = true;
	private static EffectInstance lastAppliedEffect;
	private static int lastProgramId = -1;
	private final Map<String, IntSupplier> samplerMap = Maps.<String, IntSupplier>newHashMap();
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
	private final EffectProgram vertexProgram;
	private final EffectProgram fragmentProgram;

	public EffectInstance(ResourceProvider resourceProvider, String string) throws IOException {
		ResourceLocation resourceLocation = new ResourceLocation("shaders/program/" + string + ".json");
		this.name = string;
		Resource resource = resourceProvider.getResourceOrThrow(resourceLocation);

		try {
			Reader reader = resource.openAsReader();

			try {
				JsonObject jsonObject = GsonHelper.parse(reader);
				String string2 = GsonHelper.getAsString(jsonObject, "vertex");
				String string3 = GsonHelper.getAsString(jsonObject, "fragment");
				JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "samplers", null);
				if (jsonArray != null) {
					int i = 0;

					for (JsonElement jsonElement : jsonArray) {
						try {
							this.parseSamplerNode(jsonElement);
						} catch (Exception var20) {
							ChainedJsonException chainedJsonException = ChainedJsonException.forException(var20);
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
						} catch (Exception var19) {
							ChainedJsonException chainedJsonException2 = ChainedJsonException.forException(var19);
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
						} catch (Exception var18) {
							ChainedJsonException chainedJsonException3 = ChainedJsonException.forException(var18);
							chainedJsonException3.prependJsonKey("uniforms[" + k + "]");
							throw chainedJsonException3;
						}

						k++;
					}
				}

				this.blend = parseBlendNode(GsonHelper.getAsJsonObject(jsonObject, "blend", null));
				this.vertexProgram = getOrCreate(resourceProvider, Program.Type.VERTEX, string2);
				this.fragmentProgram = getOrCreate(resourceProvider, Program.Type.FRAGMENT, string3);
				this.programId = ProgramManager.createProgram();
				ProgramManager.linkShader(this);
				this.updateLocations();
				if (this.attributeNames != null) {
					for (String string4 : this.attributeNames) {
						int l = Uniform.glGetAttribLocation(this.programId, string4);
						this.attributes.add(l);
					}
				}
			} catch (Throwable var21) {
				if (reader != null) {
					try {
						reader.close();
					} catch (Throwable var17) {
						var21.addSuppressed(var17);
					}
				}

				throw var21;
			}

			if (reader != null) {
				reader.close();
			}
		} catch (Exception var22) {
			ChainedJsonException chainedJsonException4 = ChainedJsonException.forException(var22);
			chainedJsonException4.setFilenameAndFlush(resourceLocation.getPath() + " (" + resource.sourcePackId() + ")");
			throw chainedJsonException4;
		}

		this.markDirty();
	}

	public static EffectProgram getOrCreate(ResourceProvider resourceProvider, Program.Type type, String string) throws IOException {
		Program program = (Program)type.getPrograms().get(string);
		if (program != null && !(program instanceof EffectProgram)) {
			throw new InvalidClassException("Program is not of type EffectProgram");
		} else {
			EffectProgram effectProgram;
			if (program == null) {
				ResourceLocation resourceLocation = new ResourceLocation("shaders/program/" + string + type.getExtension());
				Resource resource = resourceProvider.getResourceOrThrow(resourceLocation);
				InputStream inputStream = resource.open();

				try {
					effectProgram = EffectProgram.compileShader(type, string, inputStream, resource.sourcePackId());
				} catch (Throwable var11) {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (Throwable var10) {
							var11.addSuppressed(var10);
						}
					}

					throw var11;
				}

				if (inputStream != null) {
					inputStream.close();
				}
			} else {
				effectProgram = (EffectProgram)program;
			}

			return effectProgram;
		}
	}

	public static BlendMode parseBlendNode(@Nullable JsonObject jsonObject) {
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
		RenderSystem.assertOnRenderThread();
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
		this.dirty = false;
		lastAppliedEffect = this;
		this.blend.apply();
		if (this.programId != lastProgramId) {
			ProgramManager.glUseProgram(this.programId);
			lastProgramId = this.programId;
		}

		for (int i = 0; i < this.samplerLocations.size(); i++) {
			String string = (String)this.samplerNames.get(i);
			IntSupplier intSupplier = (IntSupplier)this.samplerMap.get(string);
			if (intSupplier != null) {
				RenderSystem.activeTexture(33984 + i);
				int j = intSupplier.getAsInt();
				if (j != -1) {
					RenderSystem.bindTexture(j);
					Uniform.uploadInteger((Integer)this.samplerLocations.get(i), i);
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
		RenderSystem.assertOnRenderThread();
		return (Uniform)this.uniformMap.get(string);
	}

	public AbstractUniform safeGetUniform(String string) {
		Uniform uniform = this.getUniform(string);
		return (AbstractUniform)(uniform == null ? DUMMY_UNIFORM : uniform);
	}

	private void updateLocations() {
		RenderSystem.assertOnRenderThread();
		IntList intList = new IntArrayList();

		for (int i = 0; i < this.samplerNames.size(); i++) {
			String string = (String)this.samplerNames.get(i);
			int j = Uniform.glGetUniformLocation(this.programId, string);
			if (j == -1) {
				LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", this.name, string);
				this.samplerMap.remove(string);
				intList.add(i);
			} else {
				this.samplerLocations.add(j);
			}
		}

		for (int ix = intList.size() - 1; ix >= 0; ix--) {
			this.samplerNames.remove(intList.getInt(ix));
		}

		for (Uniform uniform : this.uniforms) {
			String string2 = uniform.getName();
			int k = Uniform.glGetUniformLocation(this.programId, string2);
			if (k == -1) {
				LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", this.name, string2);
			} else {
				this.uniformLocations.add(k);
				uniform.setLocation(k);
				this.uniformMap.put(string2, uniform);
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

	public void setSampler(String string, IntSupplier intSupplier) {
		if (this.samplerMap.containsKey(string)) {
			this.samplerMap.remove(string);
		}

		this.samplerMap.put(string, intSupplier);
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
	public void attachToProgram() {
		this.fragmentProgram.attachToEffect(this);
		this.vertexProgram.attachToEffect(this);
	}

	public String getName() {
		return this.name;
	}

	@Override
	public int getId() {
		return this.programId;
	}
}
