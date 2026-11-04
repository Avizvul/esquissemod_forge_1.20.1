package net.avizvul.esquissemod.client;

import net.avizvul.esquissemod.EsquisseMod;
import net.avizvul.esquissemod.client.screen.SketchbookScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = EsquisseMod.MOD_ID, value = Dist.CLIENT)
public class SketchRenderHooks {
    private static final Set<net.minecraft.resources.ResourceLocation> HIDDEN_IN_SKETCHBOOK = Stream.of(
            VanillaGuiOverlay.HOTBAR, VanillaGuiOverlay.ITEM_NAME,
            VanillaGuiOverlay.PLAYER_HEALTH, VanillaGuiOverlay.ARMOR_LEVEL, VanillaGuiOverlay.FOOD_LEVEL,
            VanillaGuiOverlay.AIR_LEVEL, VanillaGuiOverlay.MOUNT_HEALTH, VanillaGuiOverlay.JUMP_BAR,
            VanillaGuiOverlay.EXPERIENCE_BAR).map(VanillaGuiOverlay::id).collect(Collectors.toSet());

    @SubscribeEvent
    public static void onFrameStart(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            SketchTextureCache.processPending();
        }
    }

    /** Хотбар и полоски над ним закрывали бы кнопки инструментов внизу экрана скетчбука. */
    @SubscribeEvent
    public static void hideHudInSketchbook(RenderGuiOverlayEvent.Pre event) {
        NamedGuiOverlay overlay = event.getOverlay();
        if (HIDDEN_IN_SKETCHBOOK.contains(overlay.id()) && Minecraft.getInstance().screen instanceof SketchbookScreen) {
            event.setCanceled(true);
        }
    }
}
