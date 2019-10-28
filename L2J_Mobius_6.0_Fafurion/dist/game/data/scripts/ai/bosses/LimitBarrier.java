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
package ai.bosses;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import ai.AbstractNpcAI;

/**
 * Limit Barrier AI
 * @author RobikBobik<br>
 *         OK - Many Raid Bosses lvl 50 and higher from now on use Ξ²β‚¬οΏ½Limit BarrierΞ²β‚¬οΏ½ skill when their HP reaches 90%, 60% and 30%.<br>
 *         OK - 600 hits in 15 seconds are required to destroy the barrier. Amount of damage does not matter.<br>
 *         OK - If barrier destruction is failed, Boss restores full HP.<br>
 *         OK - Death Knight, who randomly appear after boss's death, also use Limit Barrier.<br>
 *         OK - Epic Bosses Orfen, Queen Ant and Core also use Limit Barrier.<br>
 *         OK - Epic Bosses Antharas, Zaken and Baium and their analogues in instance zones do not use Limit Barrier.<br>
 *         OK - Raid Bosses in instances do not use Limit Barrier.<br>
 *         OK - All Raid Bosses who use Limit Barrier are listed below:<br>
 */
public final class LimitBarrier extends AbstractNpcAI
{
	// NPCs
	private static int[] RAID_BOSSES =
	{
		29325, // Orfen
		29001, // Queen Ant
		29006, // Core
		25493, // Niniel Spirit Eva
		25496, // Papurrion Pingolpin
		25016, // Guardian 3 of Garden
		25032, // Eva Guardian Millenu
		25319, // Amber
		25523, // Degeneration Golem
		29095, // Gordon
		25501, // Grave Robber Akata
		29062, // Triolls Priest Andreas
		25527, // Uruka
		25677, // Water Spirit Lian
		25674, // Gwindorr
		25214, // Fafurions Pagehood Sika
		25394, // Premo Prime the Creature
		25211, // Sebek
		25188, // Apepi
		25185, // Tasaba Patriarch Hellena
		25208, // Water Couatl Ateka
		25189, // Cronoss Summons Mumu
		25115, // Icarus Sample 21
		25179, // Hatos
		25726, // Behemoth Leader
		25725, // Dragon Beast
		25252, // Palibati Queen Themis
		25146, // Serpent Demon Bifrons
		25063, // Grandeur Soul Chertuba
		25004, // Turek Mercenary Boss
		25076, // Princess Molrang
		25369, // Soul Scavenger
		25362, // Tracker Sharuk
		25365, // Patriarch Kuroboros
		25366, // Priest of Kuroboros
		25060, // Love Reverser Kael
		25038, // Tirak
		25095, // Elf Renoa
		25127, // Langk Matriarch Rashkos
		25357, // Sukar Wererat Chief
		25149, // Zombie Lord Crowl
		25166, // Ikuntai
		25131, // Carnage Lord Gato
		25122, // Refuge Hoper Leo
		25067, // Master of Ledflage Shaka
		25088, // Crazy Mechanic Golem
		25431, // Iron Giant Totem
		25260, // Iron Giant Totem
		25026, // Katu Van Atui
		25158, // King Tarlk
		25437, // Timak Orc Gosmos
		25230, // Timak Seer Ragoth
		25085, // Timak Orc Hunter A
		25155, // Shaman King Selu
		25064, // Wizard of Storm Teruk
		25134, // Leto Chief Talkin
		25226, // Roaring Seer Kastor
		25051, // Rahha
		25248, // Doom Blade Tanatos
		25426, // Betrayer of Urutu Freki
		25429, // Mammpns Collector Talos
		25041, // Ikuntai
		25170, // Remmel
		25023, // Jeruna Queen
		25392, // Queens Nobel Leader
		25128, // Vuku Witchdr Gharmash
		25360, // Tiger Hornet
		25019, // Pan Draid
		25398, // Eyes of Bereth
		25388, // Redeye Leader Trakia
		25391, // Nurkas Messenger
		25272, // Partisan Leader Talakin
		25079, // Catseye
		25152, // Flamelord Shadar
		25404, // Corsair Captain Kylon
		25410, // Road Scavenger Leader
		25020, // Breka Warlock Pastu
		25173, // King Tiger Karuta
		25082, // Lost Cat the Cat A
		25112, // Meana Agent of Beres
		25098, // Sejarr S Summoner
		25415, // Nakondas
		25498, // Istary Papurrion
		25057, // Biconne of Blue Sky
		25192, // Earth Protecter Panathen
		25434, // Bandit Leader Barda
		25444, // Enmity Ghost Ramdal
		25447, // Hope Immortality Mardil
		25092, // Korim
		25143, // Shuriel Fire of Wrath
		25450, // Cherub Garacsia
		25044, // Barion
		25047, // Karte
		25050, // Verfa
		25163, // Roar Skylancer
		25007, // Retreat Spider Cletu
		25102, // Shacram
		25441, // Monster Cyrion
		25438, // Kelbar
		25103, // Sorcery Isirr
		25456, // Oblivion S Mirror
		25460, // Ereve Deathman
		25238, // Nightmare Drake
		25255, // Gargoyle Lord Tiphon
		25106, // Manes Lidia
		25233, // Spirit Andras Betrayer
		25478, // Priest Hisilrome
		25256, // Taik Prefect Arak
		25125, // Fiercetiger King Angel
		25089, // Soulless Wild Boar
		25241, // Harit Hero Tamash
		25463, // Harit Tutelar Garangky
		25309, // Varka Hero Shadith
		25235, // Vanor Chief Kandra
		25269, // Beastlord Behemoth
		25293, // Geyser Guardian Hestia
		25299, // Ketra Hero Hekaton
		25325, // Blinding Fire Barakiel
		25322, // Demonic Agent Falston
		3473, // Omega Golem
		3477, // Reinforced Super Kat the Cat
		3479, // Darkened Super Feline Queen
		3481, // Control-Crazed Mew the Cat
		25775, // Apherus
		25886, // Houpon the Warden Overseer
		25887, // Crook the Mad
		25892, // Guillotine of Death
		25902, // Gigantic Golem
		25922, // Nerva Chief Turakan
		25945, // Megaloprepis
		25946, // Antharas' Herald Komabor
		25947, // Skellrus' Herald Iskios
		25948, // Valakas' Herald Potigia
		25949, // Lindvior's Herald Numa
		25950, // Fafurion's Herald Aquarion
		25956, // Vengeful Eligos
		25957, // Vengeful Agarez
		25958, // Vengeful Lerazia
		25959, // Vengeful Oretross
		25960, // Vengeful Edaire
		25961, // Vengeful Agonia
		25967, // Zetahl
		25968, // Tabris
		25969, // Stelos
		25970, // Ravolas
		25971, // Stelo Soma
		25972, // Dephracor
		25978, // Garamor's Herald Gariott
		25979, // Varvacion
		25980, // Varmoni
		25981, // Varvinos
		25982, // Varmonia
		25983, // Varkaron
		25989, // Harp's Clone
		25990, // Isadora's Avatar
		25991, // Maliss' Avatar
		25992, // Embryo Garron
		25993, // Embryo Nigel
		25994, // Embryo Dabos
		26000, // Amden Orc Turahot
		26001, // Amden Orc Turation
		26002, // Amden Orc Turamathia
		26003, // Amden Orc Turabait
		26004, // Nerva Orc Nermion
		26005, // Nerva Orc Nergatt
		26011, // Bloody Earth Dragon Gagia
		26012, // Demon Fardune
		26013, // Demon Harsia
		26014, // Demon Bedukel
		26015, // Bloody Witch Rumilla
		26016, // Shilen's Priest Sasia
		26022, // Dark Messenger Afjak
		26023, // Dusk Knight Feilnor
		26024, // Chaos Wizard Amormio
		26025, // Insolence Knight Lahav
		26026, // Death Priest Borhunt
		26027, // Destruction Knight Zeruel
		26033, // Fallen Angel Tiera
		26034, // Corrupted Goblier
		26035, // Corrupted Cherkia
		26036, // Corrupted Harthemon
		26037, // Corrupted Sarboth
		26038, // Fallen Angel Eloule
		26044, // Evil Magikus
		26045, // Kerfaus
		26046, // Milinaus
		26047, // Sarkaus
		26048, // Shimeus
		26049, // Evil Kinigos
		26055, // Ekidnas Statue Tarstan
		26056, // Ekidnas Statue Abelsnif
		26057, // Ekidnas Statue Kimesis
		26058, // Ekidnas Statue Kathargon
		26059, // Ekidnas Statue Pantasaus
		26060, // Ekidnas Statue Ixignon
		26066, // Enhanced Mahum Radium
		26067, // Typheron
		26068, // Timarga
		26069, // Tycepton
		26070, // Tiverga
		26071, // Enhanced Mahum Supercium
		26077, // Monster Laum
		26078, // Monster Minotaur
		26079, // Monster Sarga
		26080, // Monster Hogliff
		26081, // Monster Artarot
		26082, // Monster Centaur
		26131, // Isabella
		25875, // Queen of Darkness
		25696, // Taklacan
		25697, // Torumba
		25698, // Dopagen
		25928, // Tebot
		25929, // Tegaffe
		25930, // Thesakar
		25931, // Theor
		25932, // Transformed: Dartanion
		25933, // Garden Patrol Captain
		25937, // Spicula Negative
		26137, // Mimir
		26162, // Demon Worshipper Dorgon
		26163, // Demon Worshipper Zarka
		26164, // Demon Worshipper Kultaan
		26165, // Demon Worshipper Nortan
		26170, // Valac
		26171, // Kurion
		26172, // Atluum
		26173, // Lebruum
		26178, // Berserker Hard
		26179, // Patrol Commander Pho
		26180, // Guardian Gastra
		26181, // Dark Wizard Ruby
		26186, // Wandering Croamis
		26187, // Wandering Harpe
		26188, // Wandering Barrena
		26189, // Wandering Purka
		26277, // Ashen Shadow Expeditionary Force Leaders Kantu
		26278, // Ashen Shadow Expeditionary Force Leaders Kai
		26279, // Ashen Shadow Expeditionary Force Leaders Heine
		26280, // Ashen Shadow Expeditionary Force Leaders Xenon
		26285, // Blood Devil Brutal
		26286, // Blood Devil Akun
		26287, // Blood Devil Epica
		26288, // Blood Devil Julia
		26293, // Valiant Edgar
		26294, // Vicious Blood
		26295, // Coquette Morrigan
		26296, // Cunning Croby
		26301, // Evil Spirit Knight Tretan
		26302, // Evil Spirit Knight Boros
		26303, // Evil Spirit Alusion
		26304, // Evil Spirits Puppet Graff
		26372, // Corrupted Atrofa
		26373, // Corrupted Arbor
		26374, // Corrupted Falena
		26375, // Corrupted Arimus
		26380, // Spirit of Time Arachine
		26381, // Spirit of Time Tisraki
		26382, // Spirit of Time Nympia
		26383, // Spirit of Time Lafros
		29374, // Cyrax
	};
	// Skill
	private static final SkillHolder LIMIT_BARRIER = new SkillHolder(32203, 1);
	// Misc
	private static final int HIT_COUNT = 600;
	private static final Map<Npc, Integer> RAIDBOSS_HITS = new ConcurrentHashMap<>();
	
