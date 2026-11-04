package net.avizvul.esquissemod;

import com.mojang.logging.LogUtils;
import net.avizvul.esquissemod.block.ModBlocks;
import net.avizvul.esquissemod.block.entity.ModBlockEntities;
import net.avizvul.esquissemod.item.ModCreativeModeTabs;
import net.avizvul.esquissemod.item.ModItems;
import net.avizvul.esquissemod.menu.ModMenuTypes;
import net.avizvul.esquissemod.network.ModNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(EsquisseMod.MOD_ID)
public class EsquisseMod {
    public static final String MOD_ID = "esquissemod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EsquisseMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }
}
