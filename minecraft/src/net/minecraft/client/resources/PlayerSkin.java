package net.minecraft.client.resources;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public record PlayerSkin(
	ResourceLocation texture, @Nullable ResourceLocation capeTexture, @Nullable ResourceLocation elytraTexture, PlayerSkin.Model model, boolean secure
) {
	@Environment(EnvType.CLIENT)
	public static enum Model {
		SLIM("slim"),
		WIDE("default");

		private final String id;

		private Model(String string2) {
			this.id = string2;
		}

		public static PlayerSkin.Model byName(@Nullable String string) {
			if (string == null) {
				return WIDE;
			} else {
				byte var2 = -1;
				switch (string.hashCode()) {
					case 3533117:
						if (string.equals("slim")) {
							var2 = 0;
						}
					default:
						return switch (var2) {
							case 0 -> SLIM;
							default -> WIDE;
						};
				}
			}
		}

		public String id() {
			return this.id;
		}
	}
}
