package slimeknights.toolleveling.compat.conarm;

import net.minecraftforge.fml.common.Loader;

public final class ConArmCompat {

  private ConArmCompat() {
  }

  public static void install() {
    if(Loader.isModLoaded("conarm")) {
      ConArmCompatInternal.install();
    }
  }
}
