package de.dafuqs.foodeffecttooltips;

import com.google.common.collect.*;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.*;
import net.minecraft.component.type.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.effect.*;
import net.minecraft.item.*;
import net.minecraft.registry.entry.*;
import net.minecraft.screen.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

@Environment(EnvType.CLIENT)
public class TooltipHelper {
	
	public static void addFoodComponentEffectTooltip(@NotNull ItemStack stack, @NotNull FoodComponent foodComponent, @NotNull List<Text> tooltip, float tickRate) {
		if (foodComponent.effects().isEmpty()) {
			return;
		}
		boolean isDrink = stack.getUseAction() == UseAction.DRINK;
		buildFoodEffectTooltip(tooltip, foodComponent.effects(), tickRate, isDrink);
	}
	
	private static void buildFoodEffectTooltip(@NotNull List<Text> tooltip, List<FoodComponent.StatusEffectEntry> effects, float tickRate, boolean isDrink) {
		
		List<Pair<RegistryEntry<EntityAttribute>, EntityAttributeModifier>> modifiers = Lists.newArrayList();
		
		MutableText mutableText;
		RegistryEntry<StatusEffect> registryEntry;
		for (Iterator<FoodComponent.StatusEffectEntry> i = effects.iterator(); i.hasNext(); tooltip.add(mutableText.formatted(registryEntry.value().getCategory().getFormatting()))) {
			FoodComponent.StatusEffectEntry entry = i.next();
			StatusEffectInstance statusEffectInstance = entry.effect();
			mutableText = Text.translatable(statusEffectInstance.getTranslationKey());
			registryEntry = statusEffectInstance.getEffectType();
			registryEntry.value().forEachAttributeModifier(statusEffectInstance.getAmplifier(), (attribute, modifier) -> {
				modifiers.add(new Pair<>(attribute, modifier));
			});
			if (statusEffectInstance.getAmplifier() > 0) {
				mutableText = Text.translatable("potion.withAmplifier", mutableText, Text.translatable("potion.potency." + statusEffectInstance.getAmplifier()));
			}
			
			if (!statusEffectInstance.isDurationBelow(20)) {
				mutableText = Text.translatable("potion.withDuration", mutableText, StatusEffectUtil.getDurationText(statusEffectInstance, 1.0F, tickRate));
			}
			if (entry.probability() < 1.0F) {
				mutableText = Text.translatable("foodeffecttooltips.food.withChance", mutableText, Math.round(entry.probability() * 100));
			}
		}
		
		if (!modifiers.isEmpty()) {
			tooltip.add(ScreenTexts.EMPTY);
			if (isDrink) {
				tooltip.add(Text.translatable("potion.whenDrank").formatted(Formatting.DARK_PURPLE));
			} else {
				tooltip.add(Text.translatable("foodeffecttooltips.food.whenEaten").formatted(Formatting.DARK_PURPLE));
			}
			
			for (Pair<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifier : modifiers) {
				EntityAttributeModifier entityAttributeModifier = modifier.getSecond();
				double d = entityAttributeModifier.value();
				double e;
				if (entityAttributeModifier.operation() != EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE && entityAttributeModifier.operation() != EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
					e = entityAttributeModifier.value();
				} else {
					e = entityAttributeModifier.value() * 100.0;
				}
				
				if (d > 0.0) {
					tooltip.add(Text.translatable("attribute.modifier.plus." + entityAttributeModifier.operation().getId(), AttributeModifiersComponent.DECIMAL_FORMAT.format(e), Text.translatable(modifier.getFirst().value().getTranslationKey())).formatted(Formatting.BLUE));
				} else if (d < 0.0) {
					e *= -1.0;
					tooltip.add(Text.translatable("attribute.modifier.take." + entityAttributeModifier.operation().getId(), AttributeModifiersComponent.DECIMAL_FORMAT.format(e), Text.translatable(modifier.getFirst().value().getTranslationKey())).formatted(Formatting.RED));
				}
			}
		}
	}
	
}
