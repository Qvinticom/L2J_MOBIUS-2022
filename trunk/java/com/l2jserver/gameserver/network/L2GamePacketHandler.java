/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import org.mmocore.network.IClientFactory;
import org.mmocore.network.IMMOExecutor;
import org.mmocore.network.IPacketHandler;
import org.mmocore.network.MMOConnection;
import org.mmocore.network.ReceivablePacket;

import com.l2jserver.Config;
import com.l2jserver.gameserver.network.L2GameClient.GameClientState;
import com.l2jserver.gameserver.network.clientpackets.*;
import com.l2jserver.gameserver.network.clientpackets.crystalization.RequestCrystallizeEstimate;
import com.l2jserver.gameserver.network.clientpackets.crystalization.RequestCrystallizeItemCancel;
import com.l2jserver.gameserver.network.clientpackets.friend.RequestAnswerFriendInvite;
import com.l2jserver.gameserver.network.clientpackets.friend.RequestFriendDel;
import com.l2jserver.gameserver.network.clientpackets.friend.RequestFriendDetailInfo;
import com.l2jserver.gameserver.network.clientpackets.friend.RequestFriendList;
import com.l2jserver.gameserver.network.clientpackets.friend.RequestSendFriendMsg;
import com.l2jserver.gameserver.network.clientpackets.friend.RequestUpdateBlockMemo;
import com.l2jserver.gameserver.network.clientpackets.friend.RequestUpdateFriendMemo;
import com.l2jserver.gameserver.network.clientpackets.mentoring.ConfirmMenteeAdd;
import com.l2jserver.gameserver.network.clientpackets.mentoring.RequestMenteeAdd;
import com.l2jserver.gameserver.network.clientpackets.mentoring.RequestMenteeWaitingList;
import com.l2jserver.gameserver.network.clientpackets.mentoring.RequestMentorCancel;
import com.l2jserver.gameserver.network.clientpackets.mentoring.RequestMentorList;
import com.l2jserver.gameserver.network.clientpackets.shuttle.CannotMoveAnymoreInShuttle;
import com.l2jserver.gameserver.network.clientpackets.shuttle.MoveToLocationInShuttle;
import com.l2jserver.gameserver.network.clientpackets.shuttle.RequestShuttleGetOff;
import com.l2jserver.gameserver.network.clientpackets.shuttle.RequestShuttleGetOn;
import com.l2jserver.util.Util;

/**
 * Stateful Packet Handler<BR>
 * The Stateful approach prevents the server from handling inconsistent packets, examples:<br>
 * <ul>
 * <li>Clients sends a MoveToLocation packet without having a character attached. (Potential errors handling the packet).</li>
 * <li>Clients sends a RequestAuthLogin being already authed. (Potential exploit).</li>
 * </ul>
 * Note: If for a given exception a packet needs to be handled on more then one state, then it should be added to all these states.
 * @author KenM
 */
public final class L2GamePacketHandler implements IPacketHandler<L2GameClient>, IClientFactory<L2GameClient>, IMMOExecutor<L2GameClient>
{
	private static final Logger _log = Logger.getLogger(L2GamePacketHandler.class.getName());
	
