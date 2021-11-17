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
package org.l2jmobius.gameserver.enums;

import static org.l2jmobius.gameserver.enums.ClassLevel.FIRST;
import static org.l2jmobius.gameserver.enums.ClassLevel.FOURTH;
import static org.l2jmobius.gameserver.enums.ClassLevel.SECOND;
import static org.l2jmobius.gameserver.enums.ClassLevel.THIRD;
import static org.l2jmobius.gameserver.enums.ClassType.Fighter;
import static org.l2jmobius.gameserver.enums.ClassType.Mystic;
import static org.l2jmobius.gameserver.enums.ClassType.Priest;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import org.l2jmobius.gameserver.model.actor.Player;

/**
 * @author luisantonioa
 */
public enum PlayerClass
{
	HumanFighter(Race.HUMAN, Fighter, FIRST),
	Warrior(Race.HUMAN, Fighter, SECOND),
	Gladiator(Race.HUMAN, Fighter, THIRD),
	Warlord(Race.HUMAN, Fighter, THIRD),
	HumanKnight(Race.HUMAN, Fighter, SECOND),
	Paladin(Race.HUMAN, Fighter, THIRD),
	DarkAvenger(Race.HUMAN, Fighter, THIRD),
	Rogue(Race.HUMAN, Fighter, SECOND),
	TreasureHunter(Race.HUMAN, Fighter, THIRD),
	Hawkeye(Race.HUMAN, Fighter, THIRD),
	HumanMystic(Race.HUMAN, Mystic, FIRST),
	HumanWizard(Race.HUMAN, Mystic, SECOND),
	Sorceror(Race.HUMAN, Mystic, THIRD),
	Necromancer(Race.HUMAN, Mystic, THIRD),
	Warlock(Race.HUMAN, Mystic, THIRD),
	Cleric(Race.HUMAN, Priest, SECOND),
	Bishop(Race.HUMAN, Priest, THIRD),
	Prophet(Race.HUMAN, Priest, THIRD),
	
	ElvenFighter(Race.ELF, Fighter, FIRST),
	ElvenKnight(Race.ELF, Fighter, SECOND),
	TempleKnight(Race.ELF, Fighter, THIRD),
	Swordsinger(Race.ELF, Fighter, THIRD),
	ElvenScout(Race.ELF, Fighter, SECOND),
	Plainswalker(Race.ELF, Fighter, THIRD),
	SilverRanger(Race.ELF, Fighter, THIRD),
	ElvenMystic(Race.ELF, Mystic, FIRST),
	ElvenWizard(Race.ELF, Mystic, SECOND),
	Spellsinger(Race.ELF, Mystic, THIRD),
	ElementalSummoner(Race.ELF, Mystic, THIRD),
	ElvenOracle(Race.ELF, Priest, SECOND),
	ElvenElder(Race.ELF, Priest, THIRD),
	
	DarkElvenFighter(Race.DARK_ELF, Fighter, FIRST),
	PalusKnight(Race.DARK_ELF, Fighter, SECOND),
	ShillienKnight(Race.DARK_ELF, Fighter, THIRD),
	Bladedancer(Race.DARK_ELF, Fighter, THIRD),
	Assassin(Race.DARK_ELF, Fighter, SECOND),
	AbyssWalker(Race.DARK_ELF, Fighter, THIRD),
	PhantomRanger(Race.DARK_ELF, Fighter, THIRD),
	DarkElvenMystic(Race.DARK_ELF, Mystic, FIRST),
	DarkElvenWizard(Race.DARK_ELF, Mystic, SECOND),
	Spellhowler(Race.DARK_ELF, Mystic, THIRD),
	PhantomSummoner(Race.DARK_ELF, Mystic, THIRD),
	ShillienOracle(Race.DARK_ELF, Priest, SECOND),
	ShillienElder(Race.DARK_ELF, Priest, THIRD),
	
	OrcFighter(Race.ORC, Fighter, FIRST),
	OrcRaider(Race.ORC, Fighter, SECOND),
	Destroyer(Race.ORC, Fighter, THIRD),
	OrcMonk(Race.ORC, Fighter, SECOND),
	Tyrant(Race.ORC, Fighter, THIRD),
	OrcMystic(Race.ORC, Mystic, FIRST),
	OrcShaman(Race.ORC, Mystic, SECOND),
	Overlord(Race.ORC, Mystic, THIRD),
	Warcryer(Race.ORC, Mystic, THIRD),
	
