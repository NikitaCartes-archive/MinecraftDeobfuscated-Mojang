/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.TagType;
import net.minecraft.network.chat.Component;

public interface Tag {
    public static final ChatFormatting SYNTAX_HIGHLIGHTING_KEY = ChatFormatting.AQUA;
    public static final ChatFormatting SYNTAX_HIGHLIGHTING_STRING = ChatFormatting.GREEN;
    public static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER = ChatFormatting.GOLD;
    public static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER_TYPE = ChatFormatting.RED;

    public void write(DataOutput var1) throws IOException;

    public String toString();

    public byte getId();

    public TagType<?> getType();

    public Tag copy();

    default public String getAsString() {
        return this.toString();
    }

    default public Component getPrettyDisplay() {
        return this.getPrettyDisplay("", 0);
    }

    public Component getPrettyDisplay(String var1, int var2);
}

