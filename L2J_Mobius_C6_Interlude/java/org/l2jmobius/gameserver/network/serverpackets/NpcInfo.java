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
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.ControlTower;
import org.l2jmobius.gameserver.model.actor.instance.FortSiegeGuard;
import org.l2jmobius.gameserver.model.actor.instance.Guard;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.actor.instance.Servitor;
import org.l2jmobius.gameserver.model.actor.instance.SiegeGuard;
import org.l2jmobius.gameserver.model.actor.instance.SiegeNpc;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @version $Revision: 1.7.2.4.2.9 $ $Date: 2005/04/11 10:05:54 $
 */
public class NpcInfo implements IClientOutgoingPacket
{
	private Creature _creature;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private int _displayId;
	private boolean _isAttackable;
	private boolean _isSummoned;
	private int _mAtkSpd;
	private int _pAtkSpd;
	private int _runSpd;
	private int _walkSpd;
	private int _swimRunSpd;
	private int _swimWalkSpd;
	private int _flRunSpd;
	private int _flWalkSpd;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private int _rhand;
	private int _lhand;
	private float _collisionHeight;
	private float _collisionRadius;
	protected int _clanCrest;
	protected int _allyCrest;
	protected int _allyId;
	protected int _clanId;
	private String _name = "";
	private String _title = "";
	
	/**
	 * Instantiates a new npc info.
	 * @param cha the cha
	 * @param attacker the attacker
	 */
	public NpcInfo(Npc cha, Creature attacker)
	{
		if (cha.getFakePlayer() != null)
		{
			attacker.sendPacket(new FakePlayerInfo(cha));
			attacker.broadcastPacket(new StopRotation(cha, cha.getHeading(), 0));
			return;
		}
		_creature = cha;
		_displayId = cha.getTemplate().getDisplayId();
		_isAttackable = cha.isAutoAttackable(attacker);
		_rhand = cha.getRightHandItem();
		_lhand = cha.getLeftHandItem();
		_isSummoned = false;
		_collisionHeight = cha.getTemplate().getFCollisionHeight();
		_collisionRadius = cha.getTemplate().getFCollisionRadius();
		if (Config.SHOW_NPC_CLAN_CREST && (cha.getCastle() != null) && (cha.getCastle().getOwnerId() != 0) && !cha.isMonster() && !cha.isArtefact() && !(cha instanceof ControlTower))
		{
			if (cha.isInsideZone(ZoneId.TOWN) || cha.isInsideZone(ZoneId.CASTLE) //
				|| (cha instanceof Guard) || (cha instanceof SiegeGuard) || (cha instanceof FortSiegeGuard) || (cha instanceof SiegeNpc))
			{
				final Clan clan = ClanTable.getInstance().getClan(cha.getCastle().getOwnerId());
				_clanCrest = clan.getCrestId();
				_clanId = clan.getClanId();
				_allyCrest = clan.getAllyCrestId();
				_allyId = clan.getAllyId();
			}
		}
		if (cha.getTemplate().isServerSideName())
		{
			_name = cha.getTemplate().getName();
		}
		if (Config.CHAMPION_ENABLE && cha.isChampion())
		{
			_title = Config.CHAMP_TITLE;
		}
		else if (cha.getTemplate().isServerSideTitle())
		{
			_title = cha.getTemplate().getTitle();
		}
		else
		{
			_title = cha.getTitle();
		}
		// Custom level titles
		if (cha.isMonster() && (Config.SHOW_NPC_LEVEL || Config.SHOW_NPC_AGGRESSION))
		{
			String t1 = "";
			if (Config.SHOW_NPC_LEVEL)
			{
				t1 += "Lv " + cha.getLevel();
			}
			String t2 = "";
			if (Config.SHOW_NPC_AGGRESSION)
			{
				if (!t1.isEmpty())
				{
					t2 += " ";
				}
				final Monster monster = (Monster) cha;
				if (monster.isAggressive())
				{
					t2 += "[A]"; // Aggressive.
				}
			}
			t1 += t2;
			if ((_title != null) && !_title.isEmpty())
			{
				t1 += " " + _title;
			}
			_title = cha.isChampion() ? Config.CHAMP_TITLE + " " + t1 : t1;
		}
		_x = _creature.getX();
		_y = _creature.getY();
		_z = _creature.getZ();
		_heading = _creature.getHeading();
		_mAtkSpd = _creature.getMAtkSpd();
		_pAtkSpd = _creature.getPAtkSpd();
		_runSpd = _creature.getRunSpeed();
		_walkSpd = _creature.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}
	
