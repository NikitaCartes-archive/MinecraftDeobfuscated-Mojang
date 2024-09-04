package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class PostChain {
	public static final ResourceLocation MAIN_TARGET_ID = ResourceLocation.withDefaultNamespace("main");
	private final List<PostPass> passes;
	private final Map<ResourceLocation, PostChainConfig.InternalTarget> internalTargets;
	private final Set<ResourceLocation> externalTargets;

	private PostChain(List<PostPass> list, Map<ResourceLocation, PostChainConfig.InternalTarget> map, Set<ResourceLocation> set) {
		this.passes = list;
		this.internalTargets = map;
		this.externalTargets = set;
	}

	public static PostChain load(PostChainConfig postChainConfig, TextureManager textureManager, ShaderManager shaderManager, Set<ResourceLocation> set) throws ShaderManager.CompilationException {
		Stream<ResourceLocation> stream = postChainConfig.passes()
			.stream()
			.flatMap(passx -> passx.inputs().stream())
			.flatMap(input -> input.referencedTargets().stream());
		Set<ResourceLocation> set2 = (Set<ResourceLocation>)stream.filter(resourceLocation -> !postChainConfig.internalTargets().containsKey(resourceLocation))
			.collect(Collectors.toSet());
		Set<ResourceLocation> set3 = Sets.<ResourceLocation>difference(set2, set);
		if (!set3.isEmpty()) {
			throw new ShaderManager.CompilationException("Referenced external targets are not available in this context: " + set3);
		} else {
			Builder<PostPass> builder = ImmutableList.builder();

			for (PostChainConfig.Pass pass : postChainConfig.passes()) {
				builder.add(createPass(textureManager, shaderManager, pass));
			}

			return new PostChain(builder.build(), postChainConfig.internalTargets(), set2);
		}
	}

	// $VF: Inserted dummy exception handlers to handle obfuscated exceptions
	private static PostPass createPass(TextureManager textureManager, ShaderManager shaderManager, PostChainConfig.Pass pass) throws ShaderManager.CompilationException {
		ResourceLocation resourceLocation = pass.program();
		CompiledShaderProgram compiledShaderProgram = shaderManager.getProgramForLoading(
			new ShaderProgram(resourceLocation, DefaultVertexFormat.POSITION, ShaderDefines.EMPTY)
		);

		for (PostChainConfig.Uniform uniform : pass.uniforms()) {
			String string = uniform.name();
			if (compiledShaderProgram.getUniform(string) == null) {
				throw new ShaderManager.CompilationException("Uniform '" + string + "' does not exist for " + resourceLocation);
			}
		}

		String string2 = resourceLocation.toString();
		PostPass postPass = new PostPass(string2, compiledShaderProgram, pass.outputTarget(), pass.uniforms());

		for (PostChainConfig.Input input : pass.inputs()) {
			Objects.requireNonNull(input);
			Throwable var45;
			switch (input) {
				case PostChainConfig.TextureInput var11:
					PostChainConfig.TextureInput var53 = var11;

					try {
						var54 = var53.samplerName();
					} catch (Throwable var31) {
						var45 = var31;
						boolean var66 = false;
						break;
					}

					String var36 = var54;
					PostChainConfig.TextureInput var55 = var11;

					try {
						var56 = var55.location();
					} catch (Throwable var30) {
						var45 = var30;
						boolean var67 = false;
						break;
					}

					ResourceLocation var37 = var56;
					PostChainConfig.TextureInput var57 = var11;

					try {
						var58 = var57.width();
					} catch (Throwable var29) {
						var45 = var29;
						boolean var68 = false;
						break;
					}

					int var38 = var58;
					PostChainConfig.TextureInput var59 = var11;

					try {
						var60 = var59.height();
					} catch (Throwable var28) {
						var45 = var28;
						boolean var69 = false;
						break;
					}

					int var39 = var60;
					PostChainConfig.TextureInput var61 = var11;

					try {
						var62 = var61.bilinear();
					} catch (Throwable var27) {
						var45 = var27;
						boolean var70 = false;
						break;
					}

					boolean var40 = var62;
					AbstractTexture abstractTexturex = textureManager.getTexture(var37.withPath((UnaryOperator<String>)(stringx -> "textures/effect/" + stringx + ".png")));
					abstractTexturex.setFilter(var40, false);
					postPass.addInput(new PostPass.TextureInput(var36, abstractTexturex, var38, var39));
					continue;
				case PostChainConfig.TargetInput abstractTexture:
					PostChainConfig.TargetInput var10000 = abstractTexture;

					try {
						var46 = var10000.samplerName();
					} catch (Throwable var26) {
						var45 = var26;
						boolean var10001 = false;
						break;
					}

					String var22 = var46;
					PostChainConfig.TargetInput var47 = abstractTexture;

					try {
						var48 = var47.targetId();
					} catch (Throwable var25) {
						var45 = var25;
						boolean var63 = false;
						break;
					}

					ResourceLocation var42 = var48;
					PostChainConfig.TargetInput var49 = abstractTexture;

					try {
						var50 = var49.useDepthBuffer();
					} catch (Throwable var24) {
						var45 = var24;
						boolean var64 = false;
						break;
					}

					boolean var43 = var50;
					PostChainConfig.TargetInput var51 = abstractTexture;

					try {
						var52 = var51.bilinear();
					} catch (Throwable var23) {
						var45 = var23;
						boolean var65 = false;
						break;
					}

					boolean var44 = var52;
					postPass.addInput(new PostPass.TargetInput(var22, var42, var43, var44));
					continue;
				default:
					throw new MatchException(null, null);
			}

			Throwable var35 = var45;
			throw new MatchException(var35.toString(), var35);
		}

		return postPass;
	}

	// $VF: Inserted dummy exception handlers to handle obfuscated exceptions
	public void addToFrame(FrameGraphBuilder frameGraphBuilder, int i, int j, PostChain.TargetBundle targetBundle) {
		Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, (float)i, 0.0F, (float)j, 0.1F, 1000.0F);
		Map<ResourceLocation, ResourceHandle<RenderTarget>> map = new HashMap(this.internalTargets.size() + this.externalTargets.size());

		for (ResourceLocation resourceLocation : this.externalTargets) {
			map.put(resourceLocation, targetBundle.getOrThrow(resourceLocation));
		}

		for (Entry<ResourceLocation, PostChainConfig.InternalTarget> entry : this.internalTargets.entrySet()) {
			ResourceLocation resourceLocation2 = (ResourceLocation)entry.getKey();
			PostChainConfig.InternalTarget var35;
			Objects.requireNonNull(var35);
			Object var11 = var35;

			var35 = (PostChainConfig.InternalTarget)entry.getValue();
			RenderTargetDescriptor renderTargetDescriptor = switch (var11) {
				case PostChainConfig.FixedSizedTarget var13 -> {
					PostChainConfig.FixedSizedTarget var29 = var13;

					int var26;
					label56: {
						label76: {
							try {
								var31 = var29.width();
							} catch (Throwable var18) {
								var30 = var18;
								boolean var10001 = false;
								break label76;
							}

							var26 = var31;
							PostChainConfig.FixedSizedTarget var32 = var13;

							try {
								var33 = var32.height();
								break label56;
							} catch (Throwable var17) {
								var30 = var17;
								boolean var34 = false;
							}
						}

						Throwable var20 = var30;
						throw new MatchException(var20.toString(), var20);
					}

					int var27 = var33;
					yield new RenderTargetDescriptor(var26, var27, true);
				}
				case PostChainConfig.FullScreenTarget var16 -> new RenderTargetDescriptor(i, j, true);
				default -> throw new MatchException(null, null);
			};
			map.put(resourceLocation2, frameGraphBuilder.createInternal(resourceLocation2.toString(), renderTargetDescriptor));
		}

		for (PostPass postPass : this.passes) {
			postPass.addToFrame(frameGraphBuilder, map, matrix4f);
		}

		for (ResourceLocation resourceLocation : this.externalTargets) {
			targetBundle.replace(resourceLocation, (ResourceHandle<RenderTarget>)map.get(resourceLocation));
		}
	}

	@Deprecated
	public void process(RenderTarget renderTarget, GraphicsResourceAllocator graphicsResourceAllocator) {
		FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();
		PostChain.TargetBundle targetBundle = PostChain.TargetBundle.of(MAIN_TARGET_ID, frameGraphBuilder.importExternal("main", renderTarget));
		this.addToFrame(frameGraphBuilder, renderTarget.width, renderTarget.height, targetBundle);
		frameGraphBuilder.execute(graphicsResourceAllocator);
	}

	public void setUniform(String string, float f) {
		for (PostPass postPass : this.passes) {
			postPass.getShader().safeGetUniform(string).set(f);
		}
	}

	@Environment(EnvType.CLIENT)
	public interface TargetBundle {
		static PostChain.TargetBundle of(ResourceLocation resourceLocation, ResourceHandle<RenderTarget> resourceHandle) {
			return new PostChain.TargetBundle() {
				private ResourceHandle<RenderTarget> handle = resourceHandle;

				@Override
				public void replace(ResourceLocation resourceLocation, ResourceHandle<RenderTarget> resourceHandle) {
					if (resourceLocation.equals(resourceLocation)) {
						this.handle = resourceHandle;
					} else {
						throw new IllegalArgumentException("No target with id " + resourceLocation);
					}
				}

				@Nullable
				@Override
				public ResourceHandle<RenderTarget> get(ResourceLocation resourceLocation) {
					return resourceLocation.equals(resourceLocation) ? this.handle : null;
				}
			};
		}

		void replace(ResourceLocation resourceLocation, ResourceHandle<RenderTarget> resourceHandle);

		@Nullable
		ResourceHandle<RenderTarget> get(ResourceLocation resourceLocation);

		default ResourceHandle<RenderTarget> getOrThrow(ResourceLocation resourceLocation) {
			ResourceHandle<RenderTarget> resourceHandle = this.get(resourceLocation);
			if (resourceHandle == null) {
				throw new IllegalArgumentException("Missing target with id " + resourceLocation);
			} else {
				return resourceHandle;
			}
		}
	}
}
