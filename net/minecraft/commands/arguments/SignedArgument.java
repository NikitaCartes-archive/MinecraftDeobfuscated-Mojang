/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import net.minecraft.commands.arguments.PreviewedArgument;
import net.minecraft.network.chat.Component;

public interface SignedArgument<T>
extends PreviewedArgument<T> {
    public Component getPlainSignableComponent(T var1);
}

