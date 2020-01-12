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

import quests.Q00032_AnObviousLie.Q00032_AnObviousLie;
import quests.Q00033_MakeAPairOfDressShoes.Q00033_MakeAPairOfDressShoes;
import quests.Q00034_InSearchOfCloth.Q00034_InSearchOfCloth;
import quests.Q00035_FindGlitteringJewelry.Q00035_FindGlitteringJewelry;
import quests.Q00036_MakeASewingKit.Q00036_MakeASewingKit;
import quests.Q00037_MakeFormalWear.Q00037_MakeFormalWear;
import quests.Q00040_ASpecialOrder.Q00040_ASpecialOrder;
import quests.Q00115_TheOtherSideOfTruth.Q00115_TheOtherSideOfTruth;
import quests.Q00119_LastImperialPrince.Q00119_LastImperialPrince;
import quests.Q00337_AudienceWithTheLandDragon.Q00337_AudienceWithTheLandDragon;
import quests.Q00452_FindingtheLostSoldiers.Q00452_FindingtheLostSoldiers;
import quests.Q00453_NotStrongEnoughAlone.Q00453_NotStrongEnoughAlone;
import quests.Q00454_CompletelyLost.Q00454_CompletelyLost;
import quests.Q00459_TheVillainOfTheUndergroundMineTeredor.Q00459_TheVillainOfTheUndergroundMineTeredor;
import quests.Q00460_PreciousResearchMaterial.Q00460_PreciousResearchMaterial;
import quests.Q00465_WeAreFriends.Q00465_WeAreFriends;
import quests.Q00466_PlacingMySmallPower.Q00466_PlacingMySmallPower;
import quests.Q00468_BeLostInTheMysteriousScent.Q00468_BeLostInTheMysteriousScent;
import quests.Q00469_SuspiciousGardener.Q00469_SuspiciousGardener;
import quests.Q00471_BreakingThroughTheEmeraldSquare.Q00471_BreakingThroughTheEmeraldSquare;
import quests.Q00472_ChallengeSteamCorridor.Q00472_ChallengeSteamCorridor;
import quests.Q00473_InTheCoralGarden.Q00473_InTheCoralGarden;
import quests.Q00493_KickingOutUnwelcomeGuests.Q00493_KickingOutUnwelcomeGuests;
import quests.Q00494_IncarnationOfGreedZellakaGroup.Q00494_IncarnationOfGreedZellakaGroup;
import quests.Q00495_IncarnationOfJealousyPellineGroup.Q00495_IncarnationOfJealousyPellineGroup;
import quests.Q00496_IncarnationOfGluttonyKaliosGroup.Q00496_IncarnationOfGluttonyKaliosGroup;
import quests.Q00497_IncarnationOfGreedZellakaSolo.Q00497_IncarnationOfGreedZellakaSolo;
import quests.Q00498_IncarnationOfJealousyPellineSolo.Q00498_IncarnationOfJealousyPellineSolo;
import quests.Q00499_IncarnationOfGluttonyKaliosSolo.Q00499_IncarnationOfGluttonyKaliosSolo;
import quests.Q00500_BrothersBoundInChains.Q00500_BrothersBoundInChains;
import quests.Q00511_AwlUnderFoot.Q00511_AwlUnderFoot;
import quests.Q00512_BladeUnderFoot.Q00512_BladeUnderFoot;
import quests.Q00551_OlympiadStarter.Q00551_OlympiadStarter;
import quests.Q00553_OlympiadUndefeated.Q00553_OlympiadUndefeated;
import quests.Q00561_BasicMissionHarnakUndergroundRuins.Q00561_BasicMissionHarnakUndergroundRuins;
import quests.Q00564_BasicMissionKartiasLabyrinthSolo.Q00564_BasicMissionKartiasLabyrinthSolo;
import quests.Q00567_BasicMissionIsleOfSouls.Q00567_BasicMissionIsleOfSouls;
import quests.Q00580_BeyondTheMemories.Q00580_BeyondTheMemories;
import quests.Q00618_IntoTheFlame.Q00618_IntoTheFlame;
import quests.Q00620_FourGoblets.Q00620_FourGoblets;
import quests.Q00670_DefeatingTheLordOfSeed.Q00670_DefeatingTheLordOfSeed;
import quests.Q00674_ChangesInTheShadowOfTheMotherTree.Q00674_ChangesInTheShadowOfTheMotherTree;
import quests.Q00675_WhatTheThreadOfThePastShows.Q00675_WhatTheThreadOfThePastShows;
import quests.Q00726_LightWithinTheDarkness.Q00726_LightWithinTheDarkness;
import quests.Q00727_HopeWithinTheDarkness.Q00727_HopeWithinTheDarkness;
import quests.Q00737_ASwordHiddenInASmile.Q00737_ASwordHiddenInASmile;
import quests.Q00738_DimensionalExplorationOfTheUnworldlyVisitors.Q00738_DimensionalExplorationOfTheUnworldlyVisitors;
import quests.Q00743_AtTheAltarOfOblivion.Q00743_AtTheAltarOfOblivion;
import quests.Q00752_UncoverTheSecret.Q00752_UncoverTheSecret;
import quests.Q00753_ReactingToACrisis.Q00753_ReactingToACrisis;
import quests.Q00754_AssistingTheRebelForces.Q00754_AssistingTheRebelForces;
import quests.Q00755_InNeedOfPetras.Q00755_InNeedOfPetras;
import quests.Q00756_TopQualityPetra.Q00756_TopQualityPetra;
import quests.Q00757_TriolsMovement.Q00757_TriolsMovement;
import quests.Q00758_TheFallenKingsMen.Q00758_TheFallenKingsMen;
import quests.Q00760_BlockTheExit.Q00760_BlockTheExit;
import quests.Q00775_RetrievingTheChaosFragment.Q00775_RetrievingTheChaosFragment;
import quests.Q00776_SlayDarkLordEkimus.Q00776_SlayDarkLordEkimus;
import quests.Q00777_SlayDarkLordTiat.Q00777_SlayDarkLordTiat;
import quests.Q00778_OperationRoaringFlame.Q00778_OperationRoaringFlame;
import quests.Q00779_UtilizeTheDarknessSeedOfDestruction.Q00779_UtilizeTheDarknessSeedOfDestruction;
import quests.Q00783_VestigeOfTheMagicPower.Q00783_VestigeOfTheMagicPower;
import quests.Q00790_ObtainingFerinsTrust.Q00790_ObtainingFerinsTrust;
import quests.Q00792_TheSuperionGiants.Q00792_TheSuperionGiants;
import quests.Q00816_PlansToRepairTheStronghold.Q00816_PlansToRepairTheStronghold;
import quests.Q00823_DisappearedRaceNewFairy.Q00823_DisappearedRaceNewFairy;
import quests.Q00826_InSearchOfTheSecretWeapon.Q00826_InSearchOfTheSecretWeapon;
import quests.Q00827_EinhasadsOrder.Q00827_EinhasadsOrder;
import quests.Q00828_EvasBlessing.Q00828_EvasBlessing;
import quests.Q00829_MaphrsSalvation.Q00829_MaphrsSalvation;
import quests.Q00830_TheWayOfTheGiantsPawn.Q00830_TheWayOfTheGiantsPawn;
import quests.Q00831_SayhasScheme.Q00831_SayhasScheme;
import quests.Q00833_DevilsTreasureTauti.Q00833_DevilsTreasureTauti;
import quests.Q00835_PitiableMelisa.Q00835_PitiableMelisa;
import quests.Q00842_CaptiveDemons.Q00842_CaptiveDemons;
import quests.Q00844_GiantsTreasure.Q00844_GiantsTreasure;
import quests.Q00901_HowLavasaurusesAreMade.Q00901_HowLavasaurusesAreMade;
import quests.Q00903_TheCallOfAntharas.Q00903_TheCallOfAntharas;
import quests.Q00905_RefinedDragonBlood.Q00905_RefinedDragonBlood;
import quests.Q00906_TheCallOfValakas.Q00906_TheCallOfValakas;
import quests.Q00923_ShinedustExtraction.Q00923_ShinedustExtraction;
import quests.Q00924_GiantOfTheRestorationRoom.Q00924_GiantOfTheRestorationRoom;
import quests.Q00926_30DaySearchOperation.Q00926_30DaySearchOperation;
import quests.Q00928_100DaySubjugationOperation.Q00928_100DaySubjugationOperation;
import quests.Q00931_MemoriesOfTheWind.Q00931_MemoriesOfTheWind;
import quests.Q00932_SayhasEnergy.Q00932_SayhasEnergy;
import quests.Q00937_ToReviveTheFishingGuild.Q00937_ToReviveTheFishingGuild;
import quests.Q10282_ToTheSeedOfAnnihilation.Q10282_ToTheSeedOfAnnihilation;
import quests.Q10283_RequestOfIceMerchant.Q10283_RequestOfIceMerchant;
import quests.Q10284_AcquisitionOfDivineSword.Q10284_AcquisitionOfDivineSword;
import quests.Q10285_MeetingSirra.Q10285_MeetingSirra;
import quests.Q10286_ReunionWithSirra.Q10286_ReunionWithSirra;
import quests.Q10287_StoryOfThoseLeft.Q10287_StoryOfThoseLeft;
import quests.Q10297_GrandOpeningComeToOurPub.Q10297_GrandOpeningComeToOurPub;
import quests.Q10303_CrossroadsBetweenLightAndDarkness.Q10303_CrossroadsBetweenLightAndDarkness;
import quests.Q10381_ToTheSeedOfHellfire.Q10381_ToTheSeedOfHellfire;
import quests.Q10383_FergasonsOffer.Q10383_FergasonsOffer;
import quests.Q10386_MysteriousJourney.Q10386_MysteriousJourney;
import quests.Q10387_SoullessOne.Q10387_SoullessOne;
import quests.Q10388_ConspiracyBehindDoor.Q10388_ConspiracyBehindDoor;
import quests.Q10389_TheVoiceOfAuthority.Q10389_TheVoiceOfAuthority;
import quests.Q10445_AnImpendingThreat.Q10445_AnImpendingThreat;
import quests.Q10446_HitAndRun.Q10446_HitAndRun;
import quests.Q10447_TimingIsEverything.Q10447_TimingIsEverything;
import quests.Q10450_ADarkAmbition.Q10450_ADarkAmbition;
import quests.Q10455_ElikiasLetter.Q10455_ElikiasLetter;
import quests.Q10459_ASickAmbition.Q10459_ASickAmbition;
import quests.Q10501_ZakenEmbroideredSoulCloak.Q10501_ZakenEmbroideredSoulCloak;
import quests.Q10502_FreyaEmbroideredSoulCloak.Q10502_FreyaEmbroideredSoulCloak;
import quests.Q10503_FrintezzaEmbroideredSoulCloak.Q10503_FrintezzaEmbroideredSoulCloak;
import quests.Q10537_KamaelDisarray.Q10537_KamaelDisarray;
import quests.Q10538_GiantsEvolution.Q10538_GiantsEvolution;
import quests.Q10539_EnergySupplyCutoffPlan.Q10539_EnergySupplyCutoffPlan;
import quests.Q10540_ThwartingMimirsPlan.Q10540_ThwartingMimirsPlan;
import quests.Q10566_BestChoice.Q10566_BestChoice;
import quests.Q10568_KamaelsTechnologicalAdvancement.Q10568_KamaelsTechnologicalAdvancement;
import quests.Q10569_DeclarationOfWar.Q10569_DeclarationOfWar;
import quests.Q10571_StrategicReconciliation.Q10571_StrategicReconciliation;
import quests.Q10575_LetsGoFishing.Q10575_LetsGoFishing;
import quests.Q10576_GlitteringWeapons.Q10576_GlitteringWeapons;
import quests.Q10577_TemperARustingBlade.Q10577_TemperARustingBlade;
import quests.Q10578_TheSoulOfASword.Q10578_TheSoulOfASword;
import quests.Q10579_ContainingTheAttributePower.Q10579_ContainingTheAttributePower;
import quests.Q10589_WhereFatesIntersect.Q10589_WhereFatesIntersect;
import quests.Q10590_ReawakenedFate.Q10590_ReawakenedFate;
import quests.Q10591_NobleMaterial.Q10591_NobleMaterial;
import quests.Q10597_EscapeToTheShadowOfTheMotherTree.Q10597_EscapeToTheShadowOfTheMotherTree;
import quests.Q10598_WithAllYourMight.Q10598_WithAllYourMight;
import quests.Q10599_ThreadOfFateHangingOnTheMotherTree.Q10599_ThreadOfFateHangingOnTheMotherTree;
import quests.Q10658_MakkumInTheDimension.Q10658_MakkumInTheDimension;
import quests.Q10701_TheRoadToDestruction.Q10701_TheRoadToDestruction;
import quests.Q10702_TheRoadToInfinity.Q10702_TheRoadToInfinity;
import quests.Q10801_TheDimensionalWarpPart1.Q10801_TheDimensionalWarpPart1;
import quests.Q10802_TheDimensionalWarpPart2.Q10802_TheDimensionalWarpPart2;
import quests.Q10803_TheDimensionalWarpPart3.Q10803_TheDimensionalWarpPart3;
import quests.Q10804_TheDimensionalWarpPart4.Q10804_TheDimensionalWarpPart4;
import quests.Q10805_TheDimensionalWarpPart5.Q10805_TheDimensionalWarpPart5;
import quests.Q10806_TheDimensionalWarpPart6.Q10806_TheDimensionalWarpPart6;
import quests.Q10807_TheDimensionalWarpPart7.Q10807_TheDimensionalWarpPart7;
import quests.Q10811_ExaltedOneWhoFacesTheLimit.Q10811_ExaltedOneWhoFacesTheLimit;
import quests.Q10812_FacingSadness.Q10812_FacingSadness;
import quests.Q10813_ForGlory.Q10813_ForGlory;
import quests.Q10814_BefittingOfTheStatus.Q10814_BefittingOfTheStatus;
import quests.Q10815_StepUp.Q10815_StepUp;
import quests.Q10817_ExaltedOneWhoOvercomesTheLimit.Q10817_ExaltedOneWhoOvercomesTheLimit;
import quests.Q10818_ConfrontingAGiantMonster.Q10818_ConfrontingAGiantMonster;
import quests.Q10819_ForHonor.Q10819_ForHonor;
import quests.Q10820_RelationshipsBefittingOfTheStatus.Q10820_RelationshipsBefittingOfTheStatus;
import quests.Q10821_HelpingOthers.Q10821_HelpingOthers;
import quests.Q10823_ExaltedOneWhoShattersTheLimit.Q10823_ExaltedOneWhoShattersTheLimit;
import quests.Q10824_ConfrontingTheGreatestDanger.Q10824_ConfrontingTheGreatestDanger;
import quests.Q10825_ForVictory.Q10825_ForVictory;
import quests.Q10826_LuckBefittingOfTheStatus.Q10826_LuckBefittingOfTheStatus;
import quests.Q10827_StepUpToLead.Q10827_StepUpToLead;
import quests.Q10829_InSearchOfTheCause.Q10829_InSearchOfTheCause;
import quests.Q10830_TheLostGardenOfSpirits.Q10830_TheLostGardenOfSpirits;
import quests.Q10831_UnbelievableSight.Q10831_UnbelievableSight;
import quests.Q10832_EnergyOfSadnessAndAnger.Q10832_EnergyOfSadnessAndAnger;
import quests.Q10833_PutTheQueenOfSpiritsToSleep.Q10833_PutTheQueenOfSpiritsToSleep;
import quests.Q10836_DisappearedClanMember.Q10836_DisappearedClanMember;
import quests.Q10837_LookingForTheBlackbirdClanMember.Q10837_LookingForTheBlackbirdClanMember;
import quests.Q10838_TheReasonForNotBeingAbleToGetOut.Q10838_TheReasonForNotBeingAbleToGetOut;
import quests.Q10839_BlackbirdsNameValue.Q10839_BlackbirdsNameValue;
import quests.Q10840_TimeToRecover.Q10840_TimeToRecover;
import quests.Q10843_AnomalyInTheEnchantedValley.Q10843_AnomalyInTheEnchantedValley;
import quests.Q10844_BloodyBattleSeizingSupplies.Q10844_BloodyBattleSeizingSupplies;
import quests.Q10849_TrialsForAdaptation.Q10849_TrialsForAdaptation;
import quests.Q10851_ElvenBotany.Q10851_ElvenBotany;
import quests.Q10852_TheMotherTreeRevivalProject.Q10852_TheMotherTreeRevivalProject;
import quests.Q10856_SuperionAppears.Q10856_SuperionAppears;
import quests.Q10857_SecretTeleport.Q10857_SecretTeleport;
import quests.Q10891_AtANewPlace.Q10891_AtANewPlace;
import quests.Q11025_PathOfDestinyProving.Q11025_PathOfDestinyProving;
import quests.Q11026_PathOfDestinyConviction.Q11026_PathOfDestinyConviction;
import quests.Q11027_PathOfDestinyOvercome.Q11027_PathOfDestinyOvercome;
import quests.Q11031_TrainingBeginsNow.Q11031_TrainingBeginsNow;
import quests.Q11032_CurseOfUndying.Q11032_CurseOfUndying;
import quests.Q11033_AntidoteIngredients.Q11033_AntidoteIngredients;
import quests.Q11034_ResurrectedOne.Q11034_ResurrectedOne;
import quests.Q11035_DeathlyMischief.Q11035_DeathlyMischief;
import quests.Q11036_ChangedSpirits.Q11036_ChangedSpirits;
import quests.Q11037_WhyAreTheRatelHere.Q11037_WhyAreTheRatelHere;
import quests.Q11038_GrowlersTurnedViolent.Q11038_GrowlersTurnedViolent;
import quests.Q11039_CommunicationBreakdown.Q11039_CommunicationBreakdown;
import quests.Q11040_AttackOfTheEnragedForest.Q11040_AttackOfTheEnragedForest;
import quests.Q11041_CheckOutTheSituation.Q11041_CheckOutTheSituation;
import quests.Q11042_SuspiciousMovements.Q11042_SuspiciousMovements;
import quests.Q11043_SomeonesTrace.Q11043_SomeonesTrace;
import quests.Q11044_KetraOrcs.Q11044_KetraOrcs;
import quests.Q11045_TheyMustBeUpToSomething.Q11045_TheyMustBeUpToSomething;
import quests.Q11046_PrayingForSafety.Q11046_PrayingForSafety;
import quests.custom.Q00529_RegularBarrierMaintenance.Q00529_RegularBarrierMaintenance;
import quests.custom.Q00560_HowToOvercomeFear.Q00560_HowToOvercomeFear;
import quests.custom.Q00589_ASecretChange.Q00589_ASecretChange;
import quests.custom.Q00590_ToEachTheirOwn.Q00590_ToEachTheirOwn;
import quests.custom.Q00683_AdventOfKrofinSubspecies.Q00683_AdventOfKrofinSubspecies;
import quests.custom.Q00684_DisturbedFields.Q00684_DisturbedFields;
import quests.custom.Q10516_UnveiledFafurionTemple.Q10516_UnveiledFafurionTemple;
import quests.custom.Q10517_FafurionsMinions.Q10517_FafurionsMinions;
import quests.custom.Q10518_SucceedingThePriestess.Q10518_SucceedingThePriestess;
import quests.custom.Q10519_ControllingYourTemper.Q10519_ControllingYourTemper;
import quests.custom.Q10520_TempleGuardians.Q10520_TempleGuardians;
import quests.custom.Q10529_IvoryTowersResearchFloatingSeaJournal.Q10529_IvoryTowersResearchFloatingSeaJournal;
import quests.custom.Q10533_OrfensAmbition.Q10533_OrfensAmbition;
import quests.not_done.*;

