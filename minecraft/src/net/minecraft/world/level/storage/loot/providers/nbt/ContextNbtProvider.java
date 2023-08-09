package net.minecraft.world.level.storage.loot.providers.nbt;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ContextNbtProvider implements NbtProvider {
	private static final String BLOCK_ENTITY_ID = "block_entity";
	private static final ContextNbtProvider.Getter BLOCK_ENTITY_PROVIDER = new ContextNbtProvider.Getter() {
		@Override
		public Tag get(LootContext lootContext) {
			BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
			return blockEntity != null ? blockEntity.saveWithFullMetadata() : null;
		}

		@Override
		public String getId() {
			return "block_entity";
		}

		@Override
		public Set<LootContextParam<?>> getReferencedContextParams() {
			return ImmutableSet.of(LootContextParams.BLOCK_ENTITY);
		}
	};
	public static final ContextNbtProvider BLOCK_ENTITY = new ContextNbtProvider(BLOCK_ENTITY_PROVIDER);
	private static final Codec<ContextNbtProvider.Getter> GETTER_CODEC = Codec.STRING.xmap(string -> {
		if (string.equals("block_entity")) {
			return BLOCK_ENTITY_PROVIDER;
		} else {
			LootContext.EntityTarget entityTarget = LootContext.EntityTarget.getByName(string);
			return forEntity(entityTarget);
		}
	}, ContextNbtProvider.Getter::getId);
	public static final Codec<ContextNbtProvider> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(GETTER_CODEC.fieldOf("target").forGetter(contextNbtProvider -> contextNbtProvider.getter))
				.apply(instance, ContextNbtProvider::new)
	);
	public static final Codec<ContextNbtProvider> INLINE_CODEC = GETTER_CODEC.xmap(ContextNbtProvider::new, contextNbtProvider -> contextNbtProvider.getter);
	private final ContextNbtProvider.Getter getter;

	private static ContextNbtProvider.Getter forEntity(LootContext.EntityTarget entityTarget) {
		return new ContextNbtProvider.Getter() {
			@Nullable
			@Override
			public Tag get(LootContext lootContext) {
				Entity entity = lootContext.getParamOrNull(entityTarget.getParam());
				return entity != null ? NbtPredicate.getEntityTagToCompare(entity) : null;
			}

			@Override
			public String getId() {
				return entityTarget.name();
			}

			@Override
			public Set<LootContextParam<?>> getReferencedContextParams() {
				return ImmutableSet.of(entityTarget.getParam());
			}
		};
	}

	private ContextNbtProvider(ContextNbtProvider.Getter getter) {
		this.getter = getter;
	}

	@Override
	public LootNbtProviderType getType() {
		return NbtProviders.CONTEXT;
	}

	@Nullable
	@Override
	public Tag get(LootContext lootContext) {
		return this.getter.get(lootContext);
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.getter.getReferencedContextParams();
	}

	public static NbtProvider forContextEntity(LootContext.EntityTarget entityTarget) {
		return new ContextNbtProvider(forEntity(entityTarget));
	}

	interface Getter {
		@Nullable
		Tag get(LootContext lootContext);

		String getId();

		Set<LootContextParam<?>> getReferencedContextParams();
	}
}
