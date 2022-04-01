package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class FloatArgumentSerializer implements ArgumentSerializer<FloatArgumentType> {
	public void serializeToNetwork(FloatArgumentType floatArgumentType, FriendlyByteBuf friendlyByteBuf) {
		boolean bl = floatArgumentType.getMinimum() != -Float.MAX_VALUE;
		boolean bl2 = floatArgumentType.getMaximum() != Float.MAX_VALUE;
		friendlyByteBuf.writeByte(BrigadierArgumentSerializers.createNumberFlags(bl, bl2));
		if (bl) {
			friendlyByteBuf.writeFloat(floatArgumentType.getMinimum());
		}

		if (bl2) {
			friendlyByteBuf.writeFloat(floatArgumentType.getMaximum());
		}
	}

	public FloatArgumentType deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		byte b = friendlyByteBuf.readByte();
		float f = BrigadierArgumentSerializers.numberHasMin(b) ? friendlyByteBuf.readFloat() : -Float.MAX_VALUE;
		float g = BrigadierArgumentSerializers.numberHasMax(b) ? friendlyByteBuf.readFloat() : Float.MAX_VALUE;
		return FloatArgumentType.floatArg(f, g);
	}

	public void serializeToJson(FloatArgumentType floatArgumentType, JsonObject jsonObject) {
		if (floatArgumentType.getMinimum() != -Float.MAX_VALUE) {
			jsonObject.addProperty("min", floatArgumentType.getMinimum());
		}

		if (floatArgumentType.getMaximum() != Float.MAX_VALUE) {
			jsonObject.addProperty("max", floatArgumentType.getMaximum());
		}
	}
}
