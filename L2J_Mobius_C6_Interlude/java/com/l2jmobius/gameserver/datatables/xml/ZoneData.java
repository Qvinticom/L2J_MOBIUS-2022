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
package com.l2jmobius.gameserver.datatables.xml;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jmobius.Config;
import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.gameserver.instancemanager.ArenaManager;
import com.l2jmobius.gameserver.instancemanager.FishingZoneManager;
import com.l2jmobius.gameserver.instancemanager.GrandBossManager;
import com.l2jmobius.gameserver.instancemanager.OlympiadStadiaManager;
import com.l2jmobius.gameserver.instancemanager.TownManager;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.L2WorldRegion;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;
import com.l2jmobius.gameserver.model.zone.form.ZoneCuboid;
import com.l2jmobius.gameserver.model.zone.form.ZoneCylinder;
import com.l2jmobius.gameserver.model.zone.form.ZoneNPoly;
import com.l2jmobius.gameserver.model.zone.type.L2ArenaZone;
import com.l2jmobius.gameserver.model.zone.type.L2BigheadZone;
import com.l2jmobius.gameserver.model.zone.type.L2BossZone;
import com.l2jmobius.gameserver.model.zone.type.L2CastleTeleportZone;
import com.l2jmobius.gameserver.model.zone.type.L2CastleZone;
import com.l2jmobius.gameserver.model.zone.type.L2ClanHallZone;
import com.l2jmobius.gameserver.model.zone.type.L2CustomZone;
import com.l2jmobius.gameserver.model.zone.type.L2DamageZone;
import com.l2jmobius.gameserver.model.zone.type.L2DerbyTrackZone;
import com.l2jmobius.gameserver.model.zone.type.L2EffectZone;
import com.l2jmobius.gameserver.model.zone.type.L2FishingZone;
import com.l2jmobius.gameserver.model.zone.type.L2FortZone;
import com.l2jmobius.gameserver.model.zone.type.L2JailZone;
import com.l2jmobius.gameserver.model.zone.type.L2MotherTreeZone;
import com.l2jmobius.gameserver.model.zone.type.L2NoHqZone;
import com.l2jmobius.gameserver.model.zone.type.L2NoLandingZone;
import com.l2jmobius.gameserver.model.zone.type.L2NoStoreZone;
import com.l2jmobius.gameserver.model.zone.type.L2OlympiadStadiumZone;
import com.l2jmobius.gameserver.model.zone.type.L2PeaceZone;
import com.l2jmobius.gameserver.model.zone.type.L2PoisonZone;
import com.l2jmobius.gameserver.model.zone.type.L2SwampZone;
import com.l2jmobius.gameserver.model.zone.type.L2TownZone;
import com.l2jmobius.gameserver.model.zone.type.L2WaterZone;

/**
 * This class manages the augmentation data and can also create new augmentations.
 * @author durgus
 */
public class ZoneData
{
	private static final Logger LOGGER = Logger.getLogger(ZoneData.class.getName());
	
	private static ZoneData _instance;
	
	public static final ZoneData getInstance()
	{
		if (_instance == null)
		{
			_instance = new ZoneData();
		}
		
		return _instance;
	}
	
	public ZoneData()
	{
		LOGGER.info("Loading zones...");
		
		load();
	}
	
	public void reload()
	{
		synchronized (_instance)
		{
			_instance = null;
			_instance = new ZoneData();
		}
	}
	
