package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootDataResolver;

public record Advancement(
	Optional<ResourceLocation> parent,
	Optional<DisplayInfo> display,
	AdvancementRewards rewards,
	Map<String, Criterion<?>> criteria,
	AdvancementRequirements requirements,
	boolean sendsTelemetryEvent,
	Optional<Component> name
) {
	private static final Codec<Map<String, Criterion<?>>> CRITERIA_CODEC = ExtraCodecs.validate(
		Codec.unboundedMap(Codec.STRING, Criterion.CODEC),
		map -> map.isEmpty() ? DataResult.error(() -> "Advancement criteria cannot be empty") : DataResult.success(map)
	);
	public static final Codec<Advancement> CODEC = ExtraCodecs.validate(
		RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(ResourceLocation.CODEC, "parent").forGetter(Advancement::parent),
						ExtraCodecs.strictOptionalField(DisplayInfo.CODEC, "display").forGetter(Advancement::display),
						ExtraCodecs.strictOptionalField(AdvancementRewards.CODEC, "rewards", AdvancementRewards.EMPTY).forGetter(Advancement::rewards),
						CRITERIA_CODEC.fieldOf("criteria").forGetter(Advancement::criteria),
						ExtraCodecs.strictOptionalField(AdvancementRequirements.CODEC, "requirements").forGetter(advancement -> Optional.of(advancement.requirements())),
						ExtraCodecs.strictOptionalField(Codec.BOOL, "sends_telemetry_event", false).forGetter(Advancement::sendsTelemetryEvent)
					)
					.apply(instance, (optional, optional2, advancementRewards, map, optional3, boolean_) -> {
						AdvancementRequirements advancementRequirements = (AdvancementRequirements)optional3.orElseGet(() -> AdvancementRequirements.allOf(map.keySet()));
						return new Advancement(optional, optional2, advancementRewards, map, advancementRequirements, boolean_);
					})
		),
		Advancement::validate
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, Advancement> STREAM_CODEC = StreamCodec.ofMember(Advancement::write, Advancement::read);

	public Advancement(
		Optional<ResourceLocation> optional,
		Optional<DisplayInfo> optional2,
		AdvancementRewards advancementRewards,
		Map<String, Criterion<?>> map,
		AdvancementRequirements advancementRequirements,
		boolean bl
	) {
		this(optional, optional2, advancementRewards, Map.copyOf(map), advancementRequirements, bl, optional2.map(Advancement::decorateName));
	}

	private static DataResult<Advancement> validate(Advancement advancement) {
		return advancement.requirements().validate(advancement.criteria().keySet()).map(advancementRequirements -> advancement);
	}

	private static Component decorateName(DisplayInfo displayInfo) {
		Component component = displayInfo.getTitle();
		ChatFormatting chatFormatting = displayInfo.getType().getChatColor();
		Component component2 = ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withColor(chatFormatting)).append("\n").append(displayInfo.getDescription());
		Component component3 = component.copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component2)));
		return ComponentUtils.wrapInSquareBrackets(component3).withStyle(chatFormatting);
	}

	public static Component name(AdvancementHolder advancementHolder) {
		return (Component)advancementHolder.value().name().orElseGet(() -> Component.literal(advancementHolder.id().toString()));
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeOptional(this.parent, FriendlyByteBuf::writeResourceLocation);
		DisplayInfo.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(registryFriendlyByteBuf, this.display);
		this.requirements.write(registryFriendlyByteBuf);
		registryFriendlyByteBuf.writeBoolean(this.sendsTelemetryEvent);
	}

	private static Advancement read(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		return new Advancement(
			registryFriendlyByteBuf.readOptional(FriendlyByteBuf::readResourceLocation),
			(Optional<DisplayInfo>)DisplayInfo.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(registryFriendlyByteBuf),
			AdvancementRewards.EMPTY,
			Map.of(),
			new AdvancementRequirements(registryFriendlyByteBuf),
			registryFriendlyByteBuf.readBoolean()
		);
	}

	public boolean isRoot() {
		return this.parent.isEmpty();
	}

	public void validate(ProblemReporter problemReporter, LootDataResolver lootDataResolver) {
		this.criteria.forEach((string, criterion) -> {
			CriterionValidator criterionValidator = new CriterionValidator(problemReporter.forChild(string), lootDataResolver);
			criterion.triggerInstance().validate(criterionValidator);
		});
	}

	public static class Builder {
		private Optional<ResourceLocation> parent = Optional.empty();
		private Optional<DisplayInfo> display = Optional.empty();
		private AdvancementRewards rewards = AdvancementRewards.EMPTY;
		private final ImmutableMap.Builder<String, Criterion<?>> criteria = ImmutableMap.builder();
		private Optional<AdvancementRequirements> requirements = Optional.empty();
		private AdvancementRequirements.Strategy requirementsStrategy = AdvancementRequirements.Strategy.AND;
		private boolean sendsTelemetryEvent;

		public static Advancement.Builder advancement() {
			return new Advancement.Builder().sendsTelemetryEvent();
		}

		public static Advancement.Builder recipeAdvancement() {
			return new Advancement.Builder();
		}

		public Advancement.Builder parent(AdvancementHolder advancementHolder) {
			this.parent = Optional.of(advancementHolder.id());
			return this;
		}

		@Deprecated(
			forRemoval = true
		)
		public Advancement.Builder parent(ResourceLocation resourceLocation) {
			this.parent = Optional.of(resourceLocation);
			return this;
		}

		public Advancement.Builder display(
			ItemStack itemStack,
			Component component,
			Component component2,
			@Nullable ResourceLocation resourceLocation,
			AdvancementType advancementType,
			boolean bl,
			boolean bl2,
			boolean bl3
		) {
			return this.display(new DisplayInfo(itemStack, component, component2, Optional.ofNullable(resourceLocation), advancementType, bl, bl2, bl3));
		}

		public Advancement.Builder display(
			ItemLike itemLike,
			Component component,
			Component component2,
			@Nullable ResourceLocation resourceLocation,
			AdvancementType advancementType,
			boolean bl,
			boolean bl2,
			boolean bl3
		) {
			return this.display(
				new DisplayInfo(new ItemStack(itemLike.asItem()), component, component2, Optional.ofNullable(resourceLocation), advancementType, bl, bl2, bl3)
			);
		}

		public Advancement.Builder display(DisplayInfo displayInfo) {
			this.display = Optional.of(displayInfo);
			return this;
		}

		public Advancement.Builder rewards(AdvancementRewards.Builder builder) {
			return this.rewards(builder.build());
		}

		public Advancement.Builder rewards(AdvancementRewards advancementRewards) {
			this.rewards = advancementRewards;
			return this;
		}

		public Advancement.Builder addCriterion(String string, Criterion<?> criterion) {
			this.criteria.put(string, criterion);
			return this;
		}

		public Advancement.Builder requirements(AdvancementRequirements.Strategy strategy) {
			this.requirementsStrategy = strategy;
			return this;
		}

		public Advancement.Builder requirements(AdvancementRequirements advancementRequirements) {
			this.requirements = Optional.of(advancementRequirements);
			return this;
		}

		public Advancement.Builder sendsTelemetryEvent() {
			this.sendsTelemetryEvent = true;
			return this;
		}

		public AdvancementHolder build(ResourceLocation resourceLocation) {
			Map<String, Criterion<?>> map = this.criteria.buildOrThrow();
			AdvancementRequirements advancementRequirements = (AdvancementRequirements)this.requirements.orElseGet(() -> this.requirementsStrategy.create(map.keySet()));
			return new AdvancementHolder(
				resourceLocation, new Advancement(this.parent, this.display, this.rewards, map, advancementRequirements, this.sendsTelemetryEvent)
			);
		}

		public AdvancementHolder save(Consumer<AdvancementHolder> consumer, String string) {
			AdvancementHolder advancementHolder = this.build(new ResourceLocation(string));
			consumer.accept(advancementHolder);
			return advancementHolder;
		}
	}
}
