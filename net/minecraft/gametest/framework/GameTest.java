/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface GameTest {
    public int timeoutTicks() default 100;

    public String batch() default "defaultBatch";

    public int rotationSteps() default 0;

    public boolean required() default true;

    public String template() default "";

    public long setupTicks() default 0L;

    public int attempts() default 1;

    public int requiredSuccesses() default 1;
}

