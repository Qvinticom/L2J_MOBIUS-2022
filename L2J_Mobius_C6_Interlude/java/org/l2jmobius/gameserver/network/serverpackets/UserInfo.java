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
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.Map;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.datatables.sql.NpcTable;
import org.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jmobius.gameserver.model.Inventory;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.CubicInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;

/**
 * 0000: 04 03 15 00 00 77 ff 00 00 80 f1 ff ff 00 00 00 .....w.......... 0010: 00 2a 89 00 4c 43 00 61 00 6c 00 61 00 64 00 6f .*..LC.a.l.a.d.o 0020: 00 6e 00 00 00 01 00 00 00 00 00 00 00 19 00 00 .n.............. 0030: 00 0d 00 00 00 ee 81 02 00 15 00 00 00 18 00 00 ................ 0040: 00 19
 * 00 00 00 25 00 00 00 17 00 00 00 28 00 00 .....%.......(.. 0050: 00 14 01 00 00 14 01 00 00 02 01 00 00 02 01 00 ................ 0060: 00 fa 09 00 00 81 06 00 00 26 34 00 00 2e 00 00 .........&4..... 0070: 00 00 00 00 00 db 9f a1 41 93 26 64 41 de c8 31 ........A.&dA..1 0080: 41 ca 73 c0 41 d5
 * 22 d0 41 83 bd 41 41 81 56 10 A.s.A.".A..AA.V. 0090: 41 00 00 00 00 27 7d 30 41 69 aa e0 40 b4 fb d3 A....'}0Ai..@... 00a0: 41 91 f9 63 41 00 00 00 00 81 56 10 41 00 00 00 A..cA.....V.A... 00b0: 00 71 00 00 00 71 00 00 00 76 00 00 00 74 00 00 .q...q...v...t.. 00c0: 00 74 00 00 00 2a 00 00 00 e8
 * 02 00 00 00 00 00 .t...*.......... 00d0: 00 5f 04 00 00 ac 01 00 00 cf 01 00 00 62 04 00 ._...........b.. 00e0: 00 00 00 00 00 e8 02 00 00 0b 00 00 00 52 01 00 .............R.. 00f0: 00 4d 00 00 00 2a 00 00 00 2f 00 00 00 29 00 00 .M...*.../...).. 0100: 00 12 00 00 00 82 01 00 00 52 01 00 00 53
 * 00 00 .........R...S.. 0110: 00 00 00 00 00 00 00 00 00 7a 00 00 00 55 00 00 .........z...U.. 0120: 00 32 00 00 00 32 00 00 00 00 00 00 00 00 00 00 .2...2.......... 0130: 00 00 00 00 00 00 00 00 00 a4 70 3d 0a d7 a3 f0 ..........p=.... 0140: 3f 64 5d dc 46 03 78 f3 3f 00 00 00 00 00 00 1e
 * ?d].F.x.?....... 0150: 40 00 00 00 00 00 00 38 40 02 00 00 00 01 00 00 @......8@....... 0160: 00 00 00 00 00 00 00 00 00 00 00 c1 0c 00 00 01 ................ 0170: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ................ 0180: 00 00 00 00 ....
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccdd (h) dddddSddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd ffffddddSdddddcccddh (h) c dc hhdh
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddddcccddh (h) c dc hhdh ddddc c dcc cddd d (from 654) but it actually reads dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddddcccddh (h) c dc *dddddddd* hhdh ddddc dcc
 * cddd d *...*: here i am not sure at least it looks like it reads that much data (32 bytes), not sure about the format inside because it is not read thanks to the ususal
 * parsingfunctiondddddSddddQddddddddddddddddddddddddddddddddddddddddddddddddhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhddddddddddddddddddddffffddddSdddddcccddh [h] c dc d hhdh ddddc c dcc cddd d c dd d d
 * @version $Revision: 1.14.2.4.2.12 $ $Date: 2005/04/11 10:05:55 $
 */
public class UserInfo extends GameServerPacket
{
	private final PlayerInstance _player;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private int _flRunSpd;
	private int _flWalkSpd;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private int _relation;
	private final float _moveMultiplier;
	
	public boolean _critical_test = false;
	
