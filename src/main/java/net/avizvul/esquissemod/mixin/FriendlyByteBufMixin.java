package net.avizvul.esquissemod.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Квота 2 МиБ на один предмет не вмещает шалкер с альбомами. Размер пакета ограничен и без неё. */
@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufMixin {

    @Redirect(method = "readItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/FriendlyByteBuf;readNbt()Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag esquissemod$readItemNbtWithoutQuota(FriendlyByteBuf buf) {
        return buf.readAnySizeNbt();
    }
}
