package net.minecraft.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.network.chat.Component;

public interface SignedArgument<T> extends ArgumentType<T> {
	Component getPlainSignableComponent(T object);
}
