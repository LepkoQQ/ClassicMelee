package net.lepko.mods.classicmelee;

import net.lepko.mods.classicmelee.config.Config;
import net.lepko.mods.classicmelee.core.CooldownHandler;
import net.lepko.mods.classicmelee.logger.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
        modid = ClassicMelee.MOD_ID,
        name = ClassicMelee.MOD_NAME,
        version = ClassicMelee.VERSION,
        guiFactory = "net.lepko.mods.classicmelee.config.ConfigGui$ConfigGuiFactory"
)
public class ClassicMelee {

    public static final String MOD_ID = "classicmelee";
    public static final String MOD_NAME = "Classic Melee";
    static final String VERSION = "${version}";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Config.load(event.getSuggestedConfigurationFile());
        Logger.load(event.getModLog());

        Logger.log("Loading {} ...", MOD_NAME);

        MinecraftForge.EVENT_BUS.register(CooldownHandler.INSTANCE);
    }
}
