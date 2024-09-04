package net.minecraft.world.entity.player;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record Input(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean shift, boolean sprint) {
	private static final byte FLAG_FORWARD = 1;
	private static final byte FLAG_BACKWARD = 2;
	private static final byte FLAG_LEFT = 4;
	private static final byte FLAG_RIGHT = 8;
	private static final byte FLAG_JUMP = 16;
	private static final byte FLAG_SHIFT = 32;
	private static final byte FLAG_SPRINT = 64;
	public static final StreamCodec<FriendlyByteBuf, Input> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, Input>() {
		public void encode(FriendlyByteBuf friendlyByteBuf, Input input) {
			byte b = 0;
			b = (byte)(b | (input.forward() ? 1 : 0));
			b = (byte)(b | (input.backward() ? 2 : 0));
			b = (byte)(b | (input.left() ? 4 : 0));
			b = (byte)(b | (input.right() ? 8 : 0));
			b = (byte)(b | (input.jump() ? 16 : 0));
			b = (byte)(b | (input.shift() ? 32 : 0));
			b = (byte)(b | (input.sprint() ? 64 : 0));
			friendlyByteBuf.writeByte(b);
		}

		public Input decode(FriendlyByteBuf friendlyByteBuf) {
			byte b = friendlyByteBuf.readByte();
			boolean bl = (b & 1) != 0;
			boolean bl2 = (b & 2) != 0;
			boolean bl3 = (b & 4) != 0;
			boolean bl4 = (b & 8) != 0;
			boolean bl5 = (b & 16) != 0;
			boolean bl6 = (b & 32) != 0;
			boolean bl7 = (b & 64) != 0;
			return new Input(bl, bl2, bl3, bl4, bl5, bl6, bl7);
		}
	};
	public static Input EMPTY = new Input(false, false, false, false, false, false, false);
}
