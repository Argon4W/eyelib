package io.github.tt432.eyelib.client.loader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.github.tt432.eyelib.client.animation.bedrock.BrAnimation;
import io.github.tt432.eyelib.molang.MolangCompileHandler;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author TT432
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BrAnimationLoader extends SimpleJsonResourceReloadListener {
    public static final BrAnimationLoader INSTANCE = new BrAnimationLoader(new Gson(), "animations/bedrock");

    @SubscribeEvent
    public static void onEvent(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(INSTANCE);
    }

    @Getter
    private final Map<ResourceLocation, BrAnimation> animations = new HashMap<>();

    private BrAnimationLoader(Gson pGson, String pDirectory) {
        super(pGson, pDirectory);
    }

    public static BrAnimation getAnimation(ResourceLocation resourceLocation) {
        return INSTANCE.animations.get(resourceLocation);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        animations.clear();

        pObject.forEach((rl, json) -> {
            animations.put(rl, BrAnimation.parse(rl.toString(), json.getAsJsonObject()));
            MolangCompileHandler.tryCompileAll(rl.toString().replace(":", "$"));
        });
    }
}
