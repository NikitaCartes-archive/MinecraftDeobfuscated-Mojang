package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class DoubleArgumentInfo implements ArgumentTypeInfo<DoubleArgumentType, DoubleArgumentInfo.Template> {
	public void serializeToNetwork(DoubleArgumentInfo.Template template, FriendlyByteBuf friendlyByteBuf) {
		boolean bl = template.min != -Double.MAX_VALUE;
		boolean bl2 = template.max != Double.MAX_VALUE;
		friendlyByteBuf.writeByte(ArgumentUtils.createNumberFlags(bl, bl2));
		if (bl) {
			friendlyByteBuf.writeDouble(template.min);
		}

		if (bl2) {
			friendlyByteBuf.writeDouble(template.max);
		}
	}

	public DoubleArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		byte b = friendlyByteBuf.readByte();
		double d = ArgumentUtils.numberHasMin(b) ? friendlyByteBuf.readDouble() : -Double.MAX_VALUE;
		double e = ArgumentUtils.numberHasMax(b) ? friendlyByteBuf.readDouble() : Double.MAX_VALUE;
		return new DoubleArgumentInfo.Template(d, e);
	}

	public void serializeToJson(DoubleArgumentInfo.Template template, JsonObject jsonObject) {
		if (template.min != -Double.MAX_VALUE) {
			jsonObject.addProperty("min", template.min);
		}

		if (template.max != Double.MAX_VALUE) {
			jsonObject.addProperty("max", template.max);
		}
	}

	public DoubleArgumentInfo.Template unpack(DoubleArgumentType doubleArgumentType) {
		return new DoubleArgumentInfo.Template(doubleArgumentType.getMinimum(), doubleArgumentType.getMaximum());
	}

	public final class Template implements ArgumentTypeInfo.Template<DoubleArgumentType> {
		final double min;
		final double max;

		Template(final double d, final double e) {
			this.min = d;
			this.max = e;
		}

		public DoubleArgumentType instantiate(CommandBuildContext commandBuildContext) {
			return DoubleArgumentType.doubleArg(this.min, this.max);
		}

		@Override
		public ArgumentTypeInfo<DoubleArgumentType, ?> type() {
			return DoubleArgumentInfo.this;
		}
	}
}
