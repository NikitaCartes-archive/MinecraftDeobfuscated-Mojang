/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.inventory;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ContainerLevelAccess {
    public static final ContainerLevelAccess NULL = new ContainerLevelAccess(){

        @Override
        public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> biFunction) {
            return Optional.empty();
        }
    };

    public static ContainerLevelAccess create(final Level level, final BlockPos blockPos) {
        return new ContainerLevelAccess(){

            @Override
            public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> biFunction) {
                return Optional.of(biFunction.apply(level, blockPos));
            }
        };
    }

    public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> var1);

    default public <T> T evaluate(BiFunction<Level, BlockPos, T> biFunction, T object) {
        return this.evaluate(biFunction).orElse(object);
    }

    default public void execute(BiConsumer<Level, BlockPos> biConsumer) {
        this.evaluate((level, blockPos) -> {
            biConsumer.accept((Level)level, (BlockPos)blockPos);
            return Optional.empty();
        });
    }
}

