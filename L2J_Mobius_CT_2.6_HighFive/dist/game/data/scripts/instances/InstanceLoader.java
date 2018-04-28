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
package instances;

import java.util.logging.Level;
import java.util.logging.Logger;

import instances.CastleDungeon.CastleDungeon;
import instances.CavernOfThePirateCaptain.CavernOfThePirateCaptain;
import instances.ChambersOfDelusion.ChamberOfDelusionEast;
import instances.ChambersOfDelusion.ChamberOfDelusionNorth;
import instances.ChambersOfDelusion.ChamberOfDelusionSouth;
import instances.ChambersOfDelusion.ChamberOfDelusionSquare;
import instances.ChambersOfDelusion.ChamberOfDelusionTower;
import instances.ChambersOfDelusion.ChamberOfDelusionWest;
import instances.CrystalCaverns.CrystalCaverns;
import instances.DarkCloudMansion.DarkCloudMansion;
import instances.FinalEmperialTomb.FinalEmperialTomb;
import instances.IceQueensCastle.IceQueensCastle;
import instances.IceQueensCastleNormalBattle.IceQueensCastleNormalBattle;
import instances.JiniaGuildHideout1.JiniaGuildHideout1;
import instances.JiniaGuildHideout2.JiniaGuildHideout2;
import instances.JiniaGuildHideout3.JiniaGuildHideout3;
import instances.JiniaGuildHideout4.JiniaGuildHideout4;
import instances.Kamaloka.Kamaloka;
import instances.MithrilMine.MithrilMine;
import instances.NornilsGarden.NornilsGarden;
import instances.NornilsGardenQuest.NornilsGardenQuest;
import instances.PailakaDevilsLegacy.PailakaDevilsLegacy;
import instances.PailakaSongOfIceAndFire.PailakaSongOfIceAndFire;
import instances.SSQDisciplesNecropolisPast.SSQDisciplesNecropolisPast;
import instances.SSQElcadiasTent.SSQElcadiasTent;
import instances.SSQHideoutOfTheDawn.SSQHideoutOfTheDawn;
import instances.SSQLibraryOfSages.SSQLibraryOfSages;
import instances.SSQMonasteryOfSilence.SSQMonasteryOfSilence;
import instances.SSQSanctumOfTheLordsOfDawn.SSQSanctumOfTheLordsOfDawn;

/**
 * Instance class-loader.
 * @author FallenAngel
 */
public final class InstanceLoader
{
	private static final Logger LOGGER = Logger.getLogger(InstanceLoader.class.getName());
	
	private static final Class<?>[] SCRIPTS =
	{
		CastleDungeon.class,
		CavernOfThePirateCaptain.class,
		CrystalCaverns.class,
		DarkCloudMansion.class,
		FinalEmperialTomb.class,
		ChamberOfDelusionEast.class,
		ChamberOfDelusionNorth.class,
		ChamberOfDelusionSouth.class,
		ChamberOfDelusionSquare.class,
		ChamberOfDelusionTower.class,
		ChamberOfDelusionWest.class,
		IceQueensCastle.class,
		IceQueensCastleNormalBattle.class,
		JiniaGuildHideout1.class,
		JiniaGuildHideout2.class,
		JiniaGuildHideout3.class,
		JiniaGuildHideout4.class,
		Kamaloka.class,
		MithrilMine.class,
		NornilsGarden.class,
		NornilsGardenQuest.class,
		PailakaDevilsLegacy.class,
		PailakaSongOfIceAndFire.class,
		SSQDisciplesNecropolisPast.class,
		SSQElcadiasTent.class,
		SSQHideoutOfTheDawn.class,
		SSQLibraryOfSages.class,
		SSQMonasteryOfSilence.class,
		SSQSanctumOfTheLordsOfDawn.class,
	};
	
	public static void main(String[] args)
	{
		LOGGER.info(InstanceLoader.class.getSimpleName() + ": Loading Instances scripts.");
		for (Class<?> script : SCRIPTS)
		{
			try
			{
				script.newInstance();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, InstanceLoader.class.getSimpleName() + ": Failed loading " + script.getSimpleName() + ":", e);
			}
		}
	}
}
