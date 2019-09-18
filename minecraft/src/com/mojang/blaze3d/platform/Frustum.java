package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.culling.FrustumData;

@Environment(EnvType.CLIENT)
public class Frustum extends FrustumData {
	private static final Frustum FRUSTUM = new Frustum();
	private final FloatBuffer _proj = MemoryTracker.createFloatBuffer(16);
	private final FloatBuffer _modl = MemoryTracker.createFloatBuffer(16);
	private final FloatBuffer _clip = MemoryTracker.createFloatBuffer(16);

	public static FrustumData getFrustum() {
		FRUSTUM.calculateFrustum();
		return FRUSTUM;
	}

	private void normalizePlane(float[] fs) {
		float f = (float)Math.sqrt((double)(fs[0] * fs[0] + fs[1] * fs[1] + fs[2] * fs[2]));
		fs[0] /= f;
		fs[1] /= f;
		fs[2] /= f;
		fs[3] /= f;
	}

	public void calculateFrustum() {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(this::_calculateFrustum);
		} else {
			this._calculateFrustum();
		}
	}

	private void _calculateFrustum() {
		this._proj.clear();
		this._modl.clear();
		this._clip.clear();
		GlStateManager._getMatrix(2983, this._proj);
		GlStateManager._getMatrix(2982, this._modl);
		float[] fs = this.projectionMatrix;
		float[] gs = this.modelViewMatrix;
		this._proj.flip().limit(16);
		this._proj.get(fs);
		this._modl.flip().limit(16);
		this._modl.get(gs);
		this.clip[0] = gs[0] * fs[0] + gs[1] * fs[4] + gs[2] * fs[8] + gs[3] * fs[12];
		this.clip[1] = gs[0] * fs[1] + gs[1] * fs[5] + gs[2] * fs[9] + gs[3] * fs[13];
		this.clip[2] = gs[0] * fs[2] + gs[1] * fs[6] + gs[2] * fs[10] + gs[3] * fs[14];
		this.clip[3] = gs[0] * fs[3] + gs[1] * fs[7] + gs[2] * fs[11] + gs[3] * fs[15];
		this.clip[4] = gs[4] * fs[0] + gs[5] * fs[4] + gs[6] * fs[8] + gs[7] * fs[12];
		this.clip[5] = gs[4] * fs[1] + gs[5] * fs[5] + gs[6] * fs[9] + gs[7] * fs[13];
		this.clip[6] = gs[4] * fs[2] + gs[5] * fs[6] + gs[6] * fs[10] + gs[7] * fs[14];
		this.clip[7] = gs[4] * fs[3] + gs[5] * fs[7] + gs[6] * fs[11] + gs[7] * fs[15];
		this.clip[8] = gs[8] * fs[0] + gs[9] * fs[4] + gs[10] * fs[8] + gs[11] * fs[12];
		this.clip[9] = gs[8] * fs[1] + gs[9] * fs[5] + gs[10] * fs[9] + gs[11] * fs[13];
		this.clip[10] = gs[8] * fs[2] + gs[9] * fs[6] + gs[10] * fs[10] + gs[11] * fs[14];
		this.clip[11] = gs[8] * fs[3] + gs[9] * fs[7] + gs[10] * fs[11] + gs[11] * fs[15];
		this.clip[12] = gs[12] * fs[0] + gs[13] * fs[4] + gs[14] * fs[8] + gs[15] * fs[12];
		this.clip[13] = gs[12] * fs[1] + gs[13] * fs[5] + gs[14] * fs[9] + gs[15] * fs[13];
		this.clip[14] = gs[12] * fs[2] + gs[13] * fs[6] + gs[14] * fs[10] + gs[15] * fs[14];
		this.clip[15] = gs[12] * fs[3] + gs[13] * fs[7] + gs[14] * fs[11] + gs[15] * fs[15];
		float[] hs = this.frustumData[0];
		hs[0] = this.clip[3] - this.clip[0];
		hs[1] = this.clip[7] - this.clip[4];
		hs[2] = this.clip[11] - this.clip[8];
		hs[3] = this.clip[15] - this.clip[12];
		this.normalizePlane(hs);
		float[] is = this.frustumData[1];
		is[0] = this.clip[3] + this.clip[0];
		is[1] = this.clip[7] + this.clip[4];
		is[2] = this.clip[11] + this.clip[8];
		is[3] = this.clip[15] + this.clip[12];
		this.normalizePlane(is);
		float[] js = this.frustumData[2];
		js[0] = this.clip[3] + this.clip[1];
		js[1] = this.clip[7] + this.clip[5];
		js[2] = this.clip[11] + this.clip[9];
		js[3] = this.clip[15] + this.clip[13];
		this.normalizePlane(js);
		float[] ks = this.frustumData[3];
		ks[0] = this.clip[3] - this.clip[1];
		ks[1] = this.clip[7] - this.clip[5];
		ks[2] = this.clip[11] - this.clip[9];
		ks[3] = this.clip[15] - this.clip[13];
		this.normalizePlane(ks);
		float[] ls = this.frustumData[4];
		ls[0] = this.clip[3] - this.clip[2];
		ls[1] = this.clip[7] - this.clip[6];
		ls[2] = this.clip[11] - this.clip[10];
		ls[3] = this.clip[15] - this.clip[14];
		this.normalizePlane(ls);
		float[] ms = this.frustumData[5];
		ms[0] = this.clip[3] + this.clip[2];
		ms[1] = this.clip[7] + this.clip[6];
		ms[2] = this.clip[11] + this.clip[10];
		ms[3] = this.clip[15] + this.clip[14];
		this.normalizePlane(ms);
	}
}
