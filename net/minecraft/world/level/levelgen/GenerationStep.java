/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public class GenerationStep {

    public static enum Carving implements StringRepresentable
    {
        AIR("air"),
        LIQUID("liquid");

        public static final Codec<Carving> CODEC;
        private static final Map<String, Carving> BY_NAME;
        private final String name;

        private Carving(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        @Nullable
        public static Carving byName(String string) {
            return BY_NAME.get(string);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Carving::values, Carving::byName);
            BY_NAME = Arrays.stream(Carving.values()).collect(Collectors.toMap(Carving::getName, carving -> carving));
        }
    }

    public static enum Decoration implements StringRepresentable
    {
        RAW_GENERATION("raw_generation"),
        LAKES("lakes"),
        LOCAL_MODIFICATIONS("local_modifications"),
        UNDERGROUND_STRUCTURES("underground_structures"),
        SURFACE_STRUCTURES("surface_structures"),
        STRONGHOLDS("strongholds"),
        UNDERGROUND_ORES("underground_ores"),
        UNDERGROUND_DECORATION("underground_decoration"),
        VEGETAL_DECORATION("vegetal_decoration"),
        TOP_LAYER_MODIFICATION("top_layer_modification");

        public static final Codec<Decoration> CODEC;
        private static final Map<String, Decoration> BY_NAME;
        private final String name;

        private Decoration(String string2) {
            this.name = string2;
        }

        public String getName() {
            return this.name;
        }

        public static Decoration byName(String string) {
            return BY_NAME.get(string);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Decoration::values, Decoration::byName);
            BY_NAME = Arrays.stream(Decoration.values()).collect(Collectors.toMap(Decoration::getName, decoration -> decoration));
        }
    }
}

