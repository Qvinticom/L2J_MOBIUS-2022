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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jmobius.listeners.FormatListener;
import com.l2jmobius.util.DebugUtil;
import com.l2jmobius.util.Util;
import com.l2jmobius.xml.exceptions.CycleArgumentException;

public class DescriptorParser
{
	private static DescriptorParser _parser = new DescriptorParser();
	private final Map<String, Map<String, Descriptor>> _descriptors = new HashMap<>();
	private final Map<String, List<ParamNode>> _definitions = new HashMap<>();
	private final Map<String, List<DescriptorLink>> _links = new HashMap<>();
	
	private DescriptorParser()
	{
		load();
	}
	
	public static DescriptorParser getInstance()
	{
		return _parser;
	}
	
	private void load()
	{
		parseDefinitions();
		Util.loadFiles("./structure/", ".xml").forEach(this::parseDescriptor);
	}
	
	private void parseDefinitions()
	{
		File def = new File("./!definitions.xml");
		if (def.exists())
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringElementContentWhitespace(true);
			factory.setIgnoringComments(true);
			try
			{
				Document doc = factory.newDocumentBuilder().parse(def);
				Node defsNode = doc.getFirstChild();
				while (defsNode != null)
				{
					if (defsNode.getNodeName().equals("definitions"))
					{
						for (Node defNode = defsNode.getFirstChild(); defNode != null; defNode = defNode.getNextSibling())
						{
							if (!defNode.getNodeName().equals("definition"))
							{
								continue;
							}
							String defName = defNode.getAttributes().getNamedItem("name").getNodeValue();
							List<ParamNode> nodes = parseNodes(defNode, true, new HashSet<String>(), "definitions->" + defName);
							_definitions.put(defName, nodes);
						}
					}
					defsNode = doc.getNextSibling();
				}
			}
			catch (Exception e)
			{
				DebugUtil.getLogger().error(e.getMessage(), e);
			}
		}
	}
	
	private void parseDescriptor(File file)
	{
		if (!file.exists())
		{
			DebugUtil.debug("File " + file.getName() + " not found.");
		}
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringElementContentWhitespace(true);
			factory.setIgnoringComments(true);
			Document doc = factory.newDocumentBuilder().parse(file);
			Node fileNode0 = doc.getFirstChild();
			while (fileNode0 != null)
			{
				if (fileNode0.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node fileNode = fileNode0.getFirstChild(); fileNode != null; fileNode = fileNode.getNextSibling())
					{
						String dir;
						Object obj;
						String namePattern;
						Map<String, Descriptor> versions;
						if (fileNode.getNodeName().equalsIgnoreCase("link"))
						{
							dir = file.getName().substring(0, file.getName().length() - 4);
							namePattern = fileNode.getAttributes().getNamedItem("pattern").getNodeValue();
							String linkFile = fileNode.getAttributes().getNamedItem("file").getNodeValue();
							String linkVersion = fileNode.getAttributes().getNamedItem("version").getNodeValue();
							List<DescriptorLink> list = _links.get(dir);
							if (list == null)
							{
								list = new ArrayList<>();
							}
							list.add(new DescriptorLink(dir, namePattern, linkFile, linkVersion));
							_links.put(dir, list);
							continue;
						}
						if (!fileNode.getNodeName().equalsIgnoreCase("file"))
						{
							continue;
						}
						dir = file.getName().substring(0, file.getName().length() - 4);
						namePattern = fileNode.getAttributes().getNamedItem("pattern").getNodeValue();
						boolean isRawData = parseBoolNode(fileNode, "isRaw", false);
						boolean isSafePackage = parseBoolNode(fileNode, "isSafePackage", false);
						String formatName = parseStringNode(fileNode, "format", null);
						DebugUtil.debug("Boot of parsing file: " + namePattern);
						List<ParamNode> nodes = parseNodes(fileNode, false, new HashSet<String>(), dir + "->" + namePattern);
						Descriptor desc = new Descriptor(file.getName(), namePattern, nodes);
						desc.setIsRawData(isRawData);
						desc.setIsSafePackage(isSafePackage);
						if ((formatName != null) && ((obj = Util.loadJavaClass(formatName, "./structure/format/")) instanceof FormatListener))
						{
							desc.setFormat((FormatListener) obj);
						}
						if ((versions = _descriptors.get(dir)) == null)
						{
							versions = new HashMap<>();
						}
						versions.put(namePattern, desc);
						_descriptors.put(dir, versions);
						DebugUtil.debug("End of parsing file: " + namePattern);
					}
				}
				fileNode0 = doc.getNextSibling();
			}
		}
		catch (Exception e)
		{
			DebugUtil.getLogger().error(e.getMessage(), e);
		}
	}
	
	private List<ParamNode> parseNodes(Node fileNode, boolean isHideName, Set<String> names, String fileName) throws Exception
	{
		HashMap<String, Integer> defsCounter = new HashMap<>();
		LinkedList<ParamNode> nodes = new LinkedList<>();
		for (Node node = fileNode.getFirstChild(); node != null; node = node.getNextSibling())
		{
			ParamNode beginNode;
			String nodeName = node.getNodeName();
			if (nodeName.equals("#text"))
			{
				continue;
			}
			boolean isHide = isHideName || parseBoolNode(node, "hidden", true);
			if (node.getAttributes().getNamedItem("name") == null)
			{
				DebugUtil.getLogger().warn("Node name == null, fileName: " + fileName);
				continue;
			}
			String entityName = node.getAttributes().getNamedItem("name").getNodeValue();
			if (nodeName.equalsIgnoreCase("node"))
			{
				String type = node.getAttributes().getNamedItem("reader").getNodeValue();
				if (_definitions.containsKey(type))
				{
					if (!defsCounter.containsKey(type))
					{
						defsCounter.put(type, 1);
					}
					else
					{
						defsCounter.put(type, defsCounter.get(type) + 1);
					}
					List<ParamNode> defNodes = _definitions.get(type);
					for (ParamNode defNode : defNodes)
					{
						ParamNode copied = defNode.copy();
						copied.setName(entityName);
						if (isHide)
						{
							copied.setHidden();
						}
						nodes.add(copied);
					}
				}
				else
				{
					ParamNode dataNode = new ParamNode(entityName, ParamNodeType.VARIABLE, ParamType.valueOf(type));
					if (isHide)
					{
						dataNode.setHidden();
					}
					nodes.add(dataNode);
					DebugUtil.debug("Found node: " + dataNode.getName());
				}
				if (names.contains(entityName))
				{
					DebugUtil.getLogger().warn("Node name duplicated [" + entityName + "]  fileName: " + fileName);
				}
				names.add(entityName);
				continue;
			}
			if (nodeName.equalsIgnoreCase("for"))
			{
				String iteratorName = entityName;
				boolean skipWriteSize = parseBoolNode(node, "skipWriteSize", false);
				int size = -1;
				if (node.getAttributes().getNamedItem("size") != null)
				{
					String sizeStr = node.getAttributes().getNamedItem("size").getNodeValue();
					if (sizeStr.startsWith("#"))
					{
						iteratorName = sizeStr.substring(1);
					}
					else
					{
						size = Integer.parseInt(sizeStr);
					}
				}
				if (size == 0)
				{
					DebugUtil.getLogger().warn("Size of cycle [" + iteratorName + "] was set to zero. Deprecated cycle?");
				}
				DebugUtil.debug("Found cycle for variable: " + entityName);
				ParamNode beginNode2 = new ParamNode(entityName, ParamNodeType.FOR, null);
				if (size >= 0)
				{
					beginNode2.setSize(size);
				}
				if (isHide)
				{
					beginNode2.setHidden();
				}
				beginNode2.setSkipWriteSize(skipWriteSize);
				beginNode2.addSubNodes(parseNodes(node, false, names, fileName));
				beginNode2.setCycleName(iteratorName);
				nodes.add(beginNode2);
				boolean iteratorFound = false;
				for (ParamNode n : nodes)
				{
					if (!iteratorName.equals(n.getName()))
					{
						continue;
					}
					n.setIterator();
					iteratorFound = true;
					break;
				}
				if (iteratorFound || (size >= 0))
				{
					continue;
				}
				throw new CycleArgumentException("Invalid argument [" + iteratorName + "] for [cycle]");
			}
			if (nodeName.equalsIgnoreCase("wrapper"))
			{
				beginNode = new ParamNode(entityName, ParamNodeType.WRAPPER, null);
				beginNode.addSubNodes(parseNodes(node, true, names, fileName));
				nodes.add(beginNode);
				DebugUtil.debug("Found [wrapper] data " + entityName);
				continue;
			}
			if (nodeName.equalsIgnoreCase("write"))
			{
				beginNode = new ParamNode(entityName, ParamNodeType.CONSTANT, ParamType.STRING);
				beginNode.setHidden();
				nodes.add(beginNode);
				DebugUtil.debug("Found [constant] data: " + entityName);
				continue;
			}
			if (!nodeName.equalsIgnoreCase("if"))
			{
				continue;
			}
			String paramName = node.getAttributes().getNamedItem("param").getNodeValue();
			String vsl = node.getAttributes().getNamedItem("val").getNodeValue();
			if (!paramName.startsWith("#"))
			{
				throw new Exception("Invalid argument [" + entityName + "] for [if]");
			}
			paramName = paramName.substring(1);
			ParamNode beginNode3 = new ParamNode(entityName, ParamNodeType.IF, null);
			beginNode3.setParamIf(paramName);
			beginNode3.setValIf(vsl);
			beginNode3.addSubNodes(parseNodes(node, false, names, fileName));
			nodes.add(beginNode3);
			DebugUtil.debug("Found [if] data: " + entityName);
		}
		return nodes;
	}
	
	public Descriptor findDescriptorForFile(String dir, String fileName)
	{
		List<DescriptorLink> listDes = _links.get(dir);
		if (listDes == null)
		{
			return null;
		}
		for (DescriptorLink desc : listDes)
		{
			Map<String, Descriptor> versions;
			if (!fileName.toLowerCase().matches(desc.getNamePattern().toLowerCase()) || !_descriptors.containsKey(desc.getLinkFile()) || !(versions = _descriptors.get(desc.getLinkFile())).containsKey(desc.getLinkVersion()))
			{
				continue;
			}
			return versions.get(desc.getLinkVersion());
		}
		return null;
	}
	
	private boolean parseBoolNode(Node node, String name, boolean def)
	{
		if (node.getAttributes() == null)
		{
			return def;
		}
		if (node.getAttributes().getNamedItem(name) == null)
		{
			return def;
		}
		return node.getAttributes().getNamedItem(name).getNodeValue().equalsIgnoreCase("true");
	}
	
	private String parseStringNode(Node node, String name, String def)
	{
		if (node.getAttributes() == null)
		{
			return def;
		}
		if (node.getAttributes().getNamedItem(name) == null)
		{
			return def;
		}
		return node.getAttributes().getNamedItem(name).getNodeValue();
	}
}