	/**
	 * @param player
	 */
	public UserInfo(PlayerInstance player)
	{
		_player = player;
		
		_moveMultiplier = _player.getMovementSpeedMultiplier();
		_runSpd = (int) (_player.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) (_player.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
		_relation = _player.isClanLeader() ? 0x40 : 0;
		
		if (_player.getSiegeState() == 1)
		{
			_relation |= 0x180;
		}
		if (_player.getSiegeState() == 2)
		{
			_relation |= 0x80;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x04);
		
		if (!_critical_test)
		{
			writeD(_player.getX());
			writeD(_player.getY());
			writeD(_player.getZ());
			writeD(_player.getHeading());
		}
		else // critical values
		{
			writeD(-999999999);
			writeD(-999999999);
			writeD(-999999999);
			writeD(-999999999);
			writeD(-999999999); // one more to change the UserInfo packet size
		}
		
		writeD(_player.getObjectId());
		writeS(_player.getName());
		writeD(_player.getRace().ordinal());
		writeD(_player.getAppearance().isFemale() ? 1 : 0);
		
		if (_player.getClassIndex() == 0)
		{
			writeD(_player.getClassId().getId());
		}
		else
		{
			writeD(_player.getBaseClass());
		}
		
		writeD(_player.getLevel());
		writeQ(_player.getExp());
		writeD(_player.getSTR());
		writeD(_player.getDEX());
		writeD(_player.getCON());
		writeD(_player.getINT());
		writeD(_player.getWIT());
		writeD(_player.getMEN());
		writeD(_player.getMaxHp());
		writeD((int) _player.getCurrentHp());
		writeD(_player.getMaxMp());
		writeD((int) _player.getCurrentMp());
		writeD(_player.getSp());
		writeD(_player.getCurrentLoad());
		writeD(_player.getMaxLoad());
		
		writeD(_player.getActiveWeaponItem() != null ? 40 : 20); // 20 no weapon, 40 weapon equippe
		
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DHAIR));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
		
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FACE));
		
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeD(_player.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeD(_player.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LRHAND));
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		
		writeD(_player.getPAtk(null));
		writeD(_player.getPAtkSpd());
		writeD(_player.getPDef(null));
		writeD(_player.getEvasionRate(null));
		writeD(_player.getAccuracy());
		writeD(_player.getCriticalHit(null, null));
		writeD(_player.getMAtk(null, null));
		
		writeD(_player.getMAtkSpd());
		writeD(_player.getPAtkSpd());
		
		writeD(_player.getMDef(null, null));
		
		writeD(_player.getPvpFlag()); // 0-non-pvp 1-pvp = violett name
		writeD(_player.getKarma());
		
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd); // swimspeed
		writeD(_swimWalkSpd); // swimspeed
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(_moveMultiplier);
		writeF(_player.getAttackSpeedMultiplier());
		
		final Summon pet = _player.getPet();
		if ((_player.getMountType() != 0) && (pet != null))
		{
			writeF(pet.getTemplate().getCollisionRadius());
			writeF(pet.getTemplate().getCollisionHeight());
		}
		else
		{
			writeF(_player.getBaseTemplate().getCollisionRadius());
			writeF(_player.getBaseTemplate().getCollisionHeight());
		}
		
		writeD(_player.getAppearance().getHairStyle());
		writeD(_player.getAppearance().getHairColor());
		writeD(_player.getAppearance().getFace());
		writeD(_player.isGM() ? 1 : 0); // builder level
		
		String title = _player.getTitle();
		if (_player.getAppearance().isInvisible() && _player.isGM())
		{
			title = "[Invisible]";
		}
		if (_player.getPoly().isMorphed())
		{
			final NpcTemplate polyObj = NpcTable.getInstance().getTemplate(_player.getPoly().getPolyId());
			if (polyObj != null)
			{
				title += " - " + polyObj.getName();
			}
		}
		writeS(title);
		
		writeD(_player.getClanId());
		writeD(_player.getClanCrestId());
		writeD(_player.getAllyId());
		writeD(_player.getAllyCrestId()); // ally crest id
		// 0x40 leader rights
		// siege flags: attacker - 0x180 sword over name, defender - 0x80 shield, 0xC0 crown (|leader), 0x1C0 flag (|leader)
		writeD(_relation);
		writeC(_player.getMountType()); // mount type
		writeC(_player.getPrivateStoreType());
		writeC(_player.hasDwarvenCraft() ? 1 : 0);
		writeD(_player.getPkKills());
		writeD(_player.getPvpKills());
		
		final Map<Integer, CubicInstance> cubics = _player.getCubics();
		writeH(cubics.size());
		for (Integer id : cubics.keySet())
		{
			writeH(id);
		}
		
		writeC(_player.isInPartyMatchRoom() ? 1 : 0);
		
		if (_player.getAppearance().isInvisible())
		{
			writeD((_player.getAbnormalEffect() | Creature.ABNORMAL_EFFECT_STEALTH));
		}
		else
		{
			writeD(_player.getAbnormalEffect()); // C2
		}
		
		writeC(0x00); // unk
		
		writeD(_player.getClanPrivileges());
		
		writeH(_player.getRecomLeft()); // c2 recommendations remaining
		writeH(_player.getRecomHave()); // c2 recommendations received
		writeD(0x00); // FIXME: MOUNT NPC ID
		writeH(_player.getInventoryLimit());
		
		writeD(_player.getClassId().getId());
		writeD(0x00); // FIXME: special effects? circles around player...
		writeD(_player.getMaxCp());
		writeD((int) _player.getCurrentCp());
		writeC(_player.isMounted() ? 0 : _player.getEnchantEffect());
		
		if (_player.getTeam() == 1)
		{
			writeC(0x01); // team circle around feet 1= Blue, 2 = red
		}
		else if (_player.getTeam() == 2)
		{
			writeC(0x02); // team circle around feet 1= Blue, 2 = red
		}
		else
		{
			writeC(0x00); // team circle around feet 1= Blue, 2 = red
		}
		
		writeD(_player.getClanCrestLargeId());
		writeC(_player.isNoble() ? 1 : 0); // 0x01: symbol on char menu ctrl+I
		writeC((_player.isHero() || (_player.isGM() && Config.GM_HERO_AURA) || _player.getIsPVPHero()) ? 1 : 0); // 0x01: Hero Aura
		
		writeC(_player.isFishing() ? 1 : 0); // Fishing Mode
		writeD(_player.GetFishx()); // fishing x
		writeD(_player.GetFishy()); // fishing y
		writeD(_player.GetFishz()); // fishing z
		writeD(_player.getAppearance().getNameColor());
		
		writeC(_player.isRunning() ? 0x01 : 0x00); // changes the Speed display on Status Window
		
		writeD(_player.getPledgeClass()); // changes the text above CP on Status Window
		writeD(_player.getPledgeType()); // TODO: PLEDGE TYPE
		
		writeD(_player.getAppearance().getTitleColor());
		
		if (_player.isCursedWeaponEquiped())
		{
			writeD(CursedWeaponsManager.getInstance().getLevel(_player.getCursedWeaponEquipedId()));
		}
		else
		{
			writeD(0x00);
		}
	}
}
