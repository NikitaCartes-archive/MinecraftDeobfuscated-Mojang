package net.minecraft.commands.arguments;

public interface SignedArgument<T> extends PreviewedArgument<T> {
	String getSignableText(T object);
}
