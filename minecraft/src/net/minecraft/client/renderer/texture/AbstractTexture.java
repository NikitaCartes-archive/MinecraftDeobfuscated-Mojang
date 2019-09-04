package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureUtil;
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

		RenderSystem.texParameter(3553, 10241, i);
		RenderSystem.texParameter(3553, 10240, j);
	}

	@Override
	public void pushFilter(boolean bl, boolean bl2) {
		this.oldBlur = this.blur;
		this.oldMipmap = this.mipmap;
		this.setFilter(bl, bl2);
	}

	@Override
	public void popFilter() {
		this.setFilter(this.oldBlur, this.oldMipmap);
	}

	@Override
	public int getId() {
		if (this.id == -1) {
			this.id = TextureUtil.generateTextureId();
		}

		return this.id;
	}

	public void releaseId() {
		if (this.id != -1) {
			TextureUtil.releaseTextureId(this.id);
			this.id = -1;
		}
	}
}
