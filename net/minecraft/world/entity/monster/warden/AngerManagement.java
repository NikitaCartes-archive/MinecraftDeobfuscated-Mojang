/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster.warden;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class AngerManagement {
    private static final int MAX_ANGER = 150;
    private static final int DEFAULT_ANGER_DECREASE = 1;
    public static final Codec<AngerManagement> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.unboundedMap(ExtraCodecs.UUID, ExtraCodecs.NON_NEGATIVE_INT).fieldOf("suspects")).forGetter(angerManagement -> angerManagement.angerBySuspect)).apply((Applicative<AngerManagement, ?>)instance, AngerManagement::new));
    private final Object2IntMap<UUID> angerBySuspect;

    public AngerManagement(Map<UUID, Integer> map) {
        this.angerBySuspect = new Object2IntOpenHashMap<UUID>(map);
    }

    public void tick() {
        Iterator objectIterator = this.angerBySuspect.object2IntEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
            int i = entry.getIntValue();
            if (i <= 1) {
                objectIterator.remove();
                continue;
            }
            entry.setValue(Math.max(0, i - 1));
        }
    }

    public int addAnger(Entity entity, int i) {
        return this.angerBySuspect.computeInt(entity.getUUID(), (uUID, integer) -> Math.min(150, (integer == null ? 0 : integer) + i));
    }

    public void clearAnger(Entity entity) {
        this.angerBySuspect.removeInt(entity.getUUID());
    }

    private Optional<Object2IntMap.Entry<UUID>> getTopEntry() {
        return this.angerBySuspect.object2IntEntrySet().stream().max(Map.Entry.comparingByValue());
    }

    public int getActiveAnger() {
        return this.getTopEntry().map(Map.Entry::getValue).orElse(0);
    }

    public Optional<LivingEntity> getActiveEntity(Level level) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            return this.getTopEntry().map(Map.Entry::getKey).map(serverLevel::getEntity).filter(entity -> entity instanceof LivingEntity).map(entity -> (LivingEntity)entity);
        }
        return Optional.empty();
    }
}

