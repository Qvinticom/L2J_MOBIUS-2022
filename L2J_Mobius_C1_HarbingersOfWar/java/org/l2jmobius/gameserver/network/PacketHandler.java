/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network;

import java.io.IOException;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.network.clientpackets.Action;
import org.l2jmobius.gameserver.network.clientpackets.AddTradeItem;
import org.l2jmobius.gameserver.network.clientpackets.AnswerTradeRequest;
import org.l2jmobius.gameserver.network.clientpackets.Appearing;
import org.l2jmobius.gameserver.network.clientpackets.AttackRequest;
import org.l2jmobius.gameserver.network.clientpackets.AuthLogin;
import org.l2jmobius.gameserver.network.clientpackets.ChangeMoveType2;
import org.l2jmobius.gameserver.network.clientpackets.ChangeWaitType2;
import org.l2jmobius.gameserver.network.clientpackets.CharacterCreate;
import org.l2jmobius.gameserver.network.clientpackets.CharacterDelete;
import org.l2jmobius.gameserver.network.clientpackets.CharacterRestore;
import org.l2jmobius.gameserver.network.clientpackets.CharacterSelected;
import org.l2jmobius.gameserver.network.clientpackets.EnterWorld;
import org.l2jmobius.gameserver.network.clientpackets.FinishRotating;
import org.l2jmobius.gameserver.network.clientpackets.Logout;
import org.l2jmobius.gameserver.network.clientpackets.MoveBackwardToLocation;
import org.l2jmobius.gameserver.network.clientpackets.NewCharacter;
import org.l2jmobius.gameserver.network.clientpackets.ProtocolVersion;
import org.l2jmobius.gameserver.network.clientpackets.RequestActionUse;
import org.l2jmobius.gameserver.network.clientpackets.RequestAllyCrest;
import org.l2jmobius.gameserver.network.clientpackets.RequestAnswerJoinParty;
import org.l2jmobius.gameserver.network.clientpackets.RequestAnswerJoinPledge;
import org.l2jmobius.gameserver.network.clientpackets.RequestAquireSkill;
import org.l2jmobius.gameserver.network.clientpackets.RequestAquireSkillInfo;
import org.l2jmobius.gameserver.network.clientpackets.RequestBuyItem;
import org.l2jmobius.gameserver.network.clientpackets.RequestBypassToServer;
import org.l2jmobius.gameserver.network.clientpackets.RequestChangePetName;
import org.l2jmobius.gameserver.network.clientpackets.RequestDestroyItem;
import org.l2jmobius.gameserver.network.clientpackets.RequestDropItem;
import org.l2jmobius.gameserver.network.clientpackets.RequestGMCommand;
import org.l2jmobius.gameserver.network.clientpackets.RequestGetItemFromPet;
import org.l2jmobius.gameserver.network.clientpackets.RequestGiveItemToPet;
import org.l2jmobius.gameserver.network.clientpackets.RequestGiveNickName;
import org.l2jmobius.gameserver.network.clientpackets.RequestGmList;
import org.l2jmobius.gameserver.network.clientpackets.RequestItemList;
import org.l2jmobius.gameserver.network.clientpackets.RequestJoinParty;
import org.l2jmobius.gameserver.network.clientpackets.RequestJoinPledge;
import org.l2jmobius.gameserver.network.clientpackets.RequestMagicSkillUse;
import org.l2jmobius.gameserver.network.clientpackets.RequestOustPartyMember;
import org.l2jmobius.gameserver.network.clientpackets.RequestOustPledgeMember;
import org.l2jmobius.gameserver.network.clientpackets.RequestPartyMatchConfig;
import org.l2jmobius.gameserver.network.clientpackets.RequestPartyMatchDetail;
import org.l2jmobius.gameserver.network.clientpackets.RequestPartyMatchList;
import org.l2jmobius.gameserver.network.clientpackets.RequestPetGetItem;
import org.l2jmobius.gameserver.network.clientpackets.RequestPledgeCrest;
import org.l2jmobius.gameserver.network.clientpackets.RequestPledgeInfo;
import org.l2jmobius.gameserver.network.clientpackets.RequestPledgeMemberList;
import org.l2jmobius.gameserver.network.clientpackets.RequestPrivateStoreBuyManage;
import org.l2jmobius.gameserver.network.clientpackets.RequestPrivateStoreManage;
import org.l2jmobius.gameserver.network.clientpackets.RequestPrivateStoreQuitBuy;
import org.l2jmobius.gameserver.network.clientpackets.RequestPrivateStoreQuitSell;
import org.l2jmobius.gameserver.network.clientpackets.RequestQuestList;
import org.l2jmobius.gameserver.network.clientpackets.RequestRestart;
import org.l2jmobius.gameserver.network.clientpackets.RequestRestartPoint;
import org.l2jmobius.gameserver.network.clientpackets.RequestSellItem;
import org.l2jmobius.gameserver.network.clientpackets.RequestSetPledgeCrest;
import org.l2jmobius.gameserver.network.clientpackets.RequestShortCutDel;
import org.l2jmobius.gameserver.network.clientpackets.RequestShortCutReg;
import org.l2jmobius.gameserver.network.clientpackets.RequestShowBoard;
import org.l2jmobius.gameserver.network.clientpackets.RequestSkillList;
import org.l2jmobius.gameserver.network.clientpackets.RequestSocialAction;
import org.l2jmobius.gameserver.network.clientpackets.RequestTargetCanceld;
import org.l2jmobius.gameserver.network.clientpackets.RequestUnEquipItem;
import org.l2jmobius.gameserver.network.clientpackets.RequestWithDrawalParty;
import org.l2jmobius.gameserver.network.clientpackets.RequestWithdrawalPledge;
import org.l2jmobius.gameserver.network.clientpackets.Say2;
import org.l2jmobius.gameserver.network.clientpackets.SendBypassBuildCmd;
import org.l2jmobius.gameserver.network.clientpackets.SendPrivateStoreBuyBuyList;
import org.l2jmobius.gameserver.network.clientpackets.SendPrivateStoreBuyList;
import org.l2jmobius.gameserver.network.clientpackets.SendWareHouseDepositList;
import org.l2jmobius.gameserver.network.clientpackets.SendWareHouseWithDrawList;
import org.l2jmobius.gameserver.network.clientpackets.SetPrivateStoreListBuy;
import org.l2jmobius.gameserver.network.clientpackets.SetPrivateStoreListSell;
import org.l2jmobius.gameserver.network.clientpackets.SetPrivateStoreMsgBuy;
import org.l2jmobius.gameserver.network.clientpackets.SetPrivateStoreMsgSell;
import org.l2jmobius.gameserver.network.clientpackets.StartRotating;
import org.l2jmobius.gameserver.network.clientpackets.StopMove;
import org.l2jmobius.gameserver.network.clientpackets.TradeDone;
import org.l2jmobius.gameserver.network.clientpackets.TradeRequest;
import org.l2jmobius.gameserver.network.clientpackets.UseItem;
import org.l2jmobius.gameserver.network.clientpackets.ValidatePosition;

