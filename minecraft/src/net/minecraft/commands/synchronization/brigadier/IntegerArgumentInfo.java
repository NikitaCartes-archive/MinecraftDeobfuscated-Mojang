package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class IntegerArgumentInfo implements ArgumentTypeInfo<IntegerArgumentType, IntegerArgumentInfo.Template> {
	public void serializeToNetwork(IntegerArgumentInfo.Template template, FriendlyByteBuf friendlyByteBuf) {
		boolean bl = template.min != Integer.MIN_VALUE;
		boolean bl2 = template.max != Integer.MAX_VALUE;
		friendlyByteBuf.writeByte(ArgumentUtils.createNumberFlags(bl, bl2));
		if (bl) {
			friendlyByteBuf.writeInt(template.min);
		}

		if (bl2) {
			friendlyByteBuf.writeInt(template.max);
		}
	}

	public IntegerArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		byte b = friendlyByteBuf.readByte();
		int i = ArgumentUtils.numberHasMin(b) ? friendlyByteBuf.readInt() : Integer.MIN_VALUE;
		int j = ArgumentUtils.numberHasMax(b) ? friendlyByteBuf.readInt() : Integer.MAX_VALUE;
		return new IntegerArgumentInfo.Template(i, j);
	}

	public void serializeToJson(IntegerArgumentInfo.Template template, JsonObject jsonObject) {
		if (template.min != Integer.MIN_VALUE) {
			jsonObject.addProperty("min", template.min);
		}

		if (template.max != Integer.MAX_VALUE) {
			jsonObject.addProperty("max", template.max);
		}
	}

	public IntegerArgumentInfo.Template unpack(IntegerArgumentType integerArgumentType) {
		return new IntegerArgumentInfo.Template(integerArgumentType.getMinimum(), integerArgumentType.getMaximum());
	}

	public final class Template implements ArgumentTypeInfo.Template<IntegerArgumentType> {
		final int min;
		final int max;

		Template(final int i, final int j) {
			this.min = i;
			this.max = j;
		}

		public IntegerArgumentType instantiate(CommandBuildContext commandBuildContext) {
			return IntegerArgumentType.integer(this.min, this.max);
		}

		@Override
		public ArgumentTypeInfo<IntegerArgumentType, ?> type() {
			return IntegerArgumentInfo.this;
		}
	}
}
