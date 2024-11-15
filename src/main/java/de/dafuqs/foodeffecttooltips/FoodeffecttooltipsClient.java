package de.dafuqs.foodeffecttooltips;

import de.dafuqs.foodeffecttooltips.config.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.serializer.*;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.client.item.v1.*;
import net.minecraft.component.*;
import net.minecraft.component.type.*;
import net.minecraft.entity.effect.*;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

@Environment(EnvType.CLIENT)
public class FoodeffecttooltipsClient implements ClientModInitializer {
	
	public static FoodEffectsConfig CONFIG;
	
	@Override
	public void onInitializeClient() {
		
		AutoConfig.register(FoodEffectsConfig.class, JanksonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(FoodEffectsConfig.class).getConfig();
		
		ItemTooltipCallback.EVENT.register((stack, context, tooltipType, lines) -> {
			@Nullable ConsumableComponent foodComponent = stack.get(DataComponentTypes.CONSUMABLE);
			if (foodComponent != null && shouldShowTooltip(stack)) {
				TooltipHelper.addFoodComponentEffectTooltip(stack, foodComponent, lines, context.getUpdateTickRate());
			}
			
			if (FoodeffecttooltipsClient.CONFIG.ShowSuspiciousStewTooltips && !tooltipType.isCreative()) {
				@Nullable SuspiciousStewEffectsComponent sus = stack.getOrDefault(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS, null);
				if (sus != null && !sus.effects().isEmpty()) {
					List<StatusEffectInstance> list = new ArrayList<>();
					for (SuspiciousStewEffectsComponent.StewEffect stewEffect : sus.effects()) {
						list.add(stewEffect.createStatusEffectInstance());
					}
					PotionContentsComponent.buildTooltip(list, lines::add, 1.0F, context.getUpdateTickRate());
				}
			}
		});
	}
	
	public static boolean shouldShowTooltip(ItemStack stack) {
		if (CONFIG == null) {
			return false;
		}
		
		Item item = stack.getItem();
		Identifier identifier = Registries.ITEM.getId(item);
		
		boolean isWhitelist = CONFIG.UseAsWhitelistInstead;
		if (CONFIG.BlacklistedItemIdentifiers.contains(identifier.toString())) {
			return isWhitelist;
		}
		if (CONFIG.BlacklistedModsIDs.contains(identifier.getNamespace())) {
			return isWhitelist;
		}
		return !isWhitelist;
	}
	
}
