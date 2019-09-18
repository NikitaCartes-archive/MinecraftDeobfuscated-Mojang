package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class AbstractTexture implements TextureObject {
	protected int id = -1;
	protected boolean blur;
	protected boolean mipmap;
	protected boolean oldBlur;
	protected boolean oldMipmap;

	public void setFilter(boolean bl, boolean bl2) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		this.blur = bl;
		this.mipmap = bl2;
		int i;
		int j;
		if (bl) {
			i = bl2 ? 9987 : 9729;
			j = 9729;
		} else {
			i = bl2 ? 9986 : 9728;
			j = 9728;
		}

		GlStateManager._texParameter(3553, 10241, i);
		GlStateManager._texParameter(3553, 10240, j);
	}

	@Override
	public void pushFilter(boolean bl, boolean bl2) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> this._pushFilter(bl, bl2));
		} else {
			this._pushFilter(bl, bl2);
		}
	}

	private void _pushFilter(boolean bl, boolean bl2) {
		this.oldBlur = this.blur;
		this.oldMipmap = this.mipmap;
		this.setFilter(bl, bl2);
	}

	@Override
	public void popFilter() {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(this::_popFilter);
		} else {
			this._popFilter();
		}
	}

	private void _popFilter() {
		this.setFilter(this.oldBlur, this.oldMipmap);
	}

	@Override
	public int getId() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
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
}
