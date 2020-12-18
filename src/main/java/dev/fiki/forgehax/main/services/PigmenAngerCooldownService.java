package dev.fiki.forgehax.main.services;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LivingUpdateEvent;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;

@RegisterMod
public class PigmenAngerCooldownService extends ServiceMod {
  @SubscribeListener
  public void onUpdate(LivingUpdateEvent event) {
    if (event.getLiving() instanceof ZombifiedPiglinEntity) {
      // update pigmens anger level
      ZombifiedPiglinEntity pigman = (ZombifiedPiglinEntity) event.getLiving();
      if (pigman.isAggressive()) {
        pigman.setAngerTime(400);
      } else if (pigman.getAngerTime() > 0) {
        pigman.setAngerTime(pigman.getAngerTime() - 1);
      }
    }
  }
}
