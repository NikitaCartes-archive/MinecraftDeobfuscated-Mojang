package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;

@Environment(EnvType.CLIENT)
public class BlockElementFace {
	public static final int NO_TINT = -1;
	public final Direction cullForDirection;
	public final int tintIndex;
	public final String texture;
	public final BlockFaceUV uv;

	public BlockElementFace(@Nullable Direction direction, int i, String string, BlockFaceUV blockFaceUV) {
		this.cullForDirection = direction;
		this.tintIndex = i;
		this.texture = string;
		this.uv = blockFaceUV;
	}

	@Environment(EnvType.CLIENT)
	public static class Deserializer implements JsonDeserializer<BlockElementFace> {
		private static final int DEFAULT_TINT_INDEX = -1;

		protected Deserializer() {
		}

		public BlockElementFace deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			Direction direction = this.getCullFacing(jsonObject);
			int i = this.getTintIndex(jsonObject);
			String string = this.getTexture(jsonObject);
			BlockFaceUV blockFaceUV = jsonDeserializationContext.deserialize(jsonObject, BlockFaceUV.class);
			return new BlockElementFace(direction, i, string, blockFaceUV);
		}

		protected int getTintIndex(JsonObject jsonObject) {
			return GsonHelper.getAsInt(jsonObject, "tintindex", -1);
		}

		private String getTexture(JsonObject jsonObject) {
			return GsonHelper.getAsString(jsonObject, "texture");
		}

		@Nullable
		private Direction getCullFacing(JsonObject jsonObject) {
			String string = GsonHelper.getAsString(jsonObject, "cullface", "");
			return Direction.byName(string);
		}
	}
}
