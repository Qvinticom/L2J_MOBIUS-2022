package org.l2jmobius.gameserver.network.serverpackets.pet;


import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * Written by Berezkin Nikolay, on 26.04.2021
 */
public class ExPetSkillList implements IClientOutgoingPacket{

    private final boolean onEnter;
    private final Summon pet;
    public ExPetSkillList(boolean onEnter, Summon pet) {
        this.onEnter = onEnter;
        this.pet = pet;
    }

    @Override
    public boolean write(PacketWriter packet) {
        OutgoingPackets.EX_PET_SKILL_LIST.writeId(packet);
        packet.writeC(onEnter ? 1 : 0);
        packet.writeD(pet.getAllSkills().size());
        for (Skill sk : pet.getAllSkills()) {
            packet.writeD(sk.getDisplayId());
            packet.writeD(sk.getDisplayLevel());
            packet.writeD(sk.getReuseDelayGroup());
            packet.writeC(0);
            packet.writeC(0);
        }
        return true;
    }
}
