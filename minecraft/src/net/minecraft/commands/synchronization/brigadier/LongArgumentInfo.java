package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class LongArgumentInfo implements ArgumentTypeInfo<LongArgumentType, LongArgumentInfo.Template> {
	public void serializeToNetwork(LongArgumentInfo.Template template, FriendlyByteBuf friendlyByteBuf) {
		boolean bl = template.min != Long.MIN_VALUE;
		boolean bl2 = template.max != Long.MAX_VALUE;
		friendlyByteBuf.writeByte(ArgumentUtils.createNumberFlags(bl, bl2));
		if (bl) {
			friendlyByteBuf.writeLong(template.min);
		}

		if (bl2) {
			friendlyByteBuf.writeLong(template.max);
		}
	}

	public LongArgumentInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		byte b = friendlyByteBuf.readByte();
		long l = ArgumentUtils.numberHasMin(b) ? friendlyByteBuf.readLong() : Long.MIN_VALUE;
		long m = ArgumentUtils.numberHasMax(b) ? friendlyByteBuf.readLong() : Long.MAX_VALUE;
		return new LongArgumentInfo.Template(l, m);
	}

	public void serializeToJson(LongArgumentInfo.Template template, JsonObject jsonObject) {
		if (template.min != Long.MIN_VALUE) {
			jsonObject.addProperty("min", template.min);
		}

		if (template.max != Long.MAX_VALUE) {
			jsonObject.addProperty("max", template.max);
		}
	}

	public LongArgumentInfo.Template unpack(LongArgumentType longArgumentType) {
		return new LongArgumentInfo.Template(longArgumentType.getMinimum(), longArgumentType.getMaximum());
	}

	public final class Template implements ArgumentTypeInfo.Template<LongArgumentType> {
		final long min;
		final long max;

		Template(final long l, final long m) {
			this.min = l;
			this.max = m;
		}

		public LongArgumentType instantiate(CommandBuildContext commandBuildContext) {
			return LongArgumentType.longArg(this.min, this.max);
		}

		@Override
		public ArgumentTypeInfo<LongArgumentType, ?> type() {
			return LongArgumentInfo.this;
		}
	}
}
