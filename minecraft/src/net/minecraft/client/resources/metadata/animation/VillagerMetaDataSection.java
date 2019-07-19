package net.minecraft.client.resources.metadata.animation;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VillagerMetaDataSection {
	public static final VillagerMetadataSectionSerializer SERIALIZER = new VillagerMetadataSectionSerializer();
	private final VillagerMetaDataSection.Hat hat;

	public VillagerMetaDataSection(VillagerMetaDataSection.Hat hat) {
		this.hat = hat;
	}

	public VillagerMetaDataSection.Hat getHat() {
		return this.hat;
	}

	@Environment(EnvType.CLIENT)
	public static enum Hat {
		NONE("none"),
		PARTIAL("partial"),
		FULL("full");

		private static final Map<String, VillagerMetaDataSection.Hat> BY_NAME = (Map<String, VillagerMetaDataSection.Hat>)Arrays.stream(values())
			.collect(Collectors.toMap(VillagerMetaDataSection.Hat::getName, hat -> hat));
		private final String name;

		private Hat(String string2) {
			this.name = string2;
		}

		public String getName() {
			return this.name;
		}

		public static VillagerMetaDataSection.Hat getByName(String string) {
			return (VillagerMetaDataSection.Hat)BY_NAME.getOrDefault(string, NONE);
		}
	}
}
