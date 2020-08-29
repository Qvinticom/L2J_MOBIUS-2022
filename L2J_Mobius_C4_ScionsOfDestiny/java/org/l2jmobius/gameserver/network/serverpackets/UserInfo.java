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

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;

public class UserInfo extends GameServerPacket
{
	private final PlayerInstance _player;
	private final Inventory _inventory;
	private final float _moveMultiplier;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private int _relation;
	
	public UserInfo(PlayerInstance player)
	{
		_player = player;
		_inventory = player.getInventory();
		_moveMultiplier = player.getMovementSpeedMultiplier();
		_runSpd = Math.round(player.getRunSpeed() / _moveMultiplier);
		_walkSpd = Math.round(player.getWalkSpeed() / _moveMultiplier);
		_flyRunSpd = player.isFlying() ? _runSpd : 0;
		_flyWalkSpd = player.isFlying() ? _walkSpd : 0;
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
		
		writeD(_player.getX());
		writeD(_player.getY());
		writeD(_player.getZ());
		writeD(_player.getBoat() != null ? _player.getBoat().getObjectId() : 0);
		
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
		
		writeD(_player.getActiveWeaponItem() != null ? 40 : 20); // 20 no weapon, 40 weapon equipped
		
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_UNDER));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
		writeD(_inventory.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_UNDER));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
		writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		
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
		
		writeD(_runSpd); // base run speed
		writeD(_walkSpd); // base walk speed
		writeD(_runSpd); // swim run speed (calculated by getter)
		writeD(_walkSpd); // swim walk speed (calculated by getter)
		writeD(0);
		writeD(0);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(_player.getMovementSpeedMultiplier()); // run speed multiplier
		writeF(_player.getAttackSpeedMultiplier()); // attack speed multiplier
		writeF(_player.getCollisionRadius());
		writeF(_player.getCollisionHeight());
		writeD(_player.getAppearance().getHairStyle());
		writeD(_player.getAppearance().getHairColor());
		writeD(_player.getAppearance().getFace());
		writeD(_player.isGM() ? 1 : 0); // builder level
		
		String title = _player.getTitle();
		if (_player.getAppearance().isInvisible() && _player.isGM())
		{
			title = "[Invisible]";
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
		
		writeH(_player.getCubics().size());
		for (int cubicId : _player.getCubics().keySet())
		{
			writeH(cubicId);
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
		
		writeC(0x00);
		
		writeD(_player.getClanPrivileges());
		
		// C4 addition
		writeD(0x00); // swim?
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		// C4 addition end
		
		writeH(_player.getRecomLeft()); // c2 recommendations remaining
		writeH(_player.getRecomHave()); // c2 recommendations received
		writeD(0x00); // _player.getMountNpcId() > 0 ? _player.getMountNpcId() + 1000000 : 0
		writeH(_player.getInventoryLimit());
		
		writeD(_player.getClassId().getId());
		writeD(0x00); // special effects? circles around player...
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
		writeC((_player.isHero() || (_player.isGM() && Config.GM_HERO_AURA) || _player.isPVPHero()) ? 1 : 0); // 0x01: Hero Aura
		
		writeC(_player.isFishing() ? 1 : 0); // Fishing Mode
		writeD(_player.getFishX()); // fishing x
		writeD(_player.getFishY()); // fishing y
		writeD(_player.getFishZ()); // fishing z
		writeD(_player.getAppearance().getNameColor());
		
		// Add heading?
	}
}
