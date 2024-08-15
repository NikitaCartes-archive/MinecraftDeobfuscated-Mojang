package com.mojang.blaze3d.vertex;

import java.nio.ByteOrder;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class BufferBuilder implements VertexConsumer {
	private static final long NOT_BUILDING = -1L;
	private static final long UNKNOWN_ELEMENT = -1L;
	private static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
	private final ByteBufferBuilder buffer;
	private long vertexPointer = -1L;
	private int vertices;
	private final VertexFormat format;
	private final VertexFormat.Mode mode;
	private final boolean fastFormat;
	private final boolean fullFormat;
	private final int vertexSize;
	private final int initialElementsToFill;
	private final int[] offsetsByElement;
	private int elementsToFill;
	private boolean building = true;

	public BufferBuilder(ByteBufferBuilder byteBufferBuilder, VertexFormat.Mode mode, VertexFormat vertexFormat) {
		if (!vertexFormat.contains(VertexFormatElement.POSITION)) {
			throw new IllegalArgumentException("Cannot build mesh with no position element");
		} else {
			this.buffer = byteBufferBuilder;
			this.mode = mode;
			this.format = vertexFormat;
			this.vertexSize = vertexFormat.getVertexSize();
			this.initialElementsToFill = vertexFormat.getElementsMask() & ~VertexFormatElement.POSITION.mask();
			this.offsetsByElement = vertexFormat.getOffsetsByElement();
			boolean bl = vertexFormat == DefaultVertexFormat.NEW_ENTITY;
			boolean bl2 = vertexFormat == DefaultVertexFormat.BLOCK;
			this.fastFormat = bl || bl2;
			this.fullFormat = bl;
		}
	}

	@Nullable
	public MeshData build() {
		this.ensureBuilding();
		this.endLastVertex();
		MeshData meshData = this.storeMesh();
		this.building = false;
		this.vertexPointer = -1L;
		return meshData;
	}

	public MeshData buildOrThrow() {
		MeshData meshData = this.build();
		if (meshData == null) {
			throw new IllegalStateException("BufferBuilder was empty");
		} else {
			return meshData;
		}
	}

	private void ensureBuilding() {
		if (!this.building) {
			throw new IllegalStateException("Not building!");
		}
	}

	@Nullable
	private MeshData storeMesh() {
		if (this.vertices == 0) {
			return null;
		} else {
			ByteBufferBuilder.Result result = this.buffer.build();
			if (result == null) {
				return null;
			} else {
				int i = this.mode.indexCount(this.vertices);
				VertexFormat.IndexType indexType = VertexFormat.IndexType.least(this.vertices);
				return new MeshData(result, new MeshData.DrawState(this.format, this.vertices, i, this.mode, indexType));
			}
		}
	}

	private long beginVertex() {
		this.ensureBuilding();
		this.endLastVertex();
		this.vertices++;
		long l = this.buffer.reserve(this.vertexSize);
		this.vertexPointer = l;
		return l;
	}

	private long beginElement(VertexFormatElement vertexFormatElement) {
		int i = this.elementsToFill;
		int j = i & ~vertexFormatElement.mask();
		if (j == i) {
			return -1L;
		} else {
			this.elementsToFill = j;
			long l = this.vertexPointer;
			if (l == -1L) {
				throw new IllegalArgumentException("Not currently building vertex");
			} else {
				return l + (long)this.offsetsByElement[vertexFormatElement.id()];
			}
		}
	}

	private void endLastVertex() {
		if (this.vertices != 0) {
			if (this.elementsToFill != 0) {
				String string = (String)VertexFormatElement.elementsFromMask(this.elementsToFill).map(this.format::getElementName).collect(Collectors.joining(", "));
				throw new IllegalStateException("Missing elements in vertex: " + string);
			} else {
				if (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP) {
					long l = this.buffer.reserve(this.vertexSize);
					MemoryUtil.memCopy(l - (long)this.vertexSize, l, (long)this.vertexSize);
					this.vertices++;
				}
			}
		}
	}

	private static void putRgba(long l, int i) {
		int j = ARGB.toABGR(i);
		MemoryUtil.memPutInt(l, IS_LITTLE_ENDIAN ? j : Integer.reverseBytes(j));
	}

	private static void putPackedUv(long l, int i) {
		if (IS_LITTLE_ENDIAN) {
			MemoryUtil.memPutInt(l, i);
		} else {
			MemoryUtil.memPutShort(l, (short)(i & 65535));
			MemoryUtil.memPutShort(l + 2L, (short)(i >> 16 & 65535));
		}
	}

	@Override
	public VertexConsumer addVertex(float f, float g, float h) {
		long l = this.beginVertex() + (long)this.offsetsByElement[VertexFormatElement.POSITION.id()];
		this.elementsToFill = this.initialElementsToFill;
		MemoryUtil.memPutFloat(l, f);
		MemoryUtil.memPutFloat(l + 4L, g);
		MemoryUtil.memPutFloat(l + 8L, h);
		return this;
	}

	@Override
	public VertexConsumer setColor(int i, int j, int k, int l) {
		long m = this.beginElement(VertexFormatElement.COLOR);
		if (m != -1L) {
			MemoryUtil.memPutByte(m, (byte)i);
			MemoryUtil.memPutByte(m + 1L, (byte)j);
			MemoryUtil.memPutByte(m + 2L, (byte)k);
			MemoryUtil.memPutByte(m + 3L, (byte)l);
		}

		return this;
	}

	@Override
	public VertexConsumer setColor(int i) {
		long l = this.beginElement(VertexFormatElement.COLOR);
		if (l != -1L) {
			putRgba(l, i);
		}

		return this;
	}

	@Override
	public VertexConsumer setUv(float f, float g) {
		long l = this.beginElement(VertexFormatElement.UV0);
		if (l != -1L) {
			MemoryUtil.memPutFloat(l, f);
			MemoryUtil.memPutFloat(l + 4L, g);
		}

		return this;
	}

	@Override
	public VertexConsumer setUv1(int i, int j) {
		return this.uvShort((short)i, (short)j, VertexFormatElement.UV1);
	}

	@Override
	public VertexConsumer setOverlay(int i) {
		long l = this.beginElement(VertexFormatElement.UV1);
		if (l != -1L) {
			putPackedUv(l, i);
		}

		return this;
	}

	@Override
	public VertexConsumer setUv2(int i, int j) {
		return this.uvShort((short)i, (short)j, VertexFormatElement.UV2);
	}

	@Override
	public VertexConsumer setLight(int i) {
		long l = this.beginElement(VertexFormatElement.UV2);
		if (l != -1L) {
			putPackedUv(l, i);
		}

		return this;
	}

	private VertexConsumer uvShort(short s, short t, VertexFormatElement vertexFormatElement) {
		long l = this.beginElement(vertexFormatElement);
		if (l != -1L) {
			MemoryUtil.memPutShort(l, s);
			MemoryUtil.memPutShort(l + 2L, t);
		}

		return this;
	}

	@Override
	public VertexConsumer setNormal(float f, float g, float h) {
		long l = this.beginElement(VertexFormatElement.NORMAL);
		if (l != -1L) {
			MemoryUtil.memPutByte(l, normalIntValue(f));
			MemoryUtil.memPutByte(l + 1L, normalIntValue(g));
			MemoryUtil.memPutByte(l + 2L, normalIntValue(h));
		}

		return this;
	}

	private static byte normalIntValue(float f) {
		return (byte)((int)(Mth.clamp(f, -1.0F, 1.0F) * 127.0F) & 0xFF);
	}

	@Override
	public void addVertex(float f, float g, float h, int i, float j, float k, int l, int m, float n, float o, float p) {
		if (this.fastFormat) {
			long q = this.beginVertex();
			MemoryUtil.memPutFloat(q + 0L, f);
			MemoryUtil.memPutFloat(q + 4L, g);
			MemoryUtil.memPutFloat(q + 8L, h);
			putRgba(q + 12L, i);
			MemoryUtil.memPutFloat(q + 16L, j);
			MemoryUtil.memPutFloat(q + 20L, k);
			long r;
			if (this.fullFormat) {
				putPackedUv(q + 24L, l);
				r = q + 28L;
			} else {
				r = q + 24L;
			}

			putPackedUv(r + 0L, m);
			MemoryUtil.memPutByte(r + 4L, normalIntValue(n));
			MemoryUtil.memPutByte(r + 5L, normalIntValue(o));
			MemoryUtil.memPutByte(r + 6L, normalIntValue(p));
		} else {
			VertexConsumer.super.addVertex(f, g, h, i, j, k, l, m, n, o, p);
		}
	}
}
