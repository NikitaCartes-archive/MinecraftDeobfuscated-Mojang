/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerationStep {

    public static enum Carving {
        AIR("air"),
        LIQUID("liquid");

        private static final Map<String, Carving> BY_NAME;
        private final String name;

        private Carving(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        static {
            BY_NAME = Arrays.stream(Carving.values()).collect(Collectors.toMap(Carving::getName, carving -> carving));
        }
    }

    public static enum Decoration {
        RAW_GENERATION("raw_generation"),
        LOCAL_MODIFICATIONS("local_modifications"),
        UNDERGROUND_STRUCTURES("underground_structures"),
        SURFACE_STRUCTURES("surface_structures"),
        UNDERGROUND_ORES("underground_ores"),
        UNDERGROUND_DECORATION("underground_decoration"),
        VEGETAL_DECORATION("vegetal_decoration"),
        TOP_LAYER_MODIFICATION("top_layer_modification");

        private static final Map<String, Decoration> BY_NAME;
        private final String name;

        private Decoration(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        static {
            BY_NAME = Arrays.stream(Decoration.values()).collect(Collectors.toMap(Decoration::getName, decoration -> decoration));
        }
    }
}

