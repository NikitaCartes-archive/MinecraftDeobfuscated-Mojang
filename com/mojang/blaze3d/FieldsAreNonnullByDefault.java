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
import org.jetbrains.annotations.NotNull;

@NotNull
@TypeQualifierDefault(value={ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
@Environment(value=EnvType.CLIENT)
public @interface FieldsAreNonnullByDefault {
}

