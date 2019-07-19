/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements;

import java.util.Collection;

public interface RequirementsStrategy {
    public static final RequirementsStrategy AND = collection -> {
        String[][] strings = new String[collection.size()][];
        int i = 0;
        for (String string : collection) {
            strings[i++] = new String[]{string};
        }
        return strings;
    };
    public static final RequirementsStrategy OR = collection -> new String[][]{collection.toArray(new String[0])};

    public String[][] createRequirements(Collection<String> var1);
}

