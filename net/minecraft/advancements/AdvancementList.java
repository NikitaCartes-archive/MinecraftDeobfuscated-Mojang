/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class AdvancementList {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<ResourceLocation, Advancement> advancements = Maps.newHashMap();
    private final Set<Advancement> roots = Sets.newLinkedHashSet();
    private final Set<Advancement> tasks = Sets.newLinkedHashSet();
    private Listener listener;

    @Environment(value=EnvType.CLIENT)
    private void remove(Advancement advancement) {
        for (Advancement advancement2 : advancement.getChildren()) {
            this.remove(advancement2);
        }
        LOGGER.info("Forgot about advancement {}", (Object)advancement.getId());
        this.advancements.remove(advancement.getId());
        if (advancement.getParent() == null) {
            this.roots.remove(advancement);
            if (this.listener != null) {
                this.listener.onRemoveAdvancementRoot(advancement);
            }
        } else {
            this.tasks.remove(advancement);
            if (this.listener != null) {
                this.listener.onRemoveAdvancementTask(advancement);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public void remove(Set<ResourceLocation> set) {
        for (ResourceLocation resourceLocation : set) {
            Advancement advancement = this.advancements.get(resourceLocation);
            if (advancement == null) {
                LOGGER.warn("Told to remove advancement {} but I don't know what that is", (Object)resourceLocation);
                continue;
            }
            this.remove(advancement);
        }
    }

    public void add(Map<ResourceLocation, Advancement.Builder> map) {
        Function<ResourceLocation, Object> function = Functions.forMap(this.advancements, null);
        while (!map.isEmpty()) {
            boolean bl = false;
            Iterator<Map.Entry<ResourceLocation, Advancement.Builder>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ResourceLocation, Advancement.Builder> entry = iterator.next();
                ResourceLocation resourceLocation = entry.getKey();
                Advancement.Builder builder = entry.getValue();
                if (!builder.canBuild(function)) continue;
                Advancement advancement = builder.build(resourceLocation);
                this.advancements.put(resourceLocation, advancement);
                bl = true;
                iterator.remove();
                if (advancement.getParent() == null) {
                    this.roots.add(advancement);
                    if (this.listener == null) continue;
                    this.listener.onAddAdvancementRoot(advancement);
                    continue;
                }
                this.tasks.add(advancement);
                if (this.listener == null) continue;
                this.listener.onAddAdvancementTask(advancement);
            }
            if (bl) continue;
            for (Map.Entry<ResourceLocation, Advancement.Builder> entry : map.entrySet()) {
                LOGGER.error("Couldn't load advancement {}: {}", (Object)entry.getKey(), (Object)entry.getValue());
            }
        }
        LOGGER.info("Loaded {} advancements", (Object)this.advancements.size());
    }

    @Environment(value=EnvType.CLIENT)
    public void clear() {
        this.advancements.clear();
        this.roots.clear();
        this.tasks.clear();
        if (this.listener != null) {
            this.listener.onAdvancementsCleared();
        }
    }

    public Iterable<Advancement> getRoots() {
        return this.roots;
    }

    public Collection<Advancement> getAllAdvancements() {
        return this.advancements.values();
    }

    @Nullable
    public Advancement get(ResourceLocation resourceLocation) {
        return this.advancements.get(resourceLocation);
    }

    @Environment(value=EnvType.CLIENT)
    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
        if (listener != null) {
            for (Advancement advancement : this.roots) {
                listener.onAddAdvancementRoot(advancement);
            }
            for (Advancement advancement : this.tasks) {
                listener.onAddAdvancementTask(advancement);
            }
        }
    }

    public static interface Listener {
        public void onAddAdvancementRoot(Advancement var1);

        @Environment(value=EnvType.CLIENT)
        public void onRemoveAdvancementRoot(Advancement var1);

        public void onAddAdvancementTask(Advancement var1);

        @Environment(value=EnvType.CLIENT)
        public void onRemoveAdvancementTask(Advancement var1);

        @Environment(value=EnvType.CLIENT)
        public void onAdvancementsCleared();
    }
}