	/**
	 * Instantiates a new npc info.
	 * @param cha the cha
	 * @param attacker the attacker
	 */
	public NpcInfo(Summon cha, Creature attacker)
	{
		_creature = cha;
		_displayId = cha.getTemplate().getDisplayId();
		_isAttackable = cha.isAutoAttackable(attacker); // (cha.getKarma() > 0);
		_rhand = 0;
		_lhand = 0;
		_isSummoned = cha.isShowSummonAnimation();
		_collisionHeight = _creature.getTemplate().getFCollisionHeight();
		_collisionRadius = _creature.getTemplate().getFCollisionRadius();
		if (cha.getTemplate().isServerSideName() || (cha instanceof Pet) || (cha instanceof Servitor))
		{
			_name = _creature.getName();
			_title = cha.getTitle();
		}
		_x = _creature.getX();
		_y = _creature.getY();
		_z = _creature.getZ();
		_heading = _creature.getHeading();
		_mAtkSpd = _creature.getMAtkSpd();
		_pAtkSpd = _creature.getPAtkSpd();
		_runSpd = _creature.getRunSpeed();
		_walkSpd = _creature.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if ((_creature == null) || ((_creature instanceof Summon) && (((Summon) _creature).getOwner() != null) && ((Summon) _creature).getOwner().getAppearance().isInvisible()))
		{
			return false;
		}
		OutgoingPackets.NPC_INFO.writeId(packet);
		packet.writeD(_creature.getObjectId());
		packet.writeD(_displayId + 1000000); // npctype id
		packet.writeD(_isAttackable ? 1 : 0);
		packet.writeD(_x);
		packet.writeD(_y);
		packet.writeD(_z);
		packet.writeD(_heading);
		packet.writeD(0);
		packet.writeD(_mAtkSpd);
		packet.writeD(_pAtkSpd);
		packet.writeD(_runSpd);
		packet.writeD(_walkSpd);
		packet.writeD(_swimRunSpd/* 0x32 */); // swimspeed
		packet.writeD(_swimWalkSpd/* 0x32 */); // swimspeed
		packet.writeD(_flRunSpd);
		packet.writeD(_flWalkSpd);
		packet.writeD(_flyRunSpd);
		packet.writeD(_flyWalkSpd);
		packet.writeF(1.1/* _activeChar.getProperMultiplier() */);
		// writeF(1/*_activeChar.getAttackSpeedMultiplier()*/);
		packet.writeF(_pAtkSpd / 277.478340719);
		packet.writeF(_collisionRadius);
		packet.writeF(_collisionHeight);
		packet.writeD(_rhand); // right hand weapon
		packet.writeD(0);
		packet.writeD(_lhand); // left hand weapon
		packet.writeC(1); // name above char 1=true ... ??
		packet.writeC(_creature.isRunning() ? 1 : 0);
		packet.writeC(_creature.isInCombat() ? 1 : 0);
		packet.writeC(_creature.isAlikeDead() ? 1 : 0);
		packet.writeC(_isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
		packet.writeS(_name);
		packet.writeS(_title);
		if (_creature instanceof Summon)
		{
			packet.writeD(1); // Title color 0=client default
			packet.writeD(((Summon) _creature).getPvpFlag());
			packet.writeD(((Summon) _creature).getKarma());
		}
		else
		{
			packet.writeD(0);
			packet.writeD(0);
			packet.writeD(0);
		}
		packet.writeD(_creature.getAbnormalEffect()); // C2
		packet.writeD(_clanId); // C2
		packet.writeD(_clanCrest); // C2
		packet.writeD(_allyId); // C2
		packet.writeD(_allyCrest); // C2
		packet.writeC(0); // C2
		if (Config.CHAMPION_ENABLE)
		{
			packet.writeC(_creature.isChampion() ? Config.CHAMPION_AURA : 0);
		}
		else
		{
			packet.writeC(0); // C3 team circle 1-blue, 2-red
		}
		packet.writeF(_collisionRadius);
		packet.writeF(_collisionHeight);
		packet.writeD(0); // C4
		packet.writeD(0); // C6
		return true;
	}
}
