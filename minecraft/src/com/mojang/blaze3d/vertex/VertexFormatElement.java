package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VertexFormatElement {
	private final VertexFormatElement.Type type;
	private final VertexFormatElement.Usage usage;
	private final int index;
	private final int count;
	private final int byteSize;

	public VertexFormatElement(int i, VertexFormatElement.Type type, VertexFormatElement.Usage usage, int j) {
		if (this.supportsUsage(i, usage)) {
			this.usage = usage;
			this.type = type;
			this.index = i;
			this.count = j;
			this.byteSize = type.getSize() * this.count;
		} else {
			throw new IllegalStateException("Multiple vertex elements of the same type other than UVs are not supported");
		}
	}

	private boolean supportsUsage(int i, VertexFormatElement.Usage usage) {
		return i == 0 || usage == VertexFormatElement.Usage.UV;
	}

	public final VertexFormatElement.Type getType() {
		return this.type;
	}

	public final VertexFormatElement.Usage getUsage() {
		return this.usage;
	}

	public final int getCount() {
		return this.count;
	}

	public final int getIndex() {
		return this.index;
	}

	public String toString() {
		return this.count + "," + this.usage.getName() + "," + this.type.getName();
	}

	public final int getByteSize() {
		return this.byteSize;
	}

	public final boolean isPosition() {
		return this.usage == VertexFormatElement.Usage.POSITION;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			VertexFormatElement vertexFormatElement = (VertexFormatElement)object;
			if (this.count != vertexFormatElement.count) {
				return false;
			} else if (this.index != vertexFormatElement.index) {
				return false;
			} else {
				return this.type != vertexFormatElement.type ? false : this.usage == vertexFormatElement.usage;
			}
		} else {
			return false;
		}
	}

	public int hashCode() {
		int i = this.type.hashCode();
		i = 31 * i + this.usage.hashCode();
		i = 31 * i + this.index;
		return 31 * i + this.count;
	}

	public void setupBufferState(int i, long l, int j) {
		this.usage.setupBufferState(this.count, this.type.getGlType(), j, l, this.index, i);
	}

	public void clearBufferState(int i) {
		this.usage.clearBufferState(this.index, i);
	}

	@Environment(EnvType.CLIENT)
	public static enum Type {
		FLOAT(4, "Float", 5126),
		UBYTE(1, "Unsigned Byte", 5121),
		BYTE(1, "Byte", 5120),
		USHORT(2, "Unsigned Short", 5123),
		SHORT(2, "Short", 5122),
		UINT(4, "Unsigned Int", 5125),
		INT(4, "Int", 5124);

		private final int size;
		private final String name;
		private final int glType;

		private Type(int j, String string2, int k) {
			this.size = j;
			this.name = string2;
			this.glType = k;
		}

		public int getSize() {
			return this.size;
		}

		public String getName() {
			return this.name;
		}

		public int getGlType() {
			return this.glType;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Usage {
		POSITION("Position", (i, j, k, l, m, n) -> {
			GlStateManager._enableVertexAttribArray(n);
			GlStateManager._vertexAttribPointer(n, i, j, false, k, l);
		}, (i, j) -> GlStateManager._disableVertexAttribArray(j)),
		NORMAL("Normal", (i, j, k, l, m, n) -> {
			GlStateManager._enableVertexAttribArray(n);
			GlStateManager._vertexAttribPointer(n, i, j, true, k, l);
		}, (i, j) -> GlStateManager._disableVertexAttribArray(j)),
		COLOR("Vertex Color", (i, j, k, l, m, n) -> {
			GlStateManager._enableVertexAttribArray(n);
			GlStateManager._vertexAttribPointer(n, i, j, true, k, l);
		}, (i, j) -> GlStateManager._disableVertexAttribArray(j)),
		UV("UV", (i, j, k, l, m, n) -> {
			GlStateManager._enableVertexAttribArray(n);
			if (j == 5126) {
				GlStateManager._vertexAttribPointer(n, i, j, false, k, l);
			} else {
				GlStateManager._vertexAttribIPointer(n, i, j, k, l);
			}
		}, (i, j) -> GlStateManager._disableVertexAttribArray(j)),
		PADDING("Padding", (i, j, k, l, m, n) -> {
		}, (i, j) -> {
		}),
		GENERIC("Generic", (i, j, k, l, m, n) -> {
			GlStateManager._enableVertexAttribArray(n);
			GlStateManager._vertexAttribPointer(n, i, j, false, k, l);
		}, (i, j) -> GlStateManager._disableVertexAttribArray(j));

		private final String name;
		private final VertexFormatElement.Usage.SetupState setupState;
		private final VertexFormatElement.Usage.ClearState clearState;

		private Usage(String string2, VertexFormatElement.Usage.SetupState setupState, VertexFormatElement.Usage.ClearState clearState) {
			this.name = string2;
			this.setupState = setupState;
			this.clearState = clearState;
		}

		private void setupBufferState(int i, int j, int k, long l, int m, int n) {
			this.setupState.setupBufferState(i, j, k, l, m, n);
		}

		public void clearBufferState(int i, int j) {
			this.clearState.clearBufferState(i, j);
		}

		public String getName() {
			return this.name;
		}

		@FunctionalInterface
		@Environment(EnvType.CLIENT)
		interface ClearState {
			void clearBufferState(int i, int j);
		}

		@FunctionalInterface
		@Environment(EnvType.CLIENT)
		interface SetupState {
			void setupBufferState(int i, int j, int k, long l, int m, int n);
		}
	}
}
