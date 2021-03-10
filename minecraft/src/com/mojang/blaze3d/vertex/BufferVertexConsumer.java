package com.mojang.blaze3d.vertex;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public interface BufferVertexConsumer extends VertexConsumer {
	VertexFormatElement currentElement();

	void nextElement();

	void putByte(int i, byte b);

	void putShort(int i, short s);

	void putFloat(int i, float f);

	@Override
	default VertexConsumer vertex(double d, double e, double f) {
		if (this.currentElement().getUsage() != VertexFormatElement.Usage.POSITION) {
			return this;
		} else if (this.currentElement().getType() == VertexFormatElement.Type.FLOAT && this.currentElement().getCount() == 3) {
			this.putFloat(0, (float)d);
			this.putFloat(4, (float)e);
			this.putFloat(8, (float)f);
			this.nextElement();
			return this;
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	default VertexConsumer color(int i, int j, int k, int l) {
		VertexFormatElement vertexFormatElement = this.currentElement();
		if (vertexFormatElement.getUsage() != VertexFormatElement.Usage.COLOR) {
			return this;
		} else if (vertexFormatElement.getType() == VertexFormatElement.Type.UBYTE && vertexFormatElement.getCount() == 4) {
			this.putByte(0, (byte)i);
			this.putByte(1, (byte)j);
			this.putByte(2, (byte)k);
			this.putByte(3, (byte)l);
			this.nextElement();
			return this;
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	default VertexConsumer uv(float f, float g) {
		VertexFormatElement vertexFormatElement = this.currentElement();
		if (vertexFormatElement.getUsage() == VertexFormatElement.Usage.UV && vertexFormatElement.getIndex() == 0) {
			if (vertexFormatElement.getType() == VertexFormatElement.Type.FLOAT && vertexFormatElement.getCount() == 2) {
				this.putFloat(0, f);
				this.putFloat(4, g);
				this.nextElement();
				return this;
			} else {
				throw new IllegalStateException();
			}
		} else {
			return this;
		}
	}

	@Override
	default VertexConsumer overlayCoords(int i, int j) {
		return this.uvShort((short)i, (short)j, 1);
	}

	@Override
	default VertexConsumer uv2(int i, int j) {
		return this.uvShort((short)i, (short)j, 2);
	}

	default VertexConsumer uvShort(short s, short t, int i) {
		VertexFormatElement vertexFormatElement = this.currentElement();
		if (vertexFormatElement.getUsage() != VertexFormatElement.Usage.UV || vertexFormatElement.getIndex() != i) {
			return this;
		} else if (vertexFormatElement.getType() == VertexFormatElement.Type.SHORT && vertexFormatElement.getCount() == 2) {
			this.putShort(0, s);
			this.putShort(2, t);
			this.nextElement();
			return this;
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	default VertexConsumer normal(float f, float g, float h) {
		VertexFormatElement vertexFormatElement = this.currentElement();
		if (vertexFormatElement.getUsage() != VertexFormatElement.Usage.NORMAL) {
			return this;
		} else if (vertexFormatElement.getType() == VertexFormatElement.Type.BYTE && vertexFormatElement.getCount() == 3) {
			this.putByte(0, normalIntValue(f));
			this.putByte(1, normalIntValue(g));
			this.putByte(2, normalIntValue(h));
			this.nextElement();
			return this;
		} else {
			throw new IllegalStateException();
		}
	}

	static byte normalIntValue(float f) {
		return (byte)((int)(Mth.clamp(f, -1.0F, 1.0F) * 127.0F) & 0xFF);
	}
}
