/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.serverpackets.auctionhouse;

import com.l2jserver.gameserver.instancemanager.AuctionHouseManager;
import com.l2jserver.gameserver.instancemanager.AuctionHouseManager.Auctions;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.serverpackets.AbstractItemPacket;

/**
 * @author Erlandys
 */
public class ExResponseCommissionList extends AbstractItemPacket
{
	L2PcInstance _player;
	L2ItemInstance _item;
	long _category;
	int _type;
	int _grade;
	String _search;
	boolean _yourAuction;
	AuctionHouseManager _am;
	int _yourAuctionsSize = 0;
	int _categories[][] =
	{
		{
			1,
			2,
			3,
			4,
			5,
			6,
			7,
			8,
			9,
			10,
			11,
			12,
			13,
			14,
			15,
			16,
			17,
			18
		},
		{
			19,
			20,
			21,
			22,
			23,
			24,
			25,
			26,
			27,
			28
		},
		{
			29,
			30,
			31,
			32,
			33,
			34
		},
		{
			35,
			36,
			37,
			38,
			39,
			40
		},
		{
			41,
			42
		},
		{
			43,
			44,
			45,
			46,
			47,
			48,
			49,
			50,
			51,
			52,
			53,
			54,
			55,
			56,
			57,
			58
		}
	};
	
	public ExResponseCommissionList(L2PcInstance player, long category, int type, int grade, String searchName)
	{
		_player = player;
		_category = category;
		_type = type;
		_grade = grade;
		_search = searchName;
		_yourAuction = false;
		_am = AuctionHouseManager.getInstance();
	}
	
	public ExResponseCommissionList(L2PcInstance player)
	{
		_player = player;
		_yourAuction = true;
		_am = AuctionHouseManager.getInstance();
		for (Auctions auction : _am.getAuctions())
		{
			if (auction.getPlayerID() == player.getObjectId())
			{
				_yourAuctionsSize++;
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xF7);
		if (_yourAuction)
		{
			writeD(_yourAuctionsSize <= 0 ? -2 : 0x02);
			writeD((int) (System.currentTimeMillis() / 1000));
			writeD(0x00);
			writeD(_yourAuctionsSize);
			for (Auctions auction : _am.getAuctions())
			{
				if (auction.getPlayerID() == _player.getObjectId())
				{
					writeAuctionItem(auction);
				}
			}
		}
		else
		{
			writeD((_search != null) && (_category == 100) && (_am.getAuctionsSizeById(_grade, _search) > 0) ? 3 : (_am.getAuctionsSizeById(_grade, _search) <= 0) || (_am.getAuctionsSizeById(_category, _grade, _search) <= 0) ? -1 : 3);
			writeD((int) (System.currentTimeMillis() / 1000));
			writeD(0x00);
			if (((_category > 60) && (_category < 66)) || (_category == 101))
			{
				writeD(_am.getAuctionsSizeById(_category, _grade, _search));
				for (Auctions auction : _am.getAuctions())
				{
					int cat = _category == 101 ? 0 : (int) (_category % 60);
					for (int ID : _categories[cat])
					{
						if ((_grade == -1) && _search.equals(""))
						{
							if (auction.getCategory() == ID)
							{
								writeAuctionItem(auction);
							}
						}
						else if (_grade != -1)
						{
							if (_search.equals(""))
							{
								if ((auction.getCategory() == ID) && (_grade == auction.getItem().getItem().getCrystalType().getId()))
								{
									writeAuctionItem(auction);
								}
							}
							if (!_search.equals(""))
							{
								if ((auction.getCategory() == ID) && (_grade == auction.getItem().getItem().getCrystalType().getId()) && auction.getItem().getName().contains(_search))
								{
									writeAuctionItem(auction);
								}
							}
						}
						else if (!_search.equals(""))
						{
							if ((auction.getCategory() == ID) && auction.getItem().getName().contains(_search))
							{
								writeAuctionItem(auction);
							}
						}
					}
				}
			}
			else if (_category < 60)
			{
				writeD(_am.getAuctionsSizeById(_category, _grade, _search)); // Auction count, maybe items putted in auction???
				for (Auctions auction : _am.getAuctions())
				{
					if ((_grade == -1) && _search.equals(""))
					{
						if (auction.getCategory() == _category)
						{
							writeAuctionItem(auction);
						}
					}
					else if (_grade != -1)
					{
						if (_search.equals(""))
						{
							if ((auction.getCategory() == _category) && (_grade == auction.getItem().getItem().getCrystalType().getId()))
							{
								writeAuctionItem(auction);
							}
						}
						if (!_search.equals(""))
						{
							if ((auction.getCategory() == _category) && (_grade == auction.getItem().getItem().getCrystalType().getId()) && auction.getItem().getName().contains(_search))
							{
								writeAuctionItem(auction);
							}
						}
					}
					else if (!_search.equals(""))
					{
						if ((auction.getCategory() == _category) && auction.getItem().getName().contains(_search))
						{
							writeAuctionItem(auction);
						}
					}
				}
			}
			else
			{
				if (_search != null)
				{
					writeD(_am.getAuctionsSizeById(_grade, _search)); // Auction count, maybe items putted in auction???
					for (Auctions auction : _am.getAuctions())
					{
						if (_grade == -1)
						{
							if (auction.getItem().getName().contains(_search))
							{
								writeAuctionItem(auction);
							}
						}
						if (_grade != -1)
						{
							if ((_grade == auction.getItem().getItem().getCrystalType().getId()) && auction.getItem().getName().contains(_search))
							{
								writeAuctionItem(auction);
							}
						}
					}
				}
			}
		}
	}
}
