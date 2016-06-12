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
package handlers;

import java.util.logging.Logger;

import com.l2jmobius.gameserver.handler.OneDayRewardHandler;

import handlers.onedayrewardshandlers.BossOneDayRewardHandler;
import handlers.onedayrewardshandlers.CeremonyOfChaosOneDayRewardHandler;
import handlers.onedayrewardshandlers.FishingOneDayRewardHandler;
import handlers.onedayrewardshandlers.LevelOneDayRewardHandler;
import handlers.onedayrewardshandlers.OlympiadOneDayRewardHandler;
import handlers.onedayrewardshandlers.QuestOneDayRewardHandler;
import handlers.onedayrewardshandlers.SiegeOneDayRewardHandler;

/**
 * @author UnAfraid
 */
public class OneDayRewardMasterHandler
{
	private static final Logger LOGGER = Logger.getLogger(OneDayRewardMasterHandler.class.getName());
	
	public static void main(String[] args)
	{
		OneDayRewardHandler.getInstance().registerHandler("level", LevelOneDayRewardHandler::new);
		// OneDayRewardHandler.getInstance().registerHandler("loginAllWeek", LoginAllWeekOneDayRewardHandler::new);
		// OneDayRewardHandler.getInstance().registerHandler("loginAllMonth", LoginAllWeekOneDayRewardHandler::new);
		OneDayRewardHandler.getInstance().registerHandler("quest", QuestOneDayRewardHandler::new);
		OneDayRewardHandler.getInstance().registerHandler("olympiad", OlympiadOneDayRewardHandler::new);
		OneDayRewardHandler.getInstance().registerHandler("siege", SiegeOneDayRewardHandler::new);
		OneDayRewardHandler.getInstance().registerHandler("ceremonyofchaos", CeremonyOfChaosOneDayRewardHandler::new);
		OneDayRewardHandler.getInstance().registerHandler("boss", BossOneDayRewardHandler::new);
		OneDayRewardHandler.getInstance().registerHandler("fishing", FishingOneDayRewardHandler::new);
		LOGGER.info(OneDayRewardMasterHandler.class.getSimpleName() + ":  Loaded " + OneDayRewardHandler.getInstance().size() + " handlers.");
	}
}
