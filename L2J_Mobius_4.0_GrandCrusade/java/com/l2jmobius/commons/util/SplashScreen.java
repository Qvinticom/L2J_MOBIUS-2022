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
package com.l2jmobius.commons.util;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JWindow;

/**
 * @author Mobius
 */
public class SplashScreen extends JWindow
{
	Image image;
	JFrame parentFrame;
	
	/**
	 * @param path of image file
	 * @param time in milliseconds
	 * @param parent frame to set visible after time ends
	 */
	public SplashScreen(String path, long time, JFrame parent)
	{
		parentFrame = parent;
		setBackground(new Color(0, 255, 0, 0)); // Transparency.
		image = Toolkit.getDefaultToolkit().getImage(path);
		ImageIcon imageIcon = new ImageIcon(image);
		setSize(imageIcon.getIconWidth(), imageIcon.getIconHeight());
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);
		setVisible(true);
		
		// Schedule to close.
		Executors.newScheduledThreadPool(1).schedule(this::close, imageIcon.getIconWidth() > 0 ? time : 100, TimeUnit.MILLISECONDS);
	}
	
	public void close()
	{
		setVisible(false);
		if (parentFrame != null)
		{
			// Make parent visible.
			parentFrame.setVisible(true);
			// Focus parent window.
			parentFrame.toFront();
			parentFrame.setState(Frame.ICONIFIED);
			parentFrame.setState(Frame.NORMAL);
		}
	}
	
	@Override
	public void paint(Graphics g)
	{
		g.drawImage(image, 0, 0, null);
	}
}