	private final void load()
	{
		int zoneCount = 0;
		
		// Get the world regions
		final L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
		
		boolean done = false;
		
		// Load the zone xml
		try (Connection con = DatabaseFactory.getConnection())
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			File file = new File(Config.DATAPACK_ROOT + "/data/zones/zone.xml");
			if (file.exists())
			{
				Document doc = factory.newDocumentBuilder().parse(file);
				
				int effect_zone_id = 150000; // FIXME Temporally workaround to avoid zone.xml modification
				for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if ("list".equalsIgnoreCase(n.getNodeName()))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if ("zone".equalsIgnoreCase(d.getNodeName()))
							{
								NamedNodeMap attrs = d.getAttributes();
								
								int zoneId = -1;
								if (attrs.getNamedItem("id") != null)
								{
									zoneId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
								}
								
								final int minZ = Integer.parseInt(attrs.getNamedItem("minZ").getNodeValue());
								final int maxZ = Integer.parseInt(attrs.getNamedItem("maxZ").getNodeValue());
								
								String zoneType = attrs.getNamedItem("type").getNodeValue();
								String zoneShape = attrs.getNamedItem("shape").getNodeValue();
								
								// Create the zone
								L2ZoneType temp = null;
								
								switch (zoneType)
								{
									case "FishingZone":
									{
										temp = new L2FishingZone(zoneId);
										break;
									}
									case "ClanHallZone":
									{
										temp = new L2ClanHallZone(zoneId);
										break;
									}
									case "PeaceZone":
									{
										temp = new L2PeaceZone(zoneId);
										break;
									}
									case "Town":
									{
										temp = new L2TownZone(zoneId);
										break;
									}
									case "OlympiadStadium":
									{
										temp = new L2OlympiadStadiumZone(zoneId);
										break;
									}
									case "CastleZone":
									{
										temp = new L2CastleZone(zoneId);
										break;
									}
									case "FortZone":
									{
										temp = new L2FortZone(zoneId);
										break;
									}
									case "DamageZone":
									{
										temp = new L2DamageZone(zoneId);
										break;
									}
									case "Arena":
									{
										temp = new L2ArenaZone(zoneId);
										break;
									}
									case "MotherTree":
									{
										temp = new L2MotherTreeZone(zoneId);
										break;
									}
									case "BigheadZone":
									{
										temp = new L2BigheadZone(zoneId);
										break;
									}
									case "NoLandingZone":
									{
										temp = new L2NoLandingZone(zoneId);
										break;
									}
									case "NoStoreZone":
									{
										temp = new L2NoStoreZone(zoneId);
										break;
									}
									case "JailZone":
									{
										temp = new L2JailZone(zoneId);
										break;
									}
									case "DerbyTrackZone":
									{
										temp = new L2DerbyTrackZone(zoneId);
										break;
									}
									case "WaterZone":
									{
										temp = new L2WaterZone(zoneId);
										break;
									}
									case "NoHqZone":
									{
										temp = new L2NoHqZone(zoneId);
										break;
									}
									case "BossZone":
									{
										int boss_id = -1;
										try
										{
											boss_id = Integer.parseInt(attrs.getNamedItem("bossId").getNodeValue());
										}
										catch (IllegalArgumentException e)
										{
											e.printStackTrace();
										}
										temp = new L2BossZone(zoneId, boss_id);
										break;
									}
									case "EffectZone":
									{
										zoneId = effect_zone_id;
										effect_zone_id++;
										temp = new L2EffectZone(zoneId);
										break;
									}
									case "PoisonZone":
									{
										temp = new L2PoisonZone(zoneId);
										break;
									}
									case "CastleTeleportZone":
									{
										temp = new L2CastleTeleportZone(zoneId);
										break;
									}
									case "CustomZone":
									{
										temp = new L2CustomZone(zoneId);
										break;
									}
									case "SwampZone":
									{
										temp = new L2SwampZone(zoneId);
										break;
									}
								}
								
								// Check for unknown type
								if (temp == null)
								{
									LOGGER.warning("ZoneData: No such zone type: " + zoneType);
									continue;
								}
								
								// get the zone shape from file if any
								int[][] coords = null;
								int[] point;
								final List<int[]> rs = new ArrayList<>();
								// loading from XML first
								for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
								{
									if ("node".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										point = new int[2];
										point[0] = Integer.parseInt(attrs.getNamedItem("X").getNodeValue());
										point[1] = Integer.parseInt(attrs.getNamedItem("Y").getNodeValue());
										rs.add(point);
									}
								}
								
								coords = rs.toArray(new int[rs.size()][]);
								
								if ((coords == null) || (coords.length == 0)) // check on database
								{
									// Get the zone shape from sql or from file if not defined into sql
									try
									{
										PreparedStatement statement = null;
										
										// Set the correct query
										statement = con.prepareStatement("SELECT x,y FROM zone_vertices WHERE id=? ORDER BY 'order' ASC ");
										
										statement.setInt(1, zoneId);
										ResultSet rset = statement.executeQuery();
										
										// Create this zone. Parsing for cuboids is a bit different than for other polygons
										// cuboids need exactly 2 points to be defined. Other polygons need at least 3 (one per vertex)
										switch (zoneShape)
										{
											case "Cuboid":
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
														LOGGER.warning("ZoneData: Missing cuboid vertex in sql data for zone: " + zoneId);
														statement.close();
														rset.close();
														successfulLoad = false;
														break;
													}
												}
												if (successfulLoad)
												{
													temp.setZone(new ZoneCuboid(x[0], x[1], y[0], y[1], minZ, maxZ));
												}
												else
												{
													continue;
												}
												break;
											}
											case "NPoly":
											{
												List<Integer> fl_x = new ArrayList<>();
												final List<Integer> fl_y = new ArrayList<>();
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
													temp.setZone(new ZoneNPoly(aX, aY, minZ, maxZ));
												}
												else
												{
													LOGGER.warning("ZoneData: Bad sql data for zone: " + zoneId);
													statement.close();
													rset.close();
													continue;
												}
												break;
											}
											default:
											{
												LOGGER.warning("ZoneData: Unknown shape: " + zoneShape);
												statement.close();
												rset.close();
												continue;
											}
										}
										
										statement.close();
										rset.close();
									}
									catch (Exception e)
									{
										LOGGER.warning("ZoneData: Failed to load zone coordinates: " + e);
									}
									
								}
								else // Create this zone. Parsing for cuboids is a bit different than for other polygons cuboids need exactly 2 points to be defined. Other polygons need at least 3 (one per vertex)
								if (zoneShape.equalsIgnoreCase("Cuboid"))
								{
									if (coords.length == 2)
									{
										temp.setZone(new ZoneCuboid(coords[0][0], coords[1][0], coords[0][1], coords[1][1], minZ, maxZ));
									}
									else
									{
										LOGGER.warning("ZoneData: Missing cuboid vertex in sql data for zone: " + zoneId);
										continue;
									}
								}
								else if (zoneShape.equalsIgnoreCase("NPoly"))
								{
									// nPoly needs to have at least 3 vertices
									if (coords.length > 2)
									{
										final int[] aX = new int[coords.length];
										final int[] aY = new int[coords.length];
										for (int i = 0; i < coords.length; i++)
										{
											aX[i] = coords[i][0];
											aY[i] = coords[i][1];
										}
										temp.setZone(new ZoneNPoly(aX, aY, minZ, maxZ));
									}
									else
									{
										LOGGER.warning("ZoneData: Bad data for zone: " + zoneId);
										continue;
									}
								}
								else if (zoneShape.equalsIgnoreCase("Cylinder"))
								{
									// A cylinder zone requires a center point at x,y and a radius
									attrs = d.getAttributes();
									final int zoneRad = Integer.parseInt(attrs.getNamedItem("rad").getNodeValue());
									if ((coords.length == 1) && (zoneRad > 0))
									{
										temp.setZone(new ZoneCylinder(coords[0][0], coords[0][1], minZ, maxZ, zoneRad));
									}
									else
									{
										LOGGER.warning("ZoneData: Bad data for zone: " + zoneId);
										continue;
									}
								}
								else
								{
									LOGGER.warning("ZoneData: Unknown shape: " + zoneShape);
									continue;
								}
								
								// Check for aditional parameters
								for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
								{
									if ("stat".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										String name = attrs.getNamedItem("name").getNodeValue();
										String val = attrs.getNamedItem("val").getNodeValue();
										
										temp.setParameter(name, val);
									}
									if ("spawn".equalsIgnoreCase(cd.getNodeName()))
									{
										temp.setSpawnLocs(cd);
									}
								}
								
								// Skip checks for fishing zones & add to fishing zone manager
								if (temp instanceof L2FishingZone)
								{
									FishingZoneManager.getInstance().addFishingZone((L2FishingZone) temp);
									continue;
								}
								
								if (temp instanceof L2WaterZone)
								{
									FishingZoneManager.getInstance().addWaterZone((L2WaterZone) temp);
								}
								
								// Register the zone into any world region it intersects with currently 11136 test for each zone :>
								int ax;
								int ay;
								int bx;
								int by;
								
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
									OlympiadStadiaManager.getInstance().addStadium((L2OlympiadStadiumZone) temp);
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
				
				done = true;
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while loading zones. " + e);
		}
		
		if (done)
		{
			GrandBossManager.getInstance().initZones();
		}
		
		LOGGER.info("Done: loaded " + zoneCount + " zones.");
	}
}
