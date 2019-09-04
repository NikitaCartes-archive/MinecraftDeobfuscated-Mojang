/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FallbackResourceManager
implements ResourceManager {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final List<Pack> fallbacks = Lists.newArrayList();
    private final PackType type;

    public FallbackResourceManager(PackType packType) {
        this.type = packType;
    }

    @Override
    public void add(Pack pack) {
        this.fallbacks.add(pack);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public Set<String> getNamespaces() {
        return Collections.emptySet();
    }

    @Override
    public Resource getResource(ResourceLocation resourceLocation) throws IOException {
        this.validateLocation(resourceLocation);
        Pack pack = null;
        ResourceLocation resourceLocation2 = FallbackResourceManager.getMetadataLocation(resourceLocation);
        for (int i = this.fallbacks.size() - 1; i >= 0; --i) {
            Pack pack2 = this.fallbacks.get(i);
            if (pack == null && pack2.hasResource(this.type, resourceLocation2)) {
                pack = pack2;
            }
            if (!pack2.hasResource(this.type, resourceLocation)) continue;
            InputStream inputStream = null;
            if (pack != null) {
                inputStream = this.getWrappedResource(resourceLocation2, pack);
            }
            return new SimpleResource(pack2.getName(), resourceLocation, this.getWrappedResource(resourceLocation, pack2), inputStream);
        }
        throw new FileNotFoundException(resourceLocation.toString());
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean hasResource(ResourceLocation resourceLocation) {
        if (!this.isValidLocation(resourceLocation)) {
            return false;
        }
        for (int i = this.fallbacks.size() - 1; i >= 0; --i) {
            Pack pack = this.fallbacks.get(i);
            if (!pack.hasResource(this.type, resourceLocation)) continue;
            return true;
        }
        return false;
    }

    protected InputStream getWrappedResource(ResourceLocation resourceLocation, Pack pack) throws IOException {
        InputStream inputStream = pack.getResource(this.type, resourceLocation);
        return LOGGER.isDebugEnabled() ? new LeakedResourceWarningInputStream(inputStream, resourceLocation, pack.getName()) : inputStream;
    }

    private void validateLocation(ResourceLocation resourceLocation) throws IOException {
        if (!this.isValidLocation(resourceLocation)) {
            throw new IOException("Invalid relative path to resource: " + resourceLocation);
        }
    }

    private boolean isValidLocation(ResourceLocation resourceLocation) {
        return !resourceLocation.getPath().contains("..");
    }

    @Override
    public List<Resource> getResources(ResourceLocation resourceLocation) throws IOException {
        this.validateLocation(resourceLocation);
        ArrayList<Resource> list = Lists.newArrayList();
        ResourceLocation resourceLocation2 = FallbackResourceManager.getMetadataLocation(resourceLocation);
        for (Pack pack : this.fallbacks) {
            if (!pack.hasResource(this.type, resourceLocation)) continue;
            InputStream inputStream = pack.hasResource(this.type, resourceLocation2) ? this.getWrappedResource(resourceLocation2, pack) : null;
            list.add(new SimpleResource(pack.getName(), resourceLocation, this.getWrappedResource(resourceLocation, pack), inputStream));
        }
        if (list.isEmpty()) {
            throw new FileNotFoundException(resourceLocation.toString());
        }
        return list;
    }

    @Override
    public Collection<ResourceLocation> listResources(String string, Predicate<String> predicate) {
        ArrayList<ResourceLocation> list = Lists.newArrayList();
        for (Pack pack : this.fallbacks) {
            list.addAll(pack.getResources(this.type, string, Integer.MAX_VALUE, predicate));
        }
        Collections.sort(list);
        return list;
    }

    static ResourceLocation getMetadataLocation(ResourceLocation resourceLocation) {
        return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + ".mcmeta");
    }

    static class LeakedResourceWarningInputStream
    extends FilterInputStream {
        private final String message;
        private boolean closed;

        public LeakedResourceWarningInputStream(InputStream inputStream, ResourceLocation resourceLocation, String string) {
            super(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            new Exception().printStackTrace(new PrintStream(byteArrayOutputStream));
            this.message = "Leaked resource: '" + resourceLocation + "' loaded from pack: '" + string + "'\n" + byteArrayOutputStream;
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }

        protected void finalize() throws Throwable {
            if (!this.closed) {
                LOGGER.warn(this.message);
            }
            super.finalize();
        }
    }
}

