package net.minecraft.realms;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RealmsBufferBuilder {
	private BufferBuilder b;

	public RealmsBufferBuilder(BufferBuilder bufferBuilder) {
		this.b = bufferBuilder;
	}

	public RealmsBufferBuilder from(BufferBuilder bufferBuilder) {
		this.b = bufferBuilder;
		return this;
	}

	public void sortQuads(float f, float g, float h) {
		this.b.sortQuads(f, g, h);
	}

	public void fixupQuadColor(int i) {
		this.b.fixupQuadColor(i);
	}

	public ByteBuffer getBuffer() {
		return this.b.getBuffer();
	}

	public void postNormal(float f, float g, float h) {
		this.b.postNormal(f, g, h);
	}

	public int getDrawMode() {
		return this.b.getDrawMode();
	}

	public void offset(double d, double e, double f) {
		this.b.offset(d, e, f);
	}

	public void restoreState(BufferBuilder.State state) {
		this.b.restoreState(state);
	}

	public void endVertex() {
		this.b.endVertex();
	}

	public RealmsBufferBuilder normal(float f, float g, float h) {
		return this.from(this.b.normal(f, g, h));
	}

	public void end() {
		this.b.end();
	}

	public void begin(int i, VertexFormat vertexFormat) {
		this.b.begin(i, vertexFormat);
	}

	public RealmsBufferBuilder color(int i, int j, int k, int l) {
		return this.from(this.b.color(i, j, k, l));
	}

	public void faceTex2(int i, int j, int k, int l) {
		this.b.faceTex2(i, j, k, l);
	}

	public void postProcessFacePosition(double d, double e, double f) {
		this.b.postProcessFacePosition(d, e, f);
	}

	public void fixupVertexColor(float f, float g, float h, int i) {
		this.b.fixupVertexColor(f, g, h, i);
	}

	public RealmsBufferBuilder color(float f, float g, float h, float i) {
		return this.from(this.b.color(f, g, h, i));
	}

	public RealmsVertexFormat getVertexFormat() {
		return new RealmsVertexFormat(this.b.getVertexFormat());
	}

	public void faceTint(float f, float g, float h, int i) {
		this.b.faceTint(f, g, h, i);
	}

	public RealmsBufferBuilder tex2(int i, int j) {
		return this.from(this.b.uv2(i, j));
	}

	public void putBulkData(int[] is) {
		this.b.putBulkData(is);
	}

	public RealmsBufferBuilder tex(double d, double e) {
		return this.from(this.b.uv(d, e));
	}

	public int getVertexCount() {
		return this.b.getVertexCount();
	}

	public void clear() {
		this.b.clear();
	}

	public RealmsBufferBuilder vertex(double d, double e, double f) {
		return this.from(this.b.vertex(d, e, f));
	}

	public void fixupQuadColor(float f, float g, float h) {
		this.b.fixupQuadColor(f, g, h);
	}

	public void noColor() {
		this.b.noColor();
	}
}
