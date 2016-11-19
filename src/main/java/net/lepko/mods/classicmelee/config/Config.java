package net.lepko.mods.classicmelee.config;

import com.google.common.collect.Lists;
import net.lepko.mods.classicmelee.ClassicMelee;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.List;

public class Config {

    public static boolean DISABLE_COOLDOWN = true;
    public static boolean NERF_DAMAGE = true;

    private static Configuration configuration;

    public static void load(File file) {
        configuration = new Configuration(file);

        readValues();

        MinecraftForge.EVENT_BUS.register(Config.class);
    }

    private static void readValues() {
        Property prop;

        prop = configuration.get(Configuration.CATEGORY_GENERAL, "disableCooldown", DISABLE_COOLDOWN);
        prop.setComment("No cooldown when using items (like before Minecraft 1.9) [true|false]");
        DISABLE_COOLDOWN = prop.getBoolean();

        prop = configuration.get(Configuration.CATEGORY_GENERAL, "nerfDamage", NERF_DAMAGE);
        prop.setComment("Nerf damage based on the weapon's attack speed when cooldown is disabled [true|false]");
        NERF_DAMAGE = prop.getBoolean();

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = Lists.newArrayList();
        for (String cat : configuration.getCategoryNames()) {
            ConfigElement element = new ConfigElement(configuration.getCategory(cat));
            list.add(element);
        }
        return list;
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (ClassicMelee.MOD_ID.equals(event.getModID())) {
            readValues();
        }
    }
}
