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

public final class Fogatas {
    private final Map < UUID, Long > descansos = new HashMap<>();
    @SubscribeEvent
    public void alActualizarJugador(TickEvent.PlayerTickEvent evento){
        if (evento.phase != TickEvent.Phase.END) {
            return;
        }
            if(!(evento.player instanceof ServerPlayer jugador)) {
                return;
            }
      if(jugador.tickCount % 10 != 0){
          return;
      }

      if (!jugador.isShiftKeyDown()){
          descansos.remove(jugador.getUUID());
          return;
      }

      if (!hayFogataEncendida(jugador)){
          descansos.remove(jugador.getUUID());
          return;
      }
      long ahora = jugador.getServer().overworld().getGameTime();
      Long inicio = descansos.putIfAbsent(jugador.getUUID(), ahora);

      if (inicio == null || ahora - inicio < 100){
          return;
      }
}
private boolean hayFogataEncendida(ServerPlayer jugador) {
    ServerLevel mundo = jugador.serverLevel();
    BlockPos centro = jugador.blockPosition();

    for (BlockPos posicion : BlockPos.betweenClosed(
        centro.offset(-3,-3,-3),
    centro.offset(3,3,3))) {
        if(!mundo.hasChunkAt(posicion)) {
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