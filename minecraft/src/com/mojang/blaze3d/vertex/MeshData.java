package com.mojang.blaze3d.vertex;

import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.mutable.MutableLong;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class MeshData implements AutoCloseable {
	private final ByteBufferBuilder.Result vertexBuffer;
	@Nullable
	private ByteBufferBuilder.Result indexBuffer;
	private final MeshData.DrawState drawState;

	public MeshData(ByteBufferBuilder.Result result, MeshData.DrawState drawState) {
		this.vertexBuffer = result;
		this.drawState = drawState;
	}

	private static Vector3f[] unpackQuadCentroids(ByteBuffer byteBuffer, int i, VertexFormat vertexFormat) {
		int j = vertexFormat.getOffset(VertexFormatElement.POSITION);
		if (j == -1) {
			throw new IllegalArgumentException("Cannot identify quad centers with no position element");
		} else {
			FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
			int k = vertexFormat.getVertexSize() / 4;
			int l = k * 4;
			int m = i / 4;
			Vector3f[] vector3fs = new Vector3f[m];

			for (int n = 0; n < m; n++) {
				int o = n * l + j;
				int p = o + k * 2;
				float f = floatBuffer.get(o + 0);
				float g = floatBuffer.get(o + 1);
				float h = floatBuffer.get(o + 2);
				float q = floatBuffer.get(p + 0);
				float r = floatBuffer.get(p + 1);
				float s = floatBuffer.get(p + 2);
				vector3fs[n] = new Vector3f((f + q) / 2.0F, (g + r) / 2.0F, (h + s) / 2.0F);
			}

			return vector3fs;
		}
	}

	public ByteBuffer vertexBuffer() {
		return this.vertexBuffer.byteBuffer();
	}

	@Nullable
	public ByteBuffer indexBuffer() {
		return this.indexBuffer != null ? this.indexBuffer.byteBuffer() : null;
	}

	public MeshData.DrawState drawState() {
		return this.drawState;
	}

	@Nullable
	public MeshData.SortState sortQuads(ByteBufferBuilder byteBufferBuilder, VertexSorting vertexSorting) {
		if (this.drawState.mode() != VertexFormat.Mode.QUADS) {
			return null;
		} else {
			Vector3f[] vector3fs = unpackQuadCentroids(this.vertexBuffer.byteBuffer(), this.drawState.vertexCount(), this.drawState.format());
			MeshData.SortState sortState = new MeshData.SortState(vector3fs, this.drawState.indexType());
			this.indexBuffer = sortState.buildSortedIndexBuffer(byteBufferBuilder, vertexSorting);
			return sortState;
		}
	}

	public void close() {
		this.vertexBuffer.close();
		if (this.indexBuffer != null) {
			this.indexBuffer.close();
		}
	}

	@Environment(EnvType.CLIENT)
	public static record DrawState(VertexFormat format, int vertexCount, int indexCount, VertexFormat.Mode mode, VertexFormat.IndexType indexType) {
	}

	@Environment(EnvType.CLIENT)
	public static record SortState(Vector3f[] centroids, VertexFormat.IndexType indexType) {
		@Nullable
		public ByteBufferBuilder.Result buildSortedIndexBuffer(ByteBufferBuilder byteBufferBuilder, VertexSorting vertexSorting) {
			int[] is = vertexSorting.sort(this.centroids);
			long l = byteBufferBuilder.reserve(is.length * 6 * this.indexType.bytes);
			IntConsumer intConsumer = this.indexWriter(l, this.indexType);

			for (int i : is) {
				intConsumer.accept(i * 4 + 0);
				intConsumer.accept(i * 4 + 1);
				intConsumer.accept(i * 4 + 2);
				intConsumer.accept(i * 4 + 2);
				intConsumer.accept(i * 4 + 3);
				intConsumer.accept(i * 4 + 0);
			}

			return byteBufferBuilder.build();
		}

		private IntConsumer indexWriter(long l, VertexFormat.IndexType indexType) {
			MutableLong mutableLong = new MutableLong(l);

			return switch (indexType) {
				case SHORT -> i -> MemoryUtil.memPutShort(mutableLong.getAndAdd(2L), (short)i);
				case INT -> i -> MemoryUtil.memPutInt(mutableLong.getAndAdd(4L), i);
			};
		}
	}
}
