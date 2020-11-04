package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VertexFormat {
	private final ImmutableList<VertexFormatElement> elements;
	private final IntList offsets = new IntArrayList();
	private final int vertexSize;

	public VertexFormat(ImmutableList<VertexFormatElement> immutableList) {
		this.elements = immutableList;
		int i = 0;

		for (VertexFormatElement vertexFormatElement : immutableList) {
			this.offsets.add(i);
			i += vertexFormatElement.getByteSize();
		}

		this.vertexSize = i;
	}

	public String toString() {
		return "format: " + this.elements.size() + " elements: " + (String)this.elements.stream().map(Object::toString).collect(Collectors.joining(" "));
	}

	public int getIntegerSize() {
		return this.getVertexSize() / 4;
	}

	public int getVertexSize() {
		return this.vertexSize;
	}

	public ImmutableList<VertexFormatElement> getElements() {
		return this.elements;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			VertexFormat vertexFormat = (VertexFormat)object;
			return this.vertexSize != vertexFormat.vertexSize ? false : this.elements.equals(vertexFormat.elements);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return this.elements.hashCode();
	}

	public void setupBufferState(long l) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> this.setupBufferState(l));
		} else {
			int i = this.getVertexSize();
			List<VertexFormatElement> list = this.getElements();

			for (int j = 0; j < list.size(); j++) {
				((VertexFormatElement)list.get(j)).setupBufferState(l + (long)this.offsets.getInt(j), i);
			}
		}
	}

	public void clearBufferState() {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(this::clearBufferState);
		} else {
			for (VertexFormatElement vertexFormatElement : this.getElements()) {
				vertexFormatElement.clearBufferState();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum IndexType {
		BYTE(5121, 1),
		SHORT(5123, 2),
		INT(5125, 4);

		public final int asGLType;
		public final int bytes;

		private IndexType(int j, int k) {
			this.asGLType = j;
			this.bytes = k;
		}

		public static VertexFormat.IndexType least(int i) {
			if ((i & -65536) != 0) {
				return INT;
			} else {
				return (i & 0xFF00) != 0 ? SHORT : BYTE;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Mode {
		LINES(1, 2, 2),
		LINE_STRIP(3, 2, 1),
		TRIANGLES(4, 3, 3),
		TRIANGLE_STRIP(5, 3, 1),
		TRIANGLE_FAN(6, 3, 1),
		QUADS(4, 4, 4);

		public final int asGLMode;
		public final int primitiveLength;
		public final int primitiveStride;

		private Mode(int j, int k, int l) {
			this.asGLMode = j;
			this.primitiveLength = k;
			this.primitiveStride = l;
		}

		public int indexCount(int i) {
			int j;
			switch (this) {
				case LINES:
				case LINE_STRIP:
				case TRIANGLES:
				case TRIANGLE_STRIP:
				case TRIANGLE_FAN:
					j = i;
					break;
				case QUADS:
					j = i / 4 * 6;
					break;
				default:
					j = 0;
			}

			return j;
		}
	}
}
