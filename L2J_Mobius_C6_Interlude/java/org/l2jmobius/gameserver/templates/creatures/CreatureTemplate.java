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

import org.l2jmobius.gameserver.templates.StatsSet;

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
		baseSTR = set.getInteger("baseSTR");
		baseCON = set.getInteger("baseCON");
		baseDEX = set.getInteger("baseDEX");
		baseINT = set.getInteger("baseINT");
		baseWIT = set.getInteger("baseWIT");
		baseMEN = set.getInteger("baseMEN");
		baseHpMax = set.getFloat("baseHpMax");
		baseCpMax = set.getFloat("baseCpMax");
		baseMpMax = set.getFloat("baseMpMax");
		baseHpReg = set.getFloat("baseHpReg");
		baseMpReg = set.getFloat("baseMpReg");
		basePAtk = set.getInteger("basePAtk");
		baseMAtk = set.getInteger("baseMAtk");
		basePDef = set.getInteger("basePDef");
		baseMDef = set.getInteger("baseMDef");
		basePAtkSpd = set.getInteger("basePAtkSpd");
		baseMAtkSpd = set.getInteger("baseMAtkSpd");
		baseMReuseRate = set.getFloat("baseMReuseDelay", 1.f);
		baseShldDef = set.getInteger("baseShldDef");
		baseAtkRange = set.getInteger("baseAtkRange");
		baseShldRate = set.getInteger("baseShldRate");
		baseCritRate = set.getInteger("baseCritRate");
		baseMCritRate = set.getInteger("baseMCritRate", 5);
		baseWalkSpd = set.getInteger("baseWalkSpd");
		baseRunSpd = set.getInteger("baseRunSpd");
		
		// SpecialStats
		baseBreath = set.getInteger("baseBreath", 100);
		baseAggression = set.getInteger("baseAggression", 0);
		baseBleed = set.getInteger("baseBleed", 0);
		basePoison = set.getInteger("basePoison", 0);
		baseStun = set.getInteger("baseStun", 0);
		baseRoot = set.getInteger("baseRoot", 0);
		baseMovement = set.getInteger("baseMovement", 0);
		baseConfusion = set.getInteger("baseConfusion", 0);
		baseSleep = set.getInteger("baseSleep", 0);
		baseFire = set.getInteger("baseFire", 0);
		baseWind = set.getInteger("baseWind", 0);
		baseWater = set.getInteger("baseWater", 0);
		baseEarth = set.getInteger("baseEarth", 0);
		baseHoly = set.getInteger("baseHoly", 0);
		baseDark = set.getInteger("baseDark", 0);
		baseAggressionVuln = set.getInteger("baseAaggressionVuln", 1);
		baseBleedVuln = set.getInteger("baseBleedVuln", 1);
		basePoisonVuln = set.getInteger("basePoisonVuln", 1);
		baseStunVuln = set.getInteger("baseStunVuln", 1);
		baseRootVuln = set.getInteger("baseRootVuln", 1);
		baseMovementVuln = set.getInteger("baseMovementVuln", 1);
		baseConfusionVuln = set.getInteger("baseConfusionVuln", 1);
		baseSleepVuln = set.getInteger("baseSleepVuln", 1);
		baseFireVuln = set.getInteger("baseFireVuln", 1);
		baseWindVuln = set.getInteger("baseWindVuln", 1);
		baseWaterVuln = set.getInteger("baseWaterVuln", 1);
		baseEarthVuln = set.getInteger("baseEarthVuln", 1);
		baseHolyVuln = set.getInteger("baseHolyVuln", 1);
		baseDarkVuln = set.getInteger("baseDarkVuln", 1);
		baseCritVuln = set.getInteger("baseCritVuln", 1);
		
		isUndead = set.getInteger("isUndead", 0) == 1;
		
		// C4 Stats
		baseMpConsumeRate = set.getInteger("baseMpConsumeRate", 0);
		baseHpConsumeRate = set.getInteger("baseHpConsumeRate", 0);
		
		// Geometry
		collisionRadius = set.getInteger("collision_radius");
		collisionHeight = set.getInteger("collision_height");
	}
}
