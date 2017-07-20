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
package quests.Q00081_SagaOfTheGhostHunter;

import quests.SagasSuperClass;

/**
 * @author Emperorc
 */
public class Q00081_SagaOfTheGhostHunter extends SagasSuperClass
{
	public static String qn1 = "Q00081_SagaOfTheGhostHunter";
	public static int qnu = 81;
	public static String qna = "Saga of the Ghost Hunter";
	
	public Q00081_SagaOfTheGhostHunter()
	{
		super(qnu, qn1, qna);
		NPC = new int[]
		{
			8603,
			8624,
			8286,
			8615,
			8617,
			8646,
			8649,
			8653,
			8654,
			8655,
			8656,
			8616
		};
		Items = new int[]
		{
			7080,
			7518,
			7081,
			7496,
			7279,
			7310,
			7341,
			7372,
			7403,
			7434,
			7104,
			0
		};
		Mob = new int[]
		{
			5301,
			5230,
			5304
		};
		qn = qn1;
		classid = new int[]
		{
			108
		};
		prevclass = new int[]
		{
			0x24
		};
		X = new int[]
		{
			164650,
			47391,
			47429
		};
		Y = new int[]
		{
			-74121,
			-56929,
			-56923
		};
		Z = new int[]
		{
			-2871,
			-2370,
			-2383
		};
		Text = new String[]
		{
			"PLAYERNAME! Pursued to here! However, I jumped out of the Banshouren boundaries! You look at the giant as the sign of power!",
			"... Oh ... good! So it was ... let's begin!",
			"I do not have the patience ..! I have been a giant force ...! Cough chatter ah ah ah!",
			"Paying homage to those who disrupt the orderly will be PLAYERNAME's death!",
			"Now, my soul freed from the shackles of the millennium, Halixia, to the back side I come ...",
			"Why do you interfere others' battles?",
			"This is a waste of time.. Say goodbye...!",
			"...That is the enemy",
			"...Goodness! PLAYERNAME you are still looking?",
			"PLAYERNAME ... Not just to whom the victory. Only personnel involved in the fighting are eligible to share in the victory.",
			"Your sword is not an ornament. Don't you think, PLAYERNAME?",
			"Goodness! I no longer sense a battle there now.",
			"let...",
			"Only engaged in the battle to bar their choice. Perhaps you should regret.",
			"The human nation was foolish to try and fight a giant's strength.",
			"Must...Retreat... Too...Strong.",
			"PLAYERNAME. Defeat...by...retaining...and...Mo...Hacker",
			"....! Fight...Defeat...It...Fight...Defeat...It..."
		};
		registerNPCs();
	}
	
	public static void main(String[] args)
	{
		new Q00081_SagaOfTheGhostHunter();
	}
}