package net.avizvul.esquissemod.block;

import net.avizvul.esquissemod.EsquisseMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.BLOCKS, EsquisseMod.MOD_ID);

    // Регистрируем блок: без коллизии, без затенения света, мгновенно ломается
    public static final RegistryObject<Block> SKETCHED_PAGE_BLOCK = BLOCKS.register("sketched_page_block",
            () -> new SketchedPageBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .noOcclusion()
                    .instabreak()
                    .sound(net.minecraft.world.level.block.SoundType.WOOL
                    )
            )
    );

    public static final RegistryObject<Block> PRINTER_BLOCK = BLOCKS.register("printer_block",
            () -> new PrinterBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .strength(2.5f)
                    .sound(SoundType.WOOD
                    )
            )
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}