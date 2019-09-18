package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class Uniform extends AbstractUniform implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private int location;
	private final int count;
	private final int type;
	private final IntBuffer intValues;
	private final FloatBuffer floatValues;
	private final String name;
	private boolean dirty;
	private final Effect parent;

	public Uniform(String string, int i, int j, Effect effect) {
		this.name = string;
		this.count = j;
		this.type = i;
		this.parent = effect;
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

	public static int glGetAttribLocation(int i, CharSequence charSequence) {
		return GlStateManager._glGetAttribLocation(i, charSequence);
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
		this.dirty = true;
		if (this.parent != null) {
			this.parent.markDirty();
		}
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
	public void set(float f) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.markDirty();
	}

	@Override
	public void set(float f, float g) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.floatValues.put(1, g);
		this.markDirty();
	}

	@Override
	public void set(float f, float g, float h) {
		this.floatValues.position(0);
		this.floatValues.put(0, f);
		this.floatValues.put(1, g);
		this.floatValues.put(2, h);
		this.markDirty();
	}

	@Override
	public void set(float f, float g, float h, float i) {
		this.floatValues.position(0);
		this.floatValues.put(f);
		this.floatValues.put(g);
		this.floatValues.put(h);
		this.floatValues.put(i);
		this.floatValues.flip();
		this.markDirty();
	}

	@Override
	public void setSafe(float f, float g, float h, float i) {
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
	public void setSafe(int i, int j, int k, int l) {
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
	public void set(float[] fs) {
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
	public void set(Matrix4f matrix4f) {
		this.floatValues.position(0);
		matrix4f.store(this.floatValues);
		this.markDirty();
	}

	public void upload() {
		if (!this.dirty) {
		}

		this.dirty = false;
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
		this.floatValues.clear();
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
		this.floatValues.clear();
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
}
