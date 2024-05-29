package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class BlockElement {
	private static final boolean DEFAULT_RESCALE = false;
	private static final float MIN_EXTENT = -16.0F;
	private static final float MAX_EXTENT = 32.0F;
	public final Vector3f from;
	public final Vector3f to;
	public final Map<Direction, BlockElementFace> faces;
	public final BlockElementRotation rotation;
	public final boolean shade;

	public BlockElement(
		Vector3f vector3f, Vector3f vector3f2, Map<Direction, BlockElementFace> map, @Nullable BlockElementRotation blockElementRotation, boolean bl
	) {
		this.from = vector3f;
		this.to = vector3f2;
		this.faces = map;
		this.rotation = blockElementRotation;
		this.shade = bl;
		this.fillUvs();
	}

	private void fillUvs() {
		for (Entry<Direction, BlockElementFace> entry : this.faces.entrySet()) {
			float[] fs = this.uvsByFace((Direction)entry.getKey());
			((BlockElementFace)entry.getValue()).uv().setMissingUv(fs);
		}
	}

	private float[] uvsByFace(Direction direction) {
		switch (direction) {
			case DOWN:
				return new float[]{this.from.x(), 16.0F - this.to.z(), this.to.x(), 16.0F - this.from.z()};
			case UP:
				return new float[]{this.from.x(), this.from.z(), this.to.x(), this.to.z()};
			case NORTH:
			default:
				return new float[]{16.0F - this.to.x(), 16.0F - this.to.y(), 16.0F - this.from.x(), 16.0F - this.from.y()};
			case SOUTH:
				return new float[]{this.from.x(), 16.0F - this.to.y(), this.to.x(), 16.0F - this.from.y()};
			case WEST:
				return new float[]{this.from.z(), 16.0F - this.to.y(), this.to.z(), 16.0F - this.from.y()};
			case EAST:
				return new float[]{16.0F - this.to.z(), 16.0F - this.to.y(), 16.0F - this.from.z(), 16.0F - this.from.y()};
		}
	}

	@Environment(EnvType.CLIENT)
	protected static class Deserializer implements JsonDeserializer<BlockElement> {
		private static final boolean DEFAULT_SHADE = true;

		public BlockElement deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			Vector3f vector3f = this.getFrom(jsonObject);
			Vector3f vector3f2 = this.getTo(jsonObject);
			BlockElementRotation blockElementRotation = this.getRotation(jsonObject);
			Map<Direction, BlockElementFace> map = this.getFaces(jsonDeserializationContext, jsonObject);
			if (jsonObject.has("shade") && !GsonHelper.isBooleanValue(jsonObject, "shade")) {
				throw new JsonParseException("Expected shade to be a Boolean");
			} else {
				boolean bl = GsonHelper.getAsBoolean(jsonObject, "shade", true);
				return new BlockElement(vector3f, vector3f2, map, blockElementRotation, bl);
			}
		}

		@Nullable
		private BlockElementRotation getRotation(JsonObject jsonObject) {
			BlockElementRotation blockElementRotation = null;
			if (jsonObject.has("rotation")) {
				JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "rotation");
				Vector3f vector3f = this.getVector3f(jsonObject2, "origin");
				vector3f.mul(0.0625F);
				Direction.Axis axis = this.getAxis(jsonObject2);
				float f = this.getAngle(jsonObject2);
				boolean bl = GsonHelper.getAsBoolean(jsonObject2, "rescale", false);
				blockElementRotation = new BlockElementRotation(vector3f, axis, f, bl);
			}

			return blockElementRotation;
		}

		private float getAngle(JsonObject jsonObject) {
			float f = GsonHelper.getAsFloat(jsonObject, "angle");
			if (f != 0.0F && Mth.abs(f) != 22.5F && Mth.abs(f) != 45.0F) {
				throw new JsonParseException("Invalid rotation " + f + " found, only -45/-22.5/0/22.5/45 allowed");
			} else {
				return f;
			}
		}

		private Direction.Axis getAxis(JsonObject jsonObject) {
			String string = GsonHelper.getAsString(jsonObject, "axis");
			Direction.Axis axis = Direction.Axis.byName(string.toLowerCase(Locale.ROOT));
			if (axis == null) {
				throw new JsonParseException("Invalid rotation axis: " + string);
			} else {
				return axis;
			}
		}

		private Map<Direction, BlockElementFace> getFaces(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
			Map<Direction, BlockElementFace> map = this.filterNullFromFaces(jsonDeserializationContext, jsonObject);
			if (map.isEmpty()) {
				throw new JsonParseException("Expected between 1 and 6 unique faces, got 0");
			} else {
				return map;
			}
		}

		private Map<Direction, BlockElementFace> filterNullFromFaces(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
			Map<Direction, BlockElementFace> map = Maps.newEnumMap(Direction.class);
			JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "faces");

			for (Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
				Direction direction = this.getFacing((String)entry.getKey());
				map.put(direction, (BlockElementFace)jsonDeserializationContext.deserialize((JsonElement)entry.getValue(), BlockElementFace.class));
			}

			return map;
		}

		private Direction getFacing(String string) {
			Direction direction = Direction.byName(string);
			if (direction == null) {
				throw new JsonParseException("Unknown facing: " + string);
			} else {
				return direction;
			}
		}

		private Vector3f getTo(JsonObject jsonObject) {
			Vector3f vector3f = this.getVector3f(jsonObject, "to");
			if (!(vector3f.x() < -16.0F)
				&& !(vector3f.y() < -16.0F)
				&& !(vector3f.z() < -16.0F)
				&& !(vector3f.x() > 32.0F)
				&& !(vector3f.y() > 32.0F)
				&& !(vector3f.z() > 32.0F)) {
				return vector3f;
			} else {
				throw new JsonParseException("'to' specifier exceeds the allowed boundaries: " + vector3f);
			}
		}

		private Vector3f getFrom(JsonObject jsonObject) {
			Vector3f vector3f = this.getVector3f(jsonObject, "from");
			if (!(vector3f.x() < -16.0F)
				&& !(vector3f.y() < -16.0F)
				&& !(vector3f.z() < -16.0F)
				&& !(vector3f.x() > 32.0F)
				&& !(vector3f.y() > 32.0F)
				&& !(vector3f.z() > 32.0F)) {
				return vector3f;
			} else {
				throw new JsonParseException("'from' specifier exceeds the allowed boundaries: " + vector3f);
			}
		}

		private Vector3f getVector3f(JsonObject jsonObject, String string) {
			JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, string);
			if (jsonArray.size() != 3) {
				throw new JsonParseException("Expected 3 " + string + " values, found: " + jsonArray.size());
			} else {
				float[] fs = new float[3];

				for (int i = 0; i < fs.length; i++) {
					fs[i] = GsonHelper.convertToFloat(jsonArray.get(i), string + "[" + i + "]");
				}

				return new Vector3f(fs[0], fs[1], fs[2]);
			}
		}
	}
}
