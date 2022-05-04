/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.searchtree;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.searchtree.SuffixArray;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public interface ResourceLocationSearchTree<T> {
    public static <T> ResourceLocationSearchTree<T> empty() {
        return new ResourceLocationSearchTree<T>(){

            @Override
            public List<T> searchNamespace(String string) {
                return List.of();
            }

            @Override
            public List<T> searchPath(String string) {
                return List.of();
            }
        };
    }

    public static <T> ResourceLocationSearchTree<T> create(List<T> list, Function<T, Stream<ResourceLocation>> function) {
        if (list.isEmpty()) {
            return ResourceLocationSearchTree.empty();
        }
        final SuffixArray suffixArray = new SuffixArray();
        final SuffixArray suffixArray2 = new SuffixArray();
        for (Object object : list) {
            function.apply(object).forEach(resourceLocation -> {
                suffixArray.add(object, resourceLocation.getNamespace().toLowerCase(Locale.ROOT));
                suffixArray2.add(object, resourceLocation.getPath().toLowerCase(Locale.ROOT));
            });
        }
        suffixArray.generate();
        suffixArray2.generate();
        return new ResourceLocationSearchTree<T>(){

            @Override
            public List<T> searchNamespace(String string) {
                return suffixArray.search(string);
            }

            @Override
            public List<T> searchPath(String string) {
                return suffixArray2.search(string);
            }
        };
    }

    public List<T> searchNamespace(String var1);

    public List<T> searchPath(String var1);
}