	DwarvenFighter(Race.DWARF, Fighter, FIRST),
	DwarvenScavenger(Race.DWARF, Fighter, SECOND),
	BountyHunter(Race.DWARF, Fighter, THIRD),
	DwarvenArtisan(Race.DWARF, Fighter, SECOND),
	Warsmith(Race.DWARF, Fighter, THIRD),
	
	// TODO: Diminish the use of PlayerClass.values() and drop this class.
	dummyEntry1(null, null, null),
	dummyEntry2(null, null, null),
	dummyEntry3(null, null, null),
	dummyEntry4(null, null, null),
	dummyEntry5(null, null, null),
	dummyEntry6(null, null, null),
	dummyEntry7(null, null, null),
	dummyEntry8(null, null, null),
	dummyEntry9(null, null, null),
	dummyEntry10(null, null, null),
	dummyEntry11(null, null, null),
	dummyEntry12(null, null, null),
	dummyEntry13(null, null, null),
	dummyEntry14(null, null, null),
	dummyEntry15(null, null, null),
	dummyEntry16(null, null, null),
	dummyEntry17(null, null, null),
	dummyEntry18(null, null, null),
	dummyEntry19(null, null, null),
	dummyEntry20(null, null, null),
	dummyEntry21(null, null, null),
	dummyEntry22(null, null, null),
	dummyEntry23(null, null, null),
	dummyEntry24(null, null, null),
	dummyEntry25(null, null, null),
	dummyEntry26(null, null, null),
	dummyEntry27(null, null, null),
	dummyEntry28(null, null, null),
	dummyEntry29(null, null, null),
	dummyEntry30(null, null, null),
	
	/*
	 * (3rd classes)
	 */
	duelist(Race.HUMAN, Fighter, FOURTH),
	dreadnought(Race.HUMAN, Fighter, FOURTH),
	phoenixKnight(Race.HUMAN, Fighter, FOURTH),
	hellKnight(Race.HUMAN, Fighter, FOURTH),
	sagittarius(Race.HUMAN, Fighter, FOURTH),
	adventurer(Race.HUMAN, Fighter, FOURTH),
	archmage(Race.HUMAN, Mystic, FOURTH),
	soultaker(Race.HUMAN, Mystic, FOURTH),
	arcanaLord(Race.HUMAN, Mystic, FOURTH),
	cardinal(Race.HUMAN, Priest, FOURTH),
	hierophant(Race.HUMAN, Priest, FOURTH),
	
	evaTemplar(Race.ELF, Fighter, FOURTH),
	swordMuse(Race.ELF, Fighter, FOURTH),
	windRider(Race.ELF, Fighter, FOURTH),
	moonlightSentinel(Race.ELF, Fighter, FOURTH),
	mysticMuse(Race.ELF, Mystic, FOURTH),
	elementalMaster(Race.ELF, Mystic, FOURTH),
	evaSaint(Race.ELF, Priest, FOURTH),
	
	shillienTemplar(Race.DARK_ELF, Fighter, FOURTH),
	spectralDancer(Race.DARK_ELF, Fighter, FOURTH),
	ghostHunter(Race.DARK_ELF, Fighter, FOURTH),
	ghostSentinel(Race.DARK_ELF, Fighter, FOURTH),
	stormScreamer(Race.DARK_ELF, Mystic, FOURTH),
	spectralMaster(Race.DARK_ELF, Mystic, FOURTH),
	shillienSaint(Race.DARK_ELF, Priest, FOURTH),
	
	titan(Race.ORC, Fighter, FOURTH),
	grandKhavatari(Race.ORC, Fighter, FOURTH),
	dominator(Race.ORC, Mystic, FOURTH),
	doomcryer(Race.ORC, Mystic, FOURTH),
	
	fortuneSeeker(Race.DWARF, Fighter, FOURTH),
	maestro(Race.DWARF, Fighter, FOURTH);
	
	private Race _race;
	private ClassLevel _level;
	private ClassType _type;
	
