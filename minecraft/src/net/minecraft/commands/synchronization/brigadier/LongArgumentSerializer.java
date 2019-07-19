package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class LongArgumentSerializer implements ArgumentSerializer<LongArgumentType> {
	public void serializeToNetwork(LongArgumentType longArgumentType, FriendlyByteBuf friendlyByteBuf) {
		boolean bl = longArgumentType.getMinimum() != Long.MIN_VALUE;
		boolean bl2 = longArgumentType.getMaximum() != Long.MAX_VALUE;
		friendlyByteBuf.writeByte(BrigadierArgumentSerializers.createNumberFlags(bl, bl2));
		if (bl) {
			friendlyByteBuf.writeLong(longArgumentType.getMinimum());
		}

		if (bl2) {
			friendlyByteBuf.writeLong(longArgumentType.getMaximum());
		}
	}

	public LongArgumentType deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		byte b = friendlyByteBuf.readByte();
		long l = BrigadierArgumentSerializers.numberHasMin(b) ? friendlyByteBuf.readLong() : Long.MIN_VALUE;
		long m = BrigadierArgumentSerializers.numberHasMax(b) ? friendlyByteBuf.readLong() : Long.MAX_VALUE;
		return LongArgumentType.longArg(l, m);
	}

	public void serializeToJson(LongArgumentType longArgumentType, JsonObject jsonObject) {
		if (longArgumentType.getMinimum() != Long.MIN_VALUE) {
			jsonObject.addProperty("min", longArgumentType.getMinimum());
		}

		if (longArgumentType.getMaximum() != Long.MAX_VALUE) {
			jsonObject.addProperty("max", longArgumentType.getMaximum());
		}
	}
}
