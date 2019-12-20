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
package org.l2jmobius.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class implements a Box instance which is used like a warehouse but<br>
 * without limits on item numbers. It can be thought of a box with six drawers.<br>
 * Depositing and withdrawing is done through a HTML dialog to circumvent the<br>
 * package size limit of the warehouse dialog.<br>
 * <br>
 * Access to a box has to be set by an admin and uses the players' names for<br>
 * easier handling (s. admin command admin_box_access).<br>
 * <br>
 * To spawn an BoxInstance change the type in npc to Box.<br>
 * example: update npc set type='L2Box' where id=1042;<br>
 * It uses two tables: boxes and boxaccess. Create these with:<br>
 * <code>
  CREATE TABLE boxes (<br>
  id int(11) NOT NULL auto_increment,<br>
  spawn decimal(11,0) default NULL,<br>
  npcid decimal(11,0) default NULL,<br>
  drawer varchar(32) default NULL,<br>
  itemid decimal(11,0) default NULL,<br>
  name varchar(32) default '',<br>
  count decimal(11,0) default NULL,<br>
  enchant decimal(2,0) default NULL,<br>
  PRIMARY KEY  (id)<br>
  ) TYPE=MyISAM;<br>
  CREATE TABLE boxaccess (<br>
  spawn decimal(11,0) default NULL,<br>
  charname varchar(32) default NULL<br>
  ) TYPE=MyISAM;<br></code>
 */
public class BoxInstance extends NpcInstance
{
	
	private class BoxItem implements Comparable<Object>
	{
		public int itemid;
		public int id;
		public int count;
		public String name;
		
		public BoxItem(int itemid, int count, String name, int id)
		{
			this.itemid = itemid;
			this.count = count;
			this.name = name;
			this.id = id;
		}
		
		@Override
		public int compareTo(Object o)
		{
			final int r = name.compareToIgnoreCase(((BoxItem) o).name);
			if (r != 0)
			{
				return r;
			}
			
			if (id < ((BoxItem) o).id)
			{
				return -1;
			}
			
			return 1;
		}
	}
	
	private static final int MAX_ITEMS_PER_PAGE = 25;
	private static final String INSERT_GRANT = "INSERT INTO boxaccess (charname,spawn) VALUES(?,?)";
	private static final String DELETE_GRANT = "DELETE FROM boxaccess WHERE charname=? AND spawn=?";
	private static final String LIST_GRANT = "SELECT charname FROM boxaccess WHERE spawn=?";
	private static final String VARIABLE_PREFIX = "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	
	public BoxInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		String playerName = player.getName();
		final boolean access = hasAccess(playerName);
		
		if (command.startsWith("Withdraw"))
		{
			if (access)
			{
				showWithdrawWindow(player, command.substring(9));
			}
		}
		else if (command.startsWith("Deposit"))
		{
			if (access)
			{
				showDepositWindow(player, command.substring(8));
			}
		}
		else if (command.startsWith("InBox"))
		{
			if (access)
			{
				putInBox(player, command.substring(6));
			}
		}
		else if (command.startsWith("OutBox"))
		{
			if (access)
			{
				takeOutBox(player, command.substring(7));
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		return "data/html/custom/" + pom + ".htm";
	}
	
	public boolean hasAccess(String player)
	{
		boolean result = false;
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement st = con.prepareStatement("SELECT spawn, charname FROM boxaccess WHERE charname=? AND spawn=?");
			st.setString(1, player);
			st.setInt(2, getSpawn().getId());
			ResultSet rs = st.executeQuery();
			
			if (rs.next())
			{
				result = true;
			}
			
			rs.close();
			st.close();
		}
		catch (Exception e)
		{
			LOGGER.info("hasAccess failed: " + e);
		}
		return result;
	}
	