	// implementation
	@Override
	public ReceivablePacket<L2GameClient> handlePacket(ByteBuffer buf, L2GameClient client)
	{
		if (client.dropPacket())
		{
			return null;
		}
		
		int opcode = buf.get() & 0xFF;
		
		ReceivablePacket<L2GameClient> msg = null;
		GameClientState state = client.getState();
		
		switch (state)
		{
			case CONNECTED:
				switch (opcode)
				{
					case 0x0e:
						msg = new ProtocolVersion();
						break;
					case 0x2b:
						msg = new AuthLogin();
						break;
					default:
						printDebug(opcode, buf, state, client);
						break;
				}
				break;
			case AUTHED:
				switch (opcode)
				{
					case 0x00:
						msg = new Logout();
						break;
					case 0x0c:
						msg = new CharacterCreate();
						break;
					case 0x0d:
						msg = new CharacterDelete();
						break;
					case 0x12:
						msg = new CharacterSelect();
						break;
					case 0x13:
						msg = new NewCharacter();
						break;
					case 0x7b:
						msg = new CharacterRestore();
						break;
					case 0xd0:
						int id2 = -1;
						if (buf.remaining() >= 2)
						{
							id2 = buf.getShort() & 0xffff;
						}
						else
						{
							if (Config.PACKET_HANDLER_DEBUG)
							{
								_log.warning("Client: " + client.toString() + " sent a 0xd0 without the second opcode.");
							}
							break;
						}
						switch (id2)
						{
							case 0x33:
								msg = new RequestGotoLobby();
								break;
							case 0xA6:
								msg = new RequestEx2ndPasswordCheck();
								break;
							case 0xA7:
								msg = new RequestEx2ndPasswordVerify();
								break;
							case 0xA8:
								msg = new RequestEx2ndPasswordReq();
								break;
							case 0xA9:
								msg = new RequestCharacterNameCreatable();
								break;
							default:
								printDebugDoubleOpcode(opcode, id2, buf, state, client);
						}
						break;
					default:
						printDebug(opcode, buf, state, client);
						break;
				}
				break;
			case IN_GAME:
				switch (opcode)
				{
					case 0x00:
						msg = new Logout();
						break;
					case 0x01:
						msg = new Attack();
						break;
					case 0x03:
						msg = new RequestStartPledgeWar();
						break;
					case 0x04:
						msg = new RequestReplyStartPledgeWar();
						break;
					case 0x05:
						msg = new RequestStopPledgeWar();
						break;
					case 0x06: // RequestSCCheck
						msg = new RequestReplyStopPledgeWar();
						break;
					case 0x07:
						msg = new RequestSurrenderPledgeWar();
						break;
					case 0x08:
						msg = new RequestReplySurrenderPledgeWar();
						break;
					case 0x09:
						msg = new RequestSetPledgeCrest();
						break;
					case 0x0b:
						msg = new RequestGiveNickName();
						break;
					case 0x0f:
						msg = new MoveBackwardToLocation();
						break;
					case 0x10:
						// Say
						break;
					case 0x11:
						msg = new EnterWorld();
						break;
					case 0x12:
						// CharacterSelect, in case of player spam clicks on loginscreen
						break;
					case 0x14:
						msg = new RequestItemList();
						break;
					case 0x15:
						// RequestEquipItem
						client.handleCheat("Used obsolete RequestEquipItem packet!");
						break;
					case 0x16:
						msg = new RequestUnEquipItem();
						break;
					case 0x17:
						msg = new RequestDropItem();
						break;
					case 0x19:
						msg = new UseItem();
						break;
					case 0x1a:
						msg = new TradeRequest();
						break;
					case 0x1b:
						msg = new AddTradeItem();
						break;
					case 0x1c:
						msg = new TradeDone();
						break;
					case 0x1f:
						msg = new Action();
						break;
					case 0x22:
						msg = new RequestLinkHtml();
						break;
					case 0x23:
						msg = new RequestBypassToServer();
						break;
					case 0x24:
						msg = new RequestBBSwrite();
						break;
					case 0x25:
						// RequestCreatePledge
						break;
					case 0x26:
						msg = new RequestJoinPledge();
						break;
					case 0x27:
						msg = new RequestAnswerJoinPledge();
						break;
					case 0x28:
						msg = new RequestWithdrawalPledge();
						break;
					case 0x29:
						msg = new RequestOustPledgeMember();
						break;
					case 0x2c:
						msg = new RequestGetItemFromPet();
						break;
					case 0x2e:
						msg = new RequestAllyInfo();
						break;
					case 0x2f:
						msg = new RequestCrystallizeItem();
						break;
					case 0x30:
						msg = new RequestPrivateStoreManageSell();
						break;
					case 0x31:
						msg = new SetPrivateStoreListSell();
						break;
					case 0x32:
						msg = new AttackRequest();
						break;
					case 0x33:
						// RequestTeleportPacket
						break;
					case 0x34:
						// msg = new RequestSocialAction();
						client.handleCheat("Used obsolete RequestSocialAction packet");
						break;
					case 0x35:
						// msg = new ChangeMoveType2();
						client.handleCheat("Used obsolete ChangeMoveType packet");
						break;
					case 0x36:
						// msg = new ChangeWaitType2();
						client.handleCheat("Used obsolete ChangeWaitType packet");
						break;
					case 0x37:
						msg = new RequestSellItem();
						break;
					case 0x38:
						// RequestMagicSkillList
						break;
					case 0x39:
						msg = new RequestMagicSkillUse();
						break;
					case 0x3a: // SendApperingPacket
						msg = new Appearing();
						break;
					case 0x3b:
						if (Config.ALLOW_WAREHOUSE)
						{
							msg = new SendWareHouseDepositList();
						}
						break;
					case 0x3c:
						msg = new SendWareHouseWithDrawList();
						break;
					case 0x3d:
						msg = new RequestShortCutReg();
						break;
					case 0x3f:
						msg = new RequestShortCutDel();
						break;
					case 0x40:
						msg = new RequestBuyItem();
						break;
					case 0x41:
						// RequestDismissPledge
						break;
					case 0x42:
						msg = new RequestJoinParty();
						break;
					case 0x43:
						msg = new RequestAnswerJoinParty();
						break;
					case 0x44:
						msg = new RequestWithDrawalParty();
						break;
					case 0x45:
						msg = new RequestOustPartyMember();
						break;
					case 0x46:
						// RequestDismissParty
						break;
					case 0x47:
						msg = new CannotMoveAnymore();
						break;
					case 0x48:
						msg = new RequestTargetCanceld();
						break;
					case 0x49:
						msg = new Say2();
						break;
					case 0x4a:
						int id_2 = -1;
						if (buf.remaining() >= 2)
						{
							id_2 = buf.getShort() & 0xffff;
						}
						else
						{
							if (Config.PACKET_HANDLER_DEBUG)
							{
								_log.warning("Client: " + client.toString() + " sent a 0x4a without the second opcode.");
							}
							break;
						}
						switch (id_2)
						{
							case 0x00:
								// SuperCmdCharacterInfo
								break;
							case 0x01:
								// SuperCmdSummonCmd
								break;
							case 0x02:
								// SuperCmdServerStatus
								break;
							case 0x03:
								// SendL2ParamSetting
								break;
							default:
								printDebugDoubleOpcode(opcode, id_2, buf, state, client);
								break;
						}
						break;
					case 0x4d:
						msg = new RequestPledgeMemberList();
						break;
					case 0x4f:
						// RequestMagicList
						break;
					case 0x50:
						msg = new RequestSkillList();
						break;
					case 0x52:
						msg = new MoveWithDelta();
						break;
					case 0x53:
						msg = new RequestGetOnVehicle();
						break;
					case 0x54:
						msg = new RequestGetOffVehicle();
						break;
					case 0x55:
						msg = new AnswerTradeRequest();
						break;
					case 0x56:
						msg = new RequestActionUse();
						break;
					case 0x57:
						msg = new RequestRestart();
						break;
					case 0x58:
						msg = new RequestSiegeInfo();
						break;
					case 0x59:
						msg = new ValidatePosition();
						break;
					case 0x5a:
						// RequestSEKCustom
						break;
					case 0x5b:
						msg = new StartRotating();
						break;
					case 0x5c:
						msg = new FinishRotating();
						break;
					case 0x5e:
						msg = new RequestShowBoard();
						break;
					case 0x5f:
						msg = new RequestEnchantItem();
						break;
					case 0x60:
						msg = new RequestDestroyItem();
						break;
					case 0x62:
						msg = new RequestQuestList();
						break;
					case 0x63: // RequestDestroyQuest
						msg = new RequestQuestAbort();
						break;
					case 0x65:
						msg = new RequestPledgeInfo();
						break;
					case 0x66:
						msg = new RequestPledgeExtendedInfo();
						break;
					case 0x67:
						msg = new RequestPledgeCrest();
						break;
					case 0x6b: // RequestSendL2FriendSay
						msg = new RequestSendFriendMsg();
						break;
					case 0x6c:
						msg = new RequestShowMiniMap();
						break;
					case 0x6d:
						// RequestSendMsnChatLog
						break;
					case 0x6e: // RequestReload
						msg = new RequestRecordInfo();
						break;
					case 0x6f:
						msg = new RequestHennaEquip();
						break;
					case 0x70:
						msg = new RequestHennaRemoveList();
						break;
					case 0x71:
						msg = new RequestHennaItemRemoveInfo();
						break;
					case 0x72:
						msg = new RequestHennaRemove();
						break;
					case 0x73:
						msg = new RequestAcquireSkillInfo();
						break;
					case 0x74:
						msg = new SendBypassBuildCmd();
						break;
					case 0x75:
						msg = new RequestMoveToLocationInVehicle();
						break;
					case 0x76:
						msg = new CannotMoveAnymoreInVehicle();
						break;
					case 0x77:
						msg = new RequestFriendInvite();
						break;
					case 0x78:
						msg = new RequestAnswerFriendInvite();
						break;
					case 0x79:
						msg = new RequestFriendList();
						break;
					case 0x7a:
						msg = new RequestFriendDel();
						break;
					case 0x7c:
						msg = new RequestAcquireSkill();
						break;
					case 0x7d:
						msg = new RequestRestartPoint();
						break;
					case 0x7e:
						msg = new RequestGMCommand();
						break;
					case 0x7f:
						msg = new RequestPartyMatchConfig();
						break;
					case 0x80:
						msg = new RequestPartyMatchList();
						break;
					case 0x81:
						msg = new RequestPartyMatchDetail();
						break;
					case 0x83: // SendPrivateStoreBuyList
						msg = new RequestPrivateStoreBuy();
						break;
					case 0x85:
						msg = new RequestTutorialLinkHtml();
						break;
					case 0x86:
						msg = new RequestTutorialPassCmdToServer();
						break;
					case 0x87:
						msg = new RequestTutorialQuestionMark();
						break;
					case 0x88:
						msg = new RequestTutorialClientEvent();
						break;
					case 0x89:
						msg = new RequestPetition();
						break;
					case 0x8a:
						msg = new RequestPetitionCancel();
						break;
					case 0x8b:
						msg = new RequestGmList();
						break;
					case 0x8c:
						msg = new RequestJoinAlly();
						break;
					case 0x8d:
						msg = new RequestAnswerJoinAlly();
						break;
					case 0x8e: // RequestWithdrawAlly
						msg = new AllyLeave();
						break;
					case 0x8f: // RequestOustAlly
						msg = new AllyDismiss();
						break;
					case 0x90:
						msg = new RequestDismissAlly();
						break;
					case 0x91:
						msg = new RequestSetAllyCrest();
						break;
					case 0x92:
						msg = new RequestAllyCrest();
						break;
					case 0x93:
						msg = new RequestChangePetName();
						break;
					case 0x94:
						msg = new RequestPetUseItem();
						break;
					case 0x95:
						msg = new RequestGiveItemToPet();
						break;
					case 0x96:
						msg = new RequestPrivateStoreQuitSell();
						break;
					case 0x97:
						msg = new SetPrivateStoreMsgSell();
						break;
					case 0x98:
						msg = new RequestPetGetItem();
						break;
					case 0x99:
						msg = new RequestPrivateStoreManageBuy();
						break;
					case 0x9a: // SetPrivateStoreList
						msg = new SetPrivateStoreListBuy();
						break;
					case 0x9c:
						msg = new RequestPrivateStoreQuitBuy();
						break;
					case 0x9d:
						msg = new SetPrivateStoreMsgBuy();
						break;
					case 0x9f: // SendPrivateStoreBuyList
						msg = new RequestPrivateStoreSell();
						break;
					case 0xa0:
						// SendTimeCheckPacket
						break;
					case 0xa6:
						// msg = new RequestSkillCoolTime();
						break;
					case 0xa7:
						msg = new RequestPackageSendableItemList();
						break;
					case 0xa8:
						msg = new RequestPackageSend();
						break;
					case 0xa9:
						msg = new RequestBlock();
						break;
					case 0xaa:
						msg = new RequestSiegeInfo();
						break;
					case 0xab: // RequestCastleSiegeAttackerList
						msg = new RequestSiegeAttackerList();
						break;
					case 0xac:
						msg = new RequestSiegeDefenderList();
						break;
					case 0xad: // RequestJoinCastleSiege
						msg = new RequestJoinSiege();
						break;
					case 0xae: // RequestConfirmCastleSiegeWaitingList
						msg = new RequestConfirmSiegeWaitingList();
						break;
					case 0xAF:
						msg = new RequestSetCastleSiegeTime();
						break;
					case 0xb0:
						msg = new MultiSellChoose();
						break;
					case 0xb1:
						// NetPing
						break;
					case 0xb2:
						// RequestRemainTime
						break;
					case 0xb3:
						msg = new BypassUserCmd();
						break;
					case 0xb4:
						msg = new SnoopQuit();
						break;
					case 0xb5:
						msg = new RequestRecipeBookOpen();
						break;
					case 0xb6: // RequestRecipeItemDelete
						msg = new RequestRecipeBookDestroy();
						break;
					case 0xb7:
						msg = new RequestRecipeItemMakeInfo();
						break;
					case 0xb8:
						msg = new RequestRecipeItemMakeSelf();
						break;
					case 0xb9:
						// RequestRecipeShopManageList
						break;
					case 0xba:
						msg = new RequestRecipeShopMessageSet();
						break;
					case 0xbb:
						msg = new RequestRecipeShopListSet();
						break;
					case 0xbc:
						msg = new RequestRecipeShopManageQuit();
						break;
					case 0xbd:
						// RequestRecipeShopManageCancel
						break;
					case 0xbe:
						msg = new RequestRecipeShopMakeInfo();
						break;
					case 0xbf: // RequestRecipeShopMakeDo
						msg = new RequestRecipeShopMakeItem();
						break;
					case 0xc0: // RequestRecipeShopSellList
						msg = new RequestRecipeShopManagePrev();
						break;
					case 0xc1: // RequestObserverEndPacket
						msg = new ObserverReturn();
						break;
					case 0xc2:
						// Unused (RequestEvaluate/VoteSociality)
						break;
					case 0xc3:
						msg = new RequestHennaItemList();
						break;
					case 0xc4:
						msg = new RequestHennaItemInfo();
						break;
					case 0xc5:
						msg = new RequestBuySeed();
						break;
					case 0xc6: // ConfirmDlg
						msg = new DlgAnswer();
						break;
					case 0xc7:
						msg = new RequestPreviewItem();
						break;
					case 0xc8:
						// msg = new RequestSSQStatus();
						break;
					case 0xc9:
						msg = new RequestPetitionFeedback();
						break;
					case 0xcb:
						msg = new GameGuardReply();
						break;
					case 0xcc:
						msg = new RequestPledgePower();
						break;
					case 0xcd:
						msg = new RequestMakeMacro();
						break;
					case 0xce:
						msg = new RequestDeleteMacro();
						break;
					case 0xcf: // RequestProcureCrop
						// msg = new RequestBuyProcure();
						break;
					
					case 0xd0:
						int id2 = -1;
						if (buf.remaining() >= 2)
						{
							id2 = buf.getShort() & 0xffff;
						}
						else
						{
							if (Config.PACKET_HANDLER_DEBUG)
							{
								_log.warning("Client: " + client.toString() + " sent a 0xd0 without the second opcode.");
							}
							break;
						}
						switch (id2)
						{
							case 0x00:
								break;
							case 0x01:
								msg = new RequestManorList();
								break;
							case 0x02:
								msg = new RequestProcureCropList();
								break;
							case 0x03:
								msg = new RequestSetSeed();
								break;
							case 0x04:
								msg = new RequestSetCrop();
								break;
							case 0x05:
								msg = new RequestWriteHeroWords();
								break;
							case 0x06:
								msg = new RequestExAskJoinMPCC();
								break;
							case 0x07:
								msg = new RequestExAcceptJoinMPCC();
								break;
							case 0x08:
								msg = new RequestExOustFromMPCC();
								break;
							case 0x09:
								msg = new RequestOustFromPartyRoom();
								break;
							case 0x0a:
								msg = new RequestDismissPartyRoom();
								break;
							case 0x0b:
								msg = new RequestWithdrawPartyRoom();
								break;
							case 0x0c:
								msg = new RequestChangePartyLeader(); // RequestHandOverPartyMaster ?
								break;
							case 0x0d:
								msg = new RequestAutoSoulShot();
								break;
							case 0x0e:
								msg = new RequestExEnchantSkillInfo();
								break;
							case 0x0f:
								msg = new RequestExEnchantSkill();
								break;
							case 0x10:
								msg = new RequestExPledgeCrestLarge();
								break;
							case 0x11:
								int id9 = buf.getInt();
								switch (id9)
								{
								// @TODO: : RequestExSetPledgeCrestLarge is now chunked, each case is a different chunk
									case 0x00:
									{
										msg = new RequestExSetPledgeCrestLarge();// 0
										break;
									}
									case 0x01:
									{
										msg = new RequestExSetPledgeCrestLarge();// 1
										break;
									}
									case 0x02:
									{
										msg = new RequestExSetPledgeCrestLarge();// 2
										break;
									}
									case 0x03:
									{
										msg = new RequestExSetPledgeCrestLarge();// 3
										break;
									}
									case 0x04:
									{
										msg = new RequestExSetPledgeCrestLarge();// 4
										break;
									}
									default:
									{
										printDebugDoubleOpcode(opcode, id9, buf, state, client);
									}
								}
								break;
							case 0x12:
								msg = new RequestPledgeSetAcademyMaster();
								break;
							case 0x13:
								msg = new RequestPledgePowerGradeList();
								break;
							case 0x14:
								msg = new RequestPledgeMemberPowerInfo();
								break;
							case 0x15:
								msg = new RequestPledgeSetMemberPowerGrade();
								break;
							case 0x16:
								msg = new RequestPledgeMemberInfo();
								break;
							case 0x17:
								msg = new RequestPledgeWarList();
								break;
							case 0x18:
								msg = new RequestExFishRanking();
								break;
							case 0x19:
								msg = new RequestPCCafeCouponUse();
								break;
							case 0x1b:
								msg = new RequestDuelStart();
								break;
							case 0x1c:
								msg = new RequestDuelAnswerStart();
								break;
							case 0x1d:
								// RequestExSetTutorial
								break;
							case 0x1e:
								msg = new RequestExRqItemLink();
								break;
							case 0x1f:
								// CanNotMoveAnymoreAirShip
								break;
							case 0x20:
								msg = new MoveToLocationInAirShip();
								break;
							case 0x21:
								msg = new RequestKeyMapping();
								break;
							case 0x22:
								msg = new RequestSaveKeyMapping();
								break;
							case 0x23:
								msg = new RequestExRemoveItemAttribute();
								break;
							case 0x24:
								msg = new RequestSaveInventoryOrder();
								break;
							case 0x25:
								msg = new RequestExitPartyMatchingWaitingRoom();
								break;
							case 0x26:
								msg = new RequestConfirmTargetItem();
								break;
							case 0x27:
								msg = new RequestConfirmRefinerItem();
								break;
							case 0x28:
								msg = new RequestConfirmGemStone();
								break;
							case 0x29:
								msg = new RequestOlympiadObserverEnd();
								break;
							case 0x2a:
								msg = new RequestCursedWeaponList();
								break;
							case 0x2b:
								msg = new RequestCursedWeaponLocation();
								break;
							case 0x2c:
								msg = new RequestPledgeReorganizeMember();
								break;
							case 0x2d:
								msg = new RequestExMPCCShowPartyMembersInfo();
								break;
							case 0x2e:
								msg = new RequestOlympiadMatchList();
								break;
							case 0x2f:
								msg = new RequestAskJoinPartyRoom();
								break;
							case 0x30:
								msg = new AnswerJoinPartyRoom();
								break;
							case 0x31:
								msg = new RequestListPartyMatchingWaitingRoom();
								break;
							case 0x32:
								msg = new RequestExEnchantItemAttribute();
								break;
							case 0x33:
								msg = new RequestGotoLobby();
								break;
							case 0x35:
								msg = new MoveToLocationAirShip();
								break;
							case 0x36:
								msg = new RequestBidItemAuction();
								break;
							case 0x37:
								msg = new RequestInfoItemAuction();
								break;
							case 0x38:
								msg = new RequestExChangeName();
								break;
							case 0x39:
								msg = new RequestAllCastleInfo();
								break;
							case 0x3a:
								msg = new RequestAllFortressInfo();
								break;
							case 0x3b:
								msg = new RequestAllAgitInfo();
								break;
							case 0x3c:
								msg = new RequestFortressSiegeInfo();
								break;
							case 0x3d:
								msg = new RequestGetBossRecord();
								break;
							case 0x3e:
								msg = new RequestRefine();
								break;
							case 0x3f:
								msg = new RequestConfirmCancelItem();
								break;
							case 0x40:
								msg = new RequestRefineCancel();
								break;
							case 0x41:
								msg = new RequestExMagicSkillUseGround();
								break;
							case 0x42:
								msg = new RequestDuelSurrender();
								break;
							case 0x43:
								msg = new RequestExEnchantSkillInfoDetail();
								break;
							case 0x44:
								msg = new RequestExMagicSkillUseGround();
								break;
							case 0x45:
								msg = new RequestFortressMapInfo();
								break;
							case 0x46:
								msg = new RequestPVPMatchRecord();
								break;
							case 0x47:
								msg = new SetPrivateStoreWholeMsg();
								break;
							case 0x48:
								msg = new RequestDispel();
								break;
							case 0x49:
								msg = new RequestExTryToPutEnchantTargetItem();
								break;
							case 0x4a:
								msg = new RequestExTryToPutEnchantSupportItem();
								break;
							case 0x4b:
								msg = new RequestExCancelEnchantItem();
								break;
							case 0x4c:
								msg = new RequestChangeNicknameColor();
								break;
							case 0x4d:
								msg = new RequestResetNickname();
								break;
							case 0x4e:
								int id4 = buf.getInt();
								switch (id4)
								{
									case 0x00:
										msg = new RequestBookMarkSlotInfo();
										break;
									case 0x01:
										msg = new RequestSaveBookMarkSlot();
										break;
									case 0x02:
										msg = new RequestModifyBookMarkSlot();
										break;
									case 0x03:
										msg = new RequestDeleteBookMarkSlot();
										break;
									case 0x04:
										msg = new RequestTeleportBookMark();
										break;
									case 0x05:
										msg = new RequestChangeBookMarkSlot();
										break;
								}
								break;
							case 0x4f:
								msg = new RequestWithDrawPremiumItem();
								break;
							case 0x50:
								// @TODO: msg = new RequestExJump();
								break;
							case 0x51:
								// @TODO: msg = new RequestExStartShowCrataeCubeRank();
								break;
							case 0x52:
								// @TODO: msg = new RequestExStopShowCrataeCubeRank();
								break;
							case 0x53:
								// @TODO: msg = new NotifyStartMiniGame();
								break;
							case 0x54:
								// @TODO: msg = new RequestExJoinDominionWar();
								break;
							case 0x55:
								// @TODO: msg = new RequestExDominionInfo();
								break;
							case 0x56:
								// @TODO: msg = new RequestExCleftEnter();
								break;
							case 0x57:
								msg = new RequestExCubeGameChangeTeam();
								break;
							case 0x58:
								msg = new EndScenePlayer();
								break;
							case 0x59:
								msg = new RequestExCubeGameReadyAnswer();
								break;
							case 0x5A:
								// @TODO:msg = new RequestExListMpccWaiting();
								break;
							case 0x5B:
								// @TODO:msg = new RequestExManageMpccRoom();
								break;
							case 0x5C:
								// @TODO:msg = new RequestExJoinMpccRoom();
								break;
							case 0x5D:
								// TODO:: RequestExOustFromMpccRoom chddd
								break;
							case 0x5E:
								// TODO:: RequestExDismissMpccRoom chdddddS
								break;
							case 0x5F:
								// TODO:: RequestExWithdrawMpccRoom chdd
								break;
							case 0x60:
								msg = new RequestSeedPhase();
								break;
							case 0x61:
								// TODO:: msg = new RequestExMpccPartymasterList();
								break;
							case 0x62:
								msg = new RequestPostItemList();
								break;
							case 0x63:
								msg = new RequestSendPost();
								break;
							case 0x64:
								msg = new RequestReceivedPostList();
								break;
							case 0x65:
								msg = new RequestDeleteReceivedPost();
								break;
							case 0x66:
								msg = new RequestReceivedPost();
								break;
							case 0x67:
								msg = new RequestPostAttachment();
								break;
							case 0x68:
								msg = new RequestRejectPostAttachment();
								break;
							case 0x69:
								msg = new RequestSentPostList();
								break;
							case 0x6a:
								msg = new RequestDeleteSentPost();
								break;
							case 0x6b:
								msg = new RequestSentPost();
								break;
							case 0x6c:
								msg = new RequestCancelPostAttachment();
								break;
							case 0x6d:
								// @TODO: RequestShowNewUserPetition
								break;
							case 0x6e:
								// @TODO: RequestShowStepTwo
								break;
							case 0x6f:
								// @TODO: RequestShowStepThree
								break;
							case 0x70:
								// msg = new ExConnectToRaidServer(); (chddd)
								break;
							case 0x71:
								// msg = new ExReturnFromRaidServer(); (chd)
								break;
							case 0x72:
								msg = new RequestRefundItem();
								break;
							case 0x73:
								msg = new RequestBuySellUIClose();
								break;
							case 0x75:
								msg = new RequestPartyLootModification();
								break;
							case 0x76:
								msg = new AnswerPartyLootModification();
								break;
							case 0x77:
								msg = new AnswerCoupleAction();
								break;
							case 0x78:
								msg = new BrEventRankerList();
								break;
							case 0x79:
								// msg = new RequestAskMemberShip();
								break;
							case 0x7a:
								// @TODO: RequestAddExpandQuestAlarm
								break;
							case 0x7b:
								msg = new RequestVoteNew();
								break;
							case 0x7c:
								msg = new RequestShuttleGetOn();
								break;
							case 0x7d:
								msg = new RequestShuttleGetOff();
								break;
							case 0x7e:
								msg = new MoveToLocationInShuttle();
								break;
							case 0x7F:
								msg = new CannotMoveAnymoreInShuttle();
								break;
							case 0x80:
								int id5 = buf.getInt();
								switch (id5)
								{
									case 0x01:
									{
										// msg = new RequestExAgitInitialize chd 0x01
										break;
									}
									case 0x02:
									{
										// msg = new RequestExAgitDetailInfo chdcd 0x02
										break;
									}
									case 0x03:
									{
										// msg = new RequestExMyAgitState chd 0x03
										break;
									}
									case 0x04:
									{
										// msg = new RequestExRegisterAgitForBidStep1 chd 0x04
										break;
									}
									case 0x05:
									{
										// msg = new RequestExRegisterAgitForBidStep2 chddQd
									}
									case 0x06:
									{
										// msg = new RequestExRegisterAgitForBidStep3 // chddQd 0x05 -no error? 0x05
										break;
									}
									case 0x07:
									{
										// msg = new RequestExConfirmCancelRegisteringAgit chd 0x07
										break;
									}
									case 0x08:
									{
										// msg = new RequestExProceedCancelRegisteringAgit chd 0x08
										break;
									}
									case 0x09:
									{
										// msg = new RequestExConfirmCancelAgitBid chdd 0x09
										break;
									}
									case 0x10:
									{
										// msg = new RequestExReBid chdd 0x10
										break;
									}
									case 0x11:
									{
										// msg = new RequestExAgitListForLot chd 0x11
										break;
									}
									case 0x12:
									{
										// msg = new RequestExApplyForAgitLotStep1 chdc 0x12
										break;
									}
									case 0x13:
									{
										// msg = new RequestExApplyForAgitLotStep2 chdc 0x13
										break;
									}
									case 0x14:
									{
										// msg = new RequestExAgitListForBid chdd 0x14
										break;
									}
									case 0x0D:
									{
										// msg = new RequestExApplyForBidStep1 chdd 0x0D
										break;
									}
									case 0x0E:
									{
										// msg = new RequestExApplyForBidStep2 chddQ 0x0E
										break;
									}
									case 0x0F:
									{
										// msg = new RequestExApplyForBidStep3 chddQ 0x0F
										break;
									}
									case 0x0A:
									{
										// msg = new RequestExProceedCancelAgitLot chdc 0x0A
										break;
									}
									default:
									{
										printDebugDoubleOpcode(opcode, id5, buf, state, client);
									}
								}
								break;
							case 0x81:
								msg = new RequestExAddContactToContactList(); // RequestExAddPostFriendForPostBox
								break;
							case 0x82:
								msg = new RequestExDeleteContactFromContactList(); // RequestExDeletePostFriendForPostBox
								break;
							case 0x83:
								msg = new RequestExShowContactList(); // RequestExShowPostFriendListForPostBox
								break;
							case 0x84:
								msg = new RequestExFriendListExtended(); // RequestExFriendListForPostBox
								break;
							case 0x85:
								msg = new RequestExOlympiadMatchListRefresh(); // RequestOlympiadMatchList
								break;
							case 0x86:
								// msg = new RequestBRGamePoint();
								break;
							case 0x87:
								// msg = new RequestBRProductList();
								break;
							case 0x88:
								// msg = new RequestBRProductInfo();
								break;
							case 0x89:
								// msg = new RequestBRBuyProduct();
								break;
							case 0x8A:
								// msg = new RequestBRRecentProductList();
								break;
							case 0x8B:
								// @ msg = new RequestBR_MiniGameLoadScores();
								break;
							case 0x8C:
								// @ msg = new RequestBR_MiniGameInsertScore();
								break;
							case 0x8D:
								// @ msg = new RequestExBR_LectureMark();
								break;
							case 0x8E:
								msg = new RequestCrystallizeEstimate();
								break;
							case 0x8F:
								msg = new RequestCrystallizeItemCancel();
								break;
							case 0x90:
								// @ msg = new RequestExEscapeScene();
								break;
							case 0x91:
								msg = new RequestFlyMove();
								break;
							case 0x92:
								// msg = new RequestSurrenderPledgeWarEX(); (chS)
								break;
							case 0x93:
							{
								int id6 = buf.getInt();
								switch (id6)
								{
									case 0x02:
										// msg = new RequestDynamicQuestProgressInfo()
										break;
									case 0x03:
										// msg = new RequestDynamicQuestScoreBoard();
										break;
									case 0x04:
										// msg = new RequestDynamicQuestHTML();
										break;
									default:
										printDebugDoubleOpcode(opcode, id6, buf, state, client);
										break;
								}
								
								break;
							}
							case 0x94:
								msg = new RequestFriendDetailInfo();
								break;
							case 0x95:
								msg = new RequestUpdateFriendMemo();
								break;
							case 0x96:
								msg = new RequestUpdateBlockMemo();
								break;
							case 0x97:
								// msg = new RequestInzonePartyInfoHistory();
								break;
							case 0x98:
								msg = new RequestCommissionRegistrableItemList();
								break;
							case 0x99:
								msg = new RequestCommissionInfo();
								break;
							case 0x9A:
								msg = new RequestCommissionRegister();
								break;
							case 0x9B:
								msg = new RequestCommissionCancel();
								break;
							case 0x9C:
								msg = new RequestCommissionDelete();
								break;
							case 0x9D:
								msg = new RequestCommissionList();
								break;
							case 0x9E:
								msg = new RequestCommissionBuyInfo();
								break;
							case 0x9F:
								msg = new RequestCommissionBuyItem();
								break;
							case 0xA0:
								msg = new RequestCommissionRegisteredItem();
								break;
							case 0xA1:
								// msg = new RequestCallToChangeClass();
								break;
							case 0xA2:
								msg = new RequestChangeToAwakenedClass();
								break;
							case 0xA3:
								// msg = new RequestWorldStatistics();
								break;
							case 0xA4:
								// msg = new RequestUserStatistics();
								break;
							case 0xA5:
								// msg = new Request24HzSessionID();
								break;
							case 0xAA:
								// msg = new RequestGoodsInventoryInfo();
								break;
							case 0xAB:
								// msg = new RequestUseGoodsInventoryItem();
								break;
							case 0xAC:
								// msg = new RequestFirstPlayStart();
								break;
							case 0xAD:
								msg = new RequestFlyMoveStart();
								break;
							case 0xAE:
							case 0xAF:
								// msg = new RequestHardWareInfo(); (SddddddSddddddddddSS)
								break;
							case 0xB0:
								// msg = new SendChangeAttributeTargetItem();
								break;
							case 0xB1:
								// msg = new RequestChangeAttributeItem();
								break;
							case 0xB2:
								// msg = new RequestChangeAttributeCancel();
								break;
							case 0xB3:
								// msg = new RequestBR_PresentBuyProduct();
								break;
							case 0xB4:
								msg = new ConfirmMenteeAdd();
								break;
							case 0xB5:
								msg = new RequestMentorCancel();
								break;
							case 0xB6:
								msg = new RequestMentorList();
								break;
							case 0xB7:
								msg = new RequestMenteeAdd();
								break;
							case 0xB8:
								msg = new RequestMenteeWaitingList();
								break;
							case 0xB9:
								// msg = new RequestClanAskJoinByName();
								break;
							case 0xBA:
								msg = new RequestInzoneWaitingTime();
								break;
							case 0xBB:
								// msg = new RequestJoinCuriousHouse();// (ch)
								break;
							case 0xBC:
								// msg = new RequestCancelCuriousHouse();// (ch)
								break;
							case 0xBD:
								// msg = new RequestLeaveCuriousHouse();// (ch)
								break;
							case 0xBE:
								// msg = new RequestObservingListCuriousHouse();// (ch)
								break;
							case 0xBF:
								// msg = new RequestObservingCuriousHouse();// (chd)
								break;
							case 0xC0:
								// msg = new RequestLeaveObservingCuriousHouse();// (ch)
								break;
							case 0xC1:
								// msg = new RequestCuriousHouseHtml();// (ch)
								break;
							case 0xC2:
								// msg = new RequestCuriousHouseRecord();// (ch)
								break;
							case 0xC3:
								// msg = new ExSysstring(); (chdS)
								break;
							case 0xC4:
								// msg = new RequestExTryToPutShapeShiftingTargetItem();
								break;
							case 0xC5:
								// msg = new RequestExTryToPutShapeShiftingEnchantSupportItem();
								break;
							case 0xC6:
								// msg = new RequestExCancelShape_Shifting_Item();
								break;
							case 0xC7:
								// msg = new RequestShapeShiftingItem();
								break;
							case 0xC8:
								// msg = new NCGuardSendDataToServer(); // (chdb)
								break;
							case 0xC9:
								// msg = new RequestEventKalieToken(); // (d)
								break;
							case 0xCA:
								msg = new RequestShowBeautyList();
								break;
							case 0xCB:
								msg = new RequestRegistBeauty();
								break;
							case 0xCC:
								break;
							case 0xCD:
								msg = new RequestShowResetShopList();
								break;
							case 0xCE:
								// NetPing
								break;
							case 0xCF:
								// RequestBR_AddBasketProductInfo
								break;
							case 0xD0:
								// RequestBR_DeleteBasketProductInfo
								break;
							case 0xD1:
								break;
							case 0xD2:
								// RequestExEvent_Campaign_Info
								break;
							case 0xD3:
								msg = new RequestPledgeRecruitInfo();
								break;
							case 0xD4:
								msg = new RequestPledgeRecruitBoardSearch();
								break;
							case 0xD5:
								msg = new RequestPledgeRecruitBoardAccess();
								break;
							case 0xD6:
								msg = new RequestPledgeRecruitBoardDetail();
								break;
							case 0xD7:
								msg = new RequestPledgeWaitingApply();
								break;
							case 0xD8:
								msg = new RequestPledgeWaitingApplied();
								break;
							case 0xD9:
								msg = new RequestPledgeWaitingList();
								break;
							case 0xDA:
								msg = new RequestPledgeWaitingUser();
								break;
							case 0xDB:
								msg = new RequestPledgeWaitingUserAccept();
								break;
							case 0xDC:
								msg = new RequestPledgeDraftListSearch();
								break;
							case 0xDD:
								msg = new RequestPledgeDraftListApply();
								break;
							case 0xDE:
								msg = new RequestPledgeRecruitApplyInfo();
								break;
							case 0xDF:
								// msg = new RequestPledgeJoinSys();
								break;
							case 0xE0:
								// ResponsePetitionAlarm
								break;
							case 0xE1:
								msg = new NotifyExitBeautyShop();
								break;
							case 0xE2:
								// RequestRegisterXMasWishCard
								break;
							case 0xE3:
								msg = new RequestExAddEnchantScrollItem();
								break;
							case 0xE4:
								msg = new RequestExRemoveEnchantSupportItem();
								break;
							case 0xE5:
								// msg = new RequestCardReward();
								break;
							case 0xE6:
								// msg = new RequestDivideAdenaStart();
								break;
							case 0xE7:
								// msg = new RequestDivideAdenaCancel();
								break;
							case 0xE8:
								// msg = new RequestDivideAdena();
								break;
							case 0xE9:
								msg = new RequestAcquireAbilityList();
								break;
							case 0xEA:
								msg = new RequestAbilityList();
								break;
							case 0xEB:
								msg = new RequestResetAbilityPoint();
								break;
							case 0xEC:
								msg = new RequestChangeAbilityPoint();
								break;
							case 0xED:
								// msg = new RequestStopMove();
								break;
							case 0xEE:
								msg = new RequestAbilityWndOpen();
								break;
							case 0xEF:
								msg = new RequestAbilityWndClose();
								break;
							case 0xF0:
								// msg = ExPCCafeRequestOpenWindowWithoutNPC();
								break;
							case 0xF2:
								// msg = new RequestLuckyGamePlay();
								break;
							case 0xF3:
								// msg = new NotifyTrainingRoomEnd();
								break;
							case 0xF4:
								// msg = new RequestNewEnchantPushOne();
								break;
							case 0xF5:
								// msg = new RequestNewEnchantRemoveOne();
								break;
							case 0xF6:
								// msg = new RequestNewEnchantPushTwo();
								break;
							case 0xF7:
								// msg = new RequestNewEnchantRemoveTwo();
								break;
							case 0xF8:
								// nsg = new RequestNewEnchantClose();
								break;
							case 0xF9:
								// msg = new RequestNewEnchantTry();
								break;
							case 0xFE:
								// msg = new ExSendSelectedQuestZoneID();
								break;
							case 0xFF:
								msg = new RequestAlchemySkillList();
								break;
							case 0x100:
								// msg = new RequestAlchemyTryMixCube();
								break;
							case 0x101:
								// msg = new RequestAlchemyConversion();
								break;
							case 0x102:
								// msg = new SendExecutedUIEventsCount();
								break;
							case 0x103:
								// msg = new ExSendClientINI();
								break;
							case 0x104:
								// msg = new RequestExAutoFish();
								break;
							default:
								printDebugDoubleOpcode(opcode, id2, buf, state, client);
								break;
						}
						break;
					default:
						printDebug(opcode, buf, state, client);
						break;
				}
				break;
		}
		return msg;
	}
	
