package dev.emi.beyondvanilla;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;
@Mod(Main.MOD_ID)
public final class Main {
    public static final String MOD_ID = "beyondvanilla";

    public Main() {
         MinecraftForge.EVENT_BUS.register(new Fogatas());
    }
}