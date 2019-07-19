/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public abstract class ValueObject {
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{");
        for (Field field : this.getClass().getFields()) {
            if (ValueObject.isStatic(field)) continue;
            try {
                stringBuilder.append(field.getName()).append("=").append(field.get(this)).append(" ");
            } catch (IllegalAccessException illegalAccessException) {
                // empty catch block
            }
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    private static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }
}

