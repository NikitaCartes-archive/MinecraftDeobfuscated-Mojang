/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster.warden;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class AngerManagement {
    @VisibleForTesting
    protected static final int CONVERSION_DELAY = 40;
    @VisibleForTesting
    protected static final int MAX_ANGER = 150;
    private static final int DEFAULT_ANGER_DECREASE = 1;
    private int conversionDelay = Mth.randomBetweenInclusive(RandomSource.create(), 0, 40);
    private static final Codec<Pair<UUID, Integer>> SUSPECT_ANGER_PAIR = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ExtraCodecs.UUID.fieldOf("uuid")).forGetter(Pair::getFirst), ((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("anger")).forGetter(Pair::getSecond)).apply((Applicative<Pair, ?>)instance, Pair::of));
    public static final Codec<AngerManagement> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)SUSPECT_ANGER_PAIR.listOf().fieldOf("suspects")).orElse(Collections.emptyList()).forGetter(AngerManagement::createUuidAngerPairs)).apply((Applicative<AngerManagement, ?>)instance, AngerManagement::new));
    @VisibleForTesting
    protected final SortedSet<Entity> suspects = new ObjectAVLTreeSet<Entity>(new Sorter(this));
    @VisibleForTesting
    protected final Object2IntMap<Entity> angerBySuspect = new Object2IntOpenHashMap<Entity>();
    @VisibleForTesting
    protected final Object2IntMap<UUID> angerByUuid;

    public AngerManagement(List<Pair<UUID, Integer>> list) {
        this.angerByUuid = new Object2IntOpenHashMap<UUID>(list.size());
        list.forEach(pair -> this.angerByUuid.put((UUID)pair.getFirst(), (Integer)pair.getSecond()));
    }

    private List<Pair<UUID, Integer>> createUuidAngerPairs() {
        return Streams.concat(this.suspects.stream().map(entity -> Pair.of(entity.getUUID(), this.angerBySuspect.getInt(entity))), this.angerByUuid.object2IntEntrySet().stream().map(entry -> Pair.of((UUID)entry.getKey(), entry.getIntValue()))).collect(Collectors.toList());
    }

    public void tick(ServerLevel serverLevel, Predicate<Entity> predicate) {
        --this.conversionDelay;
        if (this.conversionDelay <= 0) {
            this.convertFromUuids(serverLevel);
            this.conversionDelay = 40;
        }
        Iterator objectIterator = this.angerByUuid.object2IntEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
            int i = entry.getIntValue();
            if (i <= 1) {
                objectIterator.remove();
                continue;
            }
            entry.setValue(i - 1);
        }
        Iterator objectIterator2 = this.angerBySuspect.object2IntEntrySet().iterator();
        while (objectIterator2.hasNext()) {
            Object2IntMap.Entry entry2 = (Object2IntMap.Entry)objectIterator2.next();
            int j = entry2.getIntValue();
            Entity entity = (Entity)entry2.getKey();
            if (j <= 1 || !predicate.test(entity)) {
                this.suspects.remove(entity);
                objectIterator2.remove();
                continue;
            }
            entry2.setValue(j - 1);
        }
    }

    private void convertFromUuids(ServerLevel serverLevel) {
        Iterator objectIterator = this.angerByUuid.object2IntEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
            int i = entry.getIntValue();
            Entity entity = serverLevel.getEntity((UUID)entry.getKey());
            if (entity == null) continue;
            this.angerBySuspect.put(entity, i);
            this.suspects.add(entity);
            objectIterator.remove();
        }
    }

    public int increaseAnger(Entity entity2, int i) {
        boolean bl = !this.suspects.remove(entity2);
        int j = this.angerBySuspect.computeInt(entity2, (entity, integer) -> Math.min(150, (integer == null ? 0 : integer) + i));
        if (bl) {
            int k = this.angerByUuid.removeInt(entity2.getUUID());
            this.angerBySuspect.put(entity2, j += k);
        }
        this.suspects.add(entity2);
        return j;
    }

    public void clearAnger(Entity entity) {
        this.angerBySuspect.removeInt(entity);
        this.suspects.remove(entity);
    }

    @Nullable
    private Entity getTopSuspect() {
        return this.suspects.isEmpty() ? null : this.suspects.first();
    }

    public int getActiveAnger() {
        return this.angerBySuspect.getInt(this.getTopSuspect());
    }

    public Optional<LivingEntity> getActiveEntity() {
        return Optional.ofNullable(this.getTopSuspect()).filter(entity -> entity instanceof LivingEntity).map(entity -> (LivingEntity)entity);
    }

    @VisibleForTesting
    protected record Sorter(AngerManagement angerManagement) implements Comparator<Entity>
    {
        @Override
        public int compare(Entity entity, Entity entity2) {
            boolean bl4;
            boolean bl3;
            boolean bl2;
            if (entity.equals(entity2)) {
                return 0;
            }
            int i = this.angerManagement.angerBySuspect.getOrDefault((Object)entity, 0);
            int j = this.angerManagement.angerBySuspect.getOrDefault((Object)entity2, 0);
            boolean bl = i >= AngerLevel.ANGRY.getMinimumAnger();
            boolean bl5 = bl2 = j >= AngerLevel.ANGRY.getMinimumAnger();
            if (bl != bl2) {
                return bl ? -1 : 1;
            }
            if (bl && (bl3 = entity instanceof Player) != (bl4 = entity2 instanceof Player)) {
                return bl3 ? -1 : 1;
            }
            return i > j ? -1 : 1;
        }

        @Override
        public /* synthetic */ int compare(Object object, Object object2) {
            return this.compare((Entity)object, (Entity)object2);
        }
    }
}

