/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests;

import java.util.logging.Level;
import java.util.logging.Logger;

import quests.Q00127_FishingSpecialistsRequest.Q00127_FishingSpecialistsRequest;
import quests.Q00255_Tutorial.Q00255_Tutorial;
import quests.Q00502_BrothersBoundInChains.Q00502_BrothersBoundInChains;
import quests.Q00662_AGameOfCards.Q00662_AGameOfCards;
import quests.Q10673_SagaOfLegend.Q10673_SagaOfLegend;
import quests.Q10957_TheLifeOfADeathKnight.Q10957_TheLifeOfADeathKnight;
import quests.Q10958_ExploringNewOpportunities.Q10958_ExploringNewOpportunities;
import quests.Q10959_ChallengingYourDestiny.Q10959_ChallengingYourDestiny;
import quests.Q10961_EffectiveTraining.Q10961_EffectiveTraining;
import quests.Q10962_NewHorizons.Q10962_NewHorizons;
import quests.Q10964_SecretGarden.Q10964_SecretGarden;
import quests.Q10965_DeathMysteries.Q10965_DeathMysteries;
import quests.Q10966_ATripBegins.Q10966_ATripBegins;
import quests.Q10967_CulturedAdventurer.Q10967_CulturedAdventurer;
import quests.Q10981_UnbearableWolvesHowling.Q10981_UnbearableWolvesHowling;
import quests.Q10982_SpiderHunt.Q10982_SpiderHunt;
import quests.Q10983_TroubledForest.Q10983_TroubledForest;
import quests.Q10984_CollectSpiderweb.Q10984_CollectSpiderweb;
import quests.Q10985_CleaningUpTheGround.Q10985_CleaningUpTheGround;
import quests.Q10986_SwampMonster.Q10986_SwampMonster;
import quests.Q10987_PlunderedGraves.Q10987_PlunderedGraves;
import quests.Q10988_Conspiracy.Q10988_Conspiracy;
import quests.Q10989_DangerousPredators.Q10989_DangerousPredators;
import quests.Q10990_PoisonExtraction.Q10990_PoisonExtraction;
import quests.not_done.Q10968_ThePowerOfTheMagicLamp;
import quests.not_done.Q10969_SporeInfestedPlace;
import quests.not_done.Q10970_RespectForGraves;
import quests.not_done.Q10971_TalismanEnchant;
import quests.not_done.Q10972_CombiningGems;
import quests.not_done.Q10973_EnchantingAgathions;
import quests.not_done.Q10974_NewStylishEquipment;
import quests.not_done.Q10975_LetsPayRespectsToOurFallenBrethren;
import quests.not_done.Q10976_MemoryOfTheGloriousPast;
import quests.not_done.Q10977_TracesOfBattle;
import quests.not_done.Q10978_MissingPets;

/**
 * @author NosBit
 */
public class QuestMasterHandler
{
	private static final Logger LOGGER = Logger.getLogger(QuestMasterHandler.class.getName());
	
	private static final Class<?>[] QUESTS =
	{
		Q00127_FishingSpecialistsRequest.class,
		Q00255_Tutorial.class,
		Q00502_BrothersBoundInChains.class,
		Q00662_AGameOfCards.class,
		Q10673_SagaOfLegend.class,
		Q10957_TheLifeOfADeathKnight.class,
		Q10958_ExploringNewOpportunities.class,
		Q10959_ChallengingYourDestiny.class,
		Q10961_EffectiveTraining.class,
		Q10962_NewHorizons.class,
		Q10964_SecretGarden.class,
		Q10965_DeathMysteries.class,
		Q10966_ATripBegins.class,
		Q10967_CulturedAdventurer.class,
		Q10981_UnbearableWolvesHowling.class,
		Q10982_SpiderHunt.class,
		Q10983_TroubledForest.class,
		Q10984_CollectSpiderweb.class,
		Q10985_CleaningUpTheGround.class,
		Q10986_SwampMonster.class,
		Q10987_PlunderedGraves.class,
		Q10988_Conspiracy.class,
		Q10989_DangerousPredators.class,
		Q10990_PoisonExtraction.class,
		Q10968_ThePowerOfTheMagicLamp.class, // TODO: Not done.
		Q10969_SporeInfestedPlace.class, // TODO: Not done.
		Q10970_RespectForGraves.class, // TODO: Not done.
		Q10971_TalismanEnchant.class, // TODO: Not done.
		Q10972_CombiningGems.class, // TODO: Not done.
		Q10973_EnchantingAgathions.class, // TODO: Not done.
		Q10974_NewStylishEquipment.class, // TODO: Not done.
		Q10975_LetsPayRespectsToOurFallenBrethren.class, // TODO: Not done.
		Q10976_MemoryOfTheGloriousPast.class, // TODO: Not done.
		Q10977_TracesOfBattle.class, // TODO: Not done.
		Q10978_MissingPets.class, // TODO: Not done.
	};
	
	public static void main(String[] args)
	{
		for (Class<?> quest : QUESTS)
		{
			try
			{
				quest.getDeclaredConstructor().newInstance();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, QuestMasterHandler.class.getSimpleName() + ": Failed loading " + quest.getSimpleName() + ":", e);
			}
		}
	}
}
