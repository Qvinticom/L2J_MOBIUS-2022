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
package com.l2jmobius.gameserver.enums;

/**
 * This file contains all movies.
 * @author St3eT
 */
public enum Movie
{
	SC_LINDVIOR(1, true),
	SC_ECHMUS_OPENING(2, true),
	SC_ECHMUS_SUCCESS(3, true),
	SC_ECHMUS_FAIL(4, true),
	SC_BOSS_TIAT_OPENING(5, true),
	SC_BOSS_TIAT_ENDING_SUCCES(6, true),
	SC_BOSS_TIAT_ENDING_FAIL(7, true),
	SSQ_SUSPICIOUS_DEATHS(8, true),
	SSQ_DYING_MASSAGE(9, true),
	SSQ_CONTRACT_OF_MAMMON(10, true),
	SSQ_RITUAL_OF_PRIEST(11, true),
	SSQ_SEALING_EMPEROR_1ST(12, true),
	SSQ_SEALING_EMPEROR_2ND(13, true),
	SSQ_EMBRYO(14, true),
	SC_BOSS_FREYA_OPENING(15, false),
	SC_BOSS_FREYA_PHASECH_A(16, true),
	SC_BOSS_FREYA_PHASECH_B(17, true),
	SC_BOSS_KEGOR_INTRUSION(18, false),
	SC_BOSS_FREYA_ENDING_A(19, false),
	SC_BOSS_FREYA_ENDING_B(20, false),
	SC_BOSS_FREYA_FORCED_DEFEAT(21, true),
	SC_BOSS_FREYA_DEFEAT(22, true),
	SC_ICE_HEAVYKNIGHT_SPAWN(23, false),
	SSQ2_HOLY_BURIAL_GROUND_OPENING(24, true),
	SSQ2_HOLY_BURIAL_GROUND_CLOSING(25, true),
	SSQ2_SOLINA_TOMB_OPENING(26, false),
	SSQ2_SOLINA_TOMB_CLOSING(27, true),
	SSQ2_ELYSS_NARRATION(28, false),
	SSQ2_BOSS_OPENING(29, false),
	SSQ2_BOSS_CLOSING(30, false),
	LAND_KSERTH_A(1000, true),
	LAND_KSERTH_B(1001, true),
	LAND_UNDEAD_A(1002, true),
	LAND_DISTRUCTION_A(1003, true),
	LAND_ANNIHILATION_A(1004, true);
	
	private final int _clientId;
	private final boolean _isEscapable;
	
	private Movie(int clientId, boolean isEscapable)
	{
		_clientId = clientId;
		_isEscapable = isEscapable;
	}
	
	/**
	 * @return the client id.
	 */
	public int getClientId()
	{
		return _clientId;
	}
	
	/**
	 * @return {@code true} if movie can be escaped (skipped), {@code false} otherwise.
	 */
	public boolean isEscapable()
	{
		return _isEscapable;
	}
}