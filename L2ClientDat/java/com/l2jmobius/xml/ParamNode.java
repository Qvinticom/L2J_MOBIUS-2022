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

import java.util.ArrayList;
import java.util.List;

public class ParamNode
{
	private String _name;
	private int _size = -1;
	private boolean _hidden = false;
	private String _cycleName;
	private final ParamNodeType _entityType;
	private final ParamType _type;
	private List<ParamNode> _sub;
	private boolean _isIterator;
	private ParamNode _tmpIterator;
	private boolean _skipWriteSize;
	private String _paramIf;
	private String _valIf;
	
	ParamNode(String name, ParamNodeType entityType, ParamType type)
	{
		_name = name;
		_entityType = entityType;
		_type = type;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public void setSize(int size)
	{
		_size = size;
	}
	
	public int getSize()
	{
		return _size;
	}
	
	void setIterator()
	{
		_isIterator = true;
	}
	
	void setHidden()
	{
		_hidden = true;
	}
	
	boolean isIterator()
	{
		return _isIterator && (_size < 0);
	}
	
	boolean isNameHidden()
	{
		return _hidden;
	}
	
	public String getName()
	{
		return _name;
	}
	
	ParamNodeType getEntityType()
	{
		return _entityType;
	}
	
	public ParamType getType()
	{
		return _type;
	}
	
	synchronized ParamNode copy()
	{
		ParamNode node = new ParamNode(getName(), getEntityType(), getType());
		if (isNameHidden())
		{
			node.setHidden();
		}
		if (isIterator())
		{
			node.setIterator();
		}
		node.setSkipWriteSize(isSkipWriteSize());
		node.setCycleName(getCycleName());
		if (getSubNodes() != null)
		{
			ArrayList<ParamNode> list = new ArrayList<>();
			for (ParamNode n : getSubNodes())
			{
				ParamNode copyN = n.copy();
				list.add(copyN);
			}
			node.addSubNodes(list);
		}
		return node;
	}
	
	synchronized void addSubNodes(List<ParamNode> n)
	{
		if (_sub == null)
		{
			_sub = new ArrayList<>();
		}
		_sub.addAll(n);
	}
	
	List<ParamNode> getSubNodes()
	{
		return _sub;
	}
	
	@Override
	public String toString()
	{
		return _name + "[" + (_entityType) + "][" + _cycleName + "][" + (_type) + "]";
	}
	
	ParamNode getTmpIterator()
	{
		return _tmpIterator;
	}
	
	void setTmpIterator(ParamNode tmpIterator)
	{
		_tmpIterator = tmpIterator;
	}
	
	boolean isSkipWriteSize()
	{
		return _skipWriteSize;
	}
	
	void setSkipWriteSize(boolean skipWrite)
	{
		_skipWriteSize = skipWrite;
	}
	
	void setParamIf(String paramIf)
	{
		_paramIf = paramIf;
	}
	
	void setValIf(String valIf)
	{
		_valIf = valIf;
	}
	
	String getParamIf()
	{
		return _paramIf;
	}
	
	String getValIf()
	{
		return _valIf;
	}
	
	String getCycleName()
	{
		return _cycleName;
	}
	
	void setCycleName(String cycleName)
	{
		_cycleName = cycleName;
	}
}
