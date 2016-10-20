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
package com.l2jmobius.gameserver.network.serverpackets;

import com.l2jmobius.gameserver.datatables.SkillData;
import com.l2jmobius.gameserver.enums.ShortcutType;
import com.l2jmobius.gameserver.model.L2Augmentation;
import com.l2jmobius.gameserver.model.Shortcut;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;

public final class ShortCutInit extends L2GameServerPacket
{
	private ShortcutInfo[] _shortCuts;
	
	public ShortCutInit(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
		
		final Shortcut[] tmp = activeChar.getAllShortCuts();
		_shortCuts = new ShortcutInfo[tmp.length];
		
		int index = -1;
		for (Shortcut shortCut : tmp)
		{
			index++;
			_shortCuts[index] = convert(activeChar, shortCut);
		}
	}
	
	/**
	 * @param player
	 * @param shortCut
	 * @return
	 */
	private ShortcutInfo convert(L2PcInstance player, Shortcut shortCut)
	{
		ShortcutInfo shortcutInfo = null;
		final int page = shortCut.getSlot() + (shortCut.getPage() * 12);
		final ShortcutType type = shortCut.getType();
		final int id = shortCut.getId();
		int characterType = 0;
		
		switch (type)
		{
			case ITEM:
			{
				int reuseGroup = -1;
				final int currentReuse = 0, reuse = 0;
				int augmentationId = 0;
				
				characterType = shortCut.getCharacterType();
				final L2ItemInstance item = player.getInventory().getItemByObjectId(shortCut.getId());
				
				if (item != null)
				{
					final L2Augmentation augmentation = item.getAugmentation();
					if (augmentation != null)
					{
						augmentationId = augmentation.getAugmentationId();
					}
					
					reuseGroup = shortCut.getSharedReuseGroup();
				}
				
				shortcutInfo = new ItemShortcutInfo(type, page, id, reuseGroup, currentReuse, reuse, augmentationId, characterType);
				break;
			}
			case SKILL:
			{
				final int level = shortCut.getLevel();
				final int skillReuseGroup = shortCut.getSharedReuseGroup();
				final boolean isDisabled = false; // FIXME: To implement !!!
				shortcutInfo = new SkillShortcutInfo(type, page, id, skillReuseGroup, level, isDisabled, characterType);
				break;
			}
			default:
			{
				shortcutInfo = new ShortcutInfo(type, page, id, characterType);
				break;
			}
		}
		
		return shortcutInfo;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x45);
		writeD(_shortCuts.length);
		for (ShortcutInfo sc : _shortCuts)
		{
			writeD(sc.getType().ordinal());
			writeD(sc.getPage());
			
			switch (sc.getType())
			{
				case ITEM:
				{
					final ItemShortcutInfo item = (ItemShortcutInfo) sc;
					
					writeD(item.getId());
					writeD(item.getCharacterType());
					writeD(item.getReuseGroup());
					writeD(item.getCurrentReuse());
					writeD(item.getBasicReuse());
					writeD(item.get1stAugmentationId());
					writeD(item.get2ndAugmentationId());
					writeD(0x00); // TODO: Find me!
					break;
				}
				case SKILL:
				{
					final SkillShortcutInfo skill = (SkillShortcutInfo) sc;
					
					writeD(skill.getId());
					if ((skill.getLevel() < 100) || (skill.getLevel() > 10000))
					{
						writeD(skill.getLevel());
					}
					else
					{
						final int maxLevel = SkillData.getInstance().getMaxLevel(skill.getId());
						writeH(maxLevel);
						writeH(skill.getLevel());
					}
					writeD(skill.getReuseGroup());
					writeC(skill.isDisabled());
					writeD(skill.getCharacterType());
					break;
				}
				case ACTION:
				case MACRO:
				case RECIPE:
				case BOOKMARK:
				{
					writeD(sc.getId());
					writeD(sc.getCharacterType());
				}
			}
		}
	}
	
	protected class ShortcutInfo
	{
		private final ShortcutType _type;
		private final int _page;
		protected final int _id;
		protected final int _characterType;
		
		ShortcutInfo(ShortcutType type, int page, int id, int characterType)
		{
			_type = type;
			_page = page;
			_id = id;
			_characterType = characterType;
		}
		
		public ShortcutType getType()
		{
			return _type;
		}
		
		public int getPage()
		{
			return _page;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public int getCharacterType()
		{
			return _characterType;
		}
	}
	
	private class SkillShortcutInfo extends ShortcutInfo
	{
		private final int _reuseGroup;
		private final int _level;
		private final boolean _isDisabled;
		
		SkillShortcutInfo(ShortcutType type, int page, int id, int reuseGroup, int level, boolean isDisabled, int characterType)
		{
			super(type, page, id, characterType);
			_level = level;
			_reuseGroup = reuseGroup;
			_isDisabled = isDisabled;
		}
		
		/**
		 * @return
		 */
		public boolean isDisabled()
		{
			return _isDisabled;
		}
		
		public int getReuseGroup()
		{
			return _reuseGroup;
		}
		
		public int getLevel()
		{
			return _level;
		}
	}
	
	private class ItemShortcutInfo extends ShortcutInfo
	{
		private final int _reuseGroup;
		private final int _currentReuse;
		private final int _basicReuse;
		private final int _augmentationId;
		
		ItemShortcutInfo(ShortcutType type, int page, int id, int reuseGroup, int currentReuse, int basicReuse, int augmentationId, int characterType)
		{
			super(type, page, id, characterType);
			_reuseGroup = reuseGroup;
			_currentReuse = currentReuse;
			_basicReuse = basicReuse;
			_augmentationId = augmentationId;
		}
		
		public int getReuseGroup()
		{
			return _reuseGroup;
		}
		
		public int getCurrentReuse()
		{
			return _currentReuse;
		}
		
		public int getBasicReuse()
		{
			return _basicReuse;
		}
		
		public int get1stAugmentationId()
		{
			return 0x0000FFFF & _augmentationId;
		}
		
		public int get2ndAugmentationId()
		{
			return _augmentationId >> 16;
		}
	}
}
