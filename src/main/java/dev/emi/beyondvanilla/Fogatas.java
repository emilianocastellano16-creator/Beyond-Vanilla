package dev.emi.beyondvanilla;


import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

public final class Fogatas {
    private static final String ESPERA = "beyondvanilla_fogata_disponible";
    private final Map<UUID, Long> descansos = new HashMap<>();

    @SubscribeEvent
    public void alActualizarJugador(TickEvent.PlayerTickEvent evento) {
        if (evento.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(evento.player instanceof ServerPlayer jugador)) {
            return;
        }
        if (jugador.tickCount % 10 != 0) {
            return;
        }

        long ahora = jugador.getServer().overworld().getGameTime();

        if (ahora < jugador.getPersistentData().getLong(ESPERA)) {
            descansos.remove(jugador.getUUID());
            return;
        }

        if (!jugador.isShiftKeyDown()) {
            descansos.remove(jugador.getUUID());
            return;
        }

        if (!hayFogataEncendida(jugador)) {
            descansos.remove(jugador.getUUID());
            return;
        }

        Long inicio = descansos.putIfAbsent(jugador.getUUID(), ahora);

        if (inicio == null || ahora - inicio < 100) {
            return;
        }
        descansos.remove(jugador.getUUID());

        if (jugador.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION, 200, 0))) {

            jugador.getPersistentData().putLong(ESPERA, ahora + 2400);
        }
    }

    @SubscribeEvent
    public void alDesconectarse(PlayerEvent.PlayerLoggedOutEvent evento) {
        descansos.remove(evento.getEntity().getUUID());
    }

    @SubscribeEvent
    public void alCambiarDimension(PlayerEvent.PlayerChangedDimensionEvent evento) {
        descansos.remove(evento.getEntity().getUUID());
    }

    @SubscribeEvent
    public void alRecrearJugador(PlayerEvent.Clone evento) {
        if (!(evento.getEntity() instanceof ServerPlayer jugador)) {
            return;
        }

        descansos.remove(jugador.getUUID());

        long disponible = evento.getOriginal()
                .getPersistentData().getLong(ESPERA);

        jugador.getPersistentData().putLong(ESPERA, disponible);
    }

    @SubscribeEvent
    public void alDetenerServidor(ServerStoppedEvent evento) {
        descansos.clear();

    }

    private boolean hayFogataEncendida(ServerPlayer jugador) {
        ServerLevel mundo = jugador.serverLevel();
        BlockPos centro = jugador.blockPosition();

        for (BlockPos posicion : BlockPos.betweenClosed(
                centro.offset(-3, -3, -3),
                centro.offset(3, 3, 3))) {
            if (!mundo.hasChunkAt(posicion)) {
                continue;
            }

            if (jugador.position().distanceToSqr(
                    Vec3.atCenterOf(posicion)) > 9.0) {
                continue;
            }

            BlockState estado = mundo.getBlockState(posicion);

            if ((estado.is(Blocks.CAMPFIRE)
                    || estado.is(Blocks.SOUL_CAMPFIRE))
                    && estado.getValue(CampfireBlock.LIT)) {
                return true;
            }
        }
        return false;
    }
}