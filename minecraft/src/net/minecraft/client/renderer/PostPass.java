package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import java.io.IOException;
import java.util.List;
import java.util.function.IntSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public class PostPass implements AutoCloseable {
	private final EffectInstance effect;
	public final RenderTarget inTarget;
	public final RenderTarget outTarget;
	private final List<IntSupplier> auxAssets = Lists.<IntSupplier>newArrayList();
	private final List<String> auxNames = Lists.<String>newArrayList();
	private final List<Integer> auxWidths = Lists.<Integer>newArrayList();
	private final List<Integer> auxHeights = Lists.<Integer>newArrayList();
	private Matrix4f shaderOrthoMatrix;

	public PostPass(ResourceManager resourceManager, String string, RenderTarget renderTarget, RenderTarget renderTarget2) throws IOException {
		this.effect = new EffectInstance(resourceManager, string);
		this.inTarget = renderTarget;
		this.outTarget = renderTarget2;
	}

	public void close() {
		this.effect.close();
	}

	public void addAuxAsset(String string, IntSupplier intSupplier, int i, int j) {
		this.auxNames.add(this.auxNames.size(), string);
		this.auxAssets.add(this.auxAssets.size(), intSupplier);
		this.auxWidths.add(this.auxWidths.size(), i);
		this.auxHeights.add(this.auxHeights.size(), j);
	}

	public void setOrthoMatrix(Matrix4f matrix4f) {
		this.shaderOrthoMatrix = matrix4f;
	}

	public void process(float f) {
		this.inTarget.unbindWrite();
		float g = (float)this.outTarget.width;
		float h = (float)this.outTarget.height;
		RenderSystem.viewport(0, 0, (int)g, (int)h);
		this.effect.setSampler("DiffuseSampler", this.inTarget::getColorTextureId);

		for (int i = 0; i < this.auxAssets.size(); i++) {
			this.effect.setSampler((String)this.auxNames.get(i), (IntSupplier)this.auxAssets.get(i));
			this.effect.safeGetUniform("AuxSize" + i).set((float)((Integer)this.auxWidths.get(i)).intValue(), (float)((Integer)this.auxHeights.get(i)).intValue());
		}

		this.effect.safeGetUniform("ProjMat").set(this.shaderOrthoMatrix);
		this.effect.safeGetUniform("InSize").set((float)this.inTarget.width, (float)this.inTarget.height);
		this.effect.safeGetUniform("OutSize").set(g, h);
		this.effect.safeGetUniform("Time").set(f);
		Minecraft minecraft = Minecraft.getInstance();
		this.effect.safeGetUniform("ScreenSize").set((float)minecraft.getWindow().getWidth(), (float)minecraft.getWindow().getHeight());
		this.effect.apply();
		this.outTarget.clear(Minecraft.ON_OSX);
		this.outTarget.bindWrite(false);
		RenderSystem.depthFunc(519);
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex(0.0, 0.0, 500.0).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex((double)g, 0.0, 500.0).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex((double)g, (double)h, 500.0).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(0.0, (double)h, 500.0).color(255, 255, 255, 255).endVertex();
		bufferBuilder.end();
		BufferUploader.end(bufferBuilder);
		RenderSystem.depthFunc(515);
		this.effect.clear();
		this.outTarget.unbindWrite();
		this.inTarget.unbindRead();

		for (Object object : this.auxAssets) {
			if (object instanceof RenderTarget) {
				((RenderTarget)object).unbindRead();
			}
		}
	}

	public EffectInstance getEffect() {
		return this.effect;
	}
}
