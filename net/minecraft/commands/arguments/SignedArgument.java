/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.network.chat.Component;

public interface SignedArgument<T>
extends ArgumentType<T> {
    public Component getPlainSignableComponent(T var1);
}

