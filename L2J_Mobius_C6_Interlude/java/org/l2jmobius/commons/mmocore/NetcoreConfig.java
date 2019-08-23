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
package org.l2jmobius.commons.mmocore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class NetcoreConfig
{
	public boolean PACKET_HANDLER_DEBUG;
	
	/** MMO settings */
	public int MMO_SELECTOR_SLEEP_TIME = 20; // default 20
	public int MMO_MAX_SEND_PER_PASS = 12; // default 12
	public int MMO_MAX_READ_PER_PASS = 12; // default 12
	public int MMO_HELPER_BUFFER_COUNT = 20; // default 20
	
	/** Client Packets Queue settings */
	public int CLIENT_PACKET_QUEUE_SIZE; // default MMO_MAX_READ_PER_PASS + 2
	public int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE; // default MMO_MAX_READ_PER_PASS + 1
	public int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND; // default 80
	public int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL; // default 5
	public int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND; // default 40
	public int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN; // default 2
	public int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN; // default 1
	public int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN; // default 1
	public int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN; // default 5
	
	private NetcoreConfig()
	{
		final String MMO_CONFIG = "./config/protected/MmoCore.ini";
		
		try
		{
			final Properties mmoSetting = new Properties();
			final InputStream is = new FileInputStream(new File(MMO_CONFIG));
			mmoSetting.load(is);
			is.close();
			
			PACKET_HANDLER_DEBUG = Boolean.parseBoolean(mmoSetting.getProperty("PacketHandlerDebug", "False"));
			
			// CLIENT-QUEUE-SETTINGS
			CLIENT_PACKET_QUEUE_SIZE = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueSize", "14")); // default MMO_MAX_READ_PER_PASS + 2
			CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueMaxBurstSize", "13")); // default MMO_MAX_READ_PER_PASS + 1
			CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueMaxPacketsPerSecond", "80")); // default 80
			CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueMeasureInterval", "5")); // default 5
			CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueMaxAveragePacketsPerSecond", "40")); // default 40
			CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueMaxFloodPerMin", "6")); // default 6
			CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueOverflowsPerMin", "3")); // default 3
			CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueUnderflowsPerMin", "3")); // default 3
			CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = Integer.parseInt(mmoSetting.getProperty("ClientPacketQueueUnknownPerMin", "5")); // default 5
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + MMO_CONFIG + " File.");
		}
	}
	
	public static NetcoreConfig getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final NetcoreConfig INSTANCE = new NetcoreConfig();
	}
}