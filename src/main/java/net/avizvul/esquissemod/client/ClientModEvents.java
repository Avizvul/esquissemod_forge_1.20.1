package net.avizvul.esquissemod.client;

import net.avizvul.esquissemod.EsquisseMod;
import net.avizvul.esquissemod.client.tooltip.ClientSketchedPageTooltip;
import net.avizvul.esquissemod.client.tooltip.SketchedPageTooltipData;
import net.avizvul.esquissemod.item.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

// Аннотация @EventBusSubscriber автоматически зарегистрирует этот класс на клиентской шине мода [3]
@Mod.EventBusSubscriber(modid = EsquisseMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(SketchedPageTooltipData.class, ClientSketchedPageTooltip::new);
        event.register(net.avizvul.esquissemod.client.tooltip.StampTooltipData.class,
                net.avizvul.esquissemod.client.tooltip.ClientStampTooltip::new);
    }

    @SubscribeEvent
    public static void registerItemColors(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            // Красим ТОЛЬКО слой грифеля (layer1)
            if (tintIndex == 1) {
                if (net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.has(stack)) {
                    java.util.List<Integer> colors = net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.getOrDefault(stack, new java.util.ArrayList<>());

                    if (!colors.isEmpty()) {
                        int activeIndex = net.avizvul.esquissemod.component.ModDataComponents.ACTIVE_COLOR_INDEX.getOrDefault(stack, 0);

                        // Защита от сбоя рендера (Math.abs исключает отрицательные индексы)
                        int safeIndex = Math.abs(activeIndex) % colors.size();
                        int colorId = colors.get(safeIndex);

                        // ИСПРАВЛЕНИЕ 1: ПРАВИЛЬНЫЙ МЕТОД - getTextureDiffuseColor()
                        // ИСПРАВЛЕНИЕ 2: Обязательно накладываем маску | 0xFF000000 для полной непрозрачности
                        return net.avizvul.esquissemod.util.ColorUtils.dyeRgb(colorId) | 0xFF000000;
                    }
                }
                // Цвет по умолчанию для пустого карандаша (непрозрачный серый)
                return 0xFFDDDDDD;
            }

            // Для деревянного корпуса (layer0) возвращаем -1, чтобы игра его не фильтровала
            return -1;
                }, net.avizvul.esquissemod.item.ModItems.COLOR_PENCIL.get(),
                net.avizvul.esquissemod.item.ModItems.COLOR_MARKER.get(),
                net.avizvul.esquissemod.item.ModItems.STAMP_PAD.get());

    }

    @SubscribeEvent
    public static void registerBER(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(net.avizvul.esquissemod.block.entity.ModBlockEntities.SKETCHED_PAGE_BE.get(),
                net.avizvul.esquissemod.client.render.SketchedPageBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.minecraft.client.gui.screens.MenuScreens.register(net.avizvul.esquissemod.menu.ModMenuTypes.PENCIL_CASE_MENU.get(), net.avizvul.esquissemod.client.screen.PencilCaseScreen::new);
            net.minecraft.client.gui.screens.MenuScreens.register(net.avizvul.esquissemod.menu.ModMenuTypes.PRINTER_MENU.get(), net.avizvul.esquissemod.client.screen.PrinterScreen::new);
            net.minecraft.client.gui.screens.MenuScreens.register(net.avizvul.esquissemod.menu.ModMenuTypes.ALBUM_MENU.get(), net.avizvul.esquissemod.client.screen.AlbumScreen::new);
        });
    }
}
