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
package com.l2jmobius.gameserver;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import com.l2jmobius.Config;

@SuppressWarnings("rawtypes")
public class Universe implements Serializable
{
	public static final int MIN_X = -127900;
	public static final int MAX_X = 194327;
	public static final int MIN_Y = -30000;
	public static final int MAX_Y = 259536;
	public static final int MIN_Z = -17000;
	public static final int MAX_Z = 17000;
	public static final int MIN_X_GRID = 60;
	public static final int MIN_Y_GRID = 60;
	public static final int MIN_Z_GRID = 60;
	public static final int MIN_GRID = 360;
	private static Universe _instance;
	protected static Logger _log = Logger.getLogger(Universe.class.getName());
	
	public static void main(String[] args)
	{
		final Universe u = new Universe();
		u.load();
		// u.removeDoubles();
		u.implode(false);
	}
	
	private class Position implements Comparable, Serializable
	{
		int _x;
		// int _flag;
		int _y;
		int _z;
		
		// public Position(int x, int y, int z, int flag)
		// {
		// _x = x;
		// _y = y;
		// _z = z;
		// _flag = flag;
		// }
		
		// public Position(L2CharPosition pos)
		// {
		// _x = pos.x;
		// _y = pos.y;
		// _z = pos.z;
		// _flag = 0;
		// }
		
		// public L2CharPosition L2CP()
		// {
		// return new L2CharPosition(_x, _y, _z, 0);
		// }
		
		@Override
		public int compareTo(Object obj)
		{
			final Position o = (Position) obj;
			int res = Integer.valueOf(_x).compareTo(o._x);
			if (res != 0)
			{
				return res;
			}
			res = Integer.valueOf(_y).compareTo(o._y);
			if (res != 0)
			{
				return res;
			}
			res = Integer.valueOf(_z).compareTo(o._z);
			return res;
		}
		
		// public String toString()
		// {
		// return String.valueOf(_x) + " " + _y + " " + _z + " " + _flag;
		// }
	}
	
	private class Coord implements Comparable, java.io.Serializable
	{
		/**
		 * Comment for <code>serialVersionUID</code>
		 */
		private static final long serialVersionUID = -558060332886829552L;
		int _x;
		int _y;
		int _z;
		
		public Coord(int x, int y, int z)
		{
			_x = x;
			_y = y;
			_z = z;
		}
		
		// public Coord(L2CharPosition pos)
		// {
		// _x = pos.x;
		// _y = pos.y;
		// _z = pos.z;
		// }
		
		@Override
		public int compareTo(Object obj)
		{
			final Position o = (Position) obj;
			int res = Integer.valueOf(_x).compareTo(o._x);
			if (res != 0)
			{
				return res;
			}
			res = Integer.valueOf(_y).compareTo(o._y);
			if (res != 0)
			{
				return res;
			}
			res = Integer.valueOf(_z).compareTo(o._z);
			return res;
		}
		
		@Override
		public String toString()
		{
			return String.valueOf(_x) + " " + _y + " " + _z;
		}
	}
	
	protected List<Coord> coordList;
	
	private final HashSet<Integer> _logPlayers;
	private boolean _logAll = true;
	
	public static Universe getInstance()
	{
		if ((_instance == null) && Config.ACTIVATE_POSITION_RECORDER)
		{
			_instance = new Universe();
		}
		return _instance;
	}
	
