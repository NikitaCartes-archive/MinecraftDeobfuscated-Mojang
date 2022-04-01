package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class DoubleArgumentSerializer implements ArgumentSerializer<DoubleArgumentType> {
	public void serializeToNetwork(DoubleArgumentType doubleArgumentType, FriendlyByteBuf friendlyByteBuf) {
		boolean bl = doubleArgumentType.getMinimum() != -Double.MAX_VALUE;
		boolean bl2 = doubleArgumentType.getMaximum() != Double.MAX_VALUE;
		friendlyByteBuf.writeByte(BrigadierArgumentSerializers.createNumberFlags(bl, bl2));
		if (bl) {
			friendlyByteBuf.writeDouble(doubleArgumentType.getMinimum());
		}

		if (bl2) {
			friendlyByteBuf.writeDouble(doubleArgumentType.getMaximum());
		}
	}

	public DoubleArgumentType deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		byte b = friendlyByteBuf.readByte();
		double d = BrigadierArgumentSerializers.numberHasMin(b) ? friendlyByteBuf.readDouble() : -Double.MAX_VALUE;
		double e = BrigadierArgumentSerializers.numberHasMax(b) ? friendlyByteBuf.readDouble() : Double.MAX_VALUE;
		return DoubleArgumentType.doubleArg(d, e);
	}

	public void serializeToJson(DoubleArgumentType doubleArgumentType, JsonObject jsonObject) {
		if (doubleArgumentType.getMinimum() != -Double.MAX_VALUE) {
			jsonObject.addProperty("min", doubleArgumentType.getMinimum());
		}

		if (doubleArgumentType.getMaximum() != Double.MAX_VALUE) {
			jsonObject.addProperty("max", doubleArgumentType.getMaximum());
		}
	}
}
