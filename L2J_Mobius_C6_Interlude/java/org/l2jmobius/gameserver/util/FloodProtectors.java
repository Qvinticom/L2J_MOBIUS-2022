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
package org.l2jmobius.gameserver.util;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.network.GameClient;

/**
 * Collection of flood protectors for single player.
 * @author fordfrog
 */
public class FloodProtectors
{
	private final FloodProtectorAction _useItem;
	private final FloodProtectorAction _rollDice;
	private final FloodProtectorAction _itemPetSummon;
	private final FloodProtectorAction _heroVoice;
	private final FloodProtectorAction _globalChat;
	private final FloodProtectorAction _subclass;
	private final FloodProtectorAction _dropItem;
	private final FloodProtectorAction _augmentScript;
	private final FloodProtectorAction _enchantItem;
	private final FloodProtectorAction _serverBypass;
	private final FloodProtectorAction _multiSell;
	private final FloodProtectorAction _transaction;
	private final FloodProtectorAction _manufacture;
	private final FloodProtectorAction _characterSelect;
	private final FloodProtectorAction _sayAction;
	private final FloodProtectorAction _moveAction;
	private final FloodProtectorAction _macro;
	private final FloodProtectorAction _playerAction;
	
	/**
	 * Creates new instance of FloodProtectors.
	 * @param client game client for which the collection of flood protectors is being created.
	 */
	public FloodProtectors(GameClient client)
	{
		super();
		_useItem = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_USE_ITEM);
		_rollDice = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_ROLL_DICE);
		_itemPetSummon = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_ITEM_PET_SUMMON);
		_heroVoice = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_HERO_VOICE);
		_globalChat = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_GLOBAL_CHAT);
		_subclass = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SUBCLASS);
		_dropItem = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_DROP_ITEM);
		_augmentScript = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_AUGMENT_SCRIPT);
		_enchantItem = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_ENCHANT_ITEM);
		_serverBypass = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SERVER_BYPASS);
		_multiSell = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MULTISELL);
		_transaction = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_TRANSACTION);
		_manufacture = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MANUFACTURE);
		_characterSelect = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_CHARACTER_SELECT);
		_sayAction = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SAY_ACTION);
		_moveAction = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MOVE_ACTION);
		_macro = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MACRO);
		_playerAction = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_PLAYER_ACTION);
	}
	
	public boolean canUseItem()
	{
		return _useItem.canPerformAction();
	}
	
	public boolean canRollDice()
	{
		return _rollDice.canPerformAction();
	}
	
	public boolean canUsePetSummonItem()
	{
		return _itemPetSummon.canPerformAction();
	}
	
	public boolean canUseHeroVoice()
	{
		return _heroVoice.canPerformAction();
	}
	
	public boolean canUseGlobalChat()
	{
		return _globalChat.canPerformAction();
	}
	
	public boolean canChangeSubclass()
	{
		return _subclass.canPerformAction();
	}
	
	public boolean canDropItem()
	{
		return _dropItem.canPerformAction();
	}
	
	public boolean canAugmentItem()
	{
		return _augmentScript.canPerformAction();
	}
	
	public boolean canEnchantItem()
	{
		return _enchantItem.canPerformAction();
	}
	
	public boolean canUseServerBypass()
	{
		return _serverBypass.canPerformAction();
	}
	
	public boolean canUseMultiSell()
	{
		return _multiSell.canPerformAction();
	}
	
	public boolean canPerformTransaction()
	{
		return _transaction.canPerformAction();
	}
	
	public boolean canManufacture()
	{
		return _manufacture.canPerformAction();
	}
	
	public boolean canSelectCharacter()
	{
		return _characterSelect.canPerformAction();
	}
	
	public boolean canPerformSayAction()
	{
		return _sayAction.canPerformAction();
	}
	
	public boolean canPerformMoveAction()
	{
		return _moveAction.canPerformAction();
	}
	
	public boolean canUseMacro()
	{
		return _macro.canPerformAction();
	}
	
	public boolean canPerformPlayerAction()
	{
		return _playerAction.canPerformAction();
	}
}