package net.minecraft.server.packs.resources;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

public interface ResourceMetadata {
	ResourceMetadata EMPTY = new ResourceMetadata() {
		@Override
		public <T> Optional<T> getSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
			return Optional.empty();
		}
	};
	IoSupplier<ResourceMetadata> EMPTY_SUPPLIER = () -> EMPTY;

	static ResourceMetadata fromJsonStream(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

		ResourceMetadata var3;
		try {
			final JsonObject jsonObject = GsonHelper.parse(bufferedReader);
			var3 = new ResourceMetadata() {
				@Override
				public <T> Optional<T> getSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
					String string = metadataSectionSerializer.getMetadataSectionName();
					return jsonObject.has(string) ? Optional.of(metadataSectionSerializer.fromJson(GsonHelper.getAsJsonObject(jsonObject, string))) : Optional.empty();
				}
			};
		} catch (Throwable var5) {
			try {
				bufferedReader.close();
			} catch (Throwable var4) {
				var5.addSuppressed(var4);
			}

			throw var5;
		}

		bufferedReader.close();
		return var3;
	}

	<T> Optional<T> getSection(MetadataSectionSerializer<T> metadataSectionSerializer);
}
