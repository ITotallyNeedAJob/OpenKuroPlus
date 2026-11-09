package kat.respect.b2.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;

public class InventoryUtil {
   private static final Minecraft mc = Minecraft.getInstance();

   public static void swap(int selectedSlot) {
      if (selectedSlot >= 0 && selectedSlot <= 8) {
         PacketUtils.setSelectedSlot(selectedSlot);
      }
   }

   public static boolean swapStack(Predicate<ItemStack> predicate) {
      if (mc.player == null) {
         return false;
      } else {
         Inventory inventory = mc.player.getInventory();

         for (int i = 0; i < 9; i++) {
            if (predicate.test(inventory.getItem(i))) {
               PacketUtils.setSelectedSlot(i);
               return true;
            }
         }

         return false;
      }
   }

   public static boolean swapItem(Predicate<Item> predicate) {
      if (mc.player == null) {
         return false;
      } else {
         Inventory inventory = mc.player.getInventory();

         for (int i = 0; i < 9; i++) {
            if (predicate.test(inventory.getItem(i).getItem())) {
               PacketUtils.setSelectedSlot(i);
               return true;
            }
         }

         return false;
      }
   }

   public static boolean swap(Item item) {
      return swapItem(item2 -> item2 == item);
   }

   public static int findTotemSlot() {
      if (mc.player == null) {
         return -1;
      } else {
         Inventory inv = mc.player.getInventory();

         for (int i = 9; i < 36; i++) {
            if (inv.getItem(i).getItem() == Items.TOTEM_OF_UNDYING) {
               return i;
            }
         }

         return -1;
      }
   }

   public static int findRandomTotemSlot() {
      if (mc.player == null) {
         return -1;
      } else {
         Inventory inv = mc.player.getInventory();
         List<Integer> slots = new ArrayList<>();

         for (int i = 9; i < 36; i++) {
            if (inv.getItem(i).getItem() == Items.TOTEM_OF_UNDYING) {
               slots.add(i);
            }
         }

         return slots.isEmpty() ? -1 : slots.get(new Random().nextInt(slots.size()));
      }
   }

   public static int countItemExceptHotbar(Item item) {
      return countItemExceptHotbar(i -> i == item);
   }

   public static int countItemExceptHotbar(Predicate<Item> predicate) {
      if (mc.player == null) {
         return 0;
      } else {
         Inventory inv = mc.player.getInventory();
         int count = 0;

         for (int i = 9; i < 36; i++) {
            ItemStack stack = inv.getItem(i);
            if (predicate.test(stack.getItem())) {
               count += stack.getCount();
            }
         }

         return count;
      }
   }

   public static boolean isThatSplash(Holder<MobEffect> effect, ItemStack stack) {
      if (!stack.isEmpty() && stack.getItem() instanceof SplashPotionItem) {
         PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
         if (contents == null) {
            return false;
         } else {
            for (MobEffectInstance instance : contents.getAllEffects()) {
               if (instance.getEffect().equals(effect)) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public static int findSplash(Holder<MobEffect> effect) {
      if (mc.player == null) {
         return -1;
      } else {
         Inventory inv = mc.player.getInventory();

         for (int i = 0; i < 9; i++) {
            if (isThatSplash(effect, inv.getItem(i))) {
               return i;
            }
         }

         return -1;
      }
   }

   public static int findPot(Holder<MobEffect> effect) {
      if (mc.player == null) {
         return -1;
      } else {
         Inventory inv = mc.player.getInventory();

         for (int i = 9; i < 36; i++) {
            if (isThatSplash(effect, inv.getItem(i))) {
               return i;
            }
         }

         return -1;
      }
   }

   public static int findSplashPotionInHotbar(Predicate<Holder<Potion>> predicate) {
      if (mc.player == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() == Items.SPLASH_POTION) {
               PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
               if (contents != null) {
                  Optional<Holder<Potion>> potion = contents.potion();
                  if (potion.isPresent() && predicate.test(potion.get())) {
                     return i;
                  }
               }
            }
         }

         return -1;
      }
   }

   public static int findItemWithPredicateInHotbar(Predicate<ItemStack> predicate) {
      if (mc.player == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            if (predicate.test(mc.player.getInventory().getItem(i))) {
               return i;
            }
         }

         return -1;
      }
   }

   public static boolean hasPotionInOffhand(Predicate<Holder<Potion>> predicate) {
      if (mc.player == null) {
         return false;
      } else {
         ItemStack stack = mc.player.getOffhandItem();
         if (stack.getItem() != Items.SPLASH_POTION) {
            return false;
         } else {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents == null) {
               return false;
            } else {
               Optional<Holder<Potion>> potion = contents.potion();
               return potion.isPresent() && predicate.test(potion.get());
            }
         }
      }
   }

