package io.github.tt432.eyelib.client.render.sections.dynamic;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.tt432.eyelib.client.render.sections.RenderTypeExtension;
import io.github.tt432.eyelib.client.render.sections.compat.IrisCompat;
import io.github.tt432.eyelib.client.render.sections.compat.SodiumLikeCompat;
import io.github.tt432.eyelib.client.render.sections.events.ReloadDynamicChunkBufferEvent;
import io.github.tt432.eyelib.client.render.sections.events.SectionGeometryRenderTypeEvents;
import io.github.tt432.eyelib.util.EntryStreams;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Argon4W
 */
public class DynamicChunkBuffers implements ResourceManagerReloadListener {
    public static final AtomicInteger CHUNK_LAYER_IDS = new AtomicInteger(RenderType.chunkBufferLayers().size());
    public static final Function<ResourceLocation, RenderType> CUTOUT = Util.memoize(textureResourceLocation -> Util.make(SodiumLikeCompat.createCutoutRenderType(textureResourceLocation), renderType -> ((RenderTypeExtension) renderType).eyelib$setChunkLayerId(CHUNK_LAYER_IDS.getAndIncrement())));
    public static final Function<ResourceLocation, RenderType> TRANSLUCENT = Util.memoize(textureResourceLocation -> Util.make(SodiumLikeCompat.createTranslucentRenderType(textureResourceLocation), renderType -> ((RenderTypeExtension) renderType).eyelib$setChunkLayerId(CHUNK_LAYER_IDS.getAndIncrement())));
    public static final Map<ResourceLocation, RenderType> DYNAMIC_CUTOUT_LAYERS = new ConcurrentHashMap<>();
    public static final Map<ResourceLocation, RenderType> DYNAMIC_TRANSLUCENT_LAYERS = new ConcurrentHashMap<>();
    public static final Map<ResourceLocation, Function<RenderType, RenderType>> DYNAMIC_MULTI_LAYERS = new ConcurrentHashMap<>();

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        ModLoader.postEvent(new ReloadDynamicChunkBufferEvent());
    }

    public static <T extends Entity> RenderType markCutoutChunkBuffer(T entity) {
        return markCutoutChunkBuffer(getEntityTextureResourceLocation(entity));
    }

    public static <T extends Entity> RenderType markTranslucentChunkBuffer(T entity) {
        return markTranslucentChunkBuffer(getEntityTextureResourceLocation(entity));
    }

    public static <T extends Entity> RenderType markCutoutChunkBuffer(EntityType<? extends T> entityType) {
        return markCutoutChunkBuffer(getEntityTextureResourceLocation(entityType));
    }

    public static <T extends Entity> RenderType markTranslucentChunkBuffer(EntityType<? extends T> entityType) {
        return markTranslucentChunkBuffer(getEntityTextureResourceLocation(entityType));
    }

    public static RenderType markCutoutChunkBuffer(ResourceLocation textureResourceLocation) {
        return SodiumLikeCompat.addSodiumCutoutPass(textureResourceLocation, addCutoutLayer(textureResourceLocation));
    }

    public static RenderType markTranslucentChunkBuffer(ResourceLocation textureResourceLocation) {
        return SodiumLikeCompat.addSodiumTranslucentPass(textureResourceLocation, addTranslucentLayer(textureResourceLocation));
    }

    public static <E extends Entity> void markMultiCutoutChunkBuffer(E entity, ResourceLocation... cutoutTextureResourceLocations) {
        markMultiCutoutChunkBuffer(getEntityTextureResourceLocation(entity), cutoutTextureResourceLocations);
    }

    public static <E extends Entity> void markMultiCutoutChunkBuffer(EntityType<? extends E> entityType, ResourceLocation... cutoutTextureResourceLocations) {
        markMultiCutoutChunkBuffer(getEntityTextureResourceLocation(entityType), cutoutTextureResourceLocations);
    }

    public static <E extends Entity> void markMultiTranslucentChunkBuffer(E entity, ResourceLocation... translucentTextureResourceLocations) {
        markMultiTranslucentChunkBuffer(getEntityTextureResourceLocation(entity), translucentTextureResourceLocations);
    }

    public static <E extends Entity> void markMultiTranslucentChunkBuffer(EntityType<? extends E> entityType, ResourceLocation... translucentTextureResourceLocations) {
        markMultiTranslucentChunkBuffer(getEntityTextureResourceLocation(entityType), translucentTextureResourceLocations);
    }

    public static void markMultiCutoutChunkBuffer(ResourceLocation resourceLocation, ResourceLocation... cutoutTextureResourceLocations) {
        markMultiChunkBuffer(resourceLocation, Stream.of(cutoutTextureResourceLocations).map(EntryStreams.create(DynamicChunkBuffers::markCutoutChunkBuffer)).map(EntryStreams.mapEntryKey(RenderType::entityCutoutNoCull)).collect(EntryStreams.collectSequenced()));
    }

    public static void markMultiTranslucentChunkBuffer(ResourceLocation resourceLocation, ResourceLocation... translucentTextureResourceLocations) {
        markMultiChunkBuffer(resourceLocation, Stream.of(translucentTextureResourceLocations).map(EntryStreams.create(DynamicChunkBuffers::markTranslucentChunkBuffer)).map(EntryStreams.mapEntryKey(RenderType::entityCutoutNoCull)).collect(EntryStreams.collectSequenced()));
    }

    public static <E extends Entity> void markEntityChunkBuffer(E entity) {
        markMultiChunkBuffer(getEntityTextureResourceLocation(entity), Util.make(new LinkedHashMap<>(), map -> collectMultiMixedRenderTypes(entity).stream().map(EntryStreams.create(DynamicChunkBuffers::getRenderTypeTexture)).map(EntryStreams.swap()).forEach(EntryStreams.peekEntryValue((resourceLocation, renderType) -> map.computeIfAbsent(renderType, renderType1 -> renderType1.name.equals("entity_cutout_no_cull") ? markCutoutChunkBuffer(resourceLocation) : markTranslucentChunkBuffer(resourceLocation))))));
    }

    public static void markMultiChunkBuffer(ResourceLocation resourceLocation, SequencedMap<RenderType, RenderType> map) {
        DYNAMIC_MULTI_LAYERS.putIfAbsent(resourceLocation, renderType -> map.getOrDefault(IrisCompat.unwrapRenderType(renderType), map.firstEntry().getValue()));
    }

    public static <E extends Entity> List<RenderType> collectMultiMixedRenderTypes(E entity) {
        return Util.make(new CopyOnWriteArrayList<>(), list -> Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0, 0, 0, 0, 0, new PoseStack(), renderType -> Util.make(new DoNothingVertexConsumer(), ignored -> list.addIfAbsent(IrisCompat.unwrapRenderType(renderType))), LightTexture.FULL_BRIGHT));
    }

    private static RenderType addCutoutLayer(ResourceLocation resourceLocation) {
        return DYNAMIC_CUTOUT_LAYERS.computeIfAbsent(resourceLocation, DynamicChunkBuffers::createCutoutChunkRenderType);
    }

    private static RenderType addTranslucentLayer(ResourceLocation resourceLocation) {
        return DYNAMIC_TRANSLUCENT_LAYERS.computeIfAbsent(resourceLocation, DynamicChunkBuffers::createTranslucentChunkRenderType);
    }

    public static RenderType createCutoutChunkRenderType(ResourceLocation textureResourceLocation) {
        return CUTOUT.apply(textureResourceLocation);
    }

    public static RenderType createTranslucentChunkRenderType(ResourceLocation textureResourceLocation) {
        return TRANSLUCENT.apply(textureResourceLocation);
    }

    public static ResourceLocation getRenderTypeTexture(RenderType renderType) {
        return ((RenderStateShard.TextureStateShard) ((RenderType.CompositeRenderType) renderType).state.textureState).texture.orElseThrow();
    }

    public static <T extends Entity> ResourceLocation getEntityTextureResourceLocation(T entity) {
        return Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity).getTextureLocation(entity);
    }

    @SuppressWarnings("DataFlowIssue")
    public static <T extends Entity> ResourceLocation getEntityTextureResourceLocation(EntityType<T> entityType) {
        return Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(entityType).getTextureLocation(null);
    }
}