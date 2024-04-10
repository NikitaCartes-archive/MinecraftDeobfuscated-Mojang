package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class FloatArgumentInfo implements ArgumentTypeInfo<FloatArgumentType, FloatArgumentInfo.Template> {
	public void serializeToNetwork(FloatArgumentInfo.Template template, FriendlyByteBuf friendlyByteBuf) {
		boolean bl = template.min != -Float.MAX_VALUE;
		boolean bl2 = template.max != Float.MAX_VALUE;
		friendlyByteBuf.writeByte(ArgumentUtils.createNumberFlags(bl, bl2));
		if (bl) {
			friendlyByteBuf.writeFloat(template.min);
		}

		if (bl2) {
			friendlyByteBuf.writeFloat(template.max);
		}
	}

	public FloatArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		byte b = friendlyByteBuf.readByte();
		float f = ArgumentUtils.numberHasMin(b) ? friendlyByteBuf.readFloat() : -Float.MAX_VALUE;
		float g = ArgumentUtils.numberHasMax(b) ? friendlyByteBuf.readFloat() : Float.MAX_VALUE;
		return new FloatArgumentInfo.Template(f, g);
	}

	public void serializeToJson(FloatArgumentInfo.Template template, JsonObject jsonObject) {
		if (template.min != -Float.MAX_VALUE) {
			jsonObject.addProperty("min", template.min);
		}

		if (template.max != Float.MAX_VALUE) {
			jsonObject.addProperty("max", template.max);
		}
	}

	public FloatArgumentInfo.Template unpack(FloatArgumentType floatArgumentType) {
		return new FloatArgumentInfo.Template(floatArgumentType.getMinimum(), floatArgumentType.getMaximum());
	}

	public final class Template implements ArgumentTypeInfo.Template<FloatArgumentType> {
		final float min;
		final float max;

		Template(final float f, final float g) {
			this.min = f;
			this.max = g;
		}

		public FloatArgumentType instantiate(CommandBuildContext commandBuildContext) {
			return FloatArgumentType.floatArg(this.min, this.max);
		}

		@Override
		public ArgumentTypeInfo<FloatArgumentType, ?> type() {
			return FloatArgumentInfo.this;
		}
	}
}