	private Universe()
	{
		coordList = new LinkedList<>();
		_logPlayers = new HashSet<>();
		
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new UniverseDump(), 30000, 30000);
	}
	
	public void registerHeight(int x, int y, int z)
	{
		// don't overwrite obstacle entries
		// Position p = new Position(x, y, z, 0);
		// _map.add(p);
		coordList.add(new Coord(x, y, z));
		// if (Config.USE_3D_MAP) insertInto3DMap(p);
	}
	
	public void registerObstacle(int x, int y, int z)
	{
		// Position p = new Position(x, y, z, -1);
		// _map.add(p);
		coordList.add(new Coord(x, y, z));
		// if (Config.USE_3D_MAP) insertInto3DMap(p);
	}
	
	public boolean shouldLog(Integer id)
	{
		return (_logPlayers.contains(id) || _logAll);
	}
	
	public void setLogAll(boolean flag)
	{
		_logAll = flag;
	}
	
	public void addLogPlayer(Integer id)
	{
		_logPlayers.add(id);
		_logAll = false;
	}
	
	public void removeLogPlayer(Integer id)
	{
		_logPlayers.remove(id);
	}
	
	public void loadAscii()
	{
		final int initialSize = coordList.size();
		try (FileReader fr = new FileReader("data/universe.txt");
			BufferedReader r = new BufferedReader(fr))
		{
			
			String line;
			while ((line = r.readLine()) != null)
			{
				final StringTokenizer st = new StringTokenizer(line);
				final String x1 = st.nextToken();
				final String y1 = st.nextToken();
				final String z1 = st.nextToken();
				// String f1 = st.nextToken();
				final int x = Integer.parseInt(x1);
				final int y = Integer.parseInt(y1);
				final int z = Integer.parseInt(z1);
				// int f = Integer.parseInt(f1);
				coordList.add(new Coord(x, y, z));
			}
			_log.info((coordList.size() - initialSize) + " additional nodes loaded from text file.");
		}
		catch (final Exception e)
		{
			_log.info("could not read text file universe.txt");
		}
	}
	
	public void createMap()
	{
		final int zoom = 100;
		final int w = (MAX_X - MIN_X) / zoom;
		final int h = (MAX_Y - MIN_Y) / zoom;
		final BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY);
		final Graphics2D gr = bi.createGraphics();
		int min_z = 0, max_z = 0;
		for (final Coord pos : coordList)
		{
			if (pos == null)
			{
				continue;
			}
			
			if (pos._z < min_z)
			{
				min_z = pos._z;
			}
			if (pos._z > max_z)
			{
				max_z = pos._z;
			}
		}
		for (final Coord pos : coordList)
		{
			if (pos == null)
			{
				continue;
			}
			
			final int x = (pos._x - MIN_X) / zoom;
			final int y = (pos._y - MIN_Y) / zoom;
			final int color = (int) ((((long) pos._z - MIN_Z) * 0xFFFFFF) / (MAX_Z - MIN_Z));
			gr.setColor(new Color(color));
			gr.drawLine(x, y, x, y);
		}
		try
		{
			ImageIO.write(bi, "png", new File("universe.png"));
		}
		catch (final Exception e)
		{
			_log.warning("cannot create universe.png: " + e);
		}
	}
	
	public class UniverseFilter implements FilenameFilter
	{
		String ext = "";
		
		public UniverseFilter(String pExt)
		{
			ext = pExt;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		@Override
		public boolean accept(File arg0, String name)
		{
			return name.startsWith("universe") && name.endsWith("." + ext);
		}
		
	}
	
	public void load()
	{
		final int total = 0;
		if (coordList == null)
		{
			coordList = new LinkedList<>();
		}
		try
		{
			loadBinFiles();
			
			loadHexFiles();
			
			loadFinFiles();
			
			_log.info(coordList.size() + " map vertices loaded in total.");
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Total: " + total);
	}
	
	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadFinFiles() throws FileNotFoundException, IOException
	{
		final FilenameFilter filter = new UniverseFilter("fin");
		final File directory = new File("data");
		final File[] files = directory.listFiles(filter);
		for (final File file : files)
		{
			final List<Coord> newMap = new LinkedList<>();
			
			// Save to file
			try (FileInputStream fos = new FileInputStream(file);
				DataInputStream data = new DataInputStream(fos))
			{
				final int count = data.readInt();
				
				for (int i = 0; i < count; i++)
				{
					newMap.add(new Coord(data.readInt(), data.readInt(), data.readInt()));
				}
			}
			
			_log.info(newMap.size() + " map vertices loaded from file " + file.getName());
			
			coordList.addAll(newMap);
		}
	}
	
	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadHexFiles() throws FileNotFoundException, IOException
	{
		final FilenameFilter filter = new UniverseFilter("hex");
		final File directory = new File("data");
		final File[] files = directory.listFiles(filter);
		for (final File file : files)
		{
			final List<Coord> newMap = new LinkedList<>();
			
			// Save to file
			try (FileInputStream fos = new FileInputStream(file);
				GZIPInputStream gzos = new GZIPInputStream(fos);
				DataInputStream data = new DataInputStream(gzos))
			{
				final int count = data.readInt();
				
				for (int i = 0; i < count; i++)
				{
					newMap.add(new Coord(data.readInt(), data.readInt(), data.readInt()));
					data.readInt();
				}
			}
			
			_log.info(newMap.size() + " map vertices loaded from file " + file.getName());
			
			coordList.addAll(newMap);
		}
	}
	
	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings(value =
	{
		"unchecked"
	})
	private void loadBinFiles() throws FileNotFoundException, IOException, ClassNotFoundException
	{
		final FilenameFilter filter = new UniverseFilter("bin");
		final File directory = new File("data");
		final File[] files = directory.listFiles(filter);
		for (final File file : files)
		{
			// Create necessary input streams
			try (FileInputStream fis = new FileInputStream(file);
				GZIPInputStream gzis = new GZIPInputStream(fis);
				ObjectInputStream in = new ObjectInputStream(gzis))
			{
				// Read in an object. It should be a vector of scribbles
				final TreeSet<Position> temp = (TreeSet<Position>) in.readObject();
				_log.info(temp.size() + " map vertices loaded from file " + file.getName());
				
				for (final Position p : temp)
				{
					coordList.add(new Coord(p._x, p._y, p._z));
				}
			}
		}
	}
	
	public class UniverseDump implements Runnable
	{
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			final int size = coordList.size();
			if (size > 100000)
			{
				flush();
			}
		}
	}
	
	public void flush()
	{
		// System.out.println("Size of dump: "+coordList.size());
		final List<Coord> oldMap = coordList;
		coordList = new LinkedList<>();
		final int size = oldMap.size();
		dump(oldMap, true);
		_log.info("Universe Map : Dumped " + size + " vertices.");
	}
	
	public int size()
	{
		int size = 0;
		if (coordList != null)
		{
			size = coordList.size();
		}
		return size;
	}
	
	public void dump(List<Coord> _map, boolean b)
	{
		String pad = "";
		if (b)
		{
			pad = "" + System.currentTimeMillis();
		}
		
		try (FileOutputStream fos = new FileOutputStream("data/universe" + pad + ".fin"); // Save to file
			DataOutputStream data = new DataOutputStream(fos))
		{
			final int count = _map.size();
			data.writeInt(count);
			
			for (final Coord p : _map)
			{
				if (p != null)
				{
					data.writeInt(p._x);
					data.writeInt(p._y);
					data.writeInt(p._z);
				}
			}
			
			data.flush();
			_log.info("Universe Map saved to: " + "data/universe" + pad + ".fin");
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// prepare for shutdown
	public void implode(boolean b)
	{
		createMap();
		dump(coordList, b);
	}
}