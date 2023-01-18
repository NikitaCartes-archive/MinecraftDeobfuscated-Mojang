package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
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
import org.apache.commons.lang3.ArrayUtils;

public class Advancement {
	@Nullable
	private final Advancement parent;
	@Nullable
	private final DisplayInfo display;
	private final AdvancementRewards rewards;
	private final ResourceLocation id;
	private final Map<String, Criterion> criteria;
	private final String[][] requirements;
	private final Set<Advancement> children = Sets.<Advancement>newLinkedHashSet();
	private final Component chatComponent;

	public Advancement(
		ResourceLocation resourceLocation,
		@Nullable Advancement advancement,
		@Nullable DisplayInfo displayInfo,
		AdvancementRewards advancementRewards,
		Map<String, Criterion> map,
		String[][] strings
	) {
		this.id = resourceLocation;
		this.display = displayInfo;
		this.criteria = ImmutableMap.copyOf(map);
		this.parent = advancement;
		this.rewards = advancementRewards;
		this.requirements = strings;
		if (advancement != null) {
			advancement.addChild(this);
		}

		if (displayInfo == null) {
			this.chatComponent = Component.literal(resourceLocation.toString());
		} else {
			Component component = displayInfo.getTitle();
			ChatFormatting chatFormatting = displayInfo.getFrame().getChatColor();
			Component component2 = ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withColor(chatFormatting)).append("\n").append(displayInfo.getDescription());
			Component component3 = component.copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component2)));
			this.chatComponent = ComponentUtils.wrapInSquareBrackets(component3).withStyle(chatFormatting);
		}
	}

	public Advancement.Builder deconstruct() {
		return new Advancement.Builder(this.parent == null ? null : this.parent.getId(), this.display, this.rewards, this.criteria, this.requirements);
	}

	@Nullable
	public Advancement getParent() {
		return this.parent;
	}

	public Advancement getRoot() {
		return getRoot(this);
	}

	public static Advancement getRoot(Advancement advancement) {
		Advancement advancement2 = advancement;

		while (true) {
			Advancement advancement3 = advancement2.getParent();
			if (advancement3 == null) {
				return advancement2;
			}

			advancement2 = advancement3;
		}
	}

	@Nullable
	public DisplayInfo getDisplay() {
		return this.display;
	}

	public AdvancementRewards getRewards() {
		return this.rewards;
	}

	public String toString() {
		return "SimpleAdvancement{id="
			+ this.getId()
			+ ", parent="
			+ (this.parent == null ? "null" : this.parent.getId())
			+ ", display="
			+ this.display
			+ ", rewards="
			+ this.rewards
			+ ", criteria="
			+ this.criteria
			+ ", requirements="
			+ Arrays.deepToString(this.requirements)
			+ "}";
	}

	public Iterable<Advancement> getChildren() {
		return this.children;
	}

	public Map<String, Criterion> getCriteria() {
		return this.criteria;
	}

	public int getMaxCriteraRequired() {
		return this.requirements.length;
	}

	public void addChild(Advancement advancement) {
		this.children.add(advancement);
	}

	public ResourceLocation getId() {
		return this.id;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof Advancement advancement) ? false : this.id.equals(advancement.id);
		}
	}

	public int hashCode() {
		return this.id.hashCode();
	}

	public String[][] getRequirements() {
		return this.requirements;
	}

	public Component getChatComponent() {
		return this.chatComponent;
	}

	public static class Builder {
		@Nullable
		private ResourceLocation parentId;
		@Nullable
		private Advancement parent;
		@Nullable
		private DisplayInfo display;
		private AdvancementRewards rewards = AdvancementRewards.EMPTY;
		private Map<String, Criterion> criteria = Maps.<String, Criterion>newLinkedHashMap();
		@Nullable
		private String[][] requirements;
		private RequirementsStrategy requirementsStrategy = RequirementsStrategy.AND;

		Builder(
			@Nullable ResourceLocation resourceLocation,
			@Nullable DisplayInfo displayInfo,
			AdvancementRewards advancementRewards,
			Map<String, Criterion> map,
			String[][] strings
		) {
			this.parentId = resourceLocation;
			this.display = displayInfo;
			this.rewards = advancementRewards;
			this.criteria = map;
			this.requirements = strings;
		}

		private Builder() {
		}

		public static Advancement.Builder advancement() {
			return new Advancement.Builder();
		}

		public Advancement.Builder parent(Advancement advancement) {
			this.parent = advancement;
			return this;
		}

		public Advancement.Builder parent(ResourceLocation resourceLocation) {
			this.parentId = resourceLocation;
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
			this.display = displayInfo;
			return this;
		}

		public Advancement.Builder rewards(AdvancementRewards.Builder builder) {
			return this.rewards(builder.build());
		}

		public Advancement.Builder rewards(AdvancementRewards advancementRewards) {
			this.rewards = advancementRewards;
			return this;
		}

		public Advancement.Builder addCriterion(String string, CriterionTriggerInstance criterionTriggerInstance) {
			return this.addCriterion(string, new Criterion(criterionTriggerInstance));
		}

		public Advancement.Builder addCriterion(String string, Criterion criterion) {
			if (this.criteria.containsKey(string)) {
				throw new IllegalArgumentException("Duplicate criterion " + string);
			} else {
				this.criteria.put(string, criterion);
				return this;
			}
		}

		public Advancement.Builder requirements(RequirementsStrategy requirementsStrategy) {
			this.requirementsStrategy = requirementsStrategy;
			return this;
		}

		public Advancement.Builder requirements(String[][] strings) {
			this.requirements = strings;
			return this;
		}

		public boolean canBuild(Function<ResourceLocation, Advancement> function) {
			if (this.parentId == null) {
				return true;
			} else {
				if (this.parent == null) {
					this.parent = (Advancement)function.apply(this.parentId);
				}

				return this.parent != null;
			}
		}

		public Advancement build(ResourceLocation resourceLocation) {
			if (!this.canBuild(resourceLocationx -> null)) {
				throw new IllegalStateException("Tried to build incomplete advancement!");
			} else {
				if (this.requirements == null) {
					this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
				}

				return new Advancement(resourceLocation, this.parent, this.display, this.rewards, this.criteria, this.requirements);
			}
		}

		public Advancement save(Consumer<Advancement> consumer, String string) {
			Advancement advancement = this.build(new ResourceLocation(string));
			consumer.accept(advancement);
			return advancement;
		}

		public JsonObject serializeToJson() {
			if (this.requirements == null) {
				this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
			}

			JsonObject jsonObject = new JsonObject();
			if (this.parent != null) {
				jsonObject.addProperty("parent", this.parent.getId().toString());
			} else if (this.parentId != null) {
				jsonObject.addProperty("parent", this.parentId.toString());
			}

			if (this.display != null) {
				jsonObject.add("display", this.display.serializeToJson());
			}

			jsonObject.add("rewards", this.rewards.serializeToJson());
			JsonObject jsonObject2 = new JsonObject();

			for (Entry<String, Criterion> entry : this.criteria.entrySet()) {
				jsonObject2.add((String)entry.getKey(), ((Criterion)entry.getValue()).serializeToJson());
			}

			jsonObject.add("criteria", jsonObject2);
			JsonArray jsonArray = new JsonArray();

			for (String[] strings : this.requirements) {
				JsonArray jsonArray2 = new JsonArray();

				for (String string : strings) {
					jsonArray2.add(string);
				}

				jsonArray.add(jsonArray2);
			}

			jsonObject.add("requirements", jsonArray);
			return jsonObject;
		}

		public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf) {
			if (this.requirements == null) {
				this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
			}

			friendlyByteBuf.writeNullable(this.parentId, FriendlyByteBuf::writeResourceLocation);
			friendlyByteBuf.writeNullable(this.display, (friendlyByteBufx, displayInfo) -> displayInfo.serializeToNetwork(friendlyByteBufx));
			Criterion.serializeToNetwork(this.criteria, friendlyByteBuf);
			friendlyByteBuf.writeVarInt(this.requirements.length);

			for (String[] strings : this.requirements) {
				friendlyByteBuf.writeVarInt(strings.length);

				for (String string : strings) {
					friendlyByteBuf.writeUtf(string);
				}
			}
		}

		public String toString() {
			return "Task Advancement{parentId="
				+ this.parentId
				+ ", display="
				+ this.display
				+ ", rewards="
				+ this.rewards
				+ ", criteria="
				+ this.criteria
				+ ", requirements="
				+ Arrays.deepToString(this.requirements)
				+ "}";
		}

		public static Advancement.Builder fromJson(JsonObject jsonObject, DeserializationContext deserializationContext) {
			ResourceLocation resourceLocation = jsonObject.has("parent") ? new ResourceLocation(GsonHelper.getAsString(jsonObject, "parent")) : null;
			DisplayInfo displayInfo = jsonObject.has("display") ? DisplayInfo.fromJson(GsonHelper.getAsJsonObject(jsonObject, "display")) : null;
			AdvancementRewards advancementRewards = jsonObject.has("rewards")
				? AdvancementRewards.deserialize(GsonHelper.getAsJsonObject(jsonObject, "rewards"))
				: AdvancementRewards.EMPTY;
			Map<String, Criterion> map = Criterion.criteriaFromJson(GsonHelper.getAsJsonObject(jsonObject, "criteria"), deserializationContext);
			if (map.isEmpty()) {
				throw new JsonSyntaxException("Advancement criteria cannot be empty");
			} else {
				JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "requirements", new JsonArray());
				String[][] strings = new String[jsonArray.size()][];

				for (int i = 0; i < jsonArray.size(); i++) {
					JsonArray jsonArray2 = GsonHelper.convertToJsonArray(jsonArray.get(i), "requirements[" + i + "]");
					strings[i] = new String[jsonArray2.size()];

					for (int j = 0; j < jsonArray2.size(); j++) {
						strings[i][j] = GsonHelper.convertToString(jsonArray2.get(j), "requirements[" + i + "][" + j + "]");
					}
				}

				if (strings.length == 0) {
					strings = new String[map.size()][];
					int i = 0;

					for (String string : map.keySet()) {
						strings[i++] = new String[]{string};
					}
				}

				for (String[] strings2 : strings) {
					if (strings2.length == 0 && map.isEmpty()) {
						throw new JsonSyntaxException("Requirement entry cannot be empty");
					}

					for (String string2 : strings2) {
						if (!map.containsKey(string2)) {
							throw new JsonSyntaxException("Unknown required criterion '" + string2 + "'");
						}
					}
				}

				for (String string3 : map.keySet()) {
					boolean bl = false;

					for (String[] strings3 : strings) {
						if (ArrayUtils.contains(strings3, string3)) {
							bl = true;
							break;
						}
					}

					if (!bl) {
						throw new JsonSyntaxException(
							"Criterion '" + string3 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required."
						);
					}
				}

				return new Advancement.Builder(resourceLocation, displayInfo, advancementRewards, map, strings);
			}
		}

		public static Advancement.Builder fromNetwork(FriendlyByteBuf friendlyByteBuf) {
			ResourceLocation resourceLocation = friendlyByteBuf.readNullable(FriendlyByteBuf::readResourceLocation);
			DisplayInfo displayInfo = friendlyByteBuf.readNullable(DisplayInfo::fromNetwork);
			Map<String, Criterion> map = Criterion.criteriaFromNetwork(friendlyByteBuf);
			String[][] strings = new String[friendlyByteBuf.readVarInt()][];

			for (int i = 0; i < strings.length; i++) {
				strings[i] = new String[friendlyByteBuf.readVarInt()];

				for (int j = 0; j < strings[i].length; j++) {
					strings[i][j] = friendlyByteBuf.readUtf();
				}
			}

			return new Advancement.Builder(resourceLocation, displayInfo, AdvancementRewards.EMPTY, map, strings);
		}

		public Map<String, Criterion> getCriteria() {
			return this.criteria;
		}
	}
}
