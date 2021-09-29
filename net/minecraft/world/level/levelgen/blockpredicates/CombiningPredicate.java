/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

abstract class CombiningPredicate
implements BlockPredicate {
    protected final List<BlockPredicate> predicates;

    protected CombiningPredicate(List<BlockPredicate> list) {
        this.predicates = list;
    }

    public static <T extends CombiningPredicate> Codec<T> codec(Function<List<BlockPredicate>, T> function) {
        return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockPredicate.CODEC.listOf().fieldOf("predicates")).forGetter(combiningPredicate -> combiningPredicate.predicates)).apply((Applicative<CombiningPredicate, ?>)instance, function));
    }
}

