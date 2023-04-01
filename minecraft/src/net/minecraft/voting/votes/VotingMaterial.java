package net.minecraft.voting.votes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.ClampedNormalInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public interface VotingMaterial {
	MapCodec<VotingMaterial> CODEC = VotingMaterial.Type.CODEC.dispatchMap(VotingMaterial::type, type -> (Codec)type.codec.get());
	IntProvider VOTE_COUNT_GENERATOR = ClampedNormalInt.of(1.0F, 3.0F, 1, 10);
	VotingMaterial VOTES_PER_PROPOSAL = new VotingMaterial() {
		@Override
		public boolean deduct(ServerPlayer serverPlayer, int i, boolean bl) {
			throw new AssertionError("You forgot to implement the hack!");
		}

		@Override
		public VotingMaterial.Type type() {
			return VotingMaterial.Type.PER_PROPOSAL;
		}

		@Override
		public Component display() {
			return Component.translatable("vote.count_per_proposal.description");
		}
	};
	VotingMaterial VOTES_PER_OPTION = new VotingMaterial() {
		@Override
		public boolean deduct(ServerPlayer serverPlayer, int i, boolean bl) {
			throw new AssertionError("You forgot to implement the hack!");
		}

		@Override
		public VotingMaterial.Type type() {
			return VotingMaterial.Type.PER_OPTION;
		}

		@Override
		public Component display() {
			return Component.translatable("vote.count_per_option.description");
		}
	};

	boolean deduct(ServerPlayer serverPlayer, int i, boolean bl);

	VotingMaterial.Type type();

	Component display();

	public static record Cost(VotingMaterial material, int count) {
		public static final Codec<VotingMaterial.Cost> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(VotingMaterial.CODEC.forGetter(VotingMaterial.Cost::material), Codec.INT.fieldOf("count").forGetter(VotingMaterial.Cost::count))
					.apply(instance, VotingMaterial.Cost::new)
		);

		public boolean deduct(ServerPlayer serverPlayer, boolean bl) {
			return this.material.deduct(serverPlayer, this.count, bl);
		}

		public Component display(boolean bl) {
			return (Component)(!bl && this.count == 1 ? this.material.display() : Component.translatable("vote.cost_diplay", this.count, this.material.display()));
		}
	}

	public static record CustomCost(Component displayName) implements VotingMaterial {
		static final Codec<VotingMaterial.CustomCost> CODEC = ExtraCodecs.COMPONENT.xmap(VotingMaterial.CustomCost::new, VotingMaterial.CustomCost::displayName);

		@Override
		public VotingMaterial.Type type() {
			return VotingMaterial.Type.CUSTOM;
		}

		@Override
		public boolean deduct(ServerPlayer serverPlayer, int i, boolean bl) {
			return true;
		}

		@Override
		public Component display() {
			return this.displayName;
		}
	}

	public static enum Type implements StringRepresentable {
		PER_PROPOSAL("per_proposal", () -> Codec.unit(VotingMaterial.VOTES_PER_PROPOSAL)) {
			@Override
			public Optional<VotingMaterial.Cost> random(RandomSource randomSource) {
				int i = VotingMaterial.VOTE_COUNT_GENERATOR.sample(randomSource);
				return Optional.of(new VotingMaterial.Cost(VotingMaterial.VOTES_PER_PROPOSAL, i));
			}
		},
		PER_OPTION("per_option", () -> Codec.unit(VotingMaterial.VOTES_PER_OPTION)) {
			@Override
			public Optional<VotingMaterial.Cost> random(RandomSource randomSource) {
				int i = VotingMaterial.VOTE_COUNT_GENERATOR.sample(randomSource);
				return Optional.of(new VotingMaterial.Cost(VotingMaterial.VOTES_PER_OPTION, i));
			}
		},
		ITEM("item", () -> VotingMaterial.VotingItem.CODEC) {
			@Override
			public Optional<VotingMaterial.Cost> random(RandomSource randomSource) {
				return BuiltInRegistries.ITEM
					.getRandom(randomSource)
					.flatMap(
						reference -> {
							if (reference.value() == Items.AIR) {
								return Optional.empty();
							} else {
								int i = ClampedNormalInt.of(5.0F, 5.0F, 1, ((Item)reference.value()).getMaxStackSize()).sample(randomSource);
								String string = VotingMaterial.VotingItem.defaultKeyFromId(reference.key());
								String string2 = ((Item)reference.value()).getDescriptionId();
								return Optional.of(
									new VotingMaterial.Cost(new VotingMaterial.VotingItem(reference.key(), string.equals(string2) ? Optional.empty() : Optional.of(string2)), i)
								);
							}
						}
					);
			}
		},
		RESOURCE("resource", () -> VotingMaterial.VotingResource.CODEC) {
			@Override
			public Optional<VotingMaterial.Cost> random(RandomSource randomSource) {
				return Util.getRandomSafe(Arrays.asList(VotingMaterial.VotingResource.values()), randomSource).map(votingResource -> {
					int i = votingResource.costGenerator.sample(randomSource);
					return new VotingMaterial.Cost(votingResource, i);
				});
			}
		},
		CUSTOM("custom", () -> VotingMaterial.CustomCost.CODEC) {
			@Override
			public Optional<VotingMaterial.Cost> random(RandomSource randomSource) {
				return Optional.empty();
			}
		};

		public static final Codec<VotingMaterial.Type> CODEC = StringRepresentable.fromEnum(VotingMaterial.Type::values);
		private final String id;
		final Supplier<Codec<? extends VotingMaterial>> codec;

		Type(String string2, Supplier<Codec<? extends VotingMaterial>> supplier) {
			this.id = string2;
			this.codec = supplier;
		}

		@Override
		public String getSerializedName() {
			return this.id;
		}

		public abstract Optional<VotingMaterial.Cost> random(RandomSource randomSource);
	}

	public static record VotingItem(ResourceKey<Item> id, Optional<String> translationKey) implements VotingMaterial {
		static final Codec<VotingMaterial.VotingItem> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ResourceKey.codec(Registries.ITEM).fieldOf("item").forGetter(VotingMaterial.VotingItem::id),
						Codec.STRING.optionalFieldOf("translation_key").forGetter(VotingMaterial.VotingItem::translationKey)
					)
					.apply(instance, VotingMaterial.VotingItem::new)
		);

		@Override
		public VotingMaterial.Type type() {
			return VotingMaterial.Type.ITEM;
		}

		@Override
		public boolean deduct(ServerPlayer serverPlayer, int i, boolean bl) {
			int j = bl ? 0 : i;
			int k = serverPlayer.getInventory().clearOrCountMatchingItems(itemStack -> itemStack.is(this.id), j, serverPlayer.inventoryMenu.getCraftSlots());
			return k >= i;
		}

		@Override
		public Component display() {
			return Component.translatable((String)this.translationKey.orElseGet(() -> defaultKeyFromId(this.id)));
		}

		public static String defaultKeyFromId(ResourceKey<Item> resourceKey) {
			return Util.makeDescriptionId("item", resourceKey.location());
		}
	}

	public static enum VotingResource implements StringRepresentable, VotingMaterial {
		XP("xp", ClampedNormalInt.of(5.0F, 5.0F, 1, 30)) {
			private static final Component DISPLAY = Component.translatable("vote.cost.xp");

			@Override
			public boolean deduct(ServerPlayer serverPlayer, int i, boolean bl) {
				if (serverPlayer.experienceLevel < i) {
					return false;
				} else {
					if (!bl) {
						serverPlayer.giveExperienceLevels(-i);
					}

					return true;
				}
			}

			@Override
			public Component display() {
				return DISPLAY;
			}
		},
		HEALTH("health", ClampedNormalInt.of(2.0F, 3.0F, 1, 20)) {
			private static final Component DISPLAY = Component.translatable("vote.cost.health");

			@Override
			public boolean deduct(ServerPlayer serverPlayer, int i, boolean bl) {
				float f = serverPlayer.getHealth();
				if (f < (float)i) {
					return false;
				} else {
					if (!bl) {
						serverPlayer.setHealth(f - (float)i);
						serverPlayer.hurt(serverPlayer.getLevel().damageSources().generic(), 0.0F);
					}

					return true;
				}
			}

			@Override
			public Component display() {
				return DISPLAY;
			}
		};

		public static final Codec<VotingMaterial.VotingResource> CODEC = StringRepresentable.fromEnum(VotingMaterial.VotingResource::values);
		private final String id;
		final IntProvider costGenerator;

		@Override
		public String getSerializedName() {
			return this.id;
		}

		VotingResource(String string2, IntProvider intProvider) {
			this.id = string2;
			this.costGenerator = intProvider;
		}

		@Override
		public VotingMaterial.Type type() {
			return VotingMaterial.Type.RESOURCE;
		}
	}
}
