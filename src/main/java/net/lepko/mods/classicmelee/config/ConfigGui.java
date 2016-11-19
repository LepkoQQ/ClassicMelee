package net.lepko.mods.classicmelee.config;

import net.lepko.mods.classicmelee.ClassicMelee;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

import java.util.Set;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ConfigGui extends GuiConfig {

    public ConfigGui(GuiScreen parentScreen) {
        super(parentScreen, Config.getConfigElements(), ClassicMelee.MOD_ID, false, false, ClassicMelee.MOD_NAME);
    }

    public static class ConfigGuiFactory implements IModGuiFactory {

        @Override
        public void initialize(Minecraft mc) {
        }

        @Override
        public Class<? extends GuiScreen> mainConfigGuiClass() {
            return ConfigGui.class;
        }

        @Override
        public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
            return null;
        }

        @SuppressWarnings("deprecation")
        @Override
        public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
            return null;
        }
    }
}