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
package org.l2jmobius.gameserver.templates.creatures;

import org.l2jmobius.gameserver.model.StatsSet;

/**
 * @version $Revision: 1.2.4.6 $ $Date: 2005/04/02 15:57:51 $
 */
public class CreatureTemplate
{
	// BaseStats
	public int baseSTR;
	public int baseCON;
	public int baseDEX;
	public int baseINT;
	public int baseWIT;
	public int baseMEN;
	public float baseHpMax;
	public float baseCpMax;
	public float baseMpMax;
	
	/** HP Regen base */
	public float baseHpReg;
	
	/** MP Regen base */
	public float baseMpReg;
	
	public int basePAtk;
	public int baseMAtk;
	public int basePDef;
	public int baseMDef;
	public int basePAtkSpd;
	public int baseMAtkSpd;
	public float baseMReuseRate;
	public int baseShldDef;
	public int baseAtkRange;
	public int baseShldRate;
	public int baseCritRate;
	public int baseMCritRate;
	public int baseWalkSpd;
	public int baseRunSpd;
	
	// SpecialStats
	public int baseBreath;
	public int baseAggression;
	public int baseBleed;
	public int basePoison;
	public int baseStun;
	public int baseRoot;
	public int baseMovement;
	public int baseConfusion;
	public int baseSleep;
	public int baseFire;
	public int baseWind;
	public int baseWater;
	public int baseEarth;
	public int baseHoly;
	public int baseDark;
	public double baseAggressionVuln;
	public double baseBleedVuln;
	public double basePoisonVuln;
	public double baseStunVuln;
	public double baseRootVuln;
	public double baseMovementVuln;
	public double baseConfusionVuln;
	public double baseSleepVuln;
	public double baseFireVuln;
	public double baseWindVuln;
	public double baseWaterVuln;
	public double baseEarthVuln;
	public double baseHolyVuln;
	public double baseDarkVuln;
	public double baseCritVuln;
	
	public boolean isUndead;
	
	// C4 Stats
	public int baseMpConsumeRate;
	public int baseHpConsumeRate;
	
	public int collisionRadius;
	public int collisionHeight;
	
	public CreatureTemplate(StatsSet set)
	{
		// Base stats
		baseSTR = set.getInt("baseSTR");
		baseCON = set.getInt("baseCON");
		baseDEX = set.getInt("baseDEX");
		baseINT = set.getInt("baseINT");
		baseWIT = set.getInt("baseWIT");
		baseMEN = set.getInt("baseMEN");
		baseHpMax = set.getFloat("baseHpMax");
		baseCpMax = set.getFloat("baseCpMax");
		baseMpMax = set.getFloat("baseMpMax");
		baseHpReg = set.getFloat("baseHpReg");
		baseMpReg = set.getFloat("baseMpReg");
		basePAtk = set.getInt("basePAtk");
		baseMAtk = set.getInt("baseMAtk");
		basePDef = set.getInt("basePDef");
		baseMDef = set.getInt("baseMDef");
		basePAtkSpd = set.getInt("basePAtkSpd");
		baseMAtkSpd = set.getInt("baseMAtkSpd");
		baseMReuseRate = set.getFloat("baseMReuseDelay", 1.f);
		baseShldDef = set.getInt("baseShldDef");
		baseAtkRange = set.getInt("baseAtkRange");
		baseShldRate = set.getInt("baseShldRate");
		baseCritRate = set.getInt("baseCritRate");
		baseMCritRate = set.getInt("baseMCritRate", 5);
		baseWalkSpd = set.getInt("baseWalkSpd");
		baseRunSpd = set.getInt("baseRunSpd");
		
		// SpecialStats
		baseBreath = set.getInt("baseBreath", 100);
		baseAggression = set.getInt("baseAggression", 0);
		baseBleed = set.getInt("baseBleed", 0);
		basePoison = set.getInt("basePoison", 0);
		baseStun = set.getInt("baseStun", 0);
		baseRoot = set.getInt("baseRoot", 0);
		baseMovement = set.getInt("baseMovement", 0);
		baseConfusion = set.getInt("baseConfusion", 0);
		baseSleep = set.getInt("baseSleep", 0);
		baseFire = set.getInt("baseFire", 0);
		baseWind = set.getInt("baseWind", 0);
		baseWater = set.getInt("baseWater", 0);
		baseEarth = set.getInt("baseEarth", 0);
		baseHoly = set.getInt("baseHoly", 0);
		baseDark = set.getInt("baseDark", 0);
		baseAggressionVuln = set.getInt("baseAaggressionVuln", 1);
		baseBleedVuln = set.getInt("baseBleedVuln", 1);
		basePoisonVuln = set.getInt("basePoisonVuln", 1);
		baseStunVuln = set.getInt("baseStunVuln", 1);
		baseRootVuln = set.getInt("baseRootVuln", 1);
		baseMovementVuln = set.getInt("baseMovementVuln", 1);
		baseConfusionVuln = set.getInt("baseConfusionVuln", 1);
		baseSleepVuln = set.getInt("baseSleepVuln", 1);
		baseFireVuln = set.getInt("baseFireVuln", 1);
		baseWindVuln = set.getInt("baseWindVuln", 1);
		baseWaterVuln = set.getInt("baseWaterVuln", 1);
		baseEarthVuln = set.getInt("baseEarthVuln", 1);
		baseHolyVuln = set.getInt("baseHolyVuln", 1);
		baseDarkVuln = set.getInt("baseDarkVuln", 1);
		baseCritVuln = set.getInt("baseCritVuln", 1);
		
		isUndead = set.getInt("isUndead", 0) == 1;
		
		// C4 Stats
		baseMpConsumeRate = set.getInt("baseMpConsumeRate", 0);
		baseHpConsumeRate = set.getInt("baseHpConsumeRate", 0);
		
		// Geometry
		collisionRadius = set.getInt("collision_radius");
		collisionHeight = set.getInt("collision_height");
	}
}
