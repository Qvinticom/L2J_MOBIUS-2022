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

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * TODO Add support for Eval. Score dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSddd rev420 dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccddhh rev478
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccddhhddd rev551
 * @version $Revision: 1.2.2.2.2.8 $ $Date: 2005/03/27 15:29:39 $
 */
public class GMViewCharacterInfo implements IClientOutgoingPacket
{
	/** The _active char. */
	private final Player _player;
	
	/**
	 * Instantiates a new GM view character info.
	 * @param player the player
	 */
	public GMViewCharacterInfo(Player player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		final float moveMultiplier = _player.getMovementSpeedMultiplier();
		final int runSpd = (int) (_player.getRunSpeed() / moveMultiplier);
		final int walkSpd = (int) (_player.getWalkSpeed() / moveMultiplier);
		
		OutgoingPackets.GM_VIEW_CHARACTER_INFO.writeId(packet);
		
		packet.writeD(_player.getX());
		packet.writeD(_player.getY());
		packet.writeD(_player.getZ());
		packet.writeD(_player.getHeading());
		packet.writeD(_player.getObjectId());
		packet.writeS(_player.getName());
		packet.writeD(_player.getRace().ordinal());
		packet.writeD(_player.getAppearance().isFemale() ? 1 : 0);
		packet.writeD(_player.getClassId().getId());
		packet.writeD(_player.getLevel());
		packet.writeQ(_player.getExp());
		packet.writeD(_player.getSTR());
		packet.writeD(_player.getDEX());
		packet.writeD(_player.getCON());
		packet.writeD(_player.getINT());
		packet.writeD(_player.getWIT());
		packet.writeD(_player.getMEN());
		packet.writeD(_player.getMaxHp());
		packet.writeD((int) _player.getCurrentHp());
		packet.writeD(_player.getMaxMp());
		packet.writeD((int) _player.getCurrentMp());
		packet.writeD(_player.getSp());
		packet.writeD(_player.getCurrentLoad());
		packet.writeD(_player.getMaxLoad());
		packet.writeD(0x28); // unknown
		
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DHAIR));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		packet.writeD(_player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
		
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		packet.writeD(_player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FACE));
		
		// c6 new h's
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		packet.writeH(0x00);
		// end of c6 new h's
		packet.writeD(_player.getPAtk(null));
		packet.writeD(_player.getPAtkSpd());
		packet.writeD(_player.getPDef(null));
		packet.writeD(_player.getEvasionRate(null));
		packet.writeD(_player.getAccuracy());
		packet.writeD(_player.getCriticalHit(null, null));
		packet.writeD(_player.getMAtk(null, null));
		
		packet.writeD(_player.getMAtkSpd());
		packet.writeD(_player.getPAtkSpd());
		
		packet.writeD(_player.getMDef(null, null));
		
		packet.writeD(_player.getPvpFlag()); // 0-non-pvp 1-pvp = violett name
		packet.writeD(_player.getKarma());
		
		packet.writeD(runSpd);
		packet.writeD(walkSpd);
		packet.writeD(runSpd); // swimspeed
		packet.writeD(walkSpd); // swimspeed
		packet.writeD(runSpd);
		packet.writeD(walkSpd);
		packet.writeD(runSpd);
		packet.writeD(walkSpd);
		packet.writeF(moveMultiplier);
		packet.writeF(_player.getAttackSpeedMultiplier()); // 2.9); //
		packet.writeF(_player.getTemplate().getCollisionRadius()); // scale
		packet.writeF(_player.getTemplate().getCollisionHeight()); // y offset ??!? fem dwarf 4033
		packet.writeD(_player.getAppearance().getHairStyle());
		packet.writeD(_player.getAppearance().getHairColor());
		packet.writeD(_player.getAppearance().getFace());
		packet.writeD(_player.isGM() ? 0x01 : 0x00); // builder level
		
		packet.writeS(_player.getTitle());
		packet.writeD(_player.getClanId()); // pledge id
		packet.writeD(_player.getClanCrestId()); // pledge crest id
		packet.writeD(_player.getAllyId()); // ally id
		packet.writeC(_player.getMountType()); // mount type
		packet.writeC(_player.getPrivateStoreType());
		packet.writeC(_player.hasDwarvenCraft() ? 1 : 0);
		packet.writeD(_player.getPkKills());
		packet.writeD(_player.getPvpKills());
		
		packet.writeH(_player.getRecomLeft());
		packet.writeH(_player.getRecomHave()); // Blue value for name (0 = white, 255 = pure blue)
		packet.writeD(_player.getClassId().getId());
		packet.writeD(0x00); // special effects? circles around player...
		packet.writeD(_player.getMaxCp());
		packet.writeD((int) _player.getCurrentCp());
		
		packet.writeC(_player.isRunning() ? 0x01 : 0x00); // changes the Speed display on Status Window
		
		packet.writeC(321);
		
		packet.writeD(_player.getPledgeClass()); // changes the text above CP on Status Window
		
		packet.writeC(_player.isNoble() ? 0x01 : 0x00);
		packet.writeC(_player.isHero() ? 0x01 : 0x00);
		
		packet.writeD(_player.getAppearance().getNameColor());
		packet.writeD(_player.getAppearance().getTitleColor());
		return true;
	}
}