public class PacketHandler
{
	private static Logger _log = Logger.getLogger(PacketHandler.class.getName());
	private final ClientThread _client;
	
	public PacketHandler(ClientThread client)
	{
		_client = client;
	}
	
	public void handlePacket(byte[] data) throws IOException
	{
		final int id = data[0] & 0xFF;
		switch (id)
		{
			case 0:
			{
				new ProtocolVersion(data, _client);
				break;
			}
			case 1:
			{
				new MoveBackwardToLocation(data, _client);
				break;
			}
			case 3:
			{
				new EnterWorld(data, _client);
				break;
			}
			case 4:
			{
				new Action(data, _client);
				break;
			}
			case 8:
			{
				new AuthLogin(data, _client);
				break;
			}
			case 9:
			{
				new Logout(data, _client);
				break;
			}
			case 10:
			{
				new AttackRequest(data, _client);
				break;
			}
			case 11:
			{
				new CharacterCreate(data, _client);
				break;
			}
			case 12:
			{
				new CharacterDelete(data, _client);
				break;
			}
			case 13:
			{
				new CharacterSelected(data, _client);
				break;
			}
			case 14:
			{
				new NewCharacter(data, _client);
				break;
			}
			case 15:
			{
				new RequestItemList(data, _client);
				break;
			}
			case 17:
			{
				new RequestUnEquipItem(data, _client);
				break;
			}
			case 18:
			{
				new RequestDropItem(data, _client);
				break;
			}
			case 20:
			{
				new UseItem(data, _client);
				break;
			}
			case 21:
			{
				new TradeRequest(data, _client);
				break;
			}
			case 22:
			{
				new AddTradeItem(data, _client);
				break;
			}
			case 23:
			{
				new TradeDone(data, _client);
				break;
			}
			case 27:
			{
				new RequestSocialAction(data, _client);
				break;
			}
			case 28:
			{
				new ChangeMoveType2(data, _client);
				break;
			}
			case 29:
			{
				new ChangeWaitType2(data, _client);
				break;
			}
			case 30:
			{
				new RequestSellItem(data, _client);
				break;
			}
			case 31:
			{
				new RequestBuyItem(data, _client);
				break;
			}
			case 33:
			{
				new RequestBypassToServer(data, _client);
				break;
			}
			case 36:
			{
				new RequestJoinPledge(data, _client);
				break;
			}
			case 37:
			{
				new RequestAnswerJoinPledge(data, _client);
				break;
			}
			case 38:
			{
				new RequestWithdrawalPledge(data, _client);
				break;
			}
			case 39:
			{
				new RequestOustPledgeMember(data, _client);
				break;
			}
			case 41:
			{
				new RequestJoinParty(data, _client);
				break;
			}
			case 42:
			{
				new RequestAnswerJoinParty(data, _client);
				break;
			}
			case 43:
			{
				new RequestWithDrawalParty(data, _client.getActiveChar());
				break;
			}
			case 44:
			{
				new RequestOustPartyMember(data, _client);
				break;
			}
			case 47:
			{
				new RequestMagicSkillUse(data, _client);
				break;
			}
			case 48:
			{
				new Appearing(data, _client);
				break;
			}
			case 49:
			{
				new SendWareHouseDepositList(data, _client);
				break;
			}
			case 50:
			{
				new SendWareHouseWithDrawList(data, _client);
				break;
			}
			case 51:
			{
				new RequestShortCutReg(data, _client);
				break;
			}
			case 53:
			{
				new RequestShortCutDel(data, _client);
				break;
			}
			case 54:
			{
				new StopMove(data, _client);
				break;
			}
			case 55:
			{
				new RequestTargetCanceld(data, _client);
				break;
			}
			case 56:
			{
				new Say2(data, _client);
				break;
			}
			case 60:
			{
				new RequestPledgeMemberList(data, _client);
				break;
			}
			case 63:
			{
				new RequestSkillList(data, _client);
				break;
			}
			case 68:
			{
				new AnswerTradeRequest(data, _client);
				break;
			}
			case 69:
			{
				new RequestActionUse(data, _client);
				break;
			}
			case 70:
			{
				new RequestRestart(data, _client);
				break;
			}
			case 72:
			{
				new ValidatePosition(data, _client);
				break;
			}
			case 74:
			{
				new StartRotating(data, _client);
				break;
			}
			case 75:
			{
				new FinishRotating(data, _client);
				break;
			}
			case 83:
			{
				new RequestSetPledgeCrest(data, _client);
				break;
			}
			case 85:
			{
				new RequestGiveNickName(data, _client);
				break;
			}
			case 87:
			{
				new RequestShowBoard(data, _client);
				break;
			}
			case 89:
			{
				new RequestDestroyItem(data, _client);
				break;
			}
			case 91:
			{
				new SendBypassBuildCmd(data, _client);
				break;
			}
			case 98:
			{
				new CharacterRestore(data, _client);
				break;
			}
			case 99:
			{
				new RequestQuestList(data, _client);
				break;
			}
			case 102:
			{
				new RequestPledgeInfo(data, _client);
				break;
			}
			case 104:
			{
				new RequestPledgeCrest(data, _client);
				break;
			}
			case 107:
			{
				new RequestAquireSkillInfo(data, _client);
				break;
			}
			case 108:
			{
				new RequestAquireSkill(data, _client);
				break;
			}
			case 109:
			{
				new RequestRestartPoint(data, _client);
				break;
			}
			case 110:
			{
				new RequestGMCommand(data, _client);
				break;
			}
			case 111:
			{
				new RequestPartyMatchConfig(data, _client);
				break;
			}
			case 112:
			{
				new RequestPartyMatchList(data, _client);
				break;
			}
			case 113:
			{
				new RequestPartyMatchDetail(data, _client);
				break;
			}
			case 115:
			{
				new RequestPrivateStoreManage(data, _client);
				break;
			}
			case 116:
			{
				new SetPrivateStoreListSell(data, _client);
				break;
			}
			case 118:
			{
				new RequestPrivateStoreQuitSell(data, _client);
				break;
			}
			case 119:
			{
				new SetPrivateStoreMsgSell(data, _client);
				break;
			}
			case 121:
			{
				new SendPrivateStoreBuyList(data, _client);
				break;
			}
			case 129:
			{
				new RequestGmList(data, _client);
				break;
			}
			case 136:
			{
				new RequestAllyCrest(data, _client);
				break;
			}
			case 137:
			{
				new RequestChangePetName(data, _client);
				break;
			}
			case 139:
			{
				new RequestGiveItemToPet(data, _client);
				break;
			}
			case 140:
			{
				new RequestGetItemFromPet(data, _client);
				break;
			}
			case 143:
			{
				new RequestPetGetItem(data, _client);
				break;
			}
			case 144:
			{
				new RequestPrivateStoreBuyManage(data, _client);
				break;
			}
			case 145:
			{
				new SetPrivateStoreListBuy(data, _client);
				break;
			}
			case 147:
			{
				new RequestPrivateStoreQuitBuy(data, _client);
				break;
			}
			case 148:
			{
				new SetPrivateStoreMsgBuy(data, _client);
				break;
			}
			case 150:
			{
				new SendPrivateStoreBuyBuyList(data, _client);
				break;
			}
			case 157:
			{
				// _log.warning("Request Skill Cool Time .. ignored");
				break;
			}
			default:
			{
				if (Config.LOG_UNKNOWN_PACKETS)
				{
					_log.warning("Unknown Packet: " + id);
					_log.warning(printData(data, data.length));
				}
			}
		}
	}
	
