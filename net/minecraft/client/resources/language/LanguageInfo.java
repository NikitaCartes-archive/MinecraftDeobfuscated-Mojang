/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.language;

import com.mojang.bridge.game.Language;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class LanguageInfo
implements Language,
Comparable<LanguageInfo> {
    private final String code;
    private final String region;
    private final String name;
    private final boolean bidirectional;

    public LanguageInfo(String string, String string2, String string3, boolean bl) {
        this.code = string;
        this.region = string2;
        this.name = string3;
        this.bidirectional = bl;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getRegion() {
        return this.region;
    }

    public boolean isBidirectional() {
        return this.bidirectional;
    }

    public String toString() {
        return String.format(Locale.ROOT, "%s (%s)", this.name, this.region);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LanguageInfo)) {
            return false;
        }
        return this.code.equals(((LanguageInfo)object).code);
    }

    public int hashCode() {
        return this.code.hashCode();
    }

    @Override
    public int compareTo(LanguageInfo languageInfo) {
        return this.code.compareTo(languageInfo.code);
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((LanguageInfo)object);
    }
}

