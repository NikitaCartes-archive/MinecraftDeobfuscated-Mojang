package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType.StringType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

public class StringArgumentSerializer implements ArgumentTypeInfo<StringArgumentType, StringArgumentSerializer.Template> {
	public void serializeToNetwork(StringArgumentSerializer.Template template, FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(template.type);
	}

	public StringArgumentSerializer.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		StringType stringType = friendlyByteBuf.readEnum(StringType.class);
		return new StringArgumentSerializer.Template(stringType);
	}

	public void serializeToJson(StringArgumentSerializer.Template template, JsonObject jsonObject) {
		jsonObject.addProperty("type", switch (template.type) {
			case SINGLE_WORD -> "word";
			case QUOTABLE_PHRASE -> "phrase";
			case GREEDY_PHRASE -> "greedy";
		});
	}

	public StringArgumentSerializer.Template unpack(StringArgumentType stringArgumentType) {
		return new StringArgumentSerializer.Template(stringArgumentType.getType());
	}

	public final class Template implements ArgumentTypeInfo.Template<StringArgumentType> {
		final StringType type;

		public Template(final StringType stringType) {
			this.type = stringType;
		}

		public StringArgumentType instantiate(CommandBuildContext commandBuildContext) {
			return switch (this.type) {
				case SINGLE_WORD -> StringArgumentType.word();
				case QUOTABLE_PHRASE -> StringArgumentType.string();
				case GREEDY_PHRASE -> StringArgumentType.greedyString();
			};
		}

		@Override
		public ArgumentTypeInfo<StringArgumentType, ?> type() {
			return StringArgumentSerializer.this;
		}
	}
}
