package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VertexFormat {
	public static final int UNKNOWN_ELEMENT = -1;
	private final List<VertexFormatElement> elements;
	private final List<String> names;
	private final int vertexSize;
	private final int elementsMask;
	private final int[] offsetsByElement = new int[32];
	@Nullable
	private VertexBuffer immediateDrawVertexBuffer;

	VertexFormat(List<VertexFormatElement> list, List<String> list2, IntList intList, int i) {
		this.elements = list;
		this.names = list2;
		this.vertexSize = i;
		this.elementsMask = list.stream().mapToInt(VertexFormatElement::mask).reduce(0, (ix, jx) -> ix | jx);

		for (int j = 0; j < this.offsetsByElement.length; j++) {
			VertexFormatElement vertexFormatElement = VertexFormatElement.byId(j);
			int k = vertexFormatElement != null ? list.indexOf(vertexFormatElement) : -1;
			this.offsetsByElement[j] = k != -1 ? intList.getInt(k) : -1;
		}
	}

	public static VertexFormat.Builder builder() {
		return new VertexFormat.Builder();
	}

	public void bindAttributes(int i) {
		int j = 0;

		for (String string : this.getElementAttributeNames()) {
			GlStateManager._glBindAttribLocation(i, j, string);
			j++;
		}
	}

	public String toString() {
		return "VertexFormat" + this.names;
	}

	public int getVertexSize() {
		return this.vertexSize;
	}

	public List<VertexFormatElement> getElements() {
		return this.elements;
	}

	public List<String> getElementAttributeNames() {
		return this.names;
	}

	public int[] getOffsetsByElement() {
		return this.offsetsByElement;
	}

	public int getOffset(VertexFormatElement vertexFormatElement) {
		return this.offsetsByElement[vertexFormatElement.id()];
	}

	public boolean contains(VertexFormatElement vertexFormatElement) {
		return (this.elementsMask & vertexFormatElement.mask()) != 0;
	}

	public int getElementsMask() {
		return this.elementsMask;
	}

	public String getElementName(VertexFormatElement vertexFormatElement) {
		int i = this.elements.indexOf(vertexFormatElement);
		if (i == -1) {
			throw new IllegalArgumentException(vertexFormatElement + " is not contained in format");
		} else {
			return (String)this.names.get(i);
		}
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof VertexFormat vertexFormat
				&& this.elementsMask == vertexFormat.elementsMask
				&& this.vertexSize == vertexFormat.vertexSize
				&& this.names.equals(vertexFormat.names)
				&& Arrays.equals(this.offsetsByElement, vertexFormat.offsetsByElement)) {
				return true;
			}

			return false;
		}
	}

	public int hashCode() {
		return this.elementsMask * 31 + Arrays.hashCode(this.offsetsByElement);
	}

	public void setupBufferState() {
		RenderSystem.assertOnRenderThread();
		int i = this.getVertexSize();

		for (int j = 0; j < this.elements.size(); j++) {
			GlStateManager._enableVertexAttribArray(j);
			VertexFormatElement vertexFormatElement = (VertexFormatElement)this.elements.get(j);
			vertexFormatElement.setupBufferState(j, (long)this.getOffset(vertexFormatElement), i);
		}
	}

	public void clearBufferState() {
		RenderSystem.assertOnRenderThread();

		for (int i = 0; i < this.elements.size(); i++) {
			GlStateManager._disableVertexAttribArray(i);
		}
	}

	public VertexBuffer getImmediateDrawVertexBuffer() {
		VertexBuffer vertexBuffer = this.immediateDrawVertexBuffer;
		if (vertexBuffer == null) {
			this.immediateDrawVertexBuffer = vertexBuffer = new VertexBuffer(BufferUsage.DYNAMIC_WRITE);
		}

		return vertexBuffer;
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final ImmutableMap.Builder<String, VertexFormatElement> elements = ImmutableMap.builder();
		private final IntList offsets = new IntArrayList();
		private int offset;

		Builder() {
		}

		public VertexFormat.Builder add(String string, VertexFormatElement vertexFormatElement) {
			this.elements.put(string, vertexFormatElement);
			this.offsets.add(this.offset);
			this.offset = this.offset + vertexFormatElement.byteSize();
			return this;
		}

		public VertexFormat.Builder padding(int i) {
			this.offset += i;
			return this;
		}

		public VertexFormat build() {
			ImmutableMap<String, VertexFormatElement> immutableMap = this.elements.buildOrThrow();
			ImmutableList<VertexFormatElement> immutableList = immutableMap.values().asList();
			ImmutableList<String> immutableList2 = immutableMap.keySet().asList();
			return new VertexFormat(immutableList, immutableList2, this.offsets, this.offset);
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum IndexType {
		SHORT(5123, 2),
		INT(5125, 4);

		public final int asGLType;
		public final int bytes;

		private IndexType(final int j, final int k) {
			this.asGLType = j;
			this.bytes = k;
		}

		public static VertexFormat.IndexType least(int i) {
			return (i & -65536) != 0 ? INT : SHORT;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Mode {
		LINES(4, 2, 2, false),
		LINE_STRIP(5, 2, 1, true),
		DEBUG_LINES(1, 2, 2, false),
		DEBUG_LINE_STRIP(3, 2, 1, true),
		TRIANGLES(4, 3, 3, false),
		TRIANGLE_STRIP(5, 3, 1, true),
		TRIANGLE_FAN(6, 3, 1, true),
		QUADS(4, 4, 4, false);

		public final int asGLMode;
		public final int primitiveLength;
		public final int primitiveStride;
		public final boolean connectedPrimitives;

		private Mode(final int j, final int k, final int l, final boolean bl) {
			this.asGLMode = j;
			this.primitiveLength = k;
			this.primitiveStride = l;
			this.connectedPrimitives = bl;
		}

		public int indexCount(int i) {
			return switch (this) {
				case LINES, QUADS -> i / 4 * 6;
				case LINE_STRIP, DEBUG_LINES, DEBUG_LINE_STRIP, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN -> i;
				default -> 0;
			};
		}
	}
}
