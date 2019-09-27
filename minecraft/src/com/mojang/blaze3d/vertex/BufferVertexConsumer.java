package com.mojang.blaze3d.vertex;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface BufferVertexConsumer extends VertexConsumer {
	VertexFormatElement currentElement();

	void nextElement();

	void putByte(int i, byte b);

	void putShort(int i, short s);

	void putFloat(int i, float f);

	@Override
	default VertexConsumer vertex(double d, double e, double f) {
		if (this.currentElement().getType() != VertexFormatElement.Type.FLOAT) {
			throw new IllegalStateException();
		} else {
			this.putFloat(0, (float)d);
			this.putFloat(4, (float)e);
			this.putFloat(8, (float)f);
			this.nextElement();
			return this;
		}
	}

	@Override
	default VertexConsumer color(int i, int j, int k, int l) {
		if (this.currentElement().getUsage() != VertexFormatElement.Usage.COLOR) {
			return this;
		} else if (this.currentElement().getType() != VertexFormatElement.Type.UBYTE) {
			throw new IllegalStateException();
		} else {
			this.putByte(0, (byte)i);
			this.putByte(1, (byte)j);
			this.putByte(2, (byte)k);
			this.putByte(3, (byte)l);
			this.nextElement();
			return this;
		}
	}

	@Override
	default VertexConsumer uv(float f, float g) {
		if (this.currentElement().getUsage() == VertexFormatElement.Usage.UV && this.currentElement().getIndex() == 0) {
			if (this.currentElement().getType() != VertexFormatElement.Type.FLOAT) {
				throw new IllegalStateException();
			} else {
				this.putFloat(0, f);
				this.putFloat(4, g);
				this.nextElement();
				return this;
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
		if (this.currentElement().getUsage() != VertexFormatElement.Usage.UV || this.currentElement().getIndex() != i) {
			return this;
		} else if (this.currentElement().getType() != VertexFormatElement.Type.SHORT) {
			throw new IllegalStateException();
		} else {
			this.putShort(0, s);
			this.putShort(2, t);
			this.nextElement();
			return this;
		}
	}

	@Override
	default VertexConsumer normal(float f, float g, float h) {
		if (this.currentElement().getUsage() != VertexFormatElement.Usage.NORMAL) {
			return this;
		} else if (this.currentElement().getType() != VertexFormatElement.Type.BYTE) {
			throw new IllegalStateException();
		} else {
			this.putByte(0, (byte)((int)(f * 127.0F) & 0xFF));
			this.putByte(1, (byte)((int)(g * 127.0F) & 0xFF));
			this.putByte(2, (byte)((int)(h * 127.0F) & 0xFF));
			this.nextElement();
			return this;
		}
	}
}
