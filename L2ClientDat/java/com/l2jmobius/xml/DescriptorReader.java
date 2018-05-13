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

import java.io.File;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jmobius.L2ClientDat;
import com.l2jmobius.clientcryptor.crypt.DatCrypter;
import com.l2jmobius.config.ConfigDebug;
import com.l2jmobius.data.GameDataName;
import com.l2jmobius.util.ByteReader;
import com.l2jmobius.util.DebugUtil;

public class DescriptorReader
{
	private static DescriptorReader _instance = new DescriptorReader();
	final String eq = "=";
	final String tab = "\t";
	final String nl = System.getProperty("line.separator");
	final String semi = ";";
	final String lb = "[";
	final String rb = "]";
	
	public static DescriptorReader getInstance()
	{
		return _instance;
	}
	
	public String parseData(File currentFile, DatCrypter crypter, Descriptor desc, ByteBuffer data) throws Exception
	{
		String stringData;
		if (desc.isRawData())
		{
			StringBuilder builder = new StringBuilder();
			readVariables(currentFile, crypter, desc.getNodes().get(0), new HashMap<String, Variant>(), data, builder, true);
			stringData = builder.toString();
		}
		else
		{
			stringData = this.parseData(currentFile, crypter, data, null, desc.getNodes(), 1, new HashMap<String, Variant>(), false, 0).trim();
		}
		if (desc.getFormat() != null)
		{
			stringData = desc.getFormat().decode(stringData);
		}
		int pos = desc.isSafePackage() ? data.position() + 13 : data.position();
		if (data.limit() > pos)
		{
			L2ClientDat.addLogConsole("Unpacked not full " + data.position() + "/" + data.limit() + " diff: " + (data.limit() - pos), true);
		}
		return stringData;
	}
	
	private String parseData(File currentFile, DatCrypter crypter, ByteBuffer data, ParamNode lastNode, List<ParamNode> nodes, int cycleSize, Map<String, Variant> vars, boolean isNameHidden, int cycleNameLevel) throws Exception
	{
		if (cycleSize > 0)
		{
			StringBuilder out = new StringBuilder();
			for (int i = 0; i < cycleSize; ++i)
			{
				boolean isAddCycleName = !isNameHidden && (lastNode != null) && lastNode.getEntityType().isCycle();
				if (isAddCycleName)
				{
					for (int k = 0; k < cycleNameLevel; ++k)
					{
						out.append("\t");
					}
					out.append(lastNode.getName().concat("_begin"));
					++cycleNameLevel;
				}
				int nodeSize = nodes.size();
				int nNode = 0;
				for (ParamNode n : nodes)
				{
					if (n.isIterator())
					{
						continue;
					}
					++nNode;
				}
				if (isNameHidden && (nNode > 1))
				{
					out.append("{");
				}
				for (int j = 0; j < nodeSize; ++j)
				{
					ParamNode node = nodes.get(j);
					if (node.getEntityType().isIf())
					{
						Variant var = vars.get(node.getParamIf());
						if ((var == null) || !var.toString().equalsIgnoreCase(node.getValIf()))
						{
							continue;
						}
						out.append(this.parseData(currentFile, crypter, data, null, node.getSubNodes(), 1, vars, isNameHidden, cycleNameLevel));
						continue;
					}
					if (!(node.isIterator() || node.getEntityType().isConstant() || isNameHidden || (!node.getEntityType().isWrapper() && !node.isNameHidden())))
					{
						out.append("\t").append(node.getName()).append("=");
					}
					if (node.getEntityType().isWrapper())
					{
						out.append(this.parseData(currentFile, crypter, data, null, node.getSubNodes(), 1, vars, true, cycleNameLevel));
					}
					else if (node.getEntityType().isCycle())
					{
						int size;
						if (node.getSize() >= 0)
						{
							size = node.getSize();
						}
						else
						{
							Variant var = vars.get(node.getCycleName());
							if (var.isInt())
							{
								size = var.getInt();
							}
							else if (var.isShort())
							{
								size = var.getShort();
							}
							else
							{
								throw new Exception("Wrong cycle variable format for cycle: " + node.getName() + " iterator: " + node.getCycleName());
							}
						}
						if (node.isNameHidden())
						{
							out.append("{");
						}
						out.append(this.parseData(currentFile, crypter, data, node, node.getSubNodes(), size, vars, node.isNameHidden(), cycleNameLevel));
						if (node.isNameHidden())
						{
							out.append("}");
						}
					}
					else if (node.getEntityType().isConstant())
					{
						out.append(node.getName().replace("\\t", "\t").replace("\\r\\n", "\r\n"));
					}
					else if (node.getEntityType().isVariable())
					{
						readVariables(currentFile, crypter, node, vars, data, out, false);
					}
					if (!node.isIterator() && !node.getEntityType().isConstant() && isNameHidden && (j != (nodeSize - 1)))
					{
						out.append(";");
					}
					if (!vars.containsKey(node.getName()))
					{
						continue;
					}
					DebugUtil.debugPos(data.position(), node.getName(), vars.get(node.getName()));
				}
				if (isNameHidden)
				{
					if (nNode > 1)
					{
						out.append("}");
					}
					if (i < (cycleSize - 1))
					{
						out.append(";");
					}
				}
				if (!isAddCycleName)
				{
					continue;
				}
				if (out.charAt(out.length() - 1) != '\n')
				{
					out.append("\t");
				}
				out.append(lastNode.getName()).append("_end\r\n");
				--cycleNameLevel;
			}
			return out.toString();
		}
		return "";
	}
	
