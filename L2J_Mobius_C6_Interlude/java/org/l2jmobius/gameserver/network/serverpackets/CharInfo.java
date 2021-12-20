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
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class CharInfo implements IClientOutgoingPacket
{
	private final Player _player;
	private final Inventory _inventory;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final float _moveMultiplier;
	private final boolean _gmSeeInvis;
	
	public CharInfo(Player player, boolean gmSeeInvis)
	{
		_player = player;
		_inventory = player.getInventory();
		_moveMultiplier = player.getMovementSpeedMultiplier();
		_runSpd = Math.round(player.getRunSpeed() / _moveMultiplier);
		_walkSpd = Math.round(player.getWalkSpeed() / _moveMultiplier);
		_flyRunSpd = player.isFlying() ? _runSpd : 0;
		_flyWalkSpd = player.isFlying() ? _walkSpd : 0;
		_gmSeeInvis = gmSeeInvis;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.CHAR_INFO.writeId(packet);
		packet.writeD(_player.getX());
		packet.writeD(_player.getY());
		packet.writeD(_player.getZ());
		packet.writeD(_player.getBoat() != null ? _player.getBoat().getObjectId() : 0);
		packet.writeD(_player.getObjectId());
		packet.writeS(_player.getName());
		packet.writeD(_player.getRace().ordinal());
		packet.writeD(_player.getAppearance().isFemale() ? 1 : 0);
		if (_player.getClassIndex() == 0)
		{
			packet.writeD(_player.getClassId().getId());
		}
		else
		{
			packet.writeD(_player.getBaseClass());
		}
		packet.writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));
		packet.writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		packet.writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		packet.writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		packet.writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		packet.writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		packet.writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		packet.writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		packet.writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		packet.writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
		packet.writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		packet.writeD(_inventory.getPaperdollItemId(Inventory.PAPERDOLL_FACE));
		// c6 new h's
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeD(_inventory.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeD(_inventory.getPaperdollAugmentationId(Inventory.PAPERDOLL_LRHAND));
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeD(_player.getPvpFlag());
		packet.writeD(_player.getKarma());
		packet.writeD(_player.getMAtkSpd());
		packet.writeD(_player.getPAtkSpd());
		packet.writeD(_player.getPvpFlag());
		packet.writeD(_player.getKarma());
		packet.writeD(_runSpd); // base run speed
		packet.writeD(_walkSpd); // base walk speed
		packet.writeD(_runSpd); // swim run speed (calculated by getter)
		packet.writeD(_walkSpd); // swim walk speed (calculated by getter)
		packet.writeD(_flyRunSpd); // fly run speed ?
		packet.writeD(_flyWalkSpd); // fly walk speed ?
		packet.writeD(_flyRunSpd);
		packet.writeD(_flyWalkSpd);
		packet.writeF(_moveMultiplier);
		packet.writeF(_player.getAttackSpeedMultiplier());
		packet.writeF(_player.getCollisionRadius());
		packet.writeF(_player.getCollisionHeight());
		packet.writeD(_player.getAppearance().getHairStyle());
		packet.writeD(_player.getAppearance().getHairColor());
		packet.writeD(_player.getAppearance().getFace());
		packet.writeS(_gmSeeInvis ? "Invisible" : _player.getTitle());
		packet.writeD(_player.getClanId());
		packet.writeD(_player.getClanCrestId());
		packet.writeD(_player.getAllyId());
		packet.writeD(_player.getAllyCrestId());
		// In UserInfo leader rights and siege flags, but here found nothing??
		// Therefore RelationChanged packet with that info is required
		packet.writeD(0);
		packet.writeC(_player.isSitting() ? 0 : 1); // standing = 1 sitting = 0
		packet.writeC(_player.isRunning() ? 1 : 0); // running = 1 walking = 0
		packet.writeC(_player.isInCombat() ? 1 : 0);
		packet.writeC(_player.isAlikeDead() ? 1 : 0);
		packet.writeC(!_gmSeeInvis && _player.getAppearance().isInvisible() ? 1 : 0); // invisible = 1 visible = 0
		packet.writeC(_player.getMountType()); // 1 on strider 2 on wyvern 0 no mount
		packet.writeC(_player.getPrivateStoreType()); // 1 - sellshop
		
		packet.writeH(_player.getCubics().size());
		for (int cubicId : _player.getCubics().keySet())
		{
			packet.writeH(cubicId);
		}
		
		packet.writeC(_player.isInPartyMatchRoom() ? 1 : 0);
		packet.writeD(_gmSeeInvis ? (_player.getAbnormalEffect() | Creature.ABNORMAL_EFFECT_STEALTH) : _player.getAbnormalEffect());
		packet.writeC(_player.getRecomLeft());
		packet.writeH(_player.getRecomHave()); // Blue value for name (0 = white, 255 = pure blue)
		packet.writeD(_player.getClassId().getId());
		packet.writeD(_player.getMaxCp());
		packet.writeD((int) _player.getCurrentCp());
		packet.writeC(_player.isMounted() ? 0 : _player.getEnchantEffect());
		packet.writeC(_player.getTeam()); // team circle around feet 1 = Blue, 2 = red
		packet.writeD(_player.getClanCrestLargeId());
		packet.writeC(_player.isNoble() ? 1 : 0); // Symbol on char menu ctrl+I
		packet.writeC((_player.isHero() || (_player.isGM() && Config.GM_HERO_AURA) || _player.isPVPHero()) ? 1 : 0); // Hero Aura
		
		packet.writeC(_player.isFishing() ? 1 : 0); // 1: Fishing Mode (Cant be undone by setting back to 0)
		packet.writeD(_player.getFishX());
		packet.writeD(_player.getFishY());
		packet.writeD(_player.getFishZ());
		
		packet.writeD(_player.getAppearance().getNameColor());
		packet.writeD(_player.getHeading());
		packet.writeD(_player.getPledgeClass());
		packet.writeD(_player.getPledgeType());
		packet.writeD(_player.getAppearance().getTitleColor());
		if (_player.isCursedWeaponEquiped())
		{
			packet.writeD(CursedWeaponsManager.getInstance().getLevel(_player.getCursedWeaponEquipedId()));
		}
		else
		{
			packet.writeD(0);
		}
		return true;
	}
}
