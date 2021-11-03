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
package org.l2jmobius.gameserver.model.votereward;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.l2jmobius.Config;

/**
 * @author Anarchy
 */
public class L2jbrasil extends VoteSystem
{
	public L2jbrasil(int votesDiff, boolean allowReport, int boxes, Map<Integer, Integer> rewards, int checkMins)
	{
		super(votesDiff, allowReport, boxes, rewards, checkMins);
	}
	
	@Override
	public void run()
	{
		reward();
	}
	
	@Override
	public int getVotes()
	{
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try
		{
			URLConnection con = new URL(Config.L2JBRASIL_SERVER_LINK).openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/5.0");
			isr = new InputStreamReader(con.getInputStream());
			br = new BufferedReader(isr);
			
			String line;
			while ((line = br.readLine()) != null)
			{
				if (line.contains("<b>Entradas(Total):</b"))
				{
					String votesResult = String.valueOf(line.split(">")[2].replace("<br /", ""));
					int votes = Integer.valueOf(votesResult.replace(" ", ""));
					return votes;
				}
			}
			
			br.close();
			isr.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			LOGGER.warning("VoteSystem: Error while getting server vote count from " + getSiteName() + ".");
		}
		
		return -1;
	}
	
	@Override
	public String getSiteName()
	{
		return "L2JBrasil";
	}
}