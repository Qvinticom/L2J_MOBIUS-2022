package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

public class ExPCCafeRequestOpenWindowWithoutNPC extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance _activeChar = getClient().getActiveChar();
		if ((_activeChar != null) && Config.PC_BANG_ENABLED)
		{
			getHtmlPage(_activeChar);
		}
	}
	
	public void getHtmlPage(L2PcInstance player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(player.getHtmlPrefix(), "data/html/pccafe.htm");
		player.sendPacket(html);
	}
	
	@Override
	public String getType()
	{
		return getClass().getName();
	}
}
