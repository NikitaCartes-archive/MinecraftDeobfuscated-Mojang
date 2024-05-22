package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record VertexFormatElement(int id, int index, VertexFormatElement.Type type, VertexFormatElement.Usage usage, int count) {
	public static final int MAX_COUNT = 32;
	private static final VertexFormatElement[] BY_ID = new VertexFormatElement[32];
	private static final List<VertexFormatElement> ELEMENTS = new ArrayList(32);
	public static final VertexFormatElement POSITION = register(0, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
	public static final VertexFormatElement COLOR = register(1, 0, VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.COLOR, 4);
	public static final VertexFormatElement UV0 = register(2, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 2);
	public static final VertexFormatElement UV = UV0;
	public static final VertexFormatElement UV1 = register(3, 1, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.UV, 2);
	public static final VertexFormatElement UV2 = register(4, 2, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.UV, 2);
	public static final VertexFormatElement NORMAL = register(5, 0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.NORMAL, 3);

	public VertexFormatElement(int id, int index, VertexFormatElement.Type type, VertexFormatElement.Usage usage, int count) {
		if (id < 0 || id >= BY_ID.length) {
			throw new IllegalArgumentException("Element ID must be in range [0; " + BY_ID.length + ")");
		} else if (!this.supportsUsage(index, usage)) {
			throw new IllegalStateException("Multiple vertex elements of the same type other than UVs are not supported");
		} else {
			this.id = id;
			this.index = index;
			this.type = type;
			this.usage = usage;
			this.count = count;
		}
	}

	public static VertexFormatElement register(int i, int j, VertexFormatElement.Type type, VertexFormatElement.Usage usage, int k) {
		VertexFormatElement vertexFormatElement = new VertexFormatElement(i, j, type, usage, k);
		if (BY_ID[i] != null) {
			throw new IllegalArgumentException("Duplicate element registration for: " + i);
		} else {
			BY_ID[i] = vertexFormatElement;
			ELEMENTS.add(vertexFormatElement);
			return vertexFormatElement;
		}
	}

	private boolean supportsUsage(int i, VertexFormatElement.Usage usage) {
		return i == 0 || usage == VertexFormatElement.Usage.UV;
	}

	public String toString() {
		return this.count + "," + this.usage + "," + this.type + " (" + this.id + ")";
	}

	public int mask() {
		return 1 << this.id;
	}

	public int byteSize() {
		return this.type.size() * this.count;
	}

	public void setupBufferState(int i, long l, int j) {
		this.usage.setupState.setupBufferState(this.count, this.type.glType(), j, l, i);
	}

	@Nullable
	public static VertexFormatElement byId(int i) {
		return BY_ID[i];
	}

	public static Stream<VertexFormatElement> elementsFromMask(int i) {
		return ELEMENTS.stream().filter(vertexFormatElement -> vertexFormatElement != null && (i & vertexFormatElement.mask()) != 0);
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

		private Type(final int j, final String string2, final int k) {
			this.size = j;
			this.name = string2;
			this.glType = k;
		}

		public int size() {
			return this.size;
		}

		public int glType() {
			return this.glType;
		}

		public String toString() {
			return this.name;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Usage {
		POSITION("Position", (i, j, k, l, m) -> GlStateManager._vertexAttribPointer(m, i, j, false, k, l)),
		NORMAL("Normal", (i, j, k, l, m) -> GlStateManager._vertexAttribPointer(m, i, j, true, k, l)),
		COLOR("Vertex Color", (i, j, k, l, m) -> GlStateManager._vertexAttribPointer(m, i, j, true, k, l)),
		UV("UV", (i, j, k, l, m) -> {
			if (j == 5126) {
				GlStateManager._vertexAttribPointer(m, i, j, false, k, l);
			} else {
				GlStateManager._vertexAttribIPointer(m, i, j, k, l);
			}
		}),
		GENERIC("Generic", (i, j, k, l, m) -> GlStateManager._vertexAttribPointer(m, i, j, false, k, l));

		private final String name;
		final VertexFormatElement.Usage.SetupState setupState;

		private Usage(final String string2, final VertexFormatElement.Usage.SetupState setupState) {
			this.name = string2;
			this.setupState = setupState;
		}

		public String toString() {
			return this.name;
		}

		@FunctionalInterface
		@Environment(EnvType.CLIENT)
		interface SetupState {
			void setupBufferState(int i, int j, int k, long l, int m);
		}
	}
}
