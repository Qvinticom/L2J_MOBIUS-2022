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
package org.l2jmobius.gameserver.datatables.xml;

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

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.instancemanager.ArenaManager;
import org.l2jmobius.gameserver.instancemanager.FishingZoneManager;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.instancemanager.OlympiadStadiaManager;
import org.l2jmobius.gameserver.instancemanager.TownManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldRegion;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.model.zone.form.ZoneCuboid;
import org.l2jmobius.gameserver.model.zone.form.ZoneCylinder;
import org.l2jmobius.gameserver.model.zone.form.ZoneNPoly;
import org.l2jmobius.gameserver.model.zone.type.ArenaZone;
import org.l2jmobius.gameserver.model.zone.type.BigheadZone;
import org.l2jmobius.gameserver.model.zone.type.BossZone;
import org.l2jmobius.gameserver.model.zone.type.CastleTeleportZone;
import org.l2jmobius.gameserver.model.zone.type.CastleZone;
import org.l2jmobius.gameserver.model.zone.type.ClanHallZone;
import org.l2jmobius.gameserver.model.zone.type.CustomZone;
import org.l2jmobius.gameserver.model.zone.type.DamageZone;
import org.l2jmobius.gameserver.model.zone.type.DerbyTrackZone;
import org.l2jmobius.gameserver.model.zone.type.EffectZone;
import org.l2jmobius.gameserver.model.zone.type.FishingZone;
import org.l2jmobius.gameserver.model.zone.type.FortZone;
import org.l2jmobius.gameserver.model.zone.type.JailZone;
import org.l2jmobius.gameserver.model.zone.type.MotherTreeZone;
import org.l2jmobius.gameserver.model.zone.type.NoHqZone;
import org.l2jmobius.gameserver.model.zone.type.NoLandingZone;
import org.l2jmobius.gameserver.model.zone.type.NoStoreZone;
import org.l2jmobius.gameserver.model.zone.type.OlympiadStadiumZone;
import org.l2jmobius.gameserver.model.zone.type.PeaceZone;
import org.l2jmobius.gameserver.model.zone.type.PoisonZone;
import org.l2jmobius.gameserver.model.zone.type.SwampZone;
import org.l2jmobius.gameserver.model.zone.type.TownZone;
import org.l2jmobius.gameserver.model.zone.type.WaterZone;

/**
 * @author durgus
 */
public class ZoneData
{
	private static final Logger LOGGER = Logger.getLogger(ZoneData.class.getName());
	
	public ZoneData()
	{
		LOGGER.info("Loading zones...");
		load();
	}
	
	public void load()
	{
		int zoneCount = 0;
		
		// Get the world regions
		final WorldRegion[][] worldRegions = World.getInstance().getAllWorldRegions();
		
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
								ZoneType temp = null;
								
								switch (zoneType)
								{
									case "FishingZone":
									{
										temp = new FishingZone(zoneId);
										break;
									}
									case "ClanHallZone":
									{
										temp = new ClanHallZone(zoneId);
										break;
									}
									case "PeaceZone":
									{
										temp = new PeaceZone(zoneId);
										break;
									}
									case "Town":
									{
										temp = new TownZone(zoneId);
										break;
									}
									case "OlympiadStadium":
									{
										temp = new OlympiadStadiumZone(zoneId);
										break;
									}
									case "CastleZone":
									{
										temp = new CastleZone(zoneId);
										break;
									}
									case "FortZone":
									{
										temp = new FortZone(zoneId);
										break;
									}
									case "DamageZone":
									{
										temp = new DamageZone(zoneId);
										break;
									}
									case "Arena":
									{
										temp = new ArenaZone(zoneId);
										break;
									}
									case "MotherTree":
									{
										temp = new MotherTreeZone(zoneId);
										break;
									}
									case "BigheadZone":
									{
										temp = new BigheadZone(zoneId);
										break;
									}
									case "NoLandingZone":
									{
										temp = new NoLandingZone(zoneId);
										break;
									}
									case "NoStoreZone":
									{
										temp = new NoStoreZone(zoneId);
										break;
									}
									case "JailZone":
									{
										temp = new JailZone(zoneId);
										break;
									}
									case "DerbyTrackZone":
									{
										temp = new DerbyTrackZone(zoneId);
										break;
									}
									case "WaterZone":
									{
										temp = new WaterZone(zoneId);
										break;
									}
									case "NoHqZone":
									{
										temp = new NoHqZone(zoneId);
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
										temp = new BossZone(zoneId, boss_id);
										break;
									}
									case "EffectZone":
									{
										zoneId = effect_zone_id;
										effect_zone_id++;
										temp = new EffectZone(zoneId);
										break;
									}
									case "PoisonZone":
									{
										temp = new PoisonZone(zoneId);
										break;
									}
									case "CastleTeleportZone":
									{
										temp = new CastleTeleportZone(zoneId);
										break;
									}
									case "CustomZone":
									{
										temp = new CustomZone(zoneId);
										break;
									}
									case "SwampZone":
									{
										temp = new SwampZone(zoneId);
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
								if (temp instanceof FishingZone)
								{
									FishingZoneManager.getInstance().addFishingZone((FishingZone) temp);
									continue;
								}
								
								if (temp instanceof WaterZone)
								{
									FishingZoneManager.getInstance().addWaterZone((WaterZone) temp);
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
										ax = (x - World.OFFSET_X) << World.SHIFT_BY;
										bx = ((x + 1) - World.OFFSET_X) << World.SHIFT_BY;
										ay = (y - World.OFFSET_Y) << World.SHIFT_BY;
										by = ((y + 1) - World.OFFSET_Y) << World.SHIFT_BY;
										
										if (temp.getZone().intersectsRectangle(ax, bx, ay, by))
										{
											worldRegions[x][y].addZone(temp);
										}
									}
								}
								
								// Special managers for arenas, towns...
								if (temp instanceof ArenaZone)
								{
									ArenaManager.getInstance().addArena((ArenaZone) temp);
								}
								else if (temp instanceof TownZone)
								{
									TownManager.getInstance().addTown((TownZone) temp);
								}
								else if (temp instanceof OlympiadStadiumZone)
								{
									OlympiadStadiaManager.getInstance().addStadium((OlympiadStadiumZone) temp);
								}
								else if (temp instanceof BossZone)
								{
									GrandBossManager.getInstance().addZone((BossZone) temp);
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
	
	public static ZoneData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ZoneData INSTANCE = new ZoneData();
	}
}