   public static boolean isTool(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         Item item = stack.getItem();
         boolean isSword = stack.is(ItemTags.SWORDS)
            || item == Items.WOODEN_SWORD
            || item == Items.STONE_SWORD
            || item == Items.IRON_SWORD
            || item == Items.GOLDEN_SWORD
            || item == Items.DIAMOND_SWORD
            || item == Items.NETHERITE_SWORD;
         return stack.get(DataComponents.TOOL) != null || isSword || getArmorSlot(stack) != null;
      }
   }

   public static boolean hasInventorySpace() {
      if (mc.player == null) {
         return false;
      } else {
         for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getItem(i).isEmpty()) {
               return true;
            }
         }

         return false;
      }
   }

   public static List<Slot> findItemsInContainer(AbstractContainerScreen<?> screen) {
      List<Slot> items = new ArrayList<>();

      for (Slot slot : screen.getMenu().slots) {
         if (!slot.getItem().isEmpty() && slot.container != mc.player.getInventory()) {
            items.add(slot);
         }
      }

      return items;
   }

   public static int getContainerSize(AbstractContainerScreen<?> screen) {
      return screen.getMenu().slots.size() - 36;
   }

   public static int findFirstEmptyContainerSlot(AbstractContainerScreen<?> screen) {
      int containerSize = getContainerSize(screen);

      for (int i = 0; i < containerSize; i++) {
         if (screen.getMenu().slots.get(i).getItem().isEmpty()) {
            return i;
         }
      }

      return -1;
   }

   public static double getItemScore(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0.0;
      } else {
         double score = 0.0;
         ItemAttributeModifiers mods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
         if (mods != null) {
            for (net.minecraft.world.item.component.ItemAttributeModifiers.Entry entry : mods.modifiers()) {
               if (entry.attribute().is(Attributes.ATTACK_DAMAGE) || entry.attribute().is(Attributes.ARMOR)) {
                  score += entry.modifier().amount();
               } else if (entry.attribute().is(Attributes.ARMOR_TOUGHNESS)) {
                  score += entry.modifier().amount() * 0.5;
               }
            }
         }

         Tool tool = stack.get(DataComponents.TOOL);
         if (tool != null) {
            score += tool.defaultMiningSpeed();
         }

         ItemEnchantments ench = stack.get(DataComponents.ENCHANTMENTS);
         if (ench != null) {
            for (Entry<Holder<Enchantment>> entryx : ench.entrySet()) {
               score += entryx.getIntValue() * 0.5;
            }
         }

         return score;
      }
   }

   public static double getArmorScore(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0.0;
      } else {
         double score = 0.0;
         ItemAttributeModifiers mods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
         if (mods != null) {
            for (net.minecraft.world.item.component.ItemAttributeModifiers.Entry entry : mods.modifiers()) {
               if (entry.attribute().is(Attributes.ARMOR)) {
                  score += entry.modifier().amount();
               } else if (entry.attribute().is(Attributes.ARMOR_TOUGHNESS)) {
                  score += entry.modifier().amount() * 0.5;
               }
            }
         }

         ItemEnchantments ench = stack.get(DataComponents.ENCHANTMENTS);
         if (ench != null) {
            for (Entry<Holder<Enchantment>> entryx : ench.entrySet()) {
               score += entryx.getIntValue();
            }
         }

         return score;
      }
   }

   public static EquipmentSlot getArmorSlot(ItemStack stack) {
      if (stack.isEmpty()) {
         return null;
      } else {
         Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
         if (equippable == null) {
            return null;
         } else {
            EquipmentSlot slot = equippable.slot();
            return slot.isArmor() ? slot : null;
         }
      }
   }

   public static boolean isJunk(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         Item item = stack.getItem();
         return item == Items.DIRT
            || item == Items.GRAVEL
            || item == Items.SAND
            || item == Items.COBBLESTONE
            || item == Items.NETHERRACK
            || item == Items.ROTTEN_FLESH
            || item == Items.POISONOUS_POTATO
            || item == Items.BONE
            || item == Items.STICK
            || item == Items.SPIDER_EYE
            || item == Items.STRING
            || item == Items.WHEAT_SEEDS;
      }
   }

   public static boolean isItemUseful(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         Item item = stack.getItem();
         if (isJunk(stack)) {
            return false;
         } else {
            return !(item instanceof BlockItem)
                  && !(item instanceof PotionItem)
                  && !(item instanceof SplashPotionItem)
                  && !(item instanceof LingeringPotionItem)
                  && item != Items.GOLDEN_APPLE
                  && item != Items.ENCHANTED_GOLDEN_APPLE
                  && item != Items.ENDER_PEARL
                  && item != Items.ENDER_EYE
                  && item != Items.TOTEM_OF_UNDYING
                  && item != Items.WATER_BUCKET
                  && item != Items.TRIDENT
                  && item != Items.MACE
                  && item != Items.BOW
                  && item != Items.CROSSBOW
                  && item != Items.ARROW
                  && item != Items.ELYTRA
                  && item != Items.SHIELD
                  && item != Items.IRON_INGOT
                  && item != Items.DIAMOND
                  && item != Items.EMERALD
                  && item != Items.GOLD_INGOT
                  && item != Items.NETHERITE_INGOT
                  && item != Items.EXPERIENCE_BOTTLE
               ? getItemScore(stack) > 0.0 || getArmorSlot(stack) != null
               : true;
         }
      }
   }
}
