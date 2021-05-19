/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.narration;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationThunk;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public interface NarrationElementOutput {
    default public void add(NarratedElementType narratedElementType, Component component) {
        this.add(narratedElementType, NarrationThunk.from(component.getString()));
    }

    default public void add(NarratedElementType narratedElementType, String string) {
        this.add(narratedElementType, NarrationThunk.from(string));
    }

    default public void add(NarratedElementType narratedElementType, Component ... components) {
        this.add(narratedElementType, NarrationThunk.from(ImmutableList.copyOf(components)));
    }

    public void add(NarratedElementType var1, NarrationThunk<?> var2);

    public NarrationElementOutput nest();
}

