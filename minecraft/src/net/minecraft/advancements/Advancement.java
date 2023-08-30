package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record Advancement(
	Optional<ResourceLocation> parent,
	Optional<DisplayInfo> display,
	AdvancementRewards rewards,
	Map<String, Criterion<?>> criteria,
	AdvancementRequirements requirements,
	boolean sendsTelemetryEvent,
	Optional<Component> name
) {
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

	private static Component decorateName(DisplayInfo displayInfo) {
		Component component = displayInfo.getTitle();
		ChatFormatting chatFormatting = displayInfo.getFrame().getChatColor();
		Component component2 = ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withColor(chatFormatting)).append("\n").append(displayInfo.getDescription());
		Component component3 = component.copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component2)));
		return ComponentUtils.wrapInSquareBrackets(component3).withStyle(chatFormatting);
	}

	public static Component name(AdvancementHolder advancementHolder) {
		return (Component)advancementHolder.value().name().orElseGet(() -> Component.literal(advancementHolder.id().toString()));
	}

	public JsonObject serializeToJson() {
		JsonObject jsonObject = new JsonObject();
		this.parent.ifPresent(resourceLocation -> jsonObject.addProperty("parent", resourceLocation.toString()));
		this.display.ifPresent(displayInfo -> jsonObject.add("display", displayInfo.serializeToJson()));
		jsonObject.add("rewards", this.rewards.serializeToJson());
		JsonObject jsonObject2 = new JsonObject();

		for (Entry<String, Criterion<?>> entry : this.criteria.entrySet()) {
			jsonObject2.add((String)entry.getKey(), ((Criterion)entry.getValue()).serializeToJson());
		}

		jsonObject.add("criteria", jsonObject2);
		jsonObject.add("requirements", this.requirements.toJson());
		jsonObject.addProperty("sends_telemetry_event", this.sendsTelemetryEvent);
		return jsonObject;
	}

	public static Advancement fromJson(JsonObject jsonObject, DeserializationContext deserializationContext) {
		Optional<ResourceLocation> optional = jsonObject.has("parent")
			? Optional.of(new ResourceLocation(GsonHelper.getAsString(jsonObject, "parent")))
			: Optional.empty();
		Optional<DisplayInfo> optional2 = jsonObject.has("display")
			? Optional.of(DisplayInfo.fromJson(GsonHelper.getAsJsonObject(jsonObject, "display")))
			: Optional.empty();
		AdvancementRewards advancementRewards = jsonObject.has("rewards")
			? AdvancementRewards.deserialize(GsonHelper.getAsJsonObject(jsonObject, "rewards"))
			: AdvancementRewards.EMPTY;
		Map<String, Criterion<?>> map = Criterion.criteriaFromJson(GsonHelper.getAsJsonObject(jsonObject, "criteria"), deserializationContext);
		if (map.isEmpty()) {
			throw new JsonSyntaxException("Advancement criteria cannot be empty");
		} else {
			AdvancementRequirements advancementRequirements = AdvancementRequirements.fromJson(
				GsonHelper.getAsJsonArray(jsonObject, "requirements", new JsonArray()), map.keySet()
			);
			if (advancementRequirements.isEmpty()) {
				advancementRequirements = AdvancementRequirements.allOf(map.keySet());
			}

			boolean bl = GsonHelper.getAsBoolean(jsonObject, "sends_telemetry_event", false);
			return new Advancement(optional, optional2, advancementRewards, map, advancementRequirements, bl);
		}
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeOptional(this.parent, FriendlyByteBuf::writeResourceLocation);
		friendlyByteBuf.writeOptional(this.display, (friendlyByteBufx, displayInfo) -> displayInfo.serializeToNetwork(friendlyByteBufx));
		this.requirements.write(friendlyByteBuf);
		friendlyByteBuf.writeBoolean(this.sendsTelemetryEvent);
	}

	public static Advancement read(FriendlyByteBuf friendlyByteBuf) {
		return new Advancement(
			friendlyByteBuf.readOptional(FriendlyByteBuf::readResourceLocation),
			friendlyByteBuf.readOptional(DisplayInfo::fromNetwork),
			AdvancementRewards.EMPTY,
			Map.of(),
			new AdvancementRequirements(friendlyByteBuf),
			friendlyByteBuf.readBoolean()
		);
	}

	public boolean isRoot() {
		return this.parent.isEmpty();
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
			FrameType frameType,
			boolean bl,
			boolean bl2,
			boolean bl3
		) {
			return this.display(new DisplayInfo(itemStack, component, component2, resourceLocation, frameType, bl, bl2, bl3));
		}

		public Advancement.Builder display(
			ItemLike itemLike,
			Component component,
			Component component2,
			@Nullable ResourceLocation resourceLocation,
			FrameType frameType,
			boolean bl,
			boolean bl2,
			boolean bl3
		) {
			return this.display(new DisplayInfo(new ItemStack(itemLike.asItem()), component, component2, resourceLocation, frameType, bl, bl2, bl3));
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
