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
package com.l2jmobius.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2ChestInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.CharInfo;
import com.l2jmobius.gameserver.network.serverpackets.Earthquake;
import com.l2jmobius.gameserver.network.serverpackets.ExRedSky;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.PlaySound;
import com.l2jmobius.gameserver.network.serverpackets.SignsSky;
import com.l2jmobius.gameserver.network.serverpackets.SocialAction;
import com.l2jmobius.gameserver.network.serverpackets.StopMove;
import com.l2jmobius.gameserver.network.serverpackets.SunRise;
import com.l2jmobius.gameserver.network.serverpackets.SunSet;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.network.serverpackets.UserInfo;

/**
 * This class handles following admin commands:
 * <li>invis/invisible/vis/visible = makes yourself invisible or visible
 * <li>earthquake = causes an earthquake of a given intensity and duration around you
 * <li>bighead/shrinkhead = changes head size
 * <li>gmspeed = temporary Super Haste effect.
 * <li>para/unpara = paralyze/remove paralysis from target
 * <li>para_all/unpara_all = same as para/unpara, affects the whole world.
 * <li>polyself/unpolyself = makes you look as a specified mob.
 * <li>changename = temporary change name
 * <li>clearteams/setteam_close/setteam = team related commands
 * <li>social = forces an L2Character instance to broadcast social action packets.
 * <li>effect = forces an L2Character instance to broadcast MSU packets.
 * <li>abnormal = force changes over an L2Character instance's abnormal state.
 * <li>play_sound/play_sounds = Music broadcasting related commands
 * <li>atmosphere = sky change related commands.
 */
