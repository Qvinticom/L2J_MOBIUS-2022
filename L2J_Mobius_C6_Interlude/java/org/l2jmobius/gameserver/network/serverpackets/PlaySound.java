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
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class PlaySound implements IClientOutgoingPacket
{
	private static final Location DUMMY_LOC = new Location(0, 0, 0);
	
	private final int _unknown;
	private final String _soundFile;
	private final boolean _isObject;
	private final int _objectId;
	private final Location _loc;
	private final int _duration;
	
	/**
	 * Used for static sound.
	 * @param soundFile : The name of the sound file.
	 */
	public PlaySound(String soundFile)
	{
		_unknown = 0;
		_soundFile = soundFile;
		_isObject = false;
		_objectId = 0;
		_loc = DUMMY_LOC;
		_duration = 0;
	}
	
	/**
	 * Used for static sound.
	 * @param unknown : Unknown parameter. Seems linked to sound names with dots (.), tutorials, sieges/bosses.
	 * @param soundFile : The name of the sound file.
	 */
	public PlaySound(int unknown, String soundFile)
	{
		_unknown = unknown;
		_soundFile = soundFile;
		_isObject = false;
		_objectId = 0;
		_loc = DUMMY_LOC;
		_duration = 0;
	}
	
	/**
	 * Play the sound file in the client. We use a {@link WorldObject} as parameter, notably to find the position of the sound.
	 * @param unknown
	 * @param soundFile : The name of the sound file.
	 * @param object : The object to use.
	 */
	public PlaySound(int unknown, String soundFile, WorldObject object)
	{
		_unknown = unknown;
		_soundFile = soundFile;
		_isObject = true;
		_objectId = object.getObjectId();
		_loc = object.getLocation();
		_duration = 0;
	}
	
	/**
	 * Play the sound file in the client. All parameters can be set.
	 * @param unknown
	 * @param soundFile : The name of the sound file.
	 * @param isObject - true, if sound file calls someone else, but not character
	 * @param objectId - object ID of caller. 0 - for quest, tutorial, etc.
	 * @param loc - Location of object
	 * @param duration - playing time
	 */
	public PlaySound(int unknown, String soundFile, boolean isObject, int objectId, Location loc, int duration)
	{
		_unknown = unknown;
		_soundFile = soundFile;
		_isObject = isObject;
		_objectId = objectId;
		_loc = loc;
		_duration = duration;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PLAY_SOUND.writeId(packet);
		packet.writeD(_unknown);
		packet.writeS(_soundFile);
		packet.writeD(_isObject ? 1 : 0);
		packet.writeD(_objectId);
		packet.writeD(_loc.getX());
		packet.writeD(_loc.getY());
		packet.writeD(_loc.getZ());
		packet.writeD(_duration);
		return true;
	}
}
