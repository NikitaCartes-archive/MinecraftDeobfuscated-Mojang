package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Reader;
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
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GsonHelper;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class PostChain implements AutoCloseable {
	public static final ResourceLocation MAIN_TARGET_ID = ResourceLocation.withDefaultNamespace("main");
	private final ResourceLocation id;
	private final List<PostPass> passes;
	private final Map<ResourceLocation, PostChainConfig.InternalTarget> internalTargets;
	private final Set<ResourceLocation> externalTargets;
	private float time;

	private PostChain(ResourceLocation resourceLocation, List<PostPass> list, Map<ResourceLocation, PostChainConfig.InternalTarget> map, Set<ResourceLocation> set) {
		this.id = resourceLocation;
		this.passes = list;
		this.internalTargets = map;
		this.externalTargets = set;
	}

	public static PostChain load(ResourceProvider resourceProvider, TextureManager textureManager, ResourceLocation resourceLocation, Set<ResourceLocation> set) throws IOException, JsonSyntaxException {
		Resource resource = resourceProvider.getResourceOrThrow(resourceLocation);

		try {
			Reader reader = resource.openAsReader();

			PostChain var18;
			try {
				JsonObject jsonObject = GsonHelper.parse(reader);
				PostChainConfig postChainConfig = PostChainConfig.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow(JsonSyntaxException::new);
				Stream<ResourceLocation> stream = postChainConfig.passes()
					.stream()
					.flatMap(passx -> passx.inputs().stream())
					.flatMap(input -> input.referencedTargets().stream());
				Set<ResourceLocation> set2 = (Set<ResourceLocation>)stream.filter(resourceLocationx -> !postChainConfig.internalTargets().containsKey(resourceLocationx))
					.collect(Collectors.toSet());
				Set<ResourceLocation> set3 = Sets.<ResourceLocation>difference(set2, set);
				if (!set3.isEmpty()) {
					throw new ChainedJsonException("Referenced external targets are not available in this context: " + set3);
				}

				Builder<PostPass> builder = ImmutableList.builder();

				for (PostChainConfig.Pass pass : postChainConfig.passes()) {
					builder.add(createPass(resourceProvider, textureManager, pass));
				}

				var18 = new PostChain(resourceLocation, builder.build(), postChainConfig.internalTargets(), set2);
			} catch (Throwable var15) {
				if (reader != null) {
					try {
						reader.close();
					} catch (Throwable var14) {
						var15.addSuppressed(var14);
					}
				}

				throw var15;
			}

			if (reader != null) {
				reader.close();
			}

			return var18;
		} catch (Exception var16) {
			ChainedJsonException chainedJsonException = ChainedJsonException.forException(var16);
			chainedJsonException.setFilenameAndFlush(resourceLocation.getPath() + " (" + resource.sourcePackId() + ")");
			throw chainedJsonException;
		}
	}

	// $VF: Inserted dummy exception handlers to handle obfuscated exceptions
	private static PostPass createPass(ResourceProvider resourceProvider, TextureManager textureManager, PostChainConfig.Pass pass) throws IOException {
		PostPass postPass = new PostPass(resourceProvider, pass.name(), pass.outputTarget());

		for (PostChainConfig.Input input : pass.inputs()) {
			Objects.requireNonNull(input);
			Throwable var43;
			switch (input) {
				case PostChainConfig.TextureInput var8:
					PostChainConfig.TextureInput var51 = var8;

					try {
						var52 = var51.samplerName();
					} catch (Throwable var28) {
						var43 = var28;
						boolean var64 = false;
						break;
					}

					String var33 = var52;
					PostChainConfig.TextureInput var53 = var8;

					try {
						var54 = var53.location();
					} catch (Throwable var27) {
						var43 = var27;
						boolean var65 = false;
						break;
					}

					ResourceLocation var34 = var54;
					ResourceLocation resourceLocation = var34;
					PostChainConfig.TextureInput var55 = var8;

					try {
						var56 = var55.width();
					} catch (Throwable var26) {
						var43 = var26;
						boolean var66 = false;
						break;
					}

					int var35 = var56;
					PostChainConfig.TextureInput var57 = var8;

					try {
						var58 = var57.height();
					} catch (Throwable var25) {
						var43 = var25;
						boolean var67 = false;
						break;
					}

					int var36 = var58;
					PostChainConfig.TextureInput var59 = var8;

					try {
						var60 = var59.bilinear();
					} catch (Throwable var24) {
						var43 = var24;
						boolean var68 = false;
						break;
					}

					boolean var37 = var60;
					ResourceLocation resourceLocation2x = resourceLocation.withPath((UnaryOperator<String>)(string -> "textures/effect/" + string + ".png"));
					resourceProvider.getResource(resourceLocation2x).orElseThrow(() -> new ChainedJsonException("Texture '" + resourceLocation + "' does not exist"));
					RenderSystem.setShaderTexture(0, resourceLocation2x);
					textureManager.bindForSetup(resourceLocation2x);
					AbstractTexture abstractTexture = textureManager.getTexture(resourceLocation2x);
					if (var37) {
						RenderSystem.texParameter(3553, 10241, 9729);
						RenderSystem.texParameter(3553, 10240, 9729);
					} else {
						RenderSystem.texParameter(3553, 10241, 9728);
						RenderSystem.texParameter(3553, 10240, 9728);
					}

					postPass.addInput(new PostPass.TextureInput(var33, abstractTexture, var35, var36));
					continue;
				case PostChainConfig.TargetInput resourceLocation2:
					PostChainConfig.TargetInput var10000 = resourceLocation2;

					try {
						var44 = var10000.samplerName();
					} catch (Throwable var23) {
						var43 = var23;
						boolean var10001 = false;
						break;
					}

					String var19 = var44;
					PostChainConfig.TargetInput var45 = resourceLocation2;

					try {
						var46 = var45.targetId();
					} catch (Throwable var22) {
						var43 = var22;
						boolean var61 = false;
						break;
					}

					ResourceLocation var40 = var46;
					PostChainConfig.TargetInput var47 = resourceLocation2;

					try {
						var48 = var47.useDepthBuffer();
					} catch (Throwable var21) {
						var43 = var21;
						boolean var62 = false;
						break;
					}

					boolean var41 = var48;
					PostChainConfig.TargetInput var49 = resourceLocation2;

					try {
						var50 = var49.bilinear();
					} catch (Throwable var20) {
						var43 = var20;
						boolean var63 = false;
						break;
					}

					boolean var42 = var50;
					postPass.addInput(new PostPass.TargetInput(var19, var40, var41, var42));
					continue;
				default:
					throw new MatchException(null, null);
			}

			Throwable var29 = var43;
			throw new MatchException(var29.toString(), var29);
		}

		for (PostChainConfig.Uniform uniform : pass.uniforms()) {
			String string3 = uniform.name();
			Uniform uniform2 = postPass.getEffect().getUniform(string3);
			if (uniform2 == null) {
				throw new ChainedJsonException("Uniform '" + string3 + "' does not exist");
			}

			storeUniform(uniform2, uniform.values());
		}

		return postPass;
	}

	private static void storeUniform(Uniform uniform, List<Float> list) {
		switch (list.size()) {
			case 0:
			default:
				break;
			case 1:
				uniform.set((Float)list.getFirst());
				break;
			case 2:
				uniform.set((Float)list.get(0), (Float)list.get(1));
				break;
			case 3:
				uniform.set((Float)list.get(0), (Float)list.get(1), (Float)list.get(2));
				break;
			case 4:
				uniform.set((Float)list.get(0), (Float)list.get(1), (Float)list.get(2), (Float)list.get(3));
		}
	}

	public void close() {
		for (PostPass postPass : this.passes) {
			postPass.close();
		}
	}

	// $VF: Inserted dummy exception handlers to handle obfuscated exceptions
	public void addToFrame(FrameGraphBuilder frameGraphBuilder, DeltaTracker deltaTracker, int i, int j, PostChain.TargetBundle targetBundle) {
		Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, (float)i, 0.0F, (float)j, 0.1F, 1000.0F);
		this.time = this.time + deltaTracker.getRealtimeDeltaTicks();

		while (this.time > 20.0F) {
			this.time -= 20.0F;
		}

		Map<ResourceLocation, ResourceHandle<RenderTarget>> map = new HashMap(this.internalTargets.size() + this.externalTargets.size());

		for (ResourceLocation resourceLocation : this.externalTargets) {
			map.put(resourceLocation, targetBundle.getOrThrow(resourceLocation));
		}

		for (Entry<ResourceLocation, PostChainConfig.InternalTarget> entry : this.internalTargets.entrySet()) {
			ResourceLocation resourceLocation2 = (ResourceLocation)entry.getKey();
			PostChainConfig.InternalTarget var36;
			Objects.requireNonNull(var36);
			Object var12 = var36;

			var36 = (PostChainConfig.InternalTarget)entry.getValue();
			RenderTargetDescriptor renderTargetDescriptor = switch (var12) {
				case PostChainConfig.FixedSizedTarget var14 -> {
					PostChainConfig.FixedSizedTarget var30 = var14;

					int var27;
					label59: {
						label85: {
							try {
								var32 = var30.width();
							} catch (Throwable var19) {
								var31 = var19;
								boolean var10001 = false;
								break label85;
							}

							var27 = var32;
							PostChainConfig.FixedSizedTarget var33 = var14;

							try {
								var34 = var33.height();
								break label59;
							} catch (Throwable var18) {
								var31 = var18;
								boolean var35 = false;
							}
						}

						Throwable var21 = var31;
						throw new MatchException(var21.toString(), var21);
					}

					int var28 = var34;
					yield new RenderTargetDescriptor(var27, var28, true);
				}
				case PostChainConfig.FullScreenTarget var17 -> new RenderTargetDescriptor(i, j, true);
				default -> throw new MatchException(null, null);
			};
			map.put(resourceLocation2, frameGraphBuilder.createInternal(resourceLocation2.toString(), renderTargetDescriptor));
		}

		for (PostPass postPass : this.passes) {
			postPass.addToFrame(frameGraphBuilder, map, matrix4f, this.time / 20.0F);
		}

		for (ResourceLocation resourceLocation : this.externalTargets) {
			targetBundle.replace(resourceLocation, (ResourceHandle<RenderTarget>)map.get(resourceLocation));
		}
	}

	@Deprecated
	public void process(RenderTarget renderTarget, GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker) {
		FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();
		PostChain.TargetBundle targetBundle = PostChain.TargetBundle.of(MAIN_TARGET_ID, frameGraphBuilder.importExternal("main", renderTarget));
		this.addToFrame(frameGraphBuilder, deltaTracker, renderTarget.width, renderTarget.height, targetBundle);
		frameGraphBuilder.execute(graphicsResourceAllocator);
	}

	public void setUniform(String string, float f) {
		for (PostPass postPass : this.passes) {
			postPass.getEffect().safeGetUniform(string).set(f);
		}
	}

	public final ResourceLocation getId() {
		return this.id;
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
