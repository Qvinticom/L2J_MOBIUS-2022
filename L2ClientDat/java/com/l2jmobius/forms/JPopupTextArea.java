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
package com.l2jmobius.forms;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

public class JPopupTextArea extends JTextArea
{
	static final String COPY = "Copy (Ctrl + C)";
	static final String CUT = "Cut (Ctrl + X)";
	static final String PASTE = "Paste (Ctrl + V)";
	static final String DELETE = "Delete";
	static final String SELECTALL = "Select all (Ctrl + A)";
	static final String LINE = "Line (Ctrl + G)";
	static final String FIND = "Find (Ctrl + F)";
	Vector<Integer> lineLength = new Vector<>();
	
	public JPopupTextArea()
	{
		addPopupMenu();
	}
	
	private void addPopupMenu()
	{
		JPopupMenu menu = new JPopupMenu();
		JMenuItem copyItem = new JMenuItem();
		copyItem.setAction(getActionMap().get("copy-to-clipboard"));
		copyItem.setText(COPY);
		JMenuItem cutItem = new JMenuItem();
		cutItem.setAction(getActionMap().get("cut-to-clipboard"));
		cutItem.setText(CUT);
		JMenuItem pasteItem = new JMenuItem();
		pasteItem.setAction(getActionMap().get("paste-from-clipboard"));
		pasteItem.setText(PASTE);
		JMenuItem deleteItem = new JMenuItem();
		deleteItem.setAction(getActionMap().get("delete-previous"));
		deleteItem.setText(DELETE);
		JMenuItem selectAllItem = new JMenuItem();
		selectAllItem.setAction(getActionMap().get("select-all"));
		selectAllItem.setText(SELECTALL);
		JMenuItem selectLine = new JMenuItem();
		selectLine.addActionListener(e -> goToLine());
		selectLine.setText(LINE);
		JMenuItem selectFind = new JMenuItem();
		selectFind.addActionListener(e -> searchString());
		selectFind.setText(FIND);
		menu.add(copyItem);
		menu.add(cutItem);
		menu.add(pasteItem);
		menu.add(deleteItem);
		menu.add(new JSeparator());
		menu.add(selectAllItem);
		menu.add(selectLine);
		menu.add(selectFind);
		this.add(menu);
		addMouseListener(new PopupTriggerMouseListener(menu, this));
		addKeyListener(new KeyListen());
	}
	
	void goToLine()
	{
		String lineno;
		boolean fnd;
		int no = 0;
		do
		{
			block8:
			{
				fnd = true;
				lineno = JOptionPane.showInputDialog("Line number:");
				try
				{
					no = Integer.parseInt(lineno);
				}
				catch (Exception exp)
				{
					if (lineno == null)
					{
						break block8;
					}
					JOptionPane.showMessageDialog(new Frame(), "Enter a valid line number", "Error", 0);
					fnd = false;
				}
			}
			if ((no > 0) || (lineno == null))
			{
				continue;
			}
			JOptionPane.showMessageDialog(new Frame(), "Enter a valid line number", "Error", 0);
			fnd = false;
		}
		while (!fnd);
		if (lineno != null)
		{
			getLinePosition();
			if ((no - 1) >= lineLength.size())
			{
				JOptionPane.showMessageDialog(new Frame(), "Line number does not exist", "Error", 0);
			}
			else
			{
				try
				{
					this.requestFocus();
					setCaretPosition(lineLength.elementAt(no - 1));
				}
				catch (Exception exp)
				{
					JOptionPane.showMessageDialog(new Frame(), "Bad position", "Error", 0);
				}
			}
		}
	}
	
	void searchString()
	{
		String lineno = JOptionPane.showInputDialog("Search string: ");
		if ((lineno == null) || lineno.isEmpty())
		{
			JOptionPane.showMessageDialog(new Frame(), "Enter a empty string", "Error", 0);
			return;
		}
		if (!lineno.isEmpty())
		{
			try
			{
				this.requestFocus();
				String editorText = this.getText();
				String searchValue = lineno;
				int start = editorText.indexOf(searchValue, getSelectionEnd());
				if (start != -1)
				{
					setCaretPosition(start);
					moveCaretPosition(start + searchValue.length());
					getCaret().setSelectionVisible(true);
				}
			}
			catch (Exception exp)
			{
				JOptionPane.showMessageDialog(new Frame(), "Bad position", "Error", 0);
			}
		}
	}
	
	private void getLinePosition()
	{
		lineLength = new Vector<>();
		String txt = this.getText();
		int width = getWidth();
		StringTokenizer st = new StringTokenizer(txt, "\n ", true);
		String str = " ";
		int len = 0;
		lineLength.addElement(new Integer(0));
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			int w = getGraphics().getFontMetrics(getGraphics().getFont()).stringWidth(str + token);
			if ((w > width) || (token.charAt(0) == '\n'))
			{
				len += str.length();
				if (token.charAt(0) == '\n')
				{
					lineLength.addElement(new Integer(len));
				}
				else
				{
					lineLength.addElement(new Integer(len - 1));
				}
				str = token;
				continue;
			}
			str = str + token;
		}
	}
	
	private class KeyListen implements KeyListener
	{
		private boolean controlDown;
		private boolean gDown;
		private boolean fDown;
		
		KeyListen()
		{
		}
		
		@Override
		public void keyTyped(KeyEvent e)
		{
		}
		
		@Override
		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == 17)
			{
				controlDown = true;
			}
			else if (e.getKeyCode() == 71)
			{
				gDown = true;
			}
			else if (e.getKeyCode() == 70)
			{
				fDown = true;
			}
			if (controlDown)
			{
				if (gDown)
				{
					controlDown = false;
					gDown = false;
					goToLine();
				}
				else if (fDown)
				{
					controlDown = false;
					fDown = false;
					searchString();
				}
			}
		}
		
		@Override
		public void keyReleased(KeyEvent e)
		{
			if (e.getKeyCode() == 17)
			{
				controlDown = false;
			}
			else if (e.getKeyCode() == 71)
			{
				gDown = false;
			}
			else if (e.getKeyCode() == 70)
			{
				fDown = false;
			}
		}
	}
	
	private static class PopupTriggerMouseListener extends MouseAdapter
	{
		private final JPopupMenu popup;
		private final JComponent component;
		
		public PopupTriggerMouseListener(JPopupMenu popup, JComponent component)
		{
			this.popup = popup;
			this.component = component;
		}
		
		private void showMenuIfPopupTrigger(MouseEvent e)
		{
			if (e.isPopupTrigger())
			{
				popup.show(component, e.getX() + 3, e.getY() + 3);
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			showMenuIfPopupTrigger(e);
		}
		
		@Override
		public void mouseReleased(MouseEvent e)
		{
			showMenuIfPopupTrigger(e);
		}
	}
}
