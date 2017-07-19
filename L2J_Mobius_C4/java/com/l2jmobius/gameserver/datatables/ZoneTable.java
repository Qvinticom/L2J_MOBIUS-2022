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
package com.l2jmobius.gameserver.datatables;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.instancemanager.ArenaManager;
import com.l2jmobius.gameserver.instancemanager.GrandBossManager;
import com.l2jmobius.gameserver.instancemanager.OlympiadStadiumManager;
import com.l2jmobius.gameserver.instancemanager.TownManager;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.L2WorldRegion;
import com.l2jmobius.gameserver.model.zone.L2ZoneSpawn;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;
import com.l2jmobius.gameserver.model.zone.form.ZoneCuboid;
import com.l2jmobius.gameserver.model.zone.form.ZoneNPoly;
import com.l2jmobius.gameserver.model.zone.type.L2ArenaZone;
import com.l2jmobius.gameserver.model.zone.type.L2BossZone;
import com.l2jmobius.gameserver.model.zone.type.L2CastleTeleportZone;
import com.l2jmobius.gameserver.model.zone.type.L2ClanHallZone;
import com.l2jmobius.gameserver.model.zone.type.L2DamageZone;
import com.l2jmobius.gameserver.model.zone.type.L2DerbyTrackZone;
import com.l2jmobius.gameserver.model.zone.type.L2EffectZone;
import com.l2jmobius.gameserver.model.zone.type.L2FishingZone;
import com.l2jmobius.gameserver.model.zone.type.L2JailZone;
import com.l2jmobius.gameserver.model.zone.type.L2MotherTreeZone;
import com.l2jmobius.gameserver.model.zone.type.L2NoHqZone;
import com.l2jmobius.gameserver.model.zone.type.L2NoLandingZone;
import com.l2jmobius.gameserver.model.zone.type.L2NoStoreZone;
import com.l2jmobius.gameserver.model.zone.type.L2OlympiadStadiumZone;
import com.l2jmobius.gameserver.model.zone.type.L2PeaceZone;
import com.l2jmobius.gameserver.model.zone.type.L2SiegeZone;
import com.l2jmobius.gameserver.model.zone.type.L2TownZone;
import com.l2jmobius.gameserver.model.zone.type.L2WaterZone;

import javolution.util.FastList;

/**
 * This class manages all zone data.
 * @author durgus
 */
public class ZoneTable
{
	private static final Logger _log = Logger.getLogger(ZoneTable.class.getName());
	
	// =========================================================
	private static ZoneTable _instance;
	