	private static final Set<PlayerClass> mainSubclassSet;
	private static final Set<PlayerClass> neverSubclassed = EnumSet.of(Overlord, Warsmith);
	
	private static final Set<PlayerClass> subclasseSet1 = EnumSet.of(DarkAvenger, Paladin, TempleKnight, ShillienKnight);
	private static final Set<PlayerClass> subclasseSet2 = EnumSet.of(TreasureHunter, AbyssWalker, Plainswalker);
	private static final Set<PlayerClass> subclasseSet3 = EnumSet.of(Hawkeye, SilverRanger, PhantomRanger);
	private static final Set<PlayerClass> subclasseSet4 = EnumSet.of(Warlock, ElementalSummoner, PhantomSummoner);
	private static final Set<PlayerClass> subclasseSet5 = EnumSet.of(Sorceror, Spellsinger, Spellhowler);
	
	private static final EnumMap<PlayerClass, Set<PlayerClass>> subclassSetMap = new EnumMap<>(PlayerClass.class);
	static
	{
		final Set<PlayerClass> subclasses = getSet(null, THIRD);
		subclasses.removeAll(neverSubclassed);
		
		mainSubclassSet = subclasses;
		
		subclassSetMap.put(DarkAvenger, subclasseSet1);
		subclassSetMap.put(Paladin, subclasseSet1);
		subclassSetMap.put(TempleKnight, subclasseSet1);
		subclassSetMap.put(ShillienKnight, subclasseSet1);
		
		subclassSetMap.put(TreasureHunter, subclasseSet2);
		subclassSetMap.put(AbyssWalker, subclasseSet2);
		subclassSetMap.put(Plainswalker, subclasseSet2);
		
		subclassSetMap.put(Hawkeye, subclasseSet3);
		subclassSetMap.put(SilverRanger, subclasseSet3);
		subclassSetMap.put(PhantomRanger, subclasseSet3);
		
		subclassSetMap.put(Warlock, subclasseSet4);
		subclassSetMap.put(ElementalSummoner, subclasseSet4);
		subclassSetMap.put(PhantomSummoner, subclasseSet4);
		
		subclassSetMap.put(Sorceror, subclasseSet5);
		subclassSetMap.put(Spellsinger, subclasseSet5);
		subclassSetMap.put(Spellhowler, subclasseSet5);
	}
	
	PlayerClass(Race race, ClassType pType, ClassLevel pLevel)
	{
		_race = race;
		_level = pLevel;
		_type = pType;
	}
	
	public Set<PlayerClass> getAvailableSubclasses(Player player)
	{
		Set<PlayerClass> subclasses = null;
		
		if (_level == THIRD)
		{
			subclasses = EnumSet.copyOf(mainSubclassSet);
			
			subclasses.removeAll(neverSubclassed);
			subclasses.remove(this);
			
			switch (player.getRace())
			{
				case ELF:
				{
					subclasses.removeAll(getSet(Race.DARK_ELF, THIRD));
					break;
				}
				case DARK_ELF:
				{
					subclasses.removeAll(getSet(Race.ELF, THIRD));
					break;
				}
			}
			
			final Set<PlayerClass> unavailableClasses = subclassSetMap.get(this);
			
			if (unavailableClasses != null)
			{
				subclasses.removeAll(unavailableClasses);
			}
		}
		
		return subclasses;
	}
	
	public static Set<PlayerClass> getSet(Race race, ClassLevel level)
	{
		final Set<PlayerClass> allOf = EnumSet.noneOf(PlayerClass.class);
		for (PlayerClass playerClass : EnumSet.allOf(PlayerClass.class))
		{
			if (((race == null) || playerClass.isOfRace(race)) && ((level == null) || playerClass.isOfLevel(level)))
			{
				allOf.add(playerClass);
			}
		}
		return allOf;
	}
	
	public boolean isOfRace(Race pRace)
	{
		return _race == pRace;
	}
	
	public boolean isOfType(ClassType pType)
	{
		return _type == pType;
	}
	
	public boolean isOfLevel(ClassLevel pLevel)
	{
		return _level == pLevel;
	}
	
	public ClassLevel getLevel()
	{
		return _level;
	}
}