	private void printDebug(int opcode, ByteBuffer buf, GameClientState state, L2GameClient client)
	{
		client.onUnknownPacket();
		if (!Config.PACKET_HANDLER_DEBUG)
		{
			return;
		}
		
		int size = buf.remaining();
		_log.warning("Unknown Packet: 0x" + Integer.toHexString(opcode) + " on State: " + state.name() + " Client: " + client.toString());
		byte[] array = new byte[size];
		buf.get(array);
		_log.warning(Util.printData(array, size));
	}
	
	private void printDebugDoubleOpcode(int opcode, int id2, ByteBuffer buf, GameClientState state, L2GameClient client)
	{
		client.onUnknownPacket();
		if (!Config.PACKET_HANDLER_DEBUG)
		{
			return;
		}
		
		int size = buf.remaining();
		_log.warning("Unknown Packet: 0x" + Integer.toHexString(opcode) + ":0x" + Integer.toHexString(id2) + " on State: " + state.name() + " Client: " + client.toString());
		byte[] array = new byte[size];
		buf.get(array);
		_log.warning(Util.printData(array, size));
	}
	
	// impl
	@Override
	public L2GameClient create(MMOConnection<L2GameClient> con)
	{
		return new L2GameClient(con);
	}
	
	@Override
	public void execute(ReceivablePacket<L2GameClient> rp)
	{
		rp.getClient().execute(rp);
	}
}