	private void readVariables(File currentFile, DatCrypter crypter, ParamNode node, Map<String, Variant> vars, ByteBuffer data, StringBuilder out, boolean isRaw) throws Exception
	{
		switch (node.getType())
		{
			case UCHAR:
			{
				short value = (byte) ByteReader.readChar(data);
				if (!node.isIterator())
				{
					out.append(value);
				}
				vars.put(node.getName(), new Variant(value, Short.class));
				break;
			}
			case UBYTE:
			{
				int value = ByteReader.readUByte(data);
				if (!node.isIterator())
				{
					out.append(value);
				}
				vars.put(node.getName(), new Variant(value, Integer.class));
				break;
			}
			case SHORT:
			{
				short value = ByteReader.readShort(data);
				if (!node.isIterator())
				{
					out.append(value);
				}
				vars.put(node.getName(), new Variant(value, Short.class));
				break;
			}
			case USHORT:
			{
				int value = ByteReader.readShort(data) & 65535;
				if (!node.isIterator())
				{
					out.append(value);
				}
				vars.put(node.getName(), new Variant(value, Integer.class));
				break;
			}
			case UINT:
			{
				int value = ByteReader.readUInt(data);
				if (!node.isIterator())
				{
					out.append(value);
				}
				vars.put(node.getName(), new Variant(value, Integer.class));
				break;
			}
			case INT:
			{
				int value = ByteReader.readInt(data);
				if (!node.isIterator())
				{
					out.append(value);
				}
				vars.put(node.getName(), new Variant(value, Integer.class));
				break;
			}
			case CNTR:
			{
				int value = ByteReader.readCompactInt(data);
				if (!node.isIterator())
				{
					out.append(value);
				}
				vars.put(node.getName(), new Variant(value, Integer.class));
				break;
			}
			case UNICODE:
			{
				String str = ByteReader.readUtfString(data, isRaw);
				if (isRaw)
				{
					out.append(str);
					break;
				}
				if (!node.isIterator())
				{
					out.append("[").append(str).append("]");
				}
				vars.put(node.getName(), new Variant(str, String.class));
				break;
			}
			case ASCF:
			{
				String str = ByteReader.readString(data, isRaw);
				if (isRaw)
				{
					out.append(str);
					break;
				}
				if (!node.isIterator())
				{
					out.append("[").append(str).append("]");
				}
				vars.put(node.getName(), new Variant(str, String.class));
				break;
			}
			case DOUBLE:
			{
				double value = ByteReader.readDouble(data);
				if (!node.isIterator())
				{
					out.append(new BigDecimal(Double.toString(value)).toPlainString());
				}
				vars.put(node.getName(), new Variant(value, Double.class));
				break;
			}
			case FLOAT:
			{
				float value = ByteReader.readFloat(data);
				if (!node.isIterator())
				{
					out.append(value);
				}
				vars.put(node.getName(), new Variant(Float.valueOf(value), Float.class));
				break;
			}
			case LONG:
			{
				long value = ByteReader.readLong(data);
				if (!node.isIterator())
				{
					out.append(value);
				}
				vars.put(node.getName(), new Variant(value, Long.class));
				break;
			}
			case RGBA:
			{
				String value = ByteReader.readRGBA(data);
				if (!node.isIterator())
				{
					out.append(value);
				}
				vars.put(node.getName(), new Variant(value, String.class));
				break;
			}
			case RGB:
			{
				String value = ByteReader.readRGB(data);
				if (!node.isIterator())
				{
					out.append(value);
				}
				vars.put(node.getName(), new Variant(value, String.class));
				break;
			}
			case HEX:
			{
				int value = ByteReader.readUByte(data);
				if (!node.isIterator())
				{
					String hex = Integer.toHexString(value).toUpperCase();
					if (hex.length() == 1)
					{
						hex = "0" + hex;
					}
					out.append(hex);
				}
				vars.put(node.getName(), new Variant(value, Integer.class));
				break;
			}
			case MAP_INT:
			{
				int index = ByteReader.readUInt(data);
				if (ConfigDebug.DAT_REPLACEMENT_NAMES)
				{
					String paramName = GameDataName.getInstance().getString(currentFile, crypter, index);
					if (!node.isIterator())
					{
						out.append(paramName);
					}
					vars.put(node.getName(), new Variant(paramName, String.class));
					break;
				}
				if (!node.isIterator())
				{
					out.append(index);
				}
				vars.put(node.getName(), new Variant(index, Integer.class));
				break;
			}
		}
	}
}
