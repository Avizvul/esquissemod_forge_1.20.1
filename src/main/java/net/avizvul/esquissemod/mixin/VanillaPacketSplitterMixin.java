package net.avizvul.esquissemod.mixin;

import net.minecraftforge.network.filters.VanillaPacketSplitter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Forge делит пакет только если он больше 8 МиБ до сжатия, но после сжатия кадр обязан уместиться
 * в 2 МиБ. Рисунки почти не сжимаются, поэтому пакет между 2 и 8 МиБ кикал бы игрока.
 */
@Mixin(value = VanillaPacketSplitter.class, remap = false)
public abstract class VanillaPacketSplitterMixin {

    private static final int ESQUISSE_MAX_PACKET_BEFORE_COMPRESSION = 1024 * 1024;

    @ModifyConstant(method = "appendPackets", constant = @Constant(intValue = 8388608))
    private static int esquissemod$lowerSplitThreshold(int original) {
        return ESQUISSE_MAX_PACKET_BEFORE_COMPRESSION;
    }
}
