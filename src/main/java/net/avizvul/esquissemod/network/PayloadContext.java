package net.avizvul.esquissemod.network;

import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

/** Отправитель пакета и способ выполнить работу в основном потоке сервера. */
public record PayloadContext(Player player, Consumer<Runnable> mainThread) {
    public void enqueueWork(Runnable work) {
        this.mainThread.accept(work);
    }
}
