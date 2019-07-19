/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.language;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.Locale;

@Environment(value=EnvType.CLIENT)
public class I18n {
    private static Locale locale;

    static void setLocale(Locale locale) {
        I18n.locale = locale;
    }

    public static String get(String string, Object ... objects) {
        return locale.get(string, objects);
    }

    public static boolean exists(String string) {
        return locale.has(string);
    }
}