	public static final ZoneTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ZoneTable();
		}
		return _instance;
	}
	
	// =========================================================
	// Data Field
	
	// =========================================================
	// Constructor
	public ZoneTable()
	{
		_log.info("Loading zones");
		load();
	}
	
	// =========================================================
	// Method - Private
	
	private final void load()
	{
		int zoneCount = 0;
		
		// Get the world regions
		final L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
		
		// Get an sql connection here
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Load the zone xml
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			final File file = new File(Config.DATAPACK_ROOT + "/data/zones/zone.xml");
			if (!file.exists())
			{
				if (Config.DEBUG)
				{
					_log.info("The zone.xml file is missing.");
				}
				return;
			}
			
			final Document doc = factory.newDocumentBuilder().parse(file);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("zone".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							final int zoneId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							final int minZ = Integer.parseInt(attrs.getNamedItem("minZ").getNodeValue());
							final int maxZ = Integer.parseInt(attrs.getNamedItem("maxZ").getNodeValue());
							final String zoneType = attrs.getNamedItem("type").getNodeValue();
							final String zoneShape = attrs.getNamedItem("shape").getNodeValue();
							
							// Create the zone
							L2ZoneType temp = null;
							
							if (zoneType.equals("FishingZone"))
							{
								temp = new L2FishingZone(zoneId);
							}
							else if (zoneType.equals("ClanHallZone"))
							{
								temp = new L2ClanHallZone(zoneId);
							}
							else if (zoneType.equals("PeaceZone"))
							{
								temp = new L2PeaceZone(zoneId);
							}
							else if (zoneType.equals("Town"))
							{
								temp = new L2TownZone(zoneId);
							}
							else if (zoneType.equals("OlympiadStadium"))
							{
								temp = new L2OlympiadStadiumZone(zoneId);
							}
							else if (zoneType.equals("SiegeZone"))
							{
								temp = new L2SiegeZone(zoneId);
							}
							else if (zoneType.equals("DamageZone"))
							{
								temp = new L2DamageZone(zoneId);
							}
							else if (zoneType.equals("Arena"))
							{
								temp = new L2ArenaZone(zoneId);
							}
							else if (zoneType.equals("MotherTree"))
							{
								temp = new L2MotherTreeZone(zoneId);
							}
							else if (zoneType.equals("EffectZone"))
							{
								temp = new L2EffectZone(zoneId);
							}
							else if (zoneType.equals("NoLandingZone"))
							{
								temp = new L2NoLandingZone(zoneId);
							}
							else if (zoneType.equals("JailZone"))
							{
								temp = new L2JailZone(zoneId);
							}
							else if (zoneType.equals("DerbyTrackZone"))
							{
								temp = new L2DerbyTrackZone(zoneId);
							}
							else if (zoneType.equals("WaterZone"))
							{
								temp = new L2WaterZone(zoneId);
							}
							else if (zoneType.equals("CastleTeleportZone"))
							{
								temp = new L2CastleTeleportZone(zoneId);
							}
							else if (zoneType.equals("NoHqZone"))
							{
								temp = new L2NoHqZone(zoneId);
							}
							else if (zoneType.equals("BossZone"))
							{
								temp = new L2BossZone(zoneId);
							}
							else if (zoneType.equals("NoStoreZone"))
							{
								temp = new L2NoStoreZone(zoneId);
							}
							
							// Check for unknown type
							if (temp == null)
							{
								_log.warning("ZoneTable: No such zone type: " + zoneType);
								continue;
							}
							
							// Get the zone shape from sql
							try (PreparedStatement statement = con.prepareStatement("SELECT x,y FROM zone_vertices WHERE id=? ORDER BY 'order' ASC "))
							{
								// Set the correct query
								statement.setInt(1, zoneId);
								try (ResultSet rset = statement.executeQuery())
								{
									// Create this zone. Parsing for cuboids is a bit different than for other polygons
									// cuboids need exactly 2 points to be defined. Other polygons need at least 3 (one per vertex)
									if (zoneShape.equals("Cuboid"))
									{
										final int[] x =
										{
											0,
											0
										};
										final int[] y =
										{
											0,
											0
										};
										boolean successfulLoad = true;
										
										for (int i = 0; i < 2; i++)
										{
											if (rset.next())
											{
												x[i] = rset.getInt("x");
												y[i] = rset.getInt("y");
											}
											else
											{
												_log.warning("ZoneTable: Missing cuboid vertex in sql data for zone: " + zoneId);
												successfulLoad = false;
												break;
											}
										}
										
										if (successfulLoad)
										{
											temp.setZone(zoneId, new ZoneCuboid(x[0], x[1], y[0], y[1], minZ, maxZ));
										}
										else
										{
											continue;
										}
									}
									else if (zoneShape.equals("NPoly"))
									{
										final FastList<Integer> fl_x = new FastList<>(), fl_y = new FastList<>();
										
										// Load the rest
										while (rset.next())
										{
											fl_x.add(rset.getInt("x"));
											fl_y.add(rset.getInt("y"));
										}
										
										// An nPoly needs to have at least 3 vertices
										if ((fl_x.size() == fl_y.size()) && (fl_x.size() > 2))
										
										{
											// Create arrays
											final int[] aX = new int[fl_x.size()];
											final int[] aY = new int[fl_y.size()];
											
											// This runs only at server startup so dont complain :>
											for (int i = 0; i < fl_x.size(); i++)
											{
												aX[i] = fl_x.get(i);
												aY[i] = fl_y.get(i);
											}
											
											// Create the zone
											temp.setZone(zoneId, new ZoneNPoly(aX, aY, minZ, maxZ));
										}
										else
										{
											_log.warning("ZoneTable: Bad sql data for zone: " + zoneId);
											continue;
										}
									}
									else
									{
										_log.warning("ZoneTable: Unknown shape: " + zoneShape);
										continue;
									}
								}
							}
							catch (final Exception e)
							{
								_log.warning("ZoneTable: Failed to load zone coordinates: " + e);
							}
							
							// Check for additional parameters
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("stat".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									final String name = attrs.getNamedItem("name").getNodeValue();
									final String val = attrs.getNamedItem("val").getNodeValue();
									
									temp.setParameter(name, val);
								}
								else if ("spawn".equalsIgnoreCase(cd.getNodeName()) && (temp instanceof L2ZoneSpawn))
								{
									attrs = cd.getAttributes();
									final int spawnX = Integer.parseInt(attrs.getNamedItem("X").getNodeValue());
									final int spawnY = Integer.parseInt(attrs.getNamedItem("Y").getNodeValue());
									final int spawnZ = Integer.parseInt(attrs.getNamedItem("Z").getNodeValue());
									
									final Node val = attrs.getNamedItem("isChaotic");
									if ((val != null) && Boolean.parseBoolean(val.getNodeValue()))
									{
										((L2ZoneSpawn) temp).addChaoticSpawn(spawnX, spawnY, spawnZ);
									}
									else
									{
										((L2ZoneSpawn) temp).addSpawn(spawnX, spawnY, spawnZ);
									}
								}
							}
							
							// Register the zone into any world region it intersects with...
							// currently 11136 test for each zone :>
							int ax, ay, bx, by;
							for (int x = 0; x < worldRegions.length; x++)
							{
								for (int y = 0; y < worldRegions[x].length; y++)
								{
									ax = (x - L2World.OFFSET_X) << L2World.SHIFT_BY;
									bx = ((x + 1) - L2World.OFFSET_X) << L2World.SHIFT_BY;
									ay = (y - L2World.OFFSET_Y) << L2World.SHIFT_BY;
									by = ((y + 1) - L2World.OFFSET_Y) << L2World.SHIFT_BY;
									
									if (temp.getZone().intersectsRectangle(ax, bx, ay, by))
									{
										if (Config.DEBUG)
										{
											_log.info("Zone (" + zoneId + ") added to: " + x + " " + y);
										}
										
										worldRegions[x][y].addZone(temp);
									}
								}
							}
							
							// Special managers for arenas, towns...
							if (temp instanceof L2ArenaZone)
							{
								ArenaManager.getInstance().addArena((L2ArenaZone) temp);
							}
							else if (temp instanceof L2TownZone)
							{
								TownManager.getInstance().addTown((L2TownZone) temp);
							}
							else if (temp instanceof L2OlympiadStadiumZone)
							{
								OlympiadStadiumManager.getInstance().addStadium((L2OlympiadStadiumZone) temp);
							}
							else if (temp instanceof L2BossZone)
							{
								GrandBossManager.getInstance().addZone((L2BossZone) temp);
							}
							
							// Increase the counter
							zoneCount++;
						}
					}
				}
			}
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Error while loading zones.", e);
			return;
		}
		
		GrandBossManager.getInstance().initZones();
		
		_log.info("Loaded " + zoneCount + " zones.");
	}
	
	public FastList<L2ZoneType> getZones(int x, int y)
	{
		final L2WorldRegion region = L2World.getInstance().getRegion(x, y);
		final FastList<L2ZoneType> temp = new FastList<>();
		for (final L2ZoneType zone : region.getZones())
		{
			if (zone.isInsideZone(x, y))
			{
				temp.add(zone);
			}
		}
		return temp;
	}
}