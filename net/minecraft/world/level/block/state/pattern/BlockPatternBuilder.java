/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state.pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class BlockPatternBuilder {
    private static final Joiner COMMA_JOINED = Joiner.on(",");
    private final List<String[]> pattern = Lists.newArrayList();
    private final Map<Character, Predicate<BlockInWorld>> lookup = Maps.newHashMap();
    private int height;
    private int width;

    private BlockPatternBuilder() {
        this.lookup.put(Character.valueOf(' '), Predicates.alwaysTrue());
    }

    public BlockPatternBuilder aisle(String ... strings) {
        if (ArrayUtils.isEmpty(strings) || StringUtils.isEmpty(strings[0])) {
            throw new IllegalArgumentException("Empty pattern for aisle");
        }
        if (this.pattern.isEmpty()) {
            this.height = strings.length;
            this.width = strings[0].length();
        }
        if (strings.length != this.height) {
            throw new IllegalArgumentException("Expected aisle with height of " + this.height + ", but was given one with a height of " + strings.length + ")");
        }
        for (String string : strings) {
            if (string.length() != this.width) {
                throw new IllegalArgumentException("Not all rows in the given aisle are the correct width (expected " + this.width + ", found one with " + string.length() + ")");
            }
            for (char c : string.toCharArray()) {
                if (this.lookup.containsKey(Character.valueOf(c))) continue;
                this.lookup.put(Character.valueOf(c), null);
            }
        }
        this.pattern.add(strings);
        return this;
    }

    public static BlockPatternBuilder start() {
        return new BlockPatternBuilder();
    }

    public BlockPatternBuilder where(char c, Predicate<BlockInWorld> predicate) {
        this.lookup.put(Character.valueOf(c), predicate);
        return this;
    }

    public BlockPattern build() {
        return new BlockPattern(this.createPattern());
    }

    private Predicate<BlockInWorld>[][][] createPattern() {
        this.ensureAllCharactersMatched();
        Predicate[][][] predicates = (Predicate[][][])Array.newInstance(Predicate.class, this.pattern.size(), this.height, this.width);
        for (int i = 0; i < this.pattern.size(); ++i) {
            for (int j = 0; j < this.height; ++j) {
                for (int k = 0; k < this.width; ++k) {
                    predicates[i][j][k] = this.lookup.get(Character.valueOf(this.pattern.get(i)[j].charAt(k)));
                }
            }
        }
        return predicates;
    }

    private void ensureAllCharactersMatched() {
        ArrayList<Character> list = Lists.newArrayList();
        for (Map.Entry<Character, Predicate<BlockInWorld>> entry : this.lookup.entrySet()) {
            if (entry.getValue() != null) continue;
            list.add(entry.getKey());
        }
        if (!list.isEmpty()) {
            throw new IllegalStateException("Predicates for character(s) " + COMMA_JOINED.join(list) + " are missing");
        }
    }
}

