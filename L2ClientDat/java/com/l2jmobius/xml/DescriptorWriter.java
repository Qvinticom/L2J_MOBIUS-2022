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
package com.l2jmobius.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.l2jmobius.clientcryptor.crypt.DatCrypter;
import com.l2jmobius.config.ConfigDebug;
import com.l2jmobius.data.GameDataName;
import com.l2jmobius.util.ByteWriter;
import com.l2jmobius.util.DebugUtil;
import com.l2jmobius.util.Util;
import com.l2jmobius.xml.exceptions.CycleArgumentException;
import com.l2jmobius.xml.exceptions.PackDataException;

public class DescriptorWriter
{
	private static final byte[] endFileBytes = new byte[]
	{
		12,
		83,
		97,
		102,
		101,
		80,
		97,
		99,
		107,
		97,
		103,
		101,
		0
	};
	private static final DescriptorWriter _instance = new DescriptorWriter();
	private static final Logger _log = LogManager.getLogger(DescriptorWriter.class);
	
	public static DescriptorWriter getInstance()
	{
		return _instance;
	}
	
	public static byte[] parseData(File currentFile, DatCrypter crypter, Descriptor desc, String data) throws Exception
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream(data.length() / 2);
		if (desc.getFormat() != null)
		{
			data = desc.getFormat().encode(data);
		}
		if (desc.isRawData())
		{
			Buffer result = parseNodeValue(currentFile, crypter, data, desc.getNodes().get(0), true);
			if (result != null)
			{
				stream.write((byte[]) result.array());
			}
			else
			{
				_log.error("Failed to parse raw data.");
			}
		}
		else
		{
			ByteBuffer buffer = ByteBuffer.allocateDirect(data.length() * 2);
			String lines = data.replace("\r\n", "\t");
			HashMap<ParamNode, Integer> counters = new HashMap<>();
			packData(currentFile, crypter, buffer, lines, counters, new HashMap<String, String>(), new HashMap<ParamNode, String>(), desc.getNodes());
			try
			{
				buffer.flip();
				stream.write(ByteBuffer.allocate(buffer.limit()).put(buffer).array());
			}
			catch (IOException e)
			{
				DebugUtil.getLogger().error(e.getMessage(), e);
			}
		}
		if (desc.isSafePackage())
		{
			stream.write(endFileBytes);
		}
		return stream.toByteArray();
	}
	
	private static void packData(File currentFile, DatCrypter crypter, ByteBuffer buffer, String lines, Map<ParamNode, Integer> counters, Map<String, String> paramMap, Map<ParamNode, String> mapData, List<ParamNode> nodes) throws Exception
	{
		for (ParamNode node : nodes)
		{
			try
			{
				String param;
				if (node.isIterator())
				{
					counters.put(node, buffer.position());
					continue;
				}
				if (node.getEntityType().isCycle())
				{
					if (!node.isNameHidden())
					{
						Pattern pattern = Pattern.compile("\\b" + node.getName().concat("_begin\\b(.*?)\\b").concat(node.getName()).concat("_end\\b"), 32);
						Matcher m = pattern.matcher(lines);
						ArrayList<String> list = new ArrayList<>();
						while (m.find())
						{
							list.add(m.group(1));
						}
						writeSize(currentFile, crypter, buffer, counters, node, list.size());
						for (String str : list)
						{
							paramMap.putAll(Util.stringToMap(str));
							packData(currentFile, crypter, buffer, str, counters, paramMap, mapData, node.getSubNodes());
						}
						continue;
					}
					param = getDataString(node, node.getName(), paramMap, mapData);
					if (param == null)
					{
						throw new Exception("Error getDataString == null node: " + node.getName());
					}
					if (param.isEmpty() || param.equals("{}"))
					{
						writeSize(currentFile, crypter, buffer, counters, node, 0);
						continue;
					}
					List<String> subParams = Util.splitList(param);
					int cycleSize = subParams.size();
					if ((node.getSize() > 0) && (node.getSize() != cycleSize))
					{
						throw new Exception("Wrong static cycle count for cycle: " + node.getName() + " size: " + subParams.size() + " params: " + Arrays.toString(subParams.toArray()));
					}
					writeSize(currentFile, crypter, buffer, counters, node, cycleSize);
					int nPramNode = 0;
					int nCycleNode = 0;
					for (ParamNode n : node.getSubNodes())
					{
						if (n.getEntityType().isCycle())
						{
							++nCycleNode;
							continue;
						}
						if (n.isIterator())
						{
							continue;
						}
						++nPramNode;
					}
					for (String subParam : subParams)
					{
						int paramIndex = 0;
						List<String> sub2Params = (nPramNode > 0) || (nCycleNode > 1) ? Util.splitList(subParam) : Collections.singletonList(subParam);
						for (ParamNode n : node.getSubNodes())
						{
							if (n.isIterator() || n.getEntityType().isConstant())
							{
								continue;
							}
							if (paramIndex >= sub2Params.size())
							{
								throw new Exception("Wrong cycle param count for cycle: " + node.getName() + " paramIndex: " + paramIndex + " params: " + Arrays.toString(sub2Params.toArray()));
							}
							mapData.put(n, sub2Params.get(paramIndex++));
						}
						packData(currentFile, crypter, buffer, lines, counters, paramMap, mapData, node.getSubNodes());
					}
					continue;
				}
				if (node.getEntityType().isWrapper())
				{
					List<String> subParams = Util.splitList(getDataString(node, node.getName(), paramMap, mapData));
					int paramIndex = 0;
					for (ParamNode n : node.getSubNodes())
					{
						if (n.isIterator() || n.getEntityType().isConstant())
						{
							continue;
						}
						mapData.put(n, subParams.get(paramIndex++));
					}
					packData(currentFile, crypter, buffer, lines, counters, paramMap, mapData, node.getSubNodes());
					continue;
				}
				if (node.getEntityType().isVariable())
				{
					buffer.put((byte[]) parseNodeValue(currentFile, crypter, getDataString(node, node.getName(), paramMap, mapData), node, false).array());
					continue;
				}
				if (!node.getEntityType().isIf() || ((param = getDataString(node, node.getParamIf(), paramMap, mapData)) == null) || !node.getValIf().equalsIgnoreCase(param))
				{
					continue;
				}
				packData(currentFile, crypter, buffer, lines, counters, paramMap, mapData, node.getSubNodes());
			}
			catch (Exception e)
			{
				throw new PackDataException(e + "\r\n-node: " + node + "\r\n\tparam: " + paramMap.get(node.getName()));
			}
		}
	}
	
	private static Buffer parseNodeValue(File currentFile, DatCrypter crypter, String data, ParamNode node, boolean isRaw)
	{
		ParamType nodeType = node.getType();
		if (nodeType == null)
		{
			_log.error("Incorrect node type for node " + node);
			return null;
		}
		try
		{
			switch (nodeType)
			{
				case UCHAR:
				{
					return ByteWriter.writeChar(Byte.parseByte(data));
				}
				case CNTR:
				{
					return ByteWriter.writeCompactInt(Integer.parseInt(data));
				}
				case UBYTE:
				{
					return ByteWriter.writeUByte(Short.parseShort(data));
				}
				case SHORT:
				{
					return ByteWriter.writeShort(Short.parseShort(data));
				}
				case USHORT:
				{
					return ByteWriter.writeUShort(Integer.parseInt(data));
				}
				case UINT:
				case INT:
				{
					return ByteWriter.writeInt(Integer.parseInt(data));
				}
				case UNICODE:
				{
					return ByteWriter.writeUtfString(isRaw ? data : data.substring(1, data.length() - 1), isRaw);
				}
				case ASCF:
				{
					return ByteWriter.writeString(isRaw ? data : data.substring(1, data.length() - 1), isRaw);
				}
				case DOUBLE:
				{
					return ByteWriter.writeDouble(Double.parseDouble(data));
				}
				case FLOAT:
				{
					return ByteWriter.writeFloat(Float.parseFloat(data));
				}
				case LONG:
				{
					return ByteWriter.writeLong(Long.parseLong(data));
				}
				case RGBA:
				{
					return ByteWriter.writeRGBA(data);
				}
				case RGB:
				{
					return ByteWriter.writeRGB(data);
				}
				case HEX:
				{
					return ByteWriter.writeByte((byte) (Integer.parseInt(data, 16) & 255));
				}
				case MAP_INT:
				{
					if (ConfigDebug.DAT_REPLACEMENT_NAMES)
					{
						return ByteWriter.writeInt(GameDataName.getInstance().getId(currentFile, crypter, node, data));
					}
					return ByteWriter.writeInt(Integer.parseInt(data));
				}
			}
			DebugUtil.getLogger().error("Unsupported primitive type " + (nodeType));
		}
		catch (Exception e)
		{
			_log.error("Failed to parse value for node " + node + " data: " + data, e);
		}
		return null;
	}
	
	private static String getDataString(ParamNode node, String name, Map<String, String> paramMap, Map<ParamNode, String> mapData)
	{
		if ((mapData != null) && mapData.containsKey(node))
		{
			return mapData.get(node);
		}
		return paramMap.get(name);
	}
	
	private static void writeSize(File currentFile, DatCrypter crypter, ByteBuffer buffer, Map<ParamNode, Integer> counters, ParamNode node, int cycleSize) throws CycleArgumentException
	{
		if (!node.isSkipWriteSize() && (node.getSize() < 0))
		{
			ParamNode iterator = node.getTmpIterator();
			if (iterator == null)
			{
				counters.keySet().stream().filter(n -> n.getName().equals(node.getCycleName())).forEach(node::setTmpIterator);
				iterator = node.getTmpIterator();
				if (iterator == null)
				{
					throw new CycleArgumentException("Invalid argument [" + node.getName() + "] for cycle");
				}
			}
			Buffer buff = parseNodeValue(currentFile, crypter, String.valueOf(cycleSize), iterator, false);
			int pos = counters.get(iterator);
			if (pos >= 0)
			{
				int rem = buffer.position();
				if (pos == rem)
				{
					buffer.put((byte[]) buff.array());
				}
				else
				{
					byte[] arrayNext = new byte[rem - pos];
					buffer.position(pos);
					buffer.get(arrayNext);
					buffer.position(pos);
					buffer.put((byte[]) buff.array());
					buffer.put(arrayNext);
				}
				counters.remove(iterator);
			}
		}
	}
}
