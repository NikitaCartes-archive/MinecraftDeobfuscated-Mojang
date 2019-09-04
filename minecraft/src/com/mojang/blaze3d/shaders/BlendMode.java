package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BlendMode {
	private static BlendMode lastApplied;
	private final int srcColorFactor;
	private final int srcAlphaFactor;
	private final int dstColorFactor;
	private final int dstAlphaFactor;
	private final int blendFunc;
	private final boolean separateBlend;
	private final boolean opaque;

	private BlendMode(boolean bl, boolean bl2, int i, int j, int k, int l, int m) {
		this.separateBlend = bl;
		this.srcColorFactor = i;
		this.dstColorFactor = j;
		this.srcAlphaFactor = k;
		this.dstAlphaFactor = l;
		this.opaque = bl2;
		this.blendFunc = m;
	}

	public BlendMode() {
		this(false, true, 1, 0, 1, 0, 32774);
	}

	public BlendMode(int i, int j, int k) {
		this(false, false, i, j, i, j, k);
	}

	public BlendMode(int i, int j, int k, int l, int m) {
		this(true, false, i, j, k, l, m);
	}

	public void apply() {
		if (!this.equals(lastApplied)) {
			if (lastApplied == null || this.opaque != lastApplied.isOpaque()) {
				lastApplied = this;
				if (this.opaque) {
					RenderSystem.disableBlend();
					return;
				}

				RenderSystem.enableBlend();
			}

			RenderSystem.blendEquation(this.blendFunc);
			if (this.separateBlend) {
				RenderSystem.blendFuncSeparate(this.srcColorFactor, this.dstColorFactor, this.srcAlphaFactor, this.dstAlphaFactor);
			} else {
				RenderSystem.blendFunc(this.srcColorFactor, this.dstColorFactor);
			}
		}
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof BlendMode)) {
			return false;
		} else {
			BlendMode blendMode = (BlendMode)object;
			if (this.blendFunc != blendMode.blendFunc) {
				return false;
			} else if (this.dstAlphaFactor != blendMode.dstAlphaFactor) {
				return false;
			} else if (this.dstColorFactor != blendMode.dstColorFactor) {
				return false;
			} else if (this.opaque != blendMode.opaque) {
				return false;
			} else if (this.separateBlend != blendMode.separateBlend) {
				return false;
			} else {
				return this.srcAlphaFactor != blendMode.srcAlphaFactor ? false : this.srcColorFactor == blendMode.srcColorFactor;
			}
		}
	}

	public int hashCode() {
		int i = this.srcColorFactor;
		i = 31 * i + this.srcAlphaFactor;
		i = 31 * i + this.dstColorFactor;
		i = 31 * i + this.dstAlphaFactor;
		i = 31 * i + this.blendFunc;
		i = 31 * i + (this.separateBlend ? 1 : 0);
		return 31 * i + (this.opaque ? 1 : 0);
	}

	public boolean isOpaque() {
		return this.opaque;
	}

	public static int stringToBlendFunc(String string) {
		String string2 = string.trim().toLowerCase(Locale.ROOT);
		if ("add".equals(string2)) {
			return 32774;
		} else if ("subtract".equals(string2)) {
			return 32778;
		} else if ("reversesubtract".equals(string2)) {
			return 32779;
		} else if ("reverse_subtract".equals(string2)) {
			return 32779;
		} else if ("min".equals(string2)) {
			return 32775;
		} else {
			return "max".equals(string2) ? 32776 : 32774;
		}
	}

	public static int stringToBlendFactor(String string) {
		String string2 = string.trim().toLowerCase(Locale.ROOT);
		string2 = string2.replaceAll("_", "");
		string2 = string2.replaceAll("one", "1");
		string2 = string2.replaceAll("zero", "0");
		string2 = string2.replaceAll("minus", "-");
		if ("0".equals(string2)) {
			return 0;
		} else if ("1".equals(string2)) {
			return 1;
		} else if ("srccolor".equals(string2)) {
			return 768;
		} else if ("1-srccolor".equals(string2)) {
			return 769;
		} else if ("dstcolor".equals(string2)) {
			return 774;
		} else if ("1-dstcolor".equals(string2)) {
			return 775;
		} else if ("srcalpha".equals(string2)) {
			return 770;
		} else if ("1-srcalpha".equals(string2)) {
			return 771;
		} else if ("dstalpha".equals(string2)) {
			return 772;
		} else {
			return "1-dstalpha".equals(string2) ? 773 : -1;
		}
	}
}
