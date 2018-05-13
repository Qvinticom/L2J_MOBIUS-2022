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
package com.l2jmobius.clientcryptor.crypt;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public abstract class DatCrypter
{
	private final ReentrantLock _lock = new ReentrantLock(true);
	private final int code;
	private boolean useStructure;
	private final List<String> fileEndNames = new ArrayList<>();
	
	public DatCrypter(int code)
	{
		this.code = code;
	}
	
	public abstract void update(byte[] var1);
	
	public abstract ByteBuffer decryptResult();
	
	public abstract ByteBuffer encryptResult();
	
	public abstract int getChunkSize(int var1);
	
	public abstract int getSkipSize();
	
	public abstract boolean isLock();
	
	public boolean checkAquired()
	{
		return _lock.isHeldByCurrentThread();
	}
	
	public void aquire()
	{
		_lock.lock();
	}
	
	public void release()
	{
		_lock.unlock();
	}
	
	public abstract boolean isEncrypt();
	
	public abstract void unlock();
	
	public int getCode()
	{
		return code;
	}
	
	public void addFileExtension(String n)
	{
		fileEndNames.addAll(Arrays.asList(n.split(";")));
	}
	
	public boolean checkFileExtension(String n)
	{
		return n.contains(".") && fileEndNames.contains(n.split("\\.")[1]);
	}
	
	public boolean isUseStructure()
	{
		return useStructure;
	}
	
	public void setUseStructure(boolean useStructure)
	{
		this.useStructure = useStructure;
	}
}