	public List<String> getAccess()
	{
		final List<String> acl = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement st = con.prepareStatement(LIST_GRANT);
			st.setInt(1, getSpawn().getId());
			ResultSet rs = st.executeQuery();
			
			while (rs.next())
			{
				acl.add(rs.getString("charname"));
			}
			rs.close();
			st.close();
		}
		catch (Exception e)
		{
			LOGGER.info("getAccess failed: " + e);
		}
		return acl;
	}
	
	public void grantAccess(String player, boolean what)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			String query;
			if (what)
			{
				query = INSERT_GRANT;
			}
			else
			{
				query = DELETE_GRANT;
			}
			
			PreparedStatement st = con.prepareStatement(query);
			st.setString(1, player);
			st.setInt(2, getSpawn().getId());
			st.execute();
			st.close();
		}
		catch (Exception e)
		{
		}
	}
	
	private void showWithdrawWindow(PlayerInstance player, String command)
	{
		String drawername = "trash";
		
		if (command == null)
		{
			return;
		}
		
		String[] cmd = command.split(" ");
		int startPos = 0;
		
		if (cmd != null)
		{
			drawername = cmd[0];
			if (cmd.length > 1)
			{
				startPos = Integer.parseInt(cmd[1]);
			}
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		int nitems = 0;
		Set<BoxItem> items = getItems(drawername);
		
		if (startPos >= items.size())
		{
			startPos = 0;
		}
		
		String button = "<button value=\"Withdraw\" width=80 height=15 action=\"bypass -h npc_" + getObjectId() + "_OutBox " + drawername;
		String next = "<button value=\"next\" width=50 height=15 action=\"bypass -h npc_" + getObjectId() + "_Withdraw " + drawername + " " + (startPos + MAX_ITEMS_PER_PAGE) + "\">";
		String back = "<button value=\"back\" width=50 height=15 action=\"bypass -h npc_" + getObjectId() + "_Chat 0\">";
		String content = "<html><body>Drawer " + drawername + ":<br>" + next + " " + back + "<table width=\"100%\">";
		content += "<tr><td>Item</td><td>Count</td><td>Withdraw</td></tr>";
		for (BoxItem i : items)
		{
			nitems++;
			if (nitems < startPos)
			{
				continue;
			}
			
			final String varname = VARIABLE_PREFIX.charAt(nitems - startPos) + String.valueOf(i.itemid);
			content += "<tr><td>" + i.name + "</td><td align=\"right\">" + i.count + "</td>";
			content += "<td><edit var=\"" + varname + "\" width=30></td></tr>";
			button += " ," + varname + " $" + varname;
			
			if ((nitems - startPos) >= MAX_ITEMS_PER_PAGE)
			{
				break;
			}
		}
		button += "\">";
		content += "</table><br>" + button + "</body></html>";
		LOGGER.info("setHtml(" + content + "); items=" + nitems);
		html.setHtml(content);
		player.sendPacket(html);
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showDepositWindow(PlayerInstance player, String command)
	{
		String drawername = "trash";
		if (command == null)
		{
			return;
		}
		
		String[] cmd = command.split(" ");
		int startPos = 0;
		
		if (cmd != null)
		{
			drawername = cmd[0];
			if (cmd.length > 1)
			{
				startPos = Integer.parseInt(cmd[1]);
			}
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		int nitems = 0;
		Set<BoxItem> items = new HashSet<>();
		for (ItemInstance i : player.getInventory().getItems())
		{
			if ((i.getItemId() == 57) || i.isEquipped())
			{
				continue;
			}
			
			final BoxItem bi = new BoxItem(i.getItemId(), i.getCount(), i.getItem().getName(), i.getObjectId()/* , i.getEnchantLevel() */);
			items.add(bi);
		}
		
		if (startPos >= items.size())
		{
			startPos = 0;
		}
		
		String button = "<button value=\"Deposit\" width=80 height=15 action=\"bypass -h npc_" + getObjectId() + "_InBox " + drawername;
		String next = "<button value=\"next\" width=50 height=15 action=\"bypass -h npc_" + getObjectId() + "_Deposit " + drawername + " " + (startPos + MAX_ITEMS_PER_PAGE) + "\">";
		String back = "<button value=\"back\" width=50 height=15 action=\"bypass -h npc_" + getObjectId() + "_Chat 0\">";
		String content = "<html><body>Drawer " + drawername + ":<br>" + next + " " + back + "<table width=\"100%\">";
		content += "<tr><td>Item</td><td>Count</td><td>Deposit</td></tr>";
		
		for (BoxItem i : items)
		{
			nitems++;
			if (nitems < startPos)
			{
				continue;
			}
			
			final String varname = VARIABLE_PREFIX.charAt(nitems - startPos) + String.valueOf(i.itemid);
			content += "<tr><td>" + i.name + "</td><td align=\"right\">" + i.count + "</td>";
			content += "<td><edit var=\"" + varname + "\" width=30></td></tr>";
			button += " ," + varname + " $" + varname;
			if ((nitems - startPos) >= MAX_ITEMS_PER_PAGE)
			{
				break;
			}
		}
		
		button += "\">";
		content += "</table><br>" + button + "</body></html>";
		LOGGER.info("setHtml(" + content + "); items=" + nitems);
		html.setHtml(content);
		player.sendPacket(html);
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private Set<BoxItem> getItems(String drawer)
	{
		final Set<BoxItem> it = new HashSet<>();
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, spawn, npcid, drawer, itemid, name, count, enchant FROM boxes where spawn=? and npcid=? and drawer=?");
			statement.setInt(1, getSpawn().getId());
			statement.setInt(2, getNpcId());
			statement.setString(3, drawer);
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				LOGGER.info("found: itemid=" + rs.getInt("itemid") + ", count=" + rs.getInt("count"));
				it.add(new BoxItem(rs.getInt("itemid"), rs.getInt("count"), rs.getString("name"), rs.getInt("id")/* , rs.getInt("enchant") */));
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.info("getItems failed: " + e);
		}
		return it;
	}
	
	private void putInBox(PlayerInstance player, String command)
	{/*
		 * NOTE: Item storing in box is currently not implemented String[] cmd = command.split(","); if (cmd.length<=1) return; String drawername = cmd[0]; for (int i = 1; i < cmd.length; i++) { String[] part = cmd[i].split(" "); if (part == null || part.length < 2) continue; try { int id =
		 * Integer.parseInt(part[0].substring(1)); int count = Integer.parseInt(part[1]); if (count <= 0) continue; int realCount = player.getInventory().getItemByItemId(id).getCount(); if (count < realCount) realCount = count; ItemInstance item = player.getInventory().destroyItemByItemId("Box", id,
		 * realCount, player, this); // other than previous l2j, destroyItemByItemId does not return the count destroyed // and we cannot just use the returned item as we cannot change the count ItemInstance newItem = ItemTable.getInstance().createItem(id); newItem.setCount(realCount);
		 * newItem.setEnchantLevel(item.getEnchantLevel()); putItemInBox(player, drawername, newItem); } catch (Exception e) { LOGGER.fine("putInBox "+command+" failed: "+e); } } } private void putItemInBox(PlayerInstance player, String drawer, ItemInstance item) { String charname =
		 * player.getName(); java.sql.Connection con = null; int foundId = 0; int foundCount = 0; try { con = DatabaseFactory.getConnection(false); if (item.isStackable()) { PreparedStatement st2 =
		 * con.prepareStatement("SELECT id,count FROM boxes where spawn=? and npcid=? and drawer=? and itemid=?"); st2.setInt(1, getSpawn().getId()); st2.setInt(2, getNpcId()); st2.setString(3, drawer); st2.setInt(4, item.getItemId()); ResultSet rs = st2.executeQuery(); if (rs.next()) { foundId =
		 * rs.getInt("id"); foundCount = rs.getInt("count"); } rs.close(); st2.close(); } if (foundCount == 0) { PreparedStatement statement = con.prepareStatement("INSERT INTO boxes (spawn,npcid,drawer,itemid,name,count,enchant) VALUES(?,?,?,?,?,?,?)"); statement.setInt(1, getSpawn().getId());
		 * statement.setInt(2, getNpcId()); statement.setString(3, drawer); statement.setInt(4, item.getItemId()); statement.setString(5, item.getItem().getName()); statement.setInt(6, item.getCount()); statement.setInt(7, item.getEnchantLevel()); statement.execute(); statement.close(); } else {
		 * PreparedStatement statement = con.prepareStatement("UPDATE boxes SET count=? WHERE id=?"); statement.setInt(1, foundCount + item.getCount()); statement.setInt(2, foundId); statement.execute(); statement.close(); } } catch (Exception e) {
		 * LOGGER.info("could not store item to box "+getSpawn().getId()+"-"+drawer+" for char "+charname); } finally { try { try { con.close(); } catch(Exception e) { } } catch (Exception e) { //null } }
		 */
	}
	
	private void takeOutBox(PlayerInstance player, String command)
	{/*
		 * NOTE: Item storing in box is currently not implemented String[] cmd = command.split(","); if (cmd.length<=1) return; String drawername = cmd[0]; BoxItem bi = null; for (int i = 1; i < cmd.length; i++) { String[] part = cmd[i].split(" "); if (part == null || part.length < 2) continue; try
		 * { int id = Integer.parseInt(part[0].substring(1)); int count = Integer.parseInt(part[1]); if (count <= 0) continue; ItemInstance item = ItemTable.getInstance().createItem(id); item.setCount(count); bi = takeItemOutBox(player, drawername, item); if (bi.count > 0) { item.setCount(bi.count);
		 * item.setEnchantLevel(bi.enchant); player.getInventory().addItem("Box", item, player, this); } } catch (Exception e) { LOGGER.fine("takeOutBox "+command+" failed: "+e); } } } private BoxItem takeItemOutBox(PlayerInstance player, String drawer, ItemInstance item) { String charname =
		 * player.getName(); java.sql.Connection con = null; BoxItem bi = new BoxItem(); bi.count = 0; try { con = DatabaseFactory.getConnection(false); PreparedStatement statement =
		 * con.prepareStatement("SELECT id,count,enchant FROM boxes WHERE spawn=? AND npcid=? AND drawer=? AND itemid=? AND count>=?"); statement.setInt(1, getSpawn().getId()); statement.setInt(2, getNpcId()); statement.setString(3, drawer); statement.setInt(4, item.getItemId()); statement.setInt(5,
		 * item.getCount()); ResultSet rs = statement.executeQuery(); while (rs.next()) { if (rs.getInt("count") == item.getCount()) { bi.count = item.getCount(); bi.itemid = item.getItemId(); bi.enchant = rs.getInt("enchant"); PreparedStatement st2 =
		 * con.prepareStatement("DELETE FROM boxes WHERE id=?"); st2.setInt(1, rs.getInt("id")); st2.execute(); st2.close(); break; } if (rs.getInt("count") > item.getCount()) { bi.count = item.getCount(); bi.itemid = item.getItemId(); bi.enchant = rs.getInt("enchant"); PreparedStatement st2 =
		 * con.prepareStatement("UPDATE boxes SET count=? WHERE id=?"); st2.setInt(1, rs.getInt("count") - bi.count); st2.setInt(2, rs.getInt("id")); st2.execute(); st2.close(); break; } } rs.close(); statement.close(); } catch (Exception e) {
		 * LOGGER.info("could not delete/update item, box "+getSpawn().getId()+"-"+drawer+" for char "+charname+": "+e); } finally { try { try { con.close(); } catch(Exception e) { } } catch (Exception e) { //null } } return bi;
		 */
	}
}
