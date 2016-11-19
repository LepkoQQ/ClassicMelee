package net.lepko.mods.classicmelee.core;

import com.google.common.collect.Multimap;
import net.lepko.mods.classicmelee.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public enum CooldownHandler {
    INSTANCE;

    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    private int attackIndicator;

    private float calculateDamage(ItemStack stack, EntityLivingBase entity, float oldDamage) {
        return calculateDamage(stack, entity, null, oldDamage);
    }

    private float calculateDamage(ItemStack stack, EntityLivingBase entity, List<String> toolTip) {
        return calculateDamage(stack, entity, toolTip, null);
    }

    @SuppressWarnings("deprecation")
    private float calculateDamage(ItemStack stack, EntityLivingBase entity, List<String> toolTip, Float oldDamage) {
        if (stack == null || !Config.DISABLE_COOLDOWN) {
            return oldDamage != null ? oldDamage : 0;
        }

        double damage = -1;
        double speed = -1;
        String damageText = null;
        String speedText = null;
        Map.Entry<String, AttributeModifier> damageEntry = null;

        Multimap<String, AttributeModifier> multimap = stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
        if (!multimap.isEmpty() && multimap.size() >= 2) {
            for (Map.Entry<String, AttributeModifier> entry : multimap.entries()) {
                AttributeModifier attr = entry.getValue();
                double amount = attr.getAmount();
                if (attr.getID().equals(ATTACK_DAMAGE_MODIFIER)) {
                    amount += entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                    amount += EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
                    if (attr.getOperation() == 1 || attr.getOperation() == 2) {
                        amount *= 100.0D;
                    }
                    //damage = amount;
                    damage = oldDamage != null ? oldDamage : amount;
                    if (toolTip != null) {
                        damageText = I18n.translateToLocal("attribute.name." + entry.getKey());
                        damageEntry = entry;
                    }
                } else if (attr.getID().equals(ATTACK_SPEED_MODIFIER)) {
                    amount += entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
                    if (attr.getOperation() == 1 || attr.getOperation() == 2) {
                        amount *= 100.0D;
                    }
                    speed = amount;
                    if (toolTip != null) {
                        speedText = I18n.translateToLocal("attribute.name." + entry.getKey());
                    }
                }
            }
        }

        if (damage != -1 && speed != -1) {
            double newDamage = damage;
            if (Config.NERF_DAMAGE) {
                if (Math.abs(speed - 1.6) > 0.01) {
                    newDamage = newDamage * speed / 1.6F;
                }
                if (newDamage > damage) {
                    newDamage = damage;
                } else {
                    newDamage = (int) (newDamage * 100) / 100F;
                }
            }

            if (toolTip != null) {
                int speedIndex = -1;
                for (int i = 0; i < toolTip.size(); i++) {
                    String line = toolTip.get(i);
                    if (line != null && line.isEmpty()) {
                        if (++i < toolTip.size()) {
                            line = toolTip.get(i);
                            if (line != null && line.equals(I18n.translateToLocal("item.modifiers.mainhand"))) {
                                while (++i < toolTip.size() && (line = toolTip.get(i)) != null && line.startsWith(" ")) {
                                    if (line.contains(speedText)) {
                                        speedIndex = i;
                                    } else if (line.contains(damageText)) {
                                        toolTip.set(i, " " + I18n.translateToLocalFormatted("attribute.modifier.equals." + damageEntry.getValue().getOperation(), ItemStack.DECIMALFORMAT.format(newDamage), I18n.translateToLocal("attribute.name." + damageEntry.getKey())));
                                    }
                                }
                            }
                        }
                    }
                }
                if (speedIndex != -1) {
                    toolTip.remove(speedIndex);
                }
            }

            return (float) newDamage;
        }

        return oldDamage != null ? oldDamage : 0;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (Config.DISABLE_COOLDOWN) {
            if (event.player.getCooledAttackStrength(0) < 1) {
                event.player.ticksSinceLastSwing = MathHelper.ceiling_double_int(event.player.getCooldownPeriod());
            }
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (Config.DISABLE_COOLDOWN && event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            attackIndicator = Minecraft.getMinecraft().gameSettings.attackIndicator;
            Minecraft.getMinecraft().gameSettings.attackIndicator = 0;
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event) {
        if (Config.DISABLE_COOLDOWN && event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            Minecraft.getMinecraft().gameSettings.attackIndicator = attackIndicator;
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (Config.DISABLE_COOLDOWN && !event.getEntity().getEntityWorld().isRemote) {
            Entity attacker = event.getSource().getEntity();
            if (attacker != null && attacker instanceof EntityLivingBase) {
                ItemStack stack = ((EntityLivingBase) attacker).getHeldItemMainhand();
                float newDamage = calculateDamage(stack, (EntityLivingBase) attacker, event.getAmount());
                event.setAmount(newDamage);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemTooltip(ItemTooltipEvent event) {
        if (Config.DISABLE_COOLDOWN) {
            calculateDamage(event.getItemStack(), event.getEntityLiving(), event.getToolTip());
        }
    }
}
