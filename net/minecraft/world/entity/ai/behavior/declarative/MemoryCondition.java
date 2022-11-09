/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior.declarative;

import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.OptionalBox;
import com.mojang.datafixers.util.Unit;
import java.util.Optional;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.Nullable;

public interface MemoryCondition<F extends K1, Value> {
    public MemoryModuleType<Value> memory();

    public MemoryStatus condition();

    @Nullable
    public MemoryAccessor<F, Value> createAccessor(Brain<?> var1, Optional<Value> var2);

    public record Absent<Value>(MemoryModuleType<Value> memory) implements MemoryCondition<Const.Mu<Unit>, Value>
    {
        @Override
        public MemoryStatus condition() {
            return MemoryStatus.VALUE_ABSENT;
        }

        @Override
        public MemoryAccessor<Const.Mu<Unit>, Value> createAccessor(Brain<?> brain, Optional<Value> optional) {
            if (optional.isPresent()) {
                return null;
            }
            return new MemoryAccessor<Unit, Value>(brain, this.memory, Const.create(Unit.INSTANCE));
        }
    }

    public record Present<Value>(MemoryModuleType<Value> memory) implements MemoryCondition<IdF.Mu, Value>
    {
        @Override
        public MemoryStatus condition() {
            return MemoryStatus.VALUE_PRESENT;
        }

        @Override
        public MemoryAccessor<IdF.Mu, Value> createAccessor(Brain<?> brain, Optional<Value> optional) {
            if (optional.isEmpty()) {
                return null;
            }
            return new MemoryAccessor(brain, this.memory, IdF.create(optional.get()));
        }
    }

    public record Registered<Value>(MemoryModuleType<Value> memory) implements MemoryCondition<OptionalBox.Mu, Value>
    {
        @Override
        public MemoryStatus condition() {
            return MemoryStatus.REGISTERED;
        }

        @Override
        public MemoryAccessor<OptionalBox.Mu, Value> createAccessor(Brain<?> brain, Optional<Value> optional) {
            return new MemoryAccessor(brain, this.memory, OptionalBox.create(optional));
        }
    }
}

