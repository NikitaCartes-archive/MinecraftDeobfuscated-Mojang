/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.obfuscate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifierDefault;

@TypeQualifierDefault(value={ElementType.TYPE, ElementType.METHOD})
@Retention(value=RetentionPolicy.CLASS)
public @interface DontObfuscate {
}

