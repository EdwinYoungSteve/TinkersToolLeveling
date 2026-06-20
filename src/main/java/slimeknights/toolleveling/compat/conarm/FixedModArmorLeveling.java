package slimeknights.toolleveling.compat.conarm;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import c4.conarm.integrations.tinkertoolleveling.ModArmorLeveling;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.Tags;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.toolleveling.TinkerToolLeveling;
import slimeknights.toolleveling.ToolLevelNBT;

public class FixedModArmorLeveling extends ModArmorLeveling {

  @Override
  public void addXp(ItemStack armor, int amount, EntityPlayer player) {
    NBTTagList tagList = TagUtil.getModifiersTagList(armor);
    int index = TinkerUtil.getIndexInCompoundList(tagList, identifier);
    if(index < 0) {
      return;
    }

    NBTTagCompound modifierTag = tagList.getCompoundTagAt(index);
    ToolLevelNBT data = new ToolLevelNBT(modifierTag);

    if(amount > 0 && data.xp > Integer.MAX_VALUE - amount) {
      data.xp = Integer.MAX_VALUE;
    } else if(amount > 0) {
      data.xp += amount;
    }
    if(data.xp < 0) {
      data.xp = 0;
    }

    if(!ModArmorLeveling.canLevelUp(data.level)) {
      return;
    }

    boolean leveledUp = false;
    int totalLevelUps = 0;
    while(ModArmorLeveling.canLevelUp(data.level)) {
      int xpForLevelup = getXpForLevelup(data.level);
      if(xpForLevelup <= 0 || data.xp < xpForLevelup) {
        break;
      }
      if(xpForLevelup >= Integer.MAX_VALUE - 1000) {
        if(data.xp >= xpForLevelup) {
          data.xp = xpForLevelup - 1;
        }
        break;
      }

      data.xp -= xpForLevelup;
      data.level++;
      data.bonusModifiers++;
      leveledUp = true;
      totalLevelUps++;
      if(totalLevelUps >= 1000) {
        break;
      }
    }
    if(data.xp < 0) {
      data.xp = 0;
    }

    data.write(modifierTag);
    TagUtil.setModifiersTagList(armor, tagList);

    if(leveledUp) {
      addFreeModifiers(armor, totalLevelUps);
      if(!player.world.isRemote) {
        TinkerToolLeveling.proxy.playLevelupDing(player);
        TinkerToolLeveling.proxy.sendLevelUpMessage(data.level, armor, player);
      }
    }
  }

  private void addFreeModifiers(ItemStack armor, int amount) {
    NBTTagCompound toolTag = TagUtil.getToolTag(armor);
    int modifiers = toolTag.getInteger(Tags.FREE_MODIFIERS) + amount;
    toolTag.setInteger(Tags.FREE_MODIFIERS, Math.max(0, modifiers));
    TagUtil.setToolTag(armor, toolTag);
  }
}