/**
 * @author NosBit
 */
public class QuestMasterHandler
{
	private static final Logger LOGGER = Logger.getLogger(QuestMasterHandler.class.getName());
	
	private static final Class<?>[] QUESTS =
	{
		Q00032_AnObviousLie.class,
		Q00033_MakeAPairOfDressShoes.class,
		Q00034_InSearchOfCloth.class,
		Q00035_FindGlitteringJewelry.class,
		Q00036_MakeASewingKit.class,
		Q00037_MakeFormalWear.class,
		Q00040_ASpecialOrder.class,
		Q00115_TheOtherSideOfTruth.class,
		Q00119_LastImperialPrince.class,
		Q00282_ADayOfKindnessAndCaring.class, // TODO: Not done.
		Q00337_AudienceWithTheLandDragon.class,
		Q00452_FindingtheLostSoldiers.class,
		Q00453_NotStrongEnoughAlone.class,
		Q00454_CompletelyLost.class,
		Q00459_TheVillainOfTheUndergroundMineTeredor.class,
		Q00460_PreciousResearchMaterial.class,
		Q00465_WeAreFriends.class,
		Q00466_PlacingMySmallPower.class,
		Q00468_BeLostInTheMysteriousScent.class,
		Q00469_SuspiciousGardener.class,
		Q00471_BreakingThroughTheEmeraldSquare.class,
		Q00472_ChallengeSteamCorridor.class,
		Q00473_InTheCoralGarden.class,
		Q00493_KickingOutUnwelcomeGuests.class,
		Q00494_IncarnationOfGreedZellakaGroup.class,
		Q00495_IncarnationOfJealousyPellineGroup.class,
		Q00496_IncarnationOfGluttonyKaliosGroup.class,
		Q00497_IncarnationOfGreedZellakaSolo.class,
		Q00498_IncarnationOfJealousyPellineSolo.class,
		Q00499_IncarnationOfGluttonyKaliosSolo.class,
		Q00500_BrothersBoundInChains.class,
		Q00504_CompetitionForTheBanditStronghold.class, // TODO: Not done.
		Q00511_AwlUnderFoot.class,
		Q00512_BladeUnderFoot.class,
		Q00529_RegularBarrierMaintenance.class, // FIXME: Custom.
		Q00551_OlympiadStarter.class,
		Q00553_OlympiadUndefeated.class,
		Q00560_HowToOvercomeFear.class, // FIXME: Custom.
		Q00561_BasicMissionHarnakUndergroundRuins.class,
		Q00564_BasicMissionKartiasLabyrinthSolo.class,
		Q00567_BasicMissionIsleOfSouls.class,
		Q00568_SpecialMissionNornilsCave.class, // TODO: Not done.
		Q00569_BasicMissionSealOfShilen.class, // TODO: Not done.
		Q00570_SpecialMissionKartiasLabyrinthParty.class, // TODO: Not done.
		Q00571_SpecialMissionProofOfUnityFieldRaid.class, // TODO: Not done.
		Q00572_SpecialMissionProofOfCourageFieldRaid.class, // TODO: Not done.
		Q00573_SpecialMissionProofOfStrengthFieldRaid.class, // TODO: Not done.
		Q00574_SpecialMissionNornilsGarden.class, // TODO: Not done.
		Q00576_SpecialMissionDefeatSpezion.class, // TODO: Not done.
		Q00577_BasicMissionSilentValley.class, // TODO: Not done.
		Q00578_BasicMissionCemetery.class, // TODO: Not done.
		Q00580_BeyondTheMemories.class,
		Q00585_CantGoAgainstTheTime.class, // TODO: Not done.
		Q00586_MutatedCreatures.class, // TODO: Not done.
		Q00587_MoreAggressiveOperation.class, // TODO: Not done.
		Q00588_HeadOnCrash.class, // TODO: Not done.
		Q00589_ASecretChange.class, // FIXME: Custom.
		Q00590_ToEachTheirOwn.class, // FIXME: Custom.
		Q00591_GreatAmbitions.class, // TODO: Not done.
		Q00593_BasicMissionPaganTemple.class, // TODO: Not done.
		Q00594_BasicMissionDimensionalRift.class, // TODO: Not done.
		Q00595_SpecialMissionRaidersCrossroads.class, // TODO: Not done.
		Q00596_SpecialMissionDefeatBaylor.class, // TODO: Not done.
		Q00599_DemonsAndDimensionalEnergy.class, // TODO: Not done.
		Q00600_KeyToTheRefiningProcess.class, // TODO: Not done.
		Q00618_IntoTheFlame.class,
		Q00620_FourGoblets.class,
		Q00655_AGrandPlanForTamingWildBeasts.class, // TODO: Not done.
		Q00665_BasicTrainingForHunterGuildMember.class, // TODO: Not done.
		Q00666_HunterGuildMembersKnowledge.class, // TODO: Not done.
		Q00668_ABattleWithTheGiants.class, // TODO: Not done.
		Q00669_DesperateFightWithTheDragons.class, // TODO: Not done.
		Q00670_DefeatingTheLordOfSeed.class,
		Q00671_PathToFindingThePast.class, // TODO: Not done.
		Q00672_ArchenemyEmbryo.class, // TODO: Not done.
		Q00673_BelethAmbition.class, // TODO: Not done.
		Q00674_ChangesInTheShadowOfTheMotherTree.class,
		Q00675_WhatTheThreadOfThePastShows.class,
		Q00682_TheStrongInTheClosedSpace.class, // TODO: Not done.
		Q00683_AdventOfKrofinSubspecies.class, // FIXME: Custom.
		Q00684_DisturbedFields.class, // FIXME: Custom.
		Q00726_LightWithinTheDarkness.class,
		Q00727_HopeWithinTheDarkness.class,
		Q00737_ASwordHiddenInASmile.class,
		Q00738_DimensionalExplorationOfTheUnworldlyVisitors.class,
		Q00743_AtTheAltarOfOblivion.class,
		Q00749_TiesWithTheGuardians.class, // TODO: Not done.
		Q00752_UncoverTheSecret.class,
		Q00753_ReactingToACrisis.class,
		Q00754_AssistingTheRebelForces.class,
		Q00755_InNeedOfPetras.class,
		Q00756_TopQualityPetra.class,
		Q00757_TriolsMovement.class,
		Q00758_TheFallenKingsMen.class,
		Q00759_TheDwarvenNightmareContinues.class, // TODO: Not done.
		Q00760_BlockTheExit.class,
		Q00773_ToCalmTheFlood.class, // TODO: Not done.
		Q00774_DreamingOfPeace.class, // TODO: Not done.
		Q00775_RetrievingTheChaosFragment.class,
		Q00776_SlayDarkLordEkimus.class,
		Q00777_SlayDarkLordTiat.class,
		Q00778_OperationRoaringFlame.class,
		Q00779_UtilizeTheDarknessSeedOfDestruction.class,
		Q00780_UtilizeTheDarknessSeedOfInfinity.class, // TODO: Not done.
		Q00781_UtilizeTheDarknessSeedOfAnnihilation.class, // TODO: Not done.
		Q00782_UtilizeTheDarknessSeedOfHellfire.class, // TODO: Not done.
		Q00783_VestigeOfTheMagicPower.class,
		Q00790_ObtainingFerinsTrust.class,
		Q00792_TheSuperionGiants.class,
		Q00816_PlansToRepairTheStronghold.class,
		Q00823_DisappearedRaceNewFairy.class,
		Q00824_AttackTheCommandPost.class, // TODO: Not done.
		Q00826_InSearchOfTheSecretWeapon.class,
		Q00827_EinhasadsOrder.class,
		Q00828_EvasBlessing.class,
		Q00829_MaphrsSalvation.class,
		Q00830_TheWayOfTheGiantsPawn.class,
		Q00831_SayhasScheme.class,
		Q00833_DevilsTreasureTauti.class,
		Q00835_PitiableMelisa.class,
		Q00836_RequestFromTheBlackbirdClan.class, // TODO: Not done.
		Q00837_RequestFromTheGiantTrackers.class, // TODO: Not done.
		Q00838_RequestFromTheMotherTreeGuardians.class, // TODO: Not done.
		Q00839_RequestFromTheUnworldlyVisitors.class, // TODO: Not done.
		Q00840_RequestFromTheKingdomsRoyalGuard.class, // TODO: Not done.
		Q00842_CaptiveDemons.class,
		Q00843_GiantEvolutionControl.class, // TODO: Not done.
		Q00844_GiantsTreasure.class,
		Q00845_SabotageTheEmbryoSupplies.class, // TODO: Not done.
		Q00846_BuildingUpStrength.class, // TODO: Not done.
		Q00901_HowLavasaurusesAreMade.class,
		Q00903_TheCallOfAntharas.class,
		Q00905_RefinedDragonBlood.class,
		Q00906_TheCallOfValakas.class,
		Q00923_ShinedustExtraction.class,
		Q00924_GiantOfTheRestorationRoom.class,
		Q00926_30DaySearchOperation.class,
		Q00928_100DaySubjugationOperation.class,
		Q00929_SeekerRescue.class, // TODO: Not done.
		Q00930_DisparagingThePhantoms.class, // TODO: Not done.
		Q00931_MemoriesOfTheWind.class,
		Q00932_SayhasEnergy.class,
		Q00937_ToReviveTheFishingGuild.class,
		Q00985_AdventureGuildsSpecialRequestLv1.class, // TODO: Not done.
		Q00986_AdventureGuildsSpecialRequestLv2.class, // TODO: Not done.
		Q00987_AdventureGuildsSpecialRequestLv3.class, // TODO: Not done.
		Q00988_AdventureGuildsSpecialRequestLv4.class, // TODO: Not done.
		Q00989_AdventureGuildsSpecialRequestLv5.class, // TODO: Not done.
		Q01900_StormIsleSecretSpot.class, // TODO: Not done.
		Q10282_ToTheSeedOfAnnihilation.class,
		Q01901_StormIsleFurtiveDeal.class, // TODO: Not done.
		Q10283_RequestOfIceMerchant.class,
		Q10284_AcquisitionOfDivineSword.class,
		Q10285_MeetingSirra.class,
		Q10286_ReunionWithSirra.class,
		Q10287_StoryOfThoseLeft.class,
		Q10297_GrandOpeningComeToOurPub.class,
		Q10298_WastelandQueen.class, // TODO: Not done.
		Q10303_CrossroadsBetweenLightAndDarkness.class,
		Q10355_BlacksmithsSoul1.class, // TODO: Not done.
		Q10356_BlacksmithsSoul2.class, // TODO: Not done.
		Q10373_ExploringTheDimensionSealingTheDimension.class, // TODO: Not done.
		Q10381_ToTheSeedOfHellfire.class,
		Q10383_FergasonsOffer.class,
		Q10386_MysteriousJourney.class,
		Q10387_SoullessOne.class,
		Q10388_ConspiracyBehindDoor.class,
		Q10389_TheVoiceOfAuthority.class,
		Q10418_TheImmortalPirateKing.class, // TODO: Not done.
		Q10423_EmbryoStrongholdRaid.class, // TODO: Not done.
		Q10445_AnImpendingThreat.class,
		Q10446_HitAndRun.class,
		Q10447_TimingIsEverything.class,
		Q10450_ADarkAmbition.class,
		Q10454_FinalEmbryoApostle.class, // TODO: Not done.
		Q10455_ElikiasLetter.class,
		Q10457_KefensisIllusion.class, // TODO: Not done.
		Q10459_ASickAmbition.class,
		Q10501_ZakenEmbroideredSoulCloak.class,
		Q10502_FreyaEmbroideredSoulCloak.class,
		Q10503_FrintezzaEmbroideredSoulCloak.class,
		Q10514_NewPathToGlory.class, // TODO: Not done.
		Q10515_NewWayForPride.class, // TODO: Not done.
		Q10516_UnveiledFafurionTemple.class, // FIXME: Custom.
		Q10517_FafurionsMinions.class, // FIXME: Custom.
		Q10518_SucceedingThePriestess.class, // FIXME: Custom.
		Q10519_ControllingYourTemper.class, // FIXME: Custom.
		Q10520_TempleGuardians.class, // FIXME: Custom.
		Q10529_IvoryTowersResearchFloatingSeaJournal.class, // FIXME: Custom.
		Q10533_OrfensAmbition.class, // FIXME: Custom.
		Q10535_BlacksmithsSoul3.class, // TODO: Not done.
		Q10537_KamaelDisarray.class,
		Q10538_GiantsEvolution.class,
		Q10539_EnergySupplyCutoffPlan.class,
		Q10540_ThwartingMimirsPlan.class,
		Q10566_BestChoice.class,
		Q10567_SpecialMissionNornilsGarden.class, // TODO: Not done.
		Q10568_KamaelsTechnologicalAdvancement.class,
		Q10569_DeclarationOfWar.class,
		Q10570_HurrahForKamaelsIndependence.class, // TODO: Not done.
		Q10571_StrategicReconciliation.class,
		Q10572_ToExpelTheEmbryosForces.class, // TODO: Not done.
		Q10575_LetsGoFishing.class,
		Q10576_GlitteringWeapons.class,
		Q10577_TemperARustingBlade.class,
		Q10578_TheSoulOfASword.class,
		Q10579_ContainingTheAttributePower.class,
		Q10589_WhereFatesIntersect.class,
		Q10590_ReawakenedFate.class,
		Q10591_NobleMaterial.class,
		Q10594_FergasonsScheme.class, // TODO: Not done.
		Q10595_TheDimensionalWarpPart8.class, // TODO: Not done.
		Q10596_TheDimensionalWarpPart9.class, // TODO: Not done.
		Q10597_EscapeToTheShadowOfTheMotherTree.class,
		Q10598_WithAllYourMight.class,
		Q10599_ThreadOfFateHangingOnTheMotherTree.class,
		Q10658_MakkumInTheDimension.class,
		Q10673_ValentinesDayLucysReply.class, // TODO: Not done.
		Q10701_TheRoadToDestruction.class,
		Q10702_TheRoadToInfinity.class,
		Q10748_MysteriousSuggestion1.class, // TODO: Not done.
		Q10749_MysteriousSuggestion2.class, // TODO: Not done.
		Q10801_TheDimensionalWarpPart1.class,
		Q10802_TheDimensionalWarpPart2.class,
		Q10803_TheDimensionalWarpPart3.class,
		Q10804_TheDimensionalWarpPart4.class,
		Q10805_TheDimensionalWarpPart5.class,
		Q10806_TheDimensionalWarpPart6.class,
		Q10807_TheDimensionalWarpPart7.class,
		Q10811_ExaltedOneWhoFacesTheLimit.class,
		Q10812_FacingSadness.class,
		Q10813_ForGlory.class,
		Q10814_BefittingOfTheStatus.class,
		Q10815_StepUp.class,
		Q10817_ExaltedOneWhoOvercomesTheLimit.class,
		Q10818_ConfrontingAGiantMonster.class,
		Q10819_ForHonor.class,
		Q10820_RelationshipsBefittingOfTheStatus.class,
		Q10821_HelpingOthers.class,
		Q10823_ExaltedOneWhoShattersTheLimit.class,
		Q10824_ConfrontingTheGreatestDanger.class,
		Q10825_ForVictory.class,
		Q10826_LuckBefittingOfTheStatus.class,
		Q10827_StepUpToLead.class,
		Q10829_InSearchOfTheCause.class,
		Q10830_TheLostGardenOfSpirits.class,
		Q10831_UnbelievableSight.class,
		Q10832_EnergyOfSadnessAndAnger.class,
		Q10833_PutTheQueenOfSpiritsToSleep.class,
		Q10836_DisappearedClanMember.class,
		Q10837_LookingForTheBlackbirdClanMember.class,
		Q10838_TheReasonForNotBeingAbleToGetOut.class,
		Q10839_BlackbirdsNameValue.class,
		Q10840_TimeToRecover.class,
		Q10843_AnomalyInTheEnchantedValley.class,
		Q10844_BloodyBattleSeizingSupplies.class,
		Q10845_BloodyBattleRescueTheSmiths.class, // TODO: Not done.
		Q10846_BloodyBattleMeetingTheCommander.class, // TODO: Not done.
		Q10848_TrialsBeforeTheBattle.class, // TODO: Not done.
		Q10849_TrialsForAdaptation.class,
		Q10851_ElvenBotany.class,
		Q10852_TheMotherTreeRevivalProject.class,
		Q10853_ToWeakenTheGiants.class, // TODO: Not done.
		Q10854_ToSeizeTheFortress.class, // TODO: Not done.
		Q10856_SuperionAppears.class,
		Q10857_SecretTeleport.class,
		Q10873_ExaltedReachingAnotherLevel.class, // TODO: Not done.
		Q10874_AgainstTheNewEnemy.class, // TODO: Not done.
		Q10875_ForReputation.class, // TODO: Not done.
		Q10876_LeadersGrace.class, // TODO: Not done.
		Q10877_BreakThroughCrisis.class, // TODO: Not done.
		Q10879_ExaltedGuideToPower.class, // TODO: Not done.
		Q10880_TheLastOneStanding.class, // TODO: Not done.
		Q10881_ForThePride.class, // TODO: Not done.
		Q10882_VictoryCollection.class, // TODO: Not done.
		Q10883_ImmortalHonor.class, // TODO: Not done.
		Q10886_SaviorsPathSearchTheRefinery.class, // TODO: Not done.
		Q10887_SaviorsPathDemonsAndAtelia.class, // TODO: Not done.
		Q10888_SaviorsPathDefeatTheEmbryo.class, // TODO: Not done.
		Q10889_SaviorsPathFallenEmperorsThrone.class, // TODO: Not done.
		Q10890_SaviorsPathFallOfEtina.class, // TODO: Not done.
		Q10891_AtANewPlace.class,
		Q10892_RevengeOneStepAtATime.class, // TODO: Not done.
		Q10893_EndOfTwistedFate.class, // TODO: Not done.
		Q10896_VisitTheAdventureGuild.class, // TODO: Not done.
		Q10897_ShowYourAbility.class, // TODO: Not done.
		Q10898_TowardAGoal.class, // TODO: Not done.
		Q10899_VeteranAdventurer.class, // TODO: Not done.
		Q10900_PathToStrength.class, // TODO: Not done.
		Q10901_AModelAdventurer.class, // TODO: Not done.
		Q11025_PathOfDestinyProving.class,
		Q11026_PathOfDestinyConviction.class,
		Q11027_PathOfDestinyOvercome.class,
		Q11031_TrainingBeginsNow.class,
		Q11032_CurseOfUndying.class,
		Q11033_AntidoteIngredients.class,
		Q11034_ResurrectedOne.class,
		Q11035_DeathlyMischief.class,
		Q11036_ChangedSpirits.class,
		Q11037_WhyAreTheRatelHere.class,
		Q11038_GrowlersTurnedViolent.class,
		Q11039_CommunicationBreakdown.class,
		Q11040_AttackOfTheEnragedForest.class,
		Q11041_CheckOutTheSituation.class,
		Q11042_SuspiciousMovements.class,
		Q11043_SomeonesTrace.class,
		Q11044_KetraOrcs.class,
		Q11045_TheyMustBeUpToSomething.class,
		Q11046_PrayingForSafety.class,
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
