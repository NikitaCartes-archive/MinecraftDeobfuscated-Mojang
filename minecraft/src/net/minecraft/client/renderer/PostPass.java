package net.minecraft.client.renderer;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class PostPass {
	private final String name;
	private final CompiledShaderProgram shader;
	private final ResourceLocation outputTargetId;
	private final List<PostChainConfig.Uniform> uniforms;
	private final List<PostPass.Input> inputs = new ArrayList();

	public PostPass(String string, CompiledShaderProgram compiledShaderProgram, ResourceLocation resourceLocation, List<PostChainConfig.Uniform> list) {
		this.name = string;
		this.shader = compiledShaderProgram;
		this.outputTargetId = resourceLocation;
		this.uniforms = list;
	}

	public void addInput(PostPass.Input input) {
		this.inputs.add(input);
	}

	public void addToFrame(FrameGraphBuilder frameGraphBuilder, Map<ResourceLocation, ResourceHandle<RenderTarget>> map, Matrix4f matrix4f) {
		FramePass framePass = frameGraphBuilder.addPass(this.name);

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
					inputx.bindTo(this.shader, map);
				}

				this.shader.safeGetUniform("OutSize").set((float)renderTarget.width, (float)renderTarget.height);

				for (PostChainConfig.Uniform uniform : this.uniforms) {
					Uniform uniform2 = this.shader.getUniform(uniform.name());
					if (uniform2 != null) {
						storeUniform(uniform2, uniform.values());
					}
				}

				renderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
				renderTarget.clear();
				renderTarget.bindWrite(false);
				RenderSystem.depthFunc(519);
				RenderSystem.setShader(this.shader);
				RenderSystem.backupProjectionMatrix();
				RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
				BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
				bufferBuilder.addVertex(0.0F, 0.0F, 500.0F);
				bufferBuilder.addVertex((float)renderTarget.width, 0.0F, 500.0F);
				bufferBuilder.addVertex((float)renderTarget.width, (float)renderTarget.height, 500.0F);
				bufferBuilder.addVertex(0.0F, (float)renderTarget.height, 500.0F);
				BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
				RenderSystem.depthFunc(515);
				RenderSystem.restoreProjectionMatrix();
				renderTarget.unbindWrite();

				for (PostPass.Input input2 : this.inputs) {
					input2.cleanup(map);
				}
			});
		}
	}

	private static void storeUniform(Uniform uniform, List<Float> list) {
		switch (list.size()) {
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

	public CompiledShaderProgram getShader() {
		return this.shader;
	}

	@Environment(EnvType.CLIENT)
	public interface Input {
		void addToPass(FramePass framePass, Map<ResourceLocation, ResourceHandle<RenderTarget>> map);

		void bindTo(CompiledShaderProgram compiledShaderProgram, Map<ResourceLocation, ResourceHandle<RenderTarget>> map);

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
		public void bindTo(CompiledShaderProgram compiledShaderProgram, Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
			ResourceHandle<RenderTarget> resourceHandle = this.getHandle(map);
			RenderTarget renderTarget = resourceHandle.get();
			renderTarget.setFilterMode(this.bilinear ? 9729 : 9728);
			compiledShaderProgram.bindSampler(this.samplerName + "Sampler", this.depthBuffer ? renderTarget.getDepthTextureId() : renderTarget.getColorTextureId());
			compiledShaderProgram.safeGetUniform(this.samplerName + "Size").set((float)renderTarget.width, (float)renderTarget.height);
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
		public void bindTo(CompiledShaderProgram compiledShaderProgram, Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
			compiledShaderProgram.bindSampler(this.samplerName + "Sampler", this.texture.getId());
			compiledShaderProgram.safeGetUniform(this.samplerName + "Size").set((float)this.width, (float)this.height);
		}
	}
}
