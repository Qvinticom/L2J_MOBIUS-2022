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
package org.l2jmobius.gameserver.taskmanager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.Config;
import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.handler.ItemHandler;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.ItemSkillHolder;
import org.l2jmobius.gameserver.model.items.EtcItem;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.model.skills.targets.AffectScope;
import org.l2jmobius.gameserver.model.zone.ZoneId;

/**
 * @author Mobius
 */
public class AutoUseTaskManager
{
	private static final Set<PlayerInstance> PLAYERS = ConcurrentHashMap.newKeySet();
	private static boolean _working = false;
	
	public AutoUseTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(() ->
		{
			if (_working)
			{
				return;
			}
			_working = true;
			
			for (PlayerInstance player : PLAYERS)
			{
				if (!player.isOnline() || player.isInOfflineMode())
				{
					stopAutoUseTask(player);
					continue;
				}
				
				if (player.hasBlockActions() || player.isControlBlocked() || player.isAlikeDead() || player.isInsideZone(ZoneId.PEACE))
				{
					continue;
				}
				
				if (Config.ENABLE_AUTO_ITEM)
				{
					ITEMS: for (Integer itemId : player.getAutoUseSettings().getAutoSupplyItems())
					{
						final ItemInstance item = player.getInventory().getItemByItemId(itemId.intValue());
						if (item == null)
						{
							player.getAutoUseSettings().getAutoSupplyItems().remove(itemId);
							continue ITEMS; // TODO: break?
						}
						
						for (ItemSkillHolder itemSkillHolder : item.getItem().getAllSkills())
						{
							final Skill skill = itemSkillHolder.getSkill();
							if (player.isAffectedBySkill(skill.getId()) || player.hasSkillReuse(skill.getReuseHashCode()) || !skill.checkCondition(player, player, false))
							{
								continue ITEMS;
							}
						}
						
						final int reuseDelay = item.getReuseDelay();
						if ((reuseDelay <= 0) || (player.getItemRemainingReuseTime(item.getObjectId()) <= 0))
						{
							final EtcItem etcItem = item.getEtcItem();
							final IItemHandler handler = ItemHandler.getInstance().getHandler(etcItem);
							if ((handler != null) && handler.useItem(player, item, false) && (reuseDelay > 0))
							{
								player.addTimeStampItem(item, reuseDelay);
							}
						}
					}
				}
				
				if (Config.ENABLE_AUTO_POTION && (player.getCurrentHpPercent() <= player.getAutoPlaySettings().getAutoPotionPercent()))
				{
					POTIONS: for (Integer itemId : player.getAutoUseSettings().getAutoPotionItems())
					{
						final ItemInstance item = player.getInventory().getItemByItemId(itemId.intValue());
						if (item == null)
						{
							player.getAutoUseSettings().getAutoPotionItems().remove(itemId);
							continue POTIONS; // TODO: break?
						}
						final int reuseDelay = item.getReuseDelay();
						if ((reuseDelay <= 0) || (player.getItemRemainingReuseTime(item.getObjectId()) <= 0))
						{
							final EtcItem etcItem = item.getEtcItem();
							final IItemHandler handler = ItemHandler.getInstance().getHandler(etcItem);
							if ((handler != null) && handler.useItem(player, item, false) && (reuseDelay > 0))
							{
								player.addTimeStampItem(item, reuseDelay);
							}
						}
					}
				}
				
				if (Config.ENABLE_AUTO_BUFF)
				{
					BUFFS: for (Integer skillId : player.getAutoUseSettings().getAutoSkills())
					{
						final Skill skill = player.getKnownSkill(skillId.intValue());
						if (skill == null)
						{
							player.getAutoUseSettings().getAutoSkills().remove(skillId);
							continue BUFFS; // TODO: break?
						}
						
						// Check bad skill target.
						final WorldObject target = player.getTarget();
						if ((skill.isBad() && (target == null)) || (target == player))
						{
							continue BUFFS;
						}
						
						if (!player.isAffectedBySkill(skillId.intValue()) && !player.hasSkillReuse(skill.getReuseHashCode()) && skill.checkCondition(player, player, false))
						{
							// Summon check.
							if (skill.getAffectScope() == AffectScope.SUMMON_EXCEPT_MASTER)
							{
								if (!player.hasServitors()) // Is this check truly needed?
								{
									continue BUFFS;
								}
								int occurrences = 0;
								for (Summon servitor : player.getServitors().values())
								{
									if (servitor.isAffectedBySkill(skillId.intValue()))
									{
										occurrences++;
									}
								}
								if (occurrences == player.getServitors().size())
								{
									continue BUFFS;
								}
							}
							
							// Check non bad skill target.
							if (!skill.isBad() && ((target == null) || !target.isPlayable()))
							{
								final WorldObject savedTarget = target;
								player.setTarget(player);
								player.doCast(skill);
								player.setTarget(savedTarget);
							}
							else
							{
								player.doCast(skill);
							}
						}
					}
				}
			}
			
			_working = false;
		}, 1000, 1000);
	}
	
	public void startAutoUseTask(PlayerInstance player)
	{
		if (!PLAYERS.contains(player))
		{
			PLAYERS.add(player);
		}
	}
	
	public void stopAutoUseTask(PlayerInstance player)
	{
		PLAYERS.remove(player);
	}
	
	public void addAutoSupplyItem(PlayerInstance player, int itemId)
	{
		player.getAutoUseSettings().getAutoSupplyItems().add(itemId);
		startAutoUseTask(player);
	}
	
	public void removeAutoSupplyItem(PlayerInstance player, int itemId)
	{
		player.getAutoUseSettings().getAutoSupplyItems().remove(itemId);
		stopAutoUseTask(player);
	}
	
	public void addAutoPotionItem(PlayerInstance player, int itemId)
	{
		player.getAutoUseSettings().getAutoPotionItems().add(itemId);
		startAutoUseTask(player);
	}
	
	public void removeAutoPotionItem(PlayerInstance player, int itemId)
	{
		player.getAutoUseSettings().getAutoPotionItems().remove(itemId);
		stopAutoUseTask(player);
	}
	
	public void addAutoSkill(PlayerInstance player, int skillId)
	{
		player.getAutoUseSettings().getAutoSkills().add(skillId);
		startAutoUseTask(player);
	}
	
	public void removeAutoSkill(PlayerInstance player, int skillId)
	{
		player.getAutoUseSettings().getAutoSkills().remove(skillId);
		stopAutoUseTask(player);
	}
	
	public static AutoUseTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoUseTaskManager INSTANCE = new AutoUseTaskManager();
	}
}
