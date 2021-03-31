/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifierDefault;
import org.jetbrains.annotations.NotNull;

@NotNull
@TypeQualifierDefault(value={ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface FieldsAreNonnullByDefault {
}

