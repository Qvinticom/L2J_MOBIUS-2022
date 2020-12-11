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
package ai.others;

import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.Id;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerItemEquip;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerItemUnequip;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.items.Weapon;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class DragonWeaponListeners extends AbstractNpcAI
{
	private static final int[] WEAPON_IDS =
	{
		36417,
		36418,
		36419,
		36420,
		36421,
		36422,
		36423,
		36424,
		36425,
		36426,
		36441,
		36442,
		36443,
		36444,
		36445,
		36446,
		36447,
		36448,
		36449,
		36450,
		36465,
		36466,
		36467,
		36468,
		36469,
		36470,
		36471,
		36472,
		36473,
		36474,
		36489,
		36490,
		36491,
		36492,
		36493,
		36494,
		36495,
		36496,
		36497,
		36498,
		36427,
		36428,
		36429,
		36430,
		36431,
		36432,
		36433,
		36451,
		36452,
		36453,
		36454,
		36455,
		36456,
		36457,
		36475,
		36476,
		36477,
		36478,
		36479,
		36480,
		36481,
		36499,
		36500,
		36501,
		36502,
		36503,
		36504,
		36505,
		36434,
		36435,
		36436,
		36437,
		36438,
		36439,
		36440,
		36458,
		36459,
		36460,
		36461,
		36462,
		36463,
		36464,
		36482,
		36483,
		36484,
		36485,
		36486,
		36487,
		36488,
		36506,
		36507,
		36508,
		36509,
		36510,
		36511,
		36512,
		80066,
		80067,
		80068,
		80069,
		80070,
		80071,
		80072,
		80073,
		80074,
		80075,
		80076,
		80077,
		80078,
		80079,
		80080,
		80081,
		80082,
		80083,
		80084,
		80085,
		80086,
		80087,
		80088,
		80089,
		80315,
		80316,
		80317,
		80318,
	};
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		final PlayerInstance player = event.getPlayer();
		if (player != null)
		{
			final Weapon weapon = player.getActiveWeaponItem();
			if ((weapon != null) && CommonUtil.contains(WEAPON_IDS, weapon.getId()))
			{
				player.setDragonWeaponEquipped(true);
			}
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_ITEM_EQUIP)
	@RegisterType(ListenerRegisterType.ITEM)
	@Id(36417)
	@Id(36418)
	@Id(36419)
	@Id(36420)
	@Id(36421)
	@Id(36422)
	@Id(36423)
	@Id(36424)
	@Id(36425)
	@Id(36426)
	@Id(36441)
	@Id(36442)
	@Id(36443)
	@Id(36444)
	@Id(36445)
	@Id(36446)
	@Id(36447)
	@Id(36448)
	@Id(36449)
	@Id(36450)
	@Id(36465)
	@Id(36466)
	@Id(36467)
	@Id(36468)
	@Id(36469)
	@Id(36470)
	@Id(36471)
	@Id(36472)
	@Id(36473)
	@Id(36474)
	@Id(36489)
	@Id(36490)
	@Id(36491)
	@Id(36492)
	@Id(36493)
	@Id(36494)
	@Id(36495)
	@Id(36496)
	@Id(36497)
	@Id(36498)
	@Id(36427)
	@Id(36428)
	@Id(36429)
	@Id(36430)
	@Id(36431)
	@Id(36432)
	@Id(36433)
	@Id(36451)
	@Id(36452)
	@Id(36453)
	@Id(36454)
	@Id(36455)
	@Id(36456)
	@Id(36457)
	@Id(36475)
	@Id(36476)
	@Id(36477)
	@Id(36478)
	@Id(36479)
	@Id(36480)
	@Id(36481)
	@Id(36499)
	@Id(36500)
	@Id(36501)
	@Id(36502)
	@Id(36503)
	@Id(36504)
	@Id(36505)
	@Id(36434)
	@Id(36435)
	@Id(36436)
	@Id(36437)
	@Id(36438)
	@Id(36439)
	@Id(36440)
	@Id(36458)
	@Id(36459)
	@Id(36460)
	@Id(36461)
	@Id(36462)
	@Id(36463)
	@Id(36464)
	@Id(36482)
	@Id(36483)
	@Id(36484)
	@Id(36485)
	@Id(36486)
	@Id(36487)
	@Id(36488)
	@Id(36506)
	@Id(36507)
	@Id(36508)
	@Id(36509)
	@Id(36510)
	@Id(36511)
	@Id(36512)
	@Id(80066)
	@Id(80067)
	@Id(80068)
	@Id(80069)
	@Id(80070)
	@Id(80071)
	@Id(80072)
	@Id(80073)
	@Id(80074)
	@Id(80075)
	@Id(80076)
	@Id(80077)
	@Id(80078)
	@Id(80079)
	@Id(80080)
	@Id(80081)
	@Id(80082)
	@Id(80083)
	@Id(80084)
	@Id(80085)
	@Id(80086)
	@Id(80087)
	@Id(80088)
	@Id(80089)
	@Id(80315)
	@Id(80316)
	@Id(80317)
	@Id(80318)
	public void onPlayerItemEquip(OnPlayerItemEquip event)
	{
		final PlayerInstance player = event.getPlayer();
		if (player != null)
		{
			player.setDragonWeaponEquipped(true);
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_ITEM_UNEQUIP)
	@RegisterType(ListenerRegisterType.ITEM)
	@Id(36417)
	@Id(36418)
	@Id(36419)
	@Id(36420)
	@Id(36421)
	@Id(36422)
	@Id(36423)
	@Id(36424)
	@Id(36425)
	@Id(36426)
	@Id(36441)
	@Id(36442)
	@Id(36443)
	@Id(36444)
	@Id(36445)
	@Id(36446)
	@Id(36447)
	@Id(36448)
	@Id(36449)
	@Id(36450)
	@Id(36465)
	@Id(36466)
	@Id(36467)
	@Id(36468)
	@Id(36469)
	@Id(36470)
	@Id(36471)
	@Id(36472)
	@Id(36473)
	@Id(36474)
	@Id(36489)
	@Id(36490)
	@Id(36491)
	@Id(36492)
	@Id(36493)
	@Id(36494)
	@Id(36495)
	@Id(36496)
	@Id(36497)
	@Id(36498)
	@Id(36427)
	@Id(36428)
	@Id(36429)
	@Id(36430)
	@Id(36431)
	@Id(36432)
	@Id(36433)
	@Id(36451)
	@Id(36452)
	@Id(36453)
	@Id(36454)
	@Id(36455)
	@Id(36456)
	@Id(36457)
	@Id(36475)
	@Id(36476)
	@Id(36477)
	@Id(36478)
	@Id(36479)
	@Id(36480)
	@Id(36481)
	@Id(36499)
	@Id(36500)
	@Id(36501)
	@Id(36502)
	@Id(36503)
	@Id(36504)
	@Id(36505)
	@Id(36434)
	@Id(36435)
	@Id(36436)
	@Id(36437)
	@Id(36438)
	@Id(36439)
	@Id(36440)
	@Id(36458)
	@Id(36459)
	@Id(36460)
	@Id(36461)
	@Id(36462)
	@Id(36463)
	@Id(36464)
	@Id(36482)
	@Id(36483)
	@Id(36484)
	@Id(36485)
	@Id(36486)
	@Id(36487)
	@Id(36488)
	@Id(36506)
	@Id(36507)
	@Id(36508)
	@Id(36509)
	@Id(36510)
	@Id(36511)
	@Id(36512)
	@Id(80066)
	@Id(80067)
	@Id(80068)
	@Id(80069)
	@Id(80070)
	@Id(80071)
	@Id(80072)
	@Id(80073)
	@Id(80074)
	@Id(80075)
	@Id(80076)
	@Id(80077)
	@Id(80078)
	@Id(80079)
	@Id(80080)
	@Id(80081)
	@Id(80082)
	@Id(80083)
	@Id(80084)
	@Id(80085)
	@Id(80086)
	@Id(80087)
	@Id(80088)
	@Id(80089)
	@Id(80315)
	@Id(80316)
	@Id(80317)
	@Id(80318)
	public void onPlayerItemUnequip(OnPlayerItemUnequip event)
	{
		final PlayerInstance player = event.getPlayer();
		if (player != null)
		{
			player.setDragonWeaponEquipped(false);
		}
	}
	
	public static void main(String[] args)
	{
		new DragonWeaponListeners();
	}
}
