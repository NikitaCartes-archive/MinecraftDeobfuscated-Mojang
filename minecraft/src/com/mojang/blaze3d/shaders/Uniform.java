package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ShaderProgramConfig;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class Uniform extends AbstractUniform implements AutoCloseable {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int UT_INT1 = 0;
	public static final int UT_INT2 = 1;
	public static final int UT_INT3 = 2;
	public static final int UT_INT4 = 3;
	public static final int UT_FLOAT1 = 4;
	public static final int UT_FLOAT2 = 5;
	public static final int UT_FLOAT3 = 6;
	public static final int UT_FLOAT4 = 7;
	public static final int UT_MAT2 = 8;
	public static final int UT_MAT3 = 9;
	public static final int UT_MAT4 = 10;
	private static final boolean TRANSPOSE_MATRICIES = false;
	private int location;
	private final int count;
	private final int type;
	private final IntBuffer intValues;
	private final FloatBuffer floatValues;
	private final String name;

	public Uniform(String string, int i, int j) {
		this.name = string;
		this.count = j;
		this.type = i;
		if (i <= 3) {
			this.intValues = MemoryUtil.memAllocInt(j);
			this.floatValues = null;
		} else {
			this.intValues = null;
			this.floatValues = MemoryUtil.memAllocFloat(j);
		}

		this.location = -1;
		this.markDirty();
	}

	public static int glGetUniformLocation(int i, CharSequence charSequence) {
		return GlStateManager._glGetUniformLocation(i, charSequence);
	}

	public static void uploadInteger(int i, int j) {
		RenderSystem.glUniform1i(i, j);
	}

	public void setFromConfig(ShaderProgramConfig.Uniform uniform) {
		this.setFromConfig(uniform.values(), uniform.count());
	}

	public void setFromConfig(List<Float> list, int i) {
		float[] fs = new float[Math.max(i, 16)];
		if (list.size() == 1) {
			Arrays.fill(fs, (Float)list.getFirst());
		} else {
			for (int j = 0; j < list.size(); j++) {
				fs[j] = (Float)list.get(j);
			}
		}

		if (this.type <= 3) {
			this.setSafe((int)fs[0], (int)fs[1], (int)fs[2], (int)fs[3]);
		} else if (this.type <= 7) {
			this.setSafe(fs[0], fs[1], fs[2], fs[3]);
		} else {
			this.set(Arrays.copyOfRange(fs, 0, i));
		}
	}

	public void close() {
		if (this.intValues != null) {
			MemoryUtil.memFree(this.intValues);
		}

		if (this.floatValues != null) {
			MemoryUtil.memFree(this.floatValues);
		}
	}

	private void markDirty() {
	}

	public static int getTypeFromString(String string) {
		int i = -1;
		if ("int".equals(string)) {
			i = 0;
		} else if ("float".equals(string)) {
			i = 4;
		} else if (string.startsWith("matrix")) {
			if (string.endsWith("2x2")) {
				i = 8;
			} else if (string.endsWith("3x3")) {
				i = 9;
			} else if (string.endsWith("4x4")) {
				i = 10;
			}
		}

		return i;
	}

	public void setLocation(int i) {
		this.location = i;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public final void set(float f) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.markDirty();
	}

	@Override
	public final void set(float f, float g) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.floatValues.put(1, g);
		this.markDirty();
	}

	public final void set(int i, float f) {
		this.floatValues.position(0);
		this.floatValues.put(i, f);
		this.markDirty();
	}

	@Override
	public final void set(float f, float g, float h) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.floatValues.put(1, g);
		this.floatValues.put(2, h);
		this.markDirty();
	}

	@Override
	public final void set(Vector3f vector3f) {
		this.floatValues.position(0);
		vector3f.get(this.floatValues);
		this.markDirty();
	}

	@Override
	public final void set(float f, float g, float h, float i) {
		this.floatValues.position(0);
		this.floatValues.put(f);
		this.floatValues.put(g);
		this.floatValues.put(h);
		this.floatValues.put(i);
		this.floatValues.flip();
		this.markDirty();
	}

	@Override
	public final void set(Vector4f vector4f) {
		this.floatValues.position(0);
		vector4f.get(this.floatValues);
		this.markDirty();
	}

	@Override
	public final void setSafe(float f, float g, float h, float i) {
		this.floatValues.position(0);
		if (this.type >= 4) {
			this.floatValues.put(0, f);
		}

		if (this.type >= 5) {
			this.floatValues.put(1, g);
		}

		if (this.type >= 6) {
			this.floatValues.put(2, h);
		}

		if (this.type >= 7) {
			this.floatValues.put(3, i);
		}

		this.markDirty();
	}

	@Override
	public final void setSafe(int i, int j, int k, int l) {
		this.intValues.position(0);
		if (this.type >= 0) {
			this.intValues.put(0, i);
		}

		if (this.type >= 1) {
			this.intValues.put(1, j);
		}

		if (this.type >= 2) {
			this.intValues.put(2, k);
		}

		if (this.type >= 3) {
			this.intValues.put(3, l);
		}

		this.markDirty();
	}

	@Override
	public final void set(int i) {
		this.intValues.position(0);
		this.intValues.put(0, i);
		this.markDirty();
	}

	@Override
	public final void set(int i, int j) {
		this.intValues.position(0);
		this.intValues.put(0, i);
		this.intValues.put(1, j);
		this.markDirty();
	}

	@Override
	public final void set(int i, int j, int k) {
		this.intValues.position(0);
		this.intValues.put(0, i);
		this.intValues.put(1, j);
		this.intValues.put(2, k);
		this.markDirty();
	}

	@Override
	public final void set(int i, int j, int k, int l) {
		this.intValues.position(0);
		this.intValues.put(0, i);
		this.intValues.put(1, j);
		this.intValues.put(2, k);
		this.intValues.put(3, l);
		this.markDirty();
	}

	@Override
	public final void set(float[] fs) {
		if (fs.length < this.count) {
			LOGGER.warn("Uniform.set called with a too-small value array (expected {}, got {}). Ignoring.", this.count, fs.length);
		} else {
			this.floatValues.position(0);
			this.floatValues.put(fs);
			this.floatValues.position(0);
			this.markDirty();
		}
	}

	@Override
	public final void setMat2x2(float f, float g, float h, float i) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.floatValues.put(1, g);
		this.floatValues.put(2, h);
		this.floatValues.put(3, i);
		this.markDirty();
	}

	@Override
	public final void setMat2x3(float f, float g, float h, float i, float j, float k) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.floatValues.put(1, g);
		this.floatValues.put(2, h);
		this.floatValues.put(3, i);
		this.floatValues.put(4, j);
		this.floatValues.put(5, k);
		this.markDirty();
	}

	@Override
	public final void setMat2x4(float f, float g, float h, float i, float j, float k, float l, float m) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.floatValues.put(1, g);
		this.floatValues.put(2, h);
		this.floatValues.put(3, i);
		this.floatValues.put(4, j);
		this.floatValues.put(5, k);
		this.floatValues.put(6, l);
		this.floatValues.put(7, m);
		this.markDirty();
	}

	@Override
	public final void setMat3x2(float f, float g, float h, float i, float j, float k) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.floatValues.put(1, g);
		this.floatValues.put(2, h);
		this.floatValues.put(3, i);
		this.floatValues.put(4, j);
		this.floatValues.put(5, k);
		this.markDirty();
	}

	@Override
	public final void setMat3x3(float f, float g, float h, float i, float j, float k, float l, float m, float n) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.floatValues.put(1, g);
		this.floatValues.put(2, h);
		this.floatValues.put(3, i);
		this.floatValues.put(4, j);
		this.floatValues.put(5, k);
		this.floatValues.put(6, l);
		this.floatValues.put(7, m);
		this.floatValues.put(8, n);
		this.markDirty();
	}

	@Override
	public final void setMat3x4(float f, float g, float h, float i, float j, float k, float l, float m, float n, float o, float p, float q) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.floatValues.put(1, g);
		this.floatValues.put(2, h);
		this.floatValues.put(3, i);
		this.floatValues.put(4, j);
		this.floatValues.put(5, k);
		this.floatValues.put(6, l);
		this.floatValues.put(7, m);
		this.floatValues.put(8, n);
		this.floatValues.put(9, o);
		this.floatValues.put(10, p);
		this.floatValues.put(11, q);
		this.markDirty();
	}

	@Override
	public final void setMat4x2(float f, float g, float h, float i, float j, float k, float l, float m) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.floatValues.put(1, g);
		this.floatValues.put(2, h);
		this.floatValues.put(3, i);
		this.floatValues.put(4, j);
		this.floatValues.put(5, k);
		this.floatValues.put(6, l);
		this.floatValues.put(7, m);
		this.markDirty();
	}

	@Override
	public final void setMat4x3(float f, float g, float h, float i, float j, float k, float l, float m, float n, float o, float p, float q) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.floatValues.put(1, g);
		this.floatValues.put(2, h);
		this.floatValues.put(3, i);
		this.floatValues.put(4, j);
		this.floatValues.put(5, k);
		this.floatValues.put(6, l);
		this.floatValues.put(7, m);
		this.floatValues.put(8, n);
		this.floatValues.put(9, o);
		this.floatValues.put(10, p);
		this.floatValues.put(11, q);
		this.markDirty();
	}

	@Override
	public final void setMat4x4(
		float f, float g, float h, float i, float j, float k, float l, float m, float n, float o, float p, float q, float r, float s, float t, float u
	) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.floatValues.put(1, g);
		this.floatValues.put(2, h);
		this.floatValues.put(3, i);
		this.floatValues.put(4, j);
		this.floatValues.put(5, k);
		this.floatValues.put(6, l);
		this.floatValues.put(7, m);
		this.floatValues.put(8, n);
		this.floatValues.put(9, o);
		this.floatValues.put(10, p);
		this.floatValues.put(11, q);
		this.floatValues.put(12, r);
		this.floatValues.put(13, s);
		this.floatValues.put(14, t);
		this.floatValues.put(15, u);
		this.markDirty();
	}

	@Override
	public final void set(Matrix4f matrix4f) {
		this.floatValues.position(0);
		matrix4f.get(this.floatValues);
		this.markDirty();
	}

	@Override
	public final void set(Matrix3f matrix3f) {
		this.floatValues.position(0);
		matrix3f.get(this.floatValues);
		this.markDirty();
	}

	public void upload() {
		if (this.type <= 3) {
			this.uploadAsInteger();
		} else if (this.type <= 7) {
			this.uploadAsFloat();
		} else {
			if (this.type > 10) {
				LOGGER.warn("Uniform.upload called, but type value ({}) is not a valid type. Ignoring.", this.type);
				return;
			}

			this.uploadAsMatrix();
		}
	}

	private void uploadAsInteger() {
		this.intValues.rewind();
		switch (this.type) {
			case 0:
				RenderSystem.glUniform1(this.location, this.intValues);
				break;
			case 1:
				RenderSystem.glUniform2(this.location, this.intValues);
				break;
			case 2:
				RenderSystem.glUniform3(this.location, this.intValues);
				break;
			case 3:
				RenderSystem.glUniform4(this.location, this.intValues);
				break;
			default:
				LOGGER.warn("Uniform.upload called, but count value ({}) is  not in the range of 1 to 4. Ignoring.", this.count);
		}
	}

	private void uploadAsFloat() {
		this.floatValues.rewind();
		switch (this.type) {
			case 4:
				RenderSystem.glUniform1(this.location, this.floatValues);
				break;
			case 5:
				RenderSystem.glUniform2(this.location, this.floatValues);
				break;
			case 6:
				RenderSystem.glUniform3(this.location, this.floatValues);
				break;
			case 7:
				RenderSystem.glUniform4(this.location, this.floatValues);
				break;
			default:
				LOGGER.warn("Uniform.upload called, but count value ({}) is not in the range of 1 to 4. Ignoring.", this.count);
		}
	}

	private void uploadAsMatrix() {
		this.floatValues.clear();
		switch (this.type) {
			case 8:
				RenderSystem.glUniformMatrix2(this.location, false, this.floatValues);
				break;
			case 9:
				RenderSystem.glUniformMatrix3(this.location, false, this.floatValues);
				break;
			case 10:
				RenderSystem.glUniformMatrix4(this.location, false, this.floatValues);
		}
	}

	public int getLocation() {
		return this.location;
	}

	public int getCount() {
		return this.count;
	}

	public int getType() {
		return this.type;
	}

	public IntBuffer getIntBuffer() {
		return this.intValues;
	}

	public FloatBuffer getFloatBuffer() {
		return this.floatValues;
	}
}