public class AdminEffects implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_invis",
		"admin_invis_menu_main",
		"admin_invisible",
		"admin_vis",
		"admin_visible",
		"admin_invis_menu",
		"admin_earthquake",
		"admin_earthquake_menu",
		"admin_bighead",
		"admin_shrinkhead",
		"admin_gmspeed",
		"admin_gmspeed_menu",
		"admin_gmspeed_menu_main",
		"admin_unpara_all",
		"admin_para_all",
		"admin_unpara",
		"admin_para",
		"admin_unpara_all_menu",
		"admin_para_all_menu",
		"admin_unpara_menu",
		"admin_para_menu",
		"admin_polyself",
		"admin_unpolyself",
		"admin_polyself_menu",
		"admin_unpolyself_menu",
		"admin_clearteams",
		"admin_setteam_close",
		"admin_setteam",
		"admin_social",
		"admin_effect",
		"admin_social_menu",
		"admin_effect_menu",
		"admin_abnormal",
		"admin_abnormal_menu",
		"admin_play_sounds",
		"admin_play_sound",
		"admin_atmosphere",
		"admin_atmosphere_menu",
		"admin_npc_say",
		"admin_debuff"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (command.startsWith("admin_invis_menu"))
		{
			if (!activeChar.getAppearance().getInvisible())
			{
				activeChar.getAppearance().setInvisible();
				activeChar.decayMe();
				activeChar.broadcastUserInfo();
				activeChar.spawnMe();
			}
			else
			{
				activeChar.getAppearance().setVisible();
				activeChar.broadcastUserInfo();
			}
		}
		
		else if (command.startsWith("admin_invis"))
		{
			activeChar.getAppearance().setInvisible();
			activeChar.decayMe();
			activeChar.broadcastUserInfo();
			activeChar.spawnMe();
		}
		else if (command.startsWith("admin_vis"))
		{
			activeChar.getAppearance().setVisible();
			activeChar.broadcastUserInfo();
		}
		
		else if (command.startsWith("admin_earthquake"))
		{
			try
			{
				String val1 = st.nextToken();
				final int intensity = Integer.parseInt(val1);
				String val2 = st.nextToken();
				final int duration = Integer.parseInt(val2);
				final Earthquake eq = new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), intensity, duration);
				activeChar.broadcastPacket(eq);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Use: //earthquake <intensity> <duration>");
			}
		}
		
		else if (command.startsWith("admin_atmosphere"))
		{
			try
			{
				String type = st.nextToken();
				String state = st.nextToken();
				adminAtmosphere(type, state, activeChar);
			}
			catch (Exception ex)
			{
			}
		}
		
		else if (command.startsWith("admin_npc_say"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				
				if (activeChar.getSayMode() != null)
				{
					activeChar.setSayMode(null);
					activeChar.sendMessage("NpcSay mode off");
				}
				else if ((target != null) && (target instanceof L2NpcInstance))
				{
					activeChar.setSayMode(target);
					activeChar.sendMessage("NpcSay mode on for " + target.getName());
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					
					return false;
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Target Npc before. Use: //npc_say");
			}
		}
		
		else if (command.equals("admin_play_sounds"))
		{
			AdminHelpPage.showHelpPage(activeChar, "songs/songs.htm");
		}
		else if (command.startsWith("admin_play_sounds"))
		{
			try
			{
				AdminHelpPage.showHelpPage(activeChar, "songs/songs" + command.substring(17) + ".htm");
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_play_sound"))
		{
			try
			{
				playAdminSound(activeChar, command.substring(17));
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		
		else if (command.equals("admin_para") || command.equals("admin_para_menu"))
		{
			String type = "1";
			try
			{
				type = st.nextToken();
			}
			catch (Exception e)
			{
			}
			try
			{
				final L2Object target = activeChar.getTarget();
				L2Character player = null;
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					if (type.equals("1"))
					{
						player.startAbnormalEffect(0x0400);
					}
					else
					{
						player.startAbnormalEffect(0x0800);
					}
					player.setIsParalyzed(true);
					final StopMove sm = new StopMove(player);
					player.sendPacket(sm);
					player.broadcastPacket(sm);
				}
			}
			catch (Exception e)
			{
			}
		}
		
		else if (command.equals("admin_unpara") || command.equals("admin_unpara_menu"))
		{
			try
			{
				final L2Object target = activeChar.getTarget();
				L2Character player = null;
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					player.stopAbnormalEffect((short) 0x0400);
					player.setIsParalyzed(false);
				}
			}
			catch (Exception e)
			{
			}
		}
		
		else if (command.startsWith("admin_para_all"))
		{
			try
			{
				for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					if (!player.isGM())
					{
						player.startAbnormalEffect(0x0400);
						player.setIsParalyzed(true);
						final StopMove sm = new StopMove(player);
						player.sendPacket(sm);
						player.broadcastPacket(sm);
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		
		else if (command.startsWith("admin_unpara_all"))
		{
			try
			{
				for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					player.stopAbnormalEffect(0x0400);
					player.setIsParalyzed(false);
				}
			}
			catch (Exception e)
			{
			}
		}
		
		else if (command.startsWith("admin_bighead"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					player.startAbnormalEffect(0x2000);
				}
			}
			catch (Exception e)
			{
			}
		}
		
		else if (command.startsWith("admin_shrinkhead"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					player.stopAbnormalEffect((short) 0x2000);
				}
			}
			catch (Exception e)
			{
			}
		}
		
		else if (command.startsWith("admin_gmspeed"))
		{
			try
			{
				final int val = Integer.parseInt(st.nextToken());
				final boolean sendMessage = activeChar.getFirstEffect(7029) != null;
				
				activeChar.stopSkillEffects(7029);
				
				if ((val == 0) && sendMessage)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED).addSkillName(7029));
				}
				else if ((val >= 1) && (val <= 4))
				{
					final L2Skill gmSpeedSkill = SkillTable.getInstance().getInfo(7029, val);
					activeChar.doCast(gmSpeedSkill);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Use //gmspeed value (0=off...4=max).");
			}
			finally
			{
				activeChar.updateEffectIcons();
			}
		}
		
		else if (command.startsWith("admin_polyself"))
		{
			try
			{
				String id = st.nextToken();
				
				activeChar.getPoly().setPolyInfo("npc", id);
				activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false);
				
				CharInfo info1 = new CharInfo(activeChar);
				activeChar.broadcastPacket(info1);
				UserInfo info2 = new UserInfo(activeChar);
				activeChar.sendPacket(info2);
			}
			catch (Exception e)
			{
			}
		}
		
		else if (command.startsWith("admin_unpolyself"))
		{
			try
			{
				activeChar.getPoly().setPolyInfo(null, "1");
				activeChar.decayMe();
				activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
				
				CharInfo info1 = new CharInfo(activeChar);
				activeChar.broadcastPacket(info1);
				UserInfo info2 = new UserInfo(activeChar);
				activeChar.sendPacket(info2);
			}
			catch (Exception e)
			{
			}
		}
		
		else if (command.equals("admin_clear_teams"))
		{
			try
			{
				for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					player.setTeam(0);
					player.broadcastUserInfo();
				}
			}
			catch (Exception e)
			{
			}
		}
		
		else if (command.startsWith("admin_setteam_close"))
		{
			try
			{
				String val = st.nextToken();
				
				final int teamVal = Integer.parseInt(val);
				
				for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					if (activeChar.isInsideRadius(player, 400, false, true))
					{
						player.setTeam(0);
						
						if (teamVal != 0)
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
							sm.addString("You have joined team " + teamVal);
							player.sendPacket(sm);
						}
						
						player.broadcastUserInfo();
					}
				}
			}
			catch (Exception e)
			{
			}
		}
		
		else if (command.startsWith("admin_setteam"))
		{
			String val = command.substring(14);
			
			final int teamVal = Integer.parseInt(val);
			
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			
			if (target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			else
			{
				return false;
			}
			
			player.setTeam(teamVal);
			
			if (teamVal != 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("You have joined team " + teamVal);
				player.sendPacket(sm);
			}
			
			player.broadcastUserInfo();
		}
		
		else if (command.startsWith("admin_social"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();
				
				if (st.countTokens() == 2)
				{
					final int social = Integer.parseInt(st.nextToken());
					
					target = st.nextToken();
					
					if (target != null)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(target);
						
						if (player != null)
						{
							if (performSocial(social, player, activeChar))
							{
								activeChar.sendMessage(player.getName() + " was affected by your request.");
							}
						}
						else
						{
							try
							{
								final int radius = Integer.parseInt(target);
								
								for (L2Object object : activeChar.getKnownList().getKnownObjects().values())
								{
									if (activeChar.isInsideRadius(object, radius, false, false))
									{
										performSocial(social, object, activeChar);
									}
								}
								
								activeChar.sendMessage(radius + " units radius affected by your request.");
							}
							catch (NumberFormatException nbe)
							{
								activeChar.sendMessage("Incorrect parameter");
							}
						}
					}
				}
				else if (st.countTokens() == 1)
				{
					final int social = Integer.parseInt(st.nextToken());
					
					if (obj == null)
					{
						obj = activeChar;
					}
					
					if (performSocial(social, obj, activeChar))
					{
						activeChar.sendMessage(obj.getName() + " was affected by your request.");
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					}
				}
				else if (!command.contains("menu"))
				{
					activeChar.sendMessage("Usage: //social <social_id> [player_name|radius]");
				}
			}
			catch (Exception e)
			{
			}
		}
		
		else if (command.startsWith("debuff"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					player.stopAllEffects();
					activeChar.sendMessage("Effects has been cleared from " + player + ".");
				}
			}
			catch (Exception e)
			{
			}
		}
		
		else if (command.startsWith("admin_abnormal"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();
				
				if (st.countTokens() == 2)
				{
					String parm = st.nextToken();
					
					final int abnormal = Integer.decode("0x" + parm);
					
					target = st.nextToken();
					
					if (target != null)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(target);
						
						if (player != null)
						{
							if (performAbnormal(abnormal, player))
							{
								activeChar.sendMessage(player.getName() + "'s abnormal status was affected by your request.");
							}
							else
							{
								activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
							}
						}
						else
						{
							try
							{
								final int radius = Integer.parseInt(target);
								
								for (L2Object object : activeChar.getKnownList().getKnownObjects().values())
								{
									if (activeChar.isInsideRadius(object, radius, false, false))
									{
										performAbnormal(abnormal, object);
									}
								}
								
								activeChar.sendMessage(radius + " units radius affected by your request.");
							}
							catch (NumberFormatException nbe)
							{
								activeChar.sendMessage("Usage: //abnormal <hex_abnormal_mask> [player|radius]");
							}
						}
					}
				}
				else if (st.countTokens() == 1)
				{
					final int abnormal = Integer.decode("0x" + st.nextToken());
					
					if (obj == null)
					{
						obj = activeChar;
					}
					
					if (performAbnormal(abnormal, obj))
					{
						activeChar.sendMessage(obj.getName() + "'s abnormal status was affected by your request.");
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					}
				}
				else if (!command.contains("menu"))
				{
					activeChar.sendMessage("Usage: //abnormal <abnormal_mask> [player_name|radius]");
				}
			}
			catch (Exception e)
			{
			}
		}
		
		else if (command.startsWith("admin_effect"))
		{
			try
			{
				L2Object obj = activeChar.getTarget();
				
				int level = 1, hittime = 1;
				final int skill = Integer.parseInt(st.nextToken());
				
				if (st.hasMoreTokens())
				{
					level = Integer.parseInt(st.nextToken());
				}
				
				if (st.hasMoreTokens())
				{
					hittime = Integer.parseInt(st.nextToken());
				}
				
				if (obj == null)
				{
					obj = activeChar;
				}
				
				if (!(obj instanceof L2Character))
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				else
				{
					final L2Character target = (L2Character) obj;
					
					target.broadcastPacket(new MagicSkillUse(target, activeChar, skill, level, hittime, 0));
					activeChar.sendMessage(obj.getName() + " performs MSU " + skill + "/" + level + " by your request.");
					
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //effect skill [level | level hittime]");
			}
		}
		
		if (command.contains("menu"))
		{
			showMainPage(activeChar, command);
		}
		
		return true;
	}
	
	/**
	 * @param action bitmask that should be applied over target's abnormal
	 * @param target
	 * @return <i>true</i> if target's abnormal state was affected , <i>false</i> otherwise.
	 */
	private boolean performAbnormal(int action, L2Object target)
	{
		if (target instanceof L2Character)
		{
			L2Character character = (L2Character) target;
			
			if ((character.getAbnormalEffect() & action) == action)
			{
				character.stopAbnormalEffect(action);
			}
			else
			{
				character.startAbnormalEffect(action);
			}
			
			return true;
		}
		return false;
	}
	
	private boolean performSocial(int action, L2Object target, L2PcInstance activeChar)
	{
		try
		{
			if (target instanceof L2Character)
			{
				if ((target instanceof L2Summon) || (target instanceof L2ChestInstance))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					
					return false;
				}
				
				if ((target instanceof L2NpcInstance) && ((action < 1) || (action > 3)))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					
					return false;
				}
				
				if ((target instanceof L2PcInstance) && ((action < 2) || (action > 16)))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					
					return false;
				}
				
				L2Character character = (L2Character) target;
				character.broadcastPacket(new SocialAction(target.getObjectId(), action));
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
		}
		return true;
	}
	
	/**
	 * @param type - atmosphere type (signssky,sky)
	 * @param state - atmosphere state(night,day)
	 * @param activeChar
	 */
	private void adminAtmosphere(String type, String state, L2PcInstance activeChar)
	{
		L2GameServerPacket packet = null;
		
		switch (type)
		{
			case "signsky":
			{
				if (state.equals("dawn"))
				{
					packet = new SignsSky(2);
				}
				else if (state.equals("dusk"))
				{
					packet = new SignsSky(1);
				}
				break;
			}
			case "sky":
			{
				if (state.equals("night"))
				{
					packet = new SunSet();
				}
				else if (state.equals("day"))
				{
					packet = new SunRise();
				}
				else if (state.equals("red"))
				{
					packet = new ExRedSky(10);
				}
				break;
			}
			default:
			{
				activeChar.sendMessage("Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red>");
				break;
			}
		}
		
		if (packet != null)
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				player.sendPacket(packet);
			}
		}
	}
	
	private void playAdminSound(L2PcInstance activeChar, String sound)
	{
		PlaySound _snd = new PlaySound(1, sound, 0, 0, 0, 0, 0);
		activeChar.sendPacket(_snd);
		activeChar.broadcastPacket(_snd);
		activeChar.sendMessage("Playing " + sound + ".");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showMainPage(L2PcInstance activeChar, String command)
	{
		String filename = "effects_menu";
		
		if (command.contains("menu_main"))
		{
			filename = "main_menu";
		}
		else if (command.contains("abnormal"))
		{
			filename = "abnormal";
		}
		else if (command.contains("social"))
		{
			filename = "social";
		}
		
		AdminHelpPage.showHelpPage(activeChar, filename + ".htm");
	}
}
