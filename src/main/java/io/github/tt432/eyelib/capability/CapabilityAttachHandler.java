package io.github.tt432.eyelib.capability;

import io.github.tt432.eyelib.Eyelib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author TT432
 */
@Mod.EventBusSubscriber
public class CapabilityAttachHandler {
    @SubscribeEvent
    public static void onEvent(AttachCapabilitiesEvent event) {
        if (event.getObject() instanceof Level || event.getObject() instanceof LevelChunk)
            return;

        event.addCapability(new ResourceLocation(Eyelib.MOD_ID, "animatable"), new AnimatableCapability.Provider(event.getObject()));
    }
}
