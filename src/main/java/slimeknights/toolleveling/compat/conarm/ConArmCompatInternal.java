package slimeknights.toolleveling.compat.conarm;

import java.lang.reflect.Field;
import java.util.Map;

import c4.conarm.integrations.tinkertoolleveling.ModArmorLeveling;
import c4.conarm.lib.ArmoryRegistry;
import slimeknights.tconstruct.library.TinkerRegistry;

final class ConArmCompatInternal {

  private static final String MODIFIER_ID = "leveling_armor";

  private ConArmCompatInternal() {
  }

  static void install() {
    ModArmorLeveling original = ModArmorLeveling.modArmorLeveling;
    if(original == null || original instanceof FixedModArmorLeveling) {
      return;
    }

    net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(original);

    removeRegisteredModifier(TinkerRegistry.class, "traits");
    removeRegisteredModifier(TinkerRegistry.class, "modifiers");
    removeRegisteredModifier(ArmoryRegistry.class, "armorModifiers");
    FixedModArmorLeveling fixed = new FixedModArmorLeveling();
    replaceRegisteredModifier(TinkerRegistry.class, "traits", fixed);
    replaceRegisteredModifier(TinkerRegistry.class, "modifiers", fixed);
    replaceRegisteredModifier(ArmoryRegistry.class, "armorModifiers", fixed);
    ModArmorLeveling.modArmorLeveling = fixed;
  }

  @SuppressWarnings("unchecked")
  private static void replaceRegisteredModifier(Class<?> registryClass, String fieldName, ModArmorLeveling fixed) {
    try {
      Field field = registryClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      Map<String, Object> modifiers = (Map<String, Object>) field.get(null);
      modifiers.put(MODIFIER_ID, fixed);
    } catch(ReflectiveOperationException e) {
      throw new RuntimeException("Failed to replace Construct's Armory armor leveling modifier", e);
    }
  }

  @SuppressWarnings("unchecked")
  private static void removeRegisteredModifier(Class<?> registryClass, String fieldName) {
    try {
      Field field = registryClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      Map<String, Object> modifiers = (Map<String, Object>) field.get(null);
      modifiers.remove(MODIFIER_ID);
    } catch(ReflectiveOperationException e) {
      throw new RuntimeException("Failed to remove Construct's Armory armor leveling modifier", e);
    }
  }
}
