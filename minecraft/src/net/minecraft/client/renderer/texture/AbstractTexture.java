package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public abstract class AbstractTexture implements AutoCloseable {
	public static final int NOT_ASSIGNED = -1;
	protected int id = -1;
	protected boolean defaultBlur;

	public void setFilter(boolean bl, boolean bl2) {
		RenderSystem.assertOnRenderThreadOrInit();
		int i;
		int j;
		if (bl) {
			i = bl2 ? 9987 : 9729;
			j = 9729;
		} else {
			i = bl2 ? 9986 : 9728;
			j = 9728;
		}

		this.bind();
		GlStateManager._texParameter(3553, 10241, i);
		GlStateManager._texParameter(3553, 10240, j);
	}

	public int getId() {
		RenderSystem.assertOnRenderThreadOrInit();
		if (this.id == -1) {
			this.id = TextureUtil.generateTextureId();
		}

		return this.id;
	}

	public void releaseId() {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> {
				if (this.id != -1) {
					TextureUtil.releaseTextureId(this.id);
					this.id = -1;
				}
			});
		} else if (this.id != -1) {
			TextureUtil.releaseTextureId(this.id);
			this.id = -1;
		}
	}

	public boolean getDefaultBlur() {
		return this.defaultBlur;
	}

	public abstract void load(ResourceManager resourceManager) throws IOException;

	public void bind() {
		if (!RenderSystem.isOnRenderThreadOrInit()) {
			RenderSystem.recordRenderCall(() -> GlStateManager._bindTexture(this.getId()));
		} else {
			GlStateManager._bindTexture(this.getId());
		}
	}

	public void reset(TextureManager textureManager, ResourceManager resourceManager, ResourceLocation resourceLocation, Executor executor) {
		textureManager.register(resourceLocation, this);
	}

	public void close() {
	}
}
