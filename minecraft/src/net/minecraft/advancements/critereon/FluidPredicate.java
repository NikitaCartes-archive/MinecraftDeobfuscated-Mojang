package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class FluidPredicate {
	public static final FluidPredicate ANY = new FluidPredicate(null, null, StatePropertiesPredicate.ANY);
	@Nullable
	private final TagKey<Fluid> tag;
	@Nullable
	private final Fluid fluid;
	private final StatePropertiesPredicate properties;

	public FluidPredicate(@Nullable TagKey<Fluid> tagKey, @Nullable Fluid fluid, StatePropertiesPredicate statePropertiesPredicate) {
		this.tag = tagKey;
		this.fluid = fluid;
		this.properties = statePropertiesPredicate;
	}

	public boolean matches(ServerLevel serverLevel, BlockPos blockPos) {
		if (this == ANY) {
			return true;
		} else if (!serverLevel.isLoaded(blockPos)) {
			return false;
		} else {
			FluidState fluidState = serverLevel.getFluidState(blockPos);
			if (this.tag != null && !fluidState.is(this.tag)) {
				return false;
			} else {
				return this.fluid != null && !fluidState.is(this.fluid) ? false : this.properties.matches(fluidState);
			}
		}
	}

	public static FluidPredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "fluid");
			Fluid fluid = null;
			if (jsonObject.has("fluid")) {
				ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "fluid"));
				fluid = BuiltInRegistries.FLUID.get(resourceLocation);
			}

			TagKey<Fluid> tagKey = null;
			if (jsonObject.has("tag")) {
				ResourceLocation resourceLocation2 = new ResourceLocation(GsonHelper.getAsString(jsonObject, "tag"));
				tagKey = TagKey.create(Registries.FLUID, resourceLocation2);
			}

			StatePropertiesPredicate statePropertiesPredicate = StatePropertiesPredicate.fromJson(jsonObject.get("state"));
			return new FluidPredicate(tagKey, fluid, statePropertiesPredicate);
		} else {
			return ANY;
		}
	}

	public JsonElement serializeToJson() {
		if (this == ANY) {
			return JsonNull.INSTANCE;
		} else {
			JsonObject jsonObject = new JsonObject();
			if (this.fluid != null) {
				jsonObject.addProperty("fluid", BuiltInRegistries.FLUID.getKey(this.fluid).toString());
			}

			if (this.tag != null) {
				jsonObject.addProperty("tag", this.tag.location().toString());
			}

			jsonObject.add("state", this.properties.serializeToJson());
			return jsonObject;
		}
	}

	public static class Builder {
		@Nullable
		private Fluid fluid;
		@Nullable
		private TagKey<Fluid> fluids;
		private StatePropertiesPredicate properties = StatePropertiesPredicate.ANY;

		private Builder() {
		}

		public static FluidPredicate.Builder fluid() {
			return new FluidPredicate.Builder();
		}

		public FluidPredicate.Builder of(Fluid fluid) {
			this.fluid = fluid;
			return this;
		}

		public FluidPredicate.Builder of(TagKey<Fluid> tagKey) {
			this.fluids = tagKey;
			return this;
		}

		public FluidPredicate.Builder setProperties(StatePropertiesPredicate statePropertiesPredicate) {
			this.properties = statePropertiesPredicate;
			return this;
		}

		public FluidPredicate build() {
			return new FluidPredicate(this.fluids, this.fluid, this.properties);
		}
	}
}
