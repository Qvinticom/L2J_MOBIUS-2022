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
package quests.Q00070_SagaOfThePhoenixKnight;

import quests.SagasSuperClass;

/**
 * @author Emperorc
 */
public class Q00070_SagaOfThePhoenixKnight extends SagasSuperClass
{
	public static String qn1 = "Q00070_SagaOfThePhoenixKnight";
	public static int qnu = 70;
	public static String qna = "Saga of the Phoenix Knight";
	
	public Q00070_SagaOfThePhoenixKnight()
	{
		super(qnu, qn1, qna);
		NPC = new int[]
		{
			7849,
			8624,
			8277,
			7849,
			8631,
			8646,
			8647,
			8650,
			8654,
			8655,
			8657,
			8277
		};
		Items = new int[]
		{
			7080,
			7534,
			7081,
			7485,
			7268,
			7299,
			7330,
			7361,
			7392,
			7423,
			7093,
			6482
		};
		Mob = new int[]
		{
			5286,
			5219,
			5278
		};
		qn = qn1;
		classid = new int[]
		{
			90
		};
		prevclass = new int[]
		{
			0x05
		};
		X = new int[]
		{
			191046,
			46087,
			46066
		};
		Y = new int[]
		{
			-40640,
			-36372,
			-36396
		};
		Z = new int[]
		{
			-3042,
			-1685,
			-1685
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
		new Q00070_SagaOfThePhoenixKnight();
	}
}