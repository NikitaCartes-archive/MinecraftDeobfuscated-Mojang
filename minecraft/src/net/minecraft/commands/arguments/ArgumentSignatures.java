package net.minecraft.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Crypt;

public record ArgumentSignatures(long salt, Map<String, byte[]> signatures) {
	private static final int MAX_ARGUMENT_COUNT = 8;
	private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

	public ArgumentSignatures(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readLong(),
			friendlyByteBuf.readMap(FriendlyByteBuf.limitValue(HashMap::new, 8), friendlyByteBufx -> friendlyByteBufx.readUtf(16), FriendlyByteBuf::readByteArray)
		);
	}

	public static ArgumentSignatures empty() {
		return new ArgumentSignatures(0L, Map.of());
	}

	@Nullable
	public Crypt.SaltSignaturePair get(String string) {
		byte[] bs = (byte[])this.signatures.get(string);
		return bs != null ? new Crypt.SaltSignaturePair(this.salt, bs) : null;
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeLong(this.salt);
		friendlyByteBuf.writeMap(this.signatures, (friendlyByteBufx, string) -> friendlyByteBufx.writeUtf(string, 16), FriendlyByteBuf::writeByteArray);
	}

	public static Map<String, Component> collectLastChildPlainSignableComponents(CommandContextBuilder<?> commandContextBuilder) {
		CommandContextBuilder<?> commandContextBuilder2 = commandContextBuilder.getLastChild();
		Map<String, Component> map = new Object2ObjectArrayMap<>();

		for (ParsedCommandNode<?> parsedCommandNode : commandContextBuilder2.getNodes()) {
			CommandNode parsedArgument = parsedCommandNode.getNode();
			if (parsedArgument instanceof ArgumentCommandNode) {
				ArgumentCommandNode<?, ?> argumentCommandNode = (ArgumentCommandNode<?, ?>)parsedArgument;
				ArgumentType var8 = argumentCommandNode.getType();
				if (var8 instanceof SignedArgument) {
					SignedArgument<?> signedArgument = (SignedArgument<?>)var8;
					ParsedArgument<?, ?> parsedArgumentx = (ParsedArgument<?, ?>)commandContextBuilder2.getArguments().get(argumentCommandNode.getName());
					if (parsedArgumentx != null) {
						map.put(argumentCommandNode.getName(), getPlainComponentUnchecked(signedArgument, parsedArgumentx));
					}
				}
			}
		}

		return map;
	}

	private static <T> Component getPlainComponentUnchecked(SignedArgument<T> signedArgument, ParsedArgument<?, ?> parsedArgument) {
		return signedArgument.getPlainSignableComponent((T)parsedArgument.getResult());
	}
}
