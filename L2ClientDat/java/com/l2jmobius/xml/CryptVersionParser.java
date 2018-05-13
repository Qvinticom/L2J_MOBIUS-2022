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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jmobius.clientcryptor.crypt.BlowFishDatCrypter;
import com.l2jmobius.clientcryptor.crypt.DESDatCrypter;
import com.l2jmobius.clientcryptor.crypt.DatCrypter;
import com.l2jmobius.clientcryptor.crypt.RSADatCrypter;
import com.l2jmobius.clientcryptor.crypt.XorDatCrypter;
import com.l2jmobius.util.DebugUtil;

public class CryptVersionParser
{
	private static Map<String, DatCrypter> encryptKeys = new LinkedHashMap<>();
	private static Map<String, DatCrypter> decryptKeys = new LinkedHashMap<>();
	private static CryptVersionParser _parser = new CryptVersionParser();
	
	public static CryptVersionParser getInstance()
	{
		return _parser;
	}
	
	private CryptVersionParser()
	{
		CryptVersionParser.parseCryptVersion();
	}
	
	private static void parseCryptVersion()
	{
		File def = new File("./config/cryptVersion.xml");
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
					if (defsNode.getNodeName().equals("keys"))
					{
						for (Node defNode = defsNode.getFirstChild(); defNode != null; defNode = defNode.getNextSibling())
						{
							if (!defNode.getNodeName().equals("key"))
							{
								continue;
							}
							String name = defNode.getAttributes().getNamedItem("name").getNodeValue();
							String type = defNode.getAttributes().getNamedItem("type").getNodeValue().toLowerCase();
							int code = Integer.parseInt(defNode.getAttributes().getNamedItem("code").getNodeValue().toLowerCase());
							boolean isDecrypt = Boolean.parseBoolean(defNode.getAttributes().getNamedItem("decrypt").getNodeValue());
							boolean useStructure = Boolean.parseBoolean(defNode.getAttributes().getNamedItem("useStructure").getNodeValue());
							String extension = defNode.getAttributes().getNamedItem("extension").getNodeValue();
							DatCrypter dat = null;
							switch (type)
							{
								case "rsa":
								{
									String modulus = defNode.getAttributes().getNamedItem("modulus").getNodeValue();
									String exp = defNode.getAttributes().getNamedItem("exp").getNodeValue();
									dat = new RSADatCrypter(code, modulus, exp, isDecrypt);
									break;
								}
								case "xor":
								{
									dat = new XorDatCrypter(code, Integer.parseInt(defNode.getAttributes().getNamedItem("key").getNodeValue()), isDecrypt);
									break;
								}
								case "blowfish":
								{
									dat = new BlowFishDatCrypter(code, defNode.getAttributes().getNamedItem("key").getNodeValue(), isDecrypt);
									break;
								}
								case "des":
								{
									dat = new DESDatCrypter(code, defNode.getAttributes().getNamedItem("key").getNodeValue(), isDecrypt);
								}
							}
							if (dat == null)
							{
								continue;
							}
							dat.addFileExtension(extension);
							dat.setUseStructure(useStructure);
							if (isDecrypt)
							{
								decryptKeys.put(name, dat);
								continue;
							}
							encryptKeys.put(name, dat);
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
	
	public Map<String, DatCrypter> getEncryptKey()
	{
		return encryptKeys;
	}
	
	public Map<String, DatCrypter> getDecryptKeys()
	{
		return decryptKeys;
	}
	
	public DatCrypter getEncryptKey(String s)
	{
		return encryptKeys.get(s);
	}
	
	public DatCrypter getDecryptKey(String s)
	{
		return decryptKeys.get(s);
	}
}
