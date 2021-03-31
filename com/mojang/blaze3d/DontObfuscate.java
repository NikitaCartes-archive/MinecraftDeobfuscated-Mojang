/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifierDefault;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@TypeQualifierDefault(value={ElementType.TYPE, ElementType.METHOD})
@Retention(value=RetentionPolicy.CLASS)
@Environment(value=EnvType.CLIENT)
public @interface DontObfuscate {
}

