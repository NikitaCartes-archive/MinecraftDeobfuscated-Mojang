package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.CompiledShader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ShaderManager extends SimplePreparableReloadListener<ShaderManager.Configs> implements AutoCloseable {
	static final Logger LOGGER = LogUtils.getLogger();
	public static final String SHADER_PATH = "shaders";
	public static final String SHADER_INCLUDE_PATH = "shaders/include/";
	private static final FileToIdConverter PROGRAM_ID_CONVERTER = FileToIdConverter.json("shaders");
	private static final FileToIdConverter POST_CHAIN_ID_CONVERTER = FileToIdConverter.json("post_effect");
	public static final int MAX_LOG_LENGTH = 32768;
	final TextureManager textureManager;
	private final Consumer<Exception> recoveryHandler;
	private ShaderManager.CompilationCache compilationCache = new ShaderManager.CompilationCache(ShaderManager.Configs.EMPTY);

	public ShaderManager(TextureManager textureManager, Consumer<Exception> consumer) {
		this.textureManager = textureManager;
		this.recoveryHandler = consumer;
	}

	protected ShaderManager.Configs prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		Builder<ResourceLocation, ShaderProgramConfig> builder = ImmutableMap.builder();
		Builder<ShaderManager.ShaderSourceKey, String> builder2 = ImmutableMap.builder();
		Map<ResourceLocation, Resource> map = resourceManager.listResources("shaders", resourceLocation -> isProgram(resourceLocation) || isShader(resourceLocation));

		for (Entry<ResourceLocation, Resource> entry : map.entrySet()) {
			ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
			CompiledShader.Type type = CompiledShader.Type.byLocation(resourceLocation);
			if (type != null) {
				loadShader(resourceLocation, (Resource)entry.getValue(), type, map, builder2);
			} else if (isProgram(resourceLocation)) {
				loadProgram(resourceLocation, (Resource)entry.getValue(), builder);
			}
		}

		Builder<ResourceLocation, PostChainConfig> builder3 = ImmutableMap.builder();

		for (Entry<ResourceLocation, Resource> entry2 : POST_CHAIN_ID_CONVERTER.listMatchingResources(resourceManager).entrySet()) {
			loadPostChain((ResourceLocation)entry2.getKey(), (Resource)entry2.getValue(), builder3);
		}

		return new ShaderManager.Configs(builder.build(), builder2.build(), builder3.build());
	}

	private static void loadShader(
		ResourceLocation resourceLocation,
		Resource resource,
		CompiledShader.Type type,
		Map<ResourceLocation, Resource> map,
		Builder<ShaderManager.ShaderSourceKey, String> builder
	) {
		ResourceLocation resourceLocation2 = type.idConverter().fileToId(resourceLocation);
		GlslPreprocessor glslPreprocessor = createPreprocessor(map, resourceLocation);

		try {
			Reader reader = resource.openAsReader();

			try {
				String string = IOUtils.toString(reader);
				builder.put(new ShaderManager.ShaderSourceKey(resourceLocation2, type), String.join("", glslPreprocessor.process(string)));
			} catch (Throwable var11) {
				if (reader != null) {
					try {
						reader.close();
					} catch (Throwable var10) {
						var11.addSuppressed(var10);
					}
				}

				throw var11;
			}

			if (reader != null) {
				reader.close();
			}
		} catch (IOException var12) {
			LOGGER.error("Failed to load shader source at {}", resourceLocation, var12);
		}
	}

	private static GlslPreprocessor createPreprocessor(Map<ResourceLocation, Resource> map, ResourceLocation resourceLocation) {
		final ResourceLocation resourceLocation2 = resourceLocation.withPath(FileUtil::getFullResourcePath);
		return new GlslPreprocessor() {
			private final Set<ResourceLocation> importedLocations = new ObjectArraySet<>();

			@Override
			public String applyImport(boolean bl, String string) {
				ResourceLocation resourceLocation;
				try {
					if (bl) {
						resourceLocation = resourceLocation2.withPath((UnaryOperator<String>)(string2 -> FileUtil.normalizeResourcePath(string2 + string)));
					} else {
						resourceLocation = ResourceLocation.parse(string).withPrefix("shaders/include/");
					}
				} catch (ResourceLocationException var8) {
					ShaderManager.LOGGER.error("Malformed GLSL import {}: {}", string, var8.getMessage());
					return "#error " + var8.getMessage();
				}

				if (!this.importedLocations.add(resourceLocation)) {
					return null;
				} else {
					try {
						Reader reader = ((Resource)map.get(resourceLocation)).openAsReader();

						String var5;
						try {
							var5 = IOUtils.toString(reader);
						} catch (Throwable var9) {
							if (reader != null) {
								try {
									reader.close();
								} catch (Throwable var7) {
									var9.addSuppressed(var7);
								}
							}

							throw var9;
						}

						if (reader != null) {
							reader.close();
						}

						return var5;
					} catch (IOException var10) {
						ShaderManager.LOGGER.error("Could not open GLSL import {}: {}", resourceLocation, var10.getMessage());
						return "#error " + var10.getMessage();
					}
				}
			}
		};
	}

	private static void loadProgram(ResourceLocation resourceLocation, Resource resource, Builder<ResourceLocation, ShaderProgramConfig> builder) {
		ResourceLocation resourceLocation2 = PROGRAM_ID_CONVERTER.fileToId(resourceLocation);

		try {
			Reader reader = resource.openAsReader();

			try {
				JsonElement jsonElement = JsonParser.parseReader(reader);
				ShaderProgramConfig shaderProgramConfig = ShaderProgramConfig.CODEC.parse(JsonOps.INSTANCE, jsonElement).getOrThrow(JsonSyntaxException::new);
				builder.put(resourceLocation2, shaderProgramConfig);
			} catch (Throwable var8) {
				if (reader != null) {
					try {
						reader.close();
					} catch (Throwable var7) {
						var8.addSuppressed(var7);
					}
				}

				throw var8;
			}

			if (reader != null) {
				reader.close();
			}
		} catch (JsonParseException | IOException var9) {
			LOGGER.error("Failed to parse shader config at {}", resourceLocation, var9);
		}
	}

	private static void loadPostChain(ResourceLocation resourceLocation, Resource resource, Builder<ResourceLocation, PostChainConfig> builder) {
		ResourceLocation resourceLocation2 = POST_CHAIN_ID_CONVERTER.fileToId(resourceLocation);

		try {
			Reader reader = resource.openAsReader();

			try {
				JsonElement jsonElement = JsonParser.parseReader(reader);
				builder.put(resourceLocation2, PostChainConfig.CODEC.parse(JsonOps.INSTANCE, jsonElement).getOrThrow(JsonSyntaxException::new));
			} catch (Throwable var8) {
				if (reader != null) {
					try {
						reader.close();
					} catch (Throwable var7) {
						var8.addSuppressed(var7);
					}
				}

				throw var8;
			}

			if (reader != null) {
				reader.close();
			}
		} catch (JsonParseException | IOException var9) {
			LOGGER.error("Failed to parse post chain at {}", resourceLocation, var9);
		}
	}

	private static boolean isProgram(ResourceLocation resourceLocation) {
		return resourceLocation.getPath().endsWith(".json");
	}

	private static boolean isShader(ResourceLocation resourceLocation) {
		return CompiledShader.Type.byLocation(resourceLocation) != null || resourceLocation.getPath().endsWith(".glsl");
	}

	protected void apply(ShaderManager.Configs configs, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		ShaderManager.CompilationCache compilationCache = new ShaderManager.CompilationCache(configs);
		Map<ShaderProgram, ShaderManager.CompilationException> map = new HashMap();

		for (ShaderProgram shaderProgram : CoreShaders.getProgramsToPreload()) {
			try {
				compilationCache.programs.put(shaderProgram, Optional.of(compilationCache.compileProgram(shaderProgram)));
			} catch (ShaderManager.CompilationException var9) {
				map.put(shaderProgram, var9);
			}
		}

		if (!map.isEmpty()) {
			compilationCache.close();
			throw new RuntimeException(
				"Failed to load required shader programs:\n"
					+ (String)map.entrySet()
						.stream()
						.map(entry -> " - " + entry.getKey() + ": " + ((ShaderManager.CompilationException)entry.getValue()).getMessage())
						.collect(Collectors.joining("\n"))
			);
		} else {
			this.compilationCache.close();
			this.compilationCache = compilationCache;
		}
	}

	@Override
	public String getName() {
		return "Shader Loader";
	}

	public void preloadForStartup(ResourceProvider resourceProvider, ShaderProgram... shaderPrograms) throws IOException, ShaderManager.CompilationException {
		for (ShaderProgram shaderProgram : shaderPrograms) {
			Resource resource = resourceProvider.getResourceOrThrow(PROGRAM_ID_CONVERTER.idToFile(shaderProgram.configId()));
			Reader reader = resource.openAsReader();

			try {
				JsonElement jsonElement = JsonParser.parseReader(reader);
				ShaderProgramConfig shaderProgramConfig = ShaderProgramConfig.CODEC.parse(JsonOps.INSTANCE, jsonElement).getOrThrow(JsonSyntaxException::new);
				ShaderDefines shaderDefines = shaderProgramConfig.defines().withOverrides(shaderProgram.defines());
				CompiledShader compiledShader = this.preloadShader(resourceProvider, shaderProgramConfig.vertex(), CompiledShader.Type.VERTEX, shaderDefines);
				CompiledShader compiledShader2 = this.preloadShader(resourceProvider, shaderProgramConfig.fragment(), CompiledShader.Type.FRAGMENT, shaderDefines);
				CompiledShaderProgram compiledShaderProgram = linkProgram(shaderProgram, shaderProgramConfig, compiledShader, compiledShader2);
				this.compilationCache.programs.put(shaderProgram, Optional.of(compiledShaderProgram));
			} catch (Throwable var16) {
				if (reader != null) {
					try {
						reader.close();
					} catch (Throwable var15) {
						var16.addSuppressed(var15);
					}
				}

				throw var16;
			}

			if (reader != null) {
				reader.close();
			}
		}
	}

	private CompiledShader preloadShader(
		ResourceProvider resourceProvider, ResourceLocation resourceLocation, CompiledShader.Type type, ShaderDefines shaderDefines
	) throws IOException, ShaderManager.CompilationException {
		ResourceLocation resourceLocation2 = type.idConverter().idToFile(resourceLocation);
		Reader reader = resourceProvider.getResourceOrThrow(resourceLocation2).openAsReader();

		CompiledShader var10;
		try {
			String string = IOUtils.toString(reader);
			String string2 = GlslPreprocessor.injectDefines(string, shaderDefines);
			CompiledShader compiledShader = CompiledShader.compile(resourceLocation, type, string2);
			this.compilationCache.shaders.put(new ShaderManager.ShaderCompilationKey(resourceLocation, type, shaderDefines), compiledShader);
			var10 = compiledShader;
		} catch (Throwable var12) {
			if (reader != null) {
				try {
					reader.close();
				} catch (Throwable var11) {
					var12.addSuppressed(var11);
				}
			}

			throw var12;
		}

		if (reader != null) {
			reader.close();
		}

		return var10;
	}

	@Nullable
	public CompiledShaderProgram getProgram(ShaderProgram shaderProgram) {
		try {
			return this.compilationCache.getOrCompileProgram(shaderProgram);
		} catch (ShaderManager.CompilationException var3) {
			LOGGER.error("Failed to load shader program: {}", shaderProgram, var3);
			this.recoveryHandler.accept(var3);
			return null;
		}
	}

	static CompiledShaderProgram linkProgram(
		ShaderProgram shaderProgram, ShaderProgramConfig shaderProgramConfig, CompiledShader compiledShader, CompiledShader compiledShader2
	) throws ShaderManager.CompilationException {
		CompiledShaderProgram compiledShaderProgram = CompiledShaderProgram.link(compiledShader, compiledShader2, shaderProgram.vertexFormat());
		compiledShaderProgram.setupUniforms(shaderProgramConfig.uniforms(), shaderProgramConfig.samplers());
		return compiledShaderProgram;
	}

	@Nullable
	public PostChain getPostChain(ResourceLocation resourceLocation, Set<ResourceLocation> set) {
		try {
			return this.compilationCache.getOrLoadPostChain(resourceLocation, set);
		} catch (ShaderManager.CompilationException var4) {
			LOGGER.error("Failed to load post chain: {}", resourceLocation, var4);
			this.recoveryHandler.accept(var4);
			return null;
		}
	}

	public void close() {
		this.compilationCache.close();
	}

	@Environment(EnvType.CLIENT)
	class CompilationCache implements AutoCloseable {
		private final ShaderManager.Configs configs;
		final Map<ShaderProgram, Optional<CompiledShaderProgram>> programs = new HashMap();
		final Map<ShaderManager.ShaderCompilationKey, CompiledShader> shaders = new HashMap();
		private final Map<ResourceLocation, Optional<PostChain>> postChains = new HashMap();

		CompilationCache(final ShaderManager.Configs configs) {
			this.configs = configs;
		}

		@Nullable
		public CompiledShaderProgram getOrCompileProgram(ShaderProgram shaderProgram) throws ShaderManager.CompilationException {
			Optional<CompiledShaderProgram> optional = (Optional<CompiledShaderProgram>)this.programs.get(shaderProgram);
			if (optional != null) {
				return (CompiledShaderProgram)optional.orElse(null);
			} else {
				try {
					CompiledShaderProgram compiledShaderProgram = this.compileProgram(shaderProgram);
					this.programs.put(shaderProgram, Optional.of(compiledShaderProgram));
					return compiledShaderProgram;
				} catch (ShaderManager.CompilationException var4) {
					this.programs.put(shaderProgram, Optional.empty());
					throw var4;
				}
			}
		}

		CompiledShaderProgram compileProgram(ShaderProgram shaderProgram) throws ShaderManager.CompilationException {
			ShaderProgramConfig shaderProgramConfig = (ShaderProgramConfig)this.configs.programs.get(shaderProgram.configId());
			if (shaderProgramConfig == null) {
				throw new ShaderManager.CompilationException("Could not find program with id: " + shaderProgram.configId());
			} else {
				ShaderDefines shaderDefines = shaderProgramConfig.defines().withOverrides(shaderProgram.defines());
				CompiledShader compiledShader = this.getOrCompileShader(shaderProgramConfig.vertex(), CompiledShader.Type.VERTEX, shaderDefines);
				CompiledShader compiledShader2 = this.getOrCompileShader(shaderProgramConfig.fragment(), CompiledShader.Type.FRAGMENT, shaderDefines);
				return ShaderManager.linkProgram(shaderProgram, shaderProgramConfig, compiledShader, compiledShader2);
			}
		}

		private CompiledShader getOrCompileShader(ResourceLocation resourceLocation, CompiledShader.Type type, ShaderDefines shaderDefines) throws ShaderManager.CompilationException {
			ShaderManager.ShaderCompilationKey shaderCompilationKey = new ShaderManager.ShaderCompilationKey(resourceLocation, type, shaderDefines);
			CompiledShader compiledShader = (CompiledShader)this.shaders.get(shaderCompilationKey);
			if (compiledShader == null) {
				compiledShader = this.compileShader(shaderCompilationKey);
				this.shaders.put(shaderCompilationKey, compiledShader);
			}

			return compiledShader;
		}

		private CompiledShader compileShader(ShaderManager.ShaderCompilationKey shaderCompilationKey) throws ShaderManager.CompilationException {
			String string = (String)this.configs.shaderSources.get(new ShaderManager.ShaderSourceKey(shaderCompilationKey.id, shaderCompilationKey.type));
			if (string == null) {
				throw new ShaderManager.CompilationException("Could not find shader: " + shaderCompilationKey);
			} else {
				String string2 = GlslPreprocessor.injectDefines(string, shaderCompilationKey.defines);
				return CompiledShader.compile(shaderCompilationKey.id, shaderCompilationKey.type, string2);
			}
		}

		@Nullable
		public PostChain getOrLoadPostChain(ResourceLocation resourceLocation, Set<ResourceLocation> set) throws ShaderManager.CompilationException {
			Optional<PostChain> optional = (Optional<PostChain>)this.postChains.get(resourceLocation);
			if (optional != null) {
				return (PostChain)optional.orElse(null);
			} else {
				try {
					PostChain postChain = this.loadPostChain(resourceLocation, set);
					this.postChains.put(resourceLocation, Optional.of(postChain));
					return postChain;
				} catch (ShaderManager.CompilationException var5) {
					this.postChains.put(resourceLocation, Optional.empty());
					throw var5;
				}
			}
		}

		private PostChain loadPostChain(ResourceLocation resourceLocation, Set<ResourceLocation> set) throws ShaderManager.CompilationException {
			PostChainConfig postChainConfig = (PostChainConfig)this.configs.postChains.get(resourceLocation);
			if (postChainConfig == null) {
				throw new ShaderManager.CompilationException("Could not find post chain with id: " + resourceLocation);
			} else {
				return PostChain.load(postChainConfig, ShaderManager.this.textureManager, ShaderManager.this, set);
			}
		}

		public void close() {
			RenderSystem.assertOnRenderThread();
			this.programs.values().forEach(optional -> optional.ifPresent(CompiledShaderProgram::close));
			this.shaders.values().forEach(CompiledShader::close);
			this.programs.clear();
			this.shaders.clear();
			this.postChains.clear();
		}
	}

	@Environment(EnvType.CLIENT)
	public static class CompilationException extends Exception {
		public CompilationException(String string) {
			super(string);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record Configs(
		Map<ResourceLocation, ShaderProgramConfig> programs,
		Map<ShaderManager.ShaderSourceKey, String> shaderSources,
		Map<ResourceLocation, PostChainConfig> postChains
	) {
		public static final ShaderManager.Configs EMPTY = new ShaderManager.Configs(Map.of(), Map.of(), Map.of());
	}

	@Environment(EnvType.CLIENT)
	static record ShaderCompilationKey(ResourceLocation id, CompiledShader.Type type, ShaderDefines defines) {

		public String toString() {
			String string = this.id + " (" + this.type + ")";
			return !this.defines.isEmpty() ? string + " with " + this.defines : string;
		}
	}

	@Environment(EnvType.CLIENT)
	static record ShaderSourceKey(ResourceLocation id, CompiledShader.Type type) {
		public String toString() {
			return this.id + " (" + this.type + ")";
		}
	}
}
