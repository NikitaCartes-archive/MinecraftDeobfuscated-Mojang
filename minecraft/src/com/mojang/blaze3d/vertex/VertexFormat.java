package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VertexFormat {
	private final ImmutableList<VertexFormatElement> elements;
	private final ImmutableMap<String, VertexFormatElement> elementMapping;
	private final IntList offsets = new IntArrayList();
	private final int vertexSize;
	@Nullable
	private VertexBuffer immediateDrawVertexBuffer;

	public VertexFormat(ImmutableMap<String, VertexFormatElement> immutableMap) {
		this.elementMapping = immutableMap;
		this.elements = immutableMap.values().asList();
		int i = 0;

		for (VertexFormatElement vertexFormatElement : immutableMap.values()) {
			this.offsets.add(i);
			i += vertexFormatElement.getByteSize();
		}

		this.vertexSize = i;
	}

	public String toString() {
		return "format: "
			+ this.elementMapping.size()
			+ " elements: "
			+ (String)this.elementMapping.entrySet().stream().map(Object::toString).collect(Collectors.joining(" "));
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

	public ImmutableList<String> getElementAttributeNames() {
		return this.elementMapping.keySet().asList();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			VertexFormat vertexFormat = (VertexFormat)object;
			return this.vertexSize != vertexFormat.vertexSize ? false : this.elementMapping.equals(vertexFormat.elementMapping);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return this.elementMapping.hashCode();
	}

	public void setupBufferState() {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(this::_setupBufferState);
		} else {
			this._setupBufferState();
		}
	}

	private void _setupBufferState() {
		int i = this.getVertexSize();
		List<VertexFormatElement> list = this.getElements();

		for (int j = 0; j < list.size(); j++) {
			((VertexFormatElement)list.get(j)).setupBufferState(j, (long)this.offsets.getInt(j), i);
		}
	}

	public void clearBufferState() {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(this::_clearBufferState);
		} else {
			this._clearBufferState();
		}
	}

	private void _clearBufferState() {
		ImmutableList<VertexFormatElement> immutableList = this.getElements();

		for (int i = 0; i < immutableList.size(); i++) {
			VertexFormatElement vertexFormatElement = (VertexFormatElement)immutableList.get(i);
			vertexFormatElement.clearBufferState(i);
		}
	}

	public VertexBuffer getImmediateDrawVertexBuffer() {
		VertexBuffer vertexBuffer = this.immediateDrawVertexBuffer;
		if (vertexBuffer == null) {
			this.immediateDrawVertexBuffer = vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
		}

		return vertexBuffer;
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
