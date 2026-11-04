package net.avizvul.esquissemod.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Большой предмет на земле приходит по частям: сущность появляется раньше своих данных.
 * Пустой предмет сразу удалил бы себя на клиенте, поэтому там ждём данные. Удаляет сущность сервер.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void esquissemod$waitForSplitItemData(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.level().isClientSide() && self.getItem().isEmpty()) ci.cancel();
    }
}
