package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class AdvancementList {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<ResourceLocation, Advancement> advancements = Maps.<ResourceLocation, Advancement>newHashMap();
	private final Set<Advancement> roots = Sets.<Advancement>newLinkedHashSet();
	private final Set<Advancement> tasks = Sets.<Advancement>newLinkedHashSet();
	@Nullable
	private AdvancementList.Listener listener;

	private void remove(Advancement advancement) {
		for (Advancement advancement2 : advancement.getChildren()) {
			this.remove(advancement2);
		}

		LOGGER.info("Forgot about advancement {}", advancement.getId());
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

	public void remove(Set<ResourceLocation> set) {
		for (ResourceLocation resourceLocation : set) {
			Advancement advancement = (Advancement)this.advancements.get(resourceLocation);
			if (advancement == null) {
				LOGGER.warn("Told to remove advancement {} but I don't know what that is", resourceLocation);
			} else {
				this.remove(advancement);
			}
		}
	}

	public void add(Map<ResourceLocation, Advancement.Builder> map) {
		Map<ResourceLocation, Advancement.Builder> map2 = Maps.<ResourceLocation, Advancement.Builder>newHashMap(map);

		while (!map2.isEmpty()) {
			boolean bl = false;
			Iterator<Entry<ResourceLocation, Advancement.Builder>> iterator = map2.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<ResourceLocation, Advancement.Builder> entry = (Entry<ResourceLocation, Advancement.Builder>)iterator.next();
				ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
				Advancement.Builder builder = (Advancement.Builder)entry.getValue();
				if (builder.canBuild(this.advancements::get)) {
					Advancement advancement = builder.build(resourceLocation);
					this.advancements.put(resourceLocation, advancement);
					bl = true;
					iterator.remove();
					if (advancement.getParent() == null) {
						this.roots.add(advancement);
						if (this.listener != null) {
							this.listener.onAddAdvancementRoot(advancement);
						}
					} else {
						this.tasks.add(advancement);
						if (this.listener != null) {
							this.listener.onAddAdvancementTask(advancement);
						}
					}
				}
			}

			if (!bl) {
				for (Entry<ResourceLocation, Advancement.Builder> entry : map2.entrySet()) {
					LOGGER.error("Couldn't load advancement {}: {}", entry.getKey(), entry.getValue());
				}
				break;
			}
		}

		LOGGER.info("Loaded {} advancements", this.advancements.size());
	}

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
		return (Advancement)this.advancements.get(resourceLocation);
	}

	public void setListener(@Nullable AdvancementList.Listener listener) {
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

	public interface Listener {
		void onAddAdvancementRoot(Advancement advancement);

		void onRemoveAdvancementRoot(Advancement advancement);

		void onAddAdvancementTask(Advancement advancement);

		void onRemoveAdvancementTask(Advancement advancement);

		void onAdvancementsCleared();
	}
}