	private String printData(byte[] data, int len)
	{
		int a;
		int charpoint;
		byte t1;
		final StringBuffer result = new StringBuffer();
		int counter = 0;
		for (int i = 0; i < len; ++i)
		{
			if ((counter % 16) == 0)
			{
				result.append(fillHex(i, 4) + ": ");
			}
			result.append(fillHex(data[i] & 0xFF, 2) + " ");
			if (++counter != 16)
			{
				continue;
			}
			result.append("   ");
			charpoint = i - 15;
			for (a = 0; a < 16; ++a)
			{
				if (((t1 = data[charpoint++]) > 31) && (t1 < 128))
				{
					result.append((char) t1);
					continue;
				}
				result.append('.');
			}
			result.append("\n");
			counter = 0;
		}
		final int rest = data.length % 16;
		if (rest > 0)
		{
			for (int i = 0; i < (17 - rest); ++i)
			{
				result.append("   ");
			}
			charpoint = data.length - rest;
			for (a = 0; a < rest; ++a)
			{
				if (((t1 = data[charpoint++]) > 31) && (t1 < 128))
				{
					result.append((char) t1);
					continue;
				}
				result.append('.');
			}
			result.append("\n");
		}
		return result.toString();
	}
	
	private String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);
		for (int i = number.length(); i < digits; ++i)
		{
			number = "0" + number;
		}
		return number;
	}
}
