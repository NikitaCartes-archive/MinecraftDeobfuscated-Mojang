package net.minecraft.client.renderer;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class PostPass implements AutoCloseable {
	private final EffectInstance effect;
	public final ResourceLocation outputTargetId;
	private final List<PostPass.Input> inputs = new ArrayList();

	public PostPass(ResourceProvider resourceProvider, String string, ResourceLocation resourceLocation) throws IOException {
		this.effect = new EffectInstance(resourceProvider, string);
		this.outputTargetId = resourceLocation;
	}

	public void close() {
		this.effect.close();
	}

	public final String getName() {
		return this.effect.getName();
	}

	public void addInput(PostPass.Input input) {
		this.inputs.add(input);
	}

	public void addToFrame(FrameGraphBuilder frameGraphBuilder, Map<ResourceLocation, ResourceHandle<RenderTarget>> map, Matrix4f matrix4f, float f) {
		FramePass framePass = frameGraphBuilder.addPass(this.getName());

		for (PostPass.Input input : this.inputs) {
			input.addToPass(framePass, map);
		}

		ResourceHandle<RenderTarget> resourceHandle = (ResourceHandle<RenderTarget>)map.computeIfPresent(
			this.outputTargetId, (resourceLocation, resourceHandlex) -> framePass.readsAndWrites(resourceHandlex)
		);
		if (resourceHandle == null) {
			throw new IllegalStateException("Missing handle for target " + this.outputTargetId);
		} else {
			framePass.executes(() -> {
				RenderTarget renderTarget = resourceHandle.get();
				RenderSystem.viewport(0, 0, renderTarget.width, renderTarget.height);

				for (PostPass.Input inputx : this.inputs) {
					inputx.bindTo(this.effect, map);
				}

				this.effect.safeGetUniform("ProjMat").set(matrix4f);
				this.effect.safeGetUniform("OutSize").set((float)renderTarget.width, (float)renderTarget.height);
				this.effect.safeGetUniform("Time").set(f);
				Minecraft minecraft = Minecraft.getInstance();
				this.effect.safeGetUniform("ScreenSize").set((float)minecraft.getWindow().getWidth(), (float)minecraft.getWindow().getHeight());
				this.effect.apply();
				renderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
				renderTarget.clear();
				renderTarget.bindWrite(false);
				RenderSystem.depthFunc(519);
				BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
				bufferBuilder.addVertex(0.0F, 0.0F, 500.0F);
				bufferBuilder.addVertex((float)renderTarget.width, 0.0F, 500.0F);
				bufferBuilder.addVertex((float)renderTarget.width, (float)renderTarget.height, 500.0F);
				bufferBuilder.addVertex(0.0F, (float)renderTarget.height, 500.0F);
				BufferUploader.draw(bufferBuilder.buildOrThrow());
				RenderSystem.depthFunc(515);
				this.effect.clear();
				renderTarget.unbindWrite();

				for (PostPass.Input input2 : this.inputs) {
					input2.cleanup(map);
				}
			});
		}
	}

	public EffectInstance getEffect() {
		return this.effect;
	}

	@Environment(EnvType.CLIENT)
	public interface Input {
		void addToPass(FramePass framePass, Map<ResourceLocation, ResourceHandle<RenderTarget>> map);

		void bindTo(EffectInstance effectInstance, Map<ResourceLocation, ResourceHandle<RenderTarget>> map);

		default void cleanup(Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
		}
	}

	@Environment(EnvType.CLIENT)
	public static record TargetInput(String samplerName, ResourceLocation targetId, boolean depthBuffer, boolean bilinear) implements PostPass.Input {
		private ResourceHandle<RenderTarget> getHandle(Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
			ResourceHandle<RenderTarget> resourceHandle = (ResourceHandle<RenderTarget>)map.get(this.targetId);
			if (resourceHandle == null) {
				throw new IllegalStateException("Missing handle for target " + this.targetId);
			} else {
				return resourceHandle;
			}
		}

		@Override
		public void addToPass(FramePass framePass, Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
			framePass.reads(this.getHandle(map));
		}

		@Override
		public void bindTo(EffectInstance effectInstance, Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
			ResourceHandle<RenderTarget> resourceHandle = this.getHandle(map);
			RenderTarget renderTarget = resourceHandle.get();
			renderTarget.setFilterMode(this.bilinear ? 9729 : 9728);
			effectInstance.setSampler(this.samplerName + "Sampler", this.depthBuffer ? renderTarget::getDepthTextureId : renderTarget::getColorTextureId);
			effectInstance.safeGetUniform(this.samplerName + "Size").set((float)renderTarget.width, (float)renderTarget.height);
		}

		@Override
		public void cleanup(Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
			if (this.bilinear) {
				this.getHandle(map).get().setFilterMode(9728);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static record TextureInput(String samplerName, AbstractTexture texture, int width, int height) implements PostPass.Input {
		@Override
		public void addToPass(FramePass framePass, Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
		}

		@Override
		public void bindTo(EffectInstance effectInstance, Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
			effectInstance.setSampler(this.samplerName + "Sampler", this.texture::getId);
			effectInstance.safeGetUniform(this.samplerName + "Size").set((float)this.width, (float)this.height);
		}
	}
}
