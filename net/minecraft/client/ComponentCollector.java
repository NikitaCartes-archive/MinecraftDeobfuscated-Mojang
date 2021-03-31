/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ComponentCollector {
    private final List<FormattedText> parts = Lists.newArrayList();

    public void append(FormattedText formattedText) {
        this.parts.add(formattedText);
    }

    @Nullable
    public FormattedText getResult() {
        if (this.parts.isEmpty()) {
            return null;
        }
        if (this.parts.size() == 1) {
            return this.parts.get(0);
        }
        return FormattedText.composite(this.parts);
    }

    public FormattedText getResultOrEmpty() {
        FormattedText formattedText = this.getResult();
        return formattedText != null ? formattedText : FormattedText.EMPTY;
    }

    public void reset() {
        this.parts.clear();
    }
}