	private LimitBarrier()
	{
		addAttackId(RAID_BOSSES);
		addKillId(RAID_BOSSES);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final int hits = RAIDBOSS_HITS.getOrDefault(npc, 0);
		
		switch (event)
		{
			case "RESTORE_FULL_HP":
			{
				if (hits < HIT_COUNT)
				{
					if (player != null)
					{
						npc.broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_HAVE_FAILED_TO_DESTROY_THE_LIMIT_BARRIER_NTHE_RAID_BOSS_FULLY_RECOVERS_ITS_CON, 2, 5000, true));
					}
					npc.setCurrentHp(npc.getStat().getMaxHp(), true);
					npc.stopSkillEffects(true, LIMIT_BARRIER.getSkillId());
					RAIDBOSS_HITS.put(npc, 0);
				}
				else if (hits > HIT_COUNT)
				{
					if (player != null)
					{
						npc.broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_HAVE_DESTROYED_THE_LIMIT_BARRIER, 2, 5000, true));
					}
					npc.stopSkillEffects(true, LIMIT_BARRIER.getSkillId());
					RAIDBOSS_HITS.put(npc, 0);
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, PlayerInstance attacker, int damage, boolean isSummon, Skill skill)
	{
		if (npc.isAffectedBySkill(LIMIT_BARRIER.getSkillId()))
		{
			final int hits = RAIDBOSS_HITS.getOrDefault(npc, 0);
			RAIDBOSS_HITS.put(npc, hits + 1);
		}
		
		if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.9)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.87)))
		{
			if (!npc.isAffectedBySkill(LIMIT_BARRIER.getSkillId()))
			{
				npc.setTarget(npc);
				npc.abortAttack();
				npc.abortCast();
				npc.doCast(LIMIT_BARRIER.getSkill());
				npc.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_RAID_BOSS_USES_THE_LIMIT_BARRIER_NFOCUS_YOUR_ATTACKS_TO_DESTROY_THE_LIMIT_BARRIER_IN_15_SECONDS, 2, 5000, true));
				startQuestTimer("RESTORE_FULL_HP", 15000, npc, attacker);
			}
		}
		else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.6)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.58)))
		{
			if (!npc.isAffectedBySkill(LIMIT_BARRIER.getSkillId()))
			{
				npc.setTarget(npc);
				npc.abortAttack();
				npc.abortCast();
				npc.doCast(LIMIT_BARRIER.getSkill());
				npc.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_RAID_BOSS_USES_THE_LIMIT_BARRIER_NFOCUS_YOUR_ATTACKS_TO_DESTROY_THE_LIMIT_BARRIER_IN_15_SECONDS, 2, 5000, true));
				startQuestTimer("RESTORE_FULL_HP", 15000, npc, attacker);
			}
		}
		else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.3)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.28)))
		{
			if (!npc.isAffectedBySkill(LIMIT_BARRIER.getSkillId()))
			{
				npc.setTarget(npc);
				npc.abortAttack();
				npc.abortCast();
				npc.doCast(LIMIT_BARRIER.getSkill());
				npc.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_RAID_BOSS_USES_THE_LIMIT_BARRIER_NFOCUS_YOUR_ATTACKS_TO_DESTROY_THE_LIMIT_BARRIER_IN_15_SECONDS, 2, 5000, true));
				startQuestTimer("RESTORE_FULL_HP", 15000, npc, attacker);
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		RAIDBOSS_HITS.remove(npc);
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new LimitBarrier();
	}
}
