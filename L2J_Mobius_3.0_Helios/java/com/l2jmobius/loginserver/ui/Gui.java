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
package com.l2jmobius.loginserver.ui;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.l2jmobius.commons.util.LimitLinesDocumentListener;
import com.l2jmobius.commons.util.SplashScreen;
import com.l2jmobius.loginserver.GameServerTable;
import com.l2jmobius.loginserver.GameServerTable.GameServerInfo;
import com.l2jmobius.loginserver.LoginController;
import com.l2jmobius.loginserver.LoginServer;
import com.l2jmobius.loginserver.network.gameserverpackets.ServerStatus;

/**
 * @author Mobius
 */
public class Gui
{
	JFrame frmLoginServer;
	JTextArea txtrConsole;
	
	JCheckBoxMenuItem chckbxmntmEnabled;
	JCheckBoxMenuItem chckbxmntmDisabled;
	JCheckBoxMenuItem chckbxmntmGmOnly;
	
	final static String[] shutdownOptions =
	{
		"Shutdown",
		"Cancel"
	};
	final static String[] restartOptions =
	{
		"Restart",
		"Cancel"
	};
	
	public Gui()
	{
		frmLoginServer = new JFrame();
		frmLoginServer.setVisible(false);
		frmLoginServer.setTitle("Mobius - LoginServer");
		frmLoginServer.setResizable(false);
		frmLoginServer.setBounds(100, 100, 825, 618);
		frmLoginServer.getContentPane().setLayout(null);
		frmLoginServer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frmLoginServer.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent ev)
			{
				if (JOptionPane.showOptionDialog(null, "Shutdown LoginServer?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, shutdownOptions, shutdownOptions[1]) == 0)
				{
					LoginServer.getInstance().shutdown(false);
				}
			}
		});
		
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setBounds(10, 11, 799, 544);
		frmLoginServer.getContentPane().add(scrollPanel);
		
		txtrConsole = new JTextArea();
		txtrConsole.setWrapStyleWord(true);
		txtrConsole.setEditable(false);
		txtrConsole.setFont(new Font("Monospaced", Font.PLAIN, 16));
		scrollPanel.setViewportView(txtrConsole);
		txtrConsole.setDropMode(DropMode.INSERT);
		txtrConsole.setLineWrap(true);
		txtrConsole.setEditable(false);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		frmLoginServer.setJMenuBar(menuBar);
		
		JMenu mnActions = new JMenu("Actions");
		mnActions.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		menuBar.add(mnActions);
		
		JMenuItem mntmShutdown = new JMenuItem("Shutdown");
		mntmShutdown.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmShutdown.addActionListener(arg0 ->
		{
			if (JOptionPane.showOptionDialog(null, "Shutdown LoginServer?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, shutdownOptions, shutdownOptions[1]) == 0)
			{
				LoginServer.getInstance().shutdown(false);
			}
		});
		mnActions.add(mntmShutdown);
		
		JMenuItem mntmRestart = new JMenuItem("Restart");
		mntmRestart.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmRestart.addActionListener(arg0 ->
		{
			if (JOptionPane.showOptionDialog(null, "Restart LoginServer?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, restartOptions, restartOptions[1]) == 0)
			{
				LoginServer.getInstance().shutdown(true);
			}
		});
		mnActions.add(mntmRestart);
		
		JMenu mnReload = new JMenu("Reload");
		mnReload.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		menuBar.add(mnReload);
		
		JMenuItem mntmBannedIps = new JMenuItem("Banned IPs");
		mntmBannedIps.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmBannedIps.addActionListener(arg0 ->
		{
			LoginController.getInstance().getBannedIps().clear();
			LoginServer.getInstance().loadBanFile();
		});
		mnReload.add(mntmBannedIps);
		
		JMenu mnStatus = new JMenu("Status");
		mnStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		menuBar.add(mnStatus);
		
		chckbxmntmEnabled = new JCheckBoxMenuItem("Enabled");
		chckbxmntmEnabled.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		chckbxmntmEnabled.addActionListener(arg0 ->
		{
			chckbxmntmEnabled.setSelected(true);
			chckbxmntmDisabled.setSelected(false);
			chckbxmntmGmOnly.setSelected(false);
			LoginServer.getInstance().setStatus(ServerStatus.STATUS_NORMAL);
			for (GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
			{
				gsi.setStatus(ServerStatus.STATUS_NORMAL);
			}
			LoginServer.getInstance().LOGGER.info("Status changed to enabled.");
		});
		chckbxmntmEnabled.setSelected(true);
		mnStatus.add(chckbxmntmEnabled);
		
		chckbxmntmDisabled = new JCheckBoxMenuItem("Disabled");
		chckbxmntmDisabled.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		chckbxmntmDisabled.addActionListener(arg0 ->
		{
			chckbxmntmEnabled.setSelected(false);
			chckbxmntmDisabled.setSelected(true);
			chckbxmntmGmOnly.setSelected(false);
			LoginServer.getInstance().setStatus(ServerStatus.STATUS_DOWN);
			for (GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
			{
				gsi.setStatus(ServerStatus.STATUS_DOWN);
			}
			LoginServer.getInstance().LOGGER.info("Status changed to disabled.");
		});
		mnStatus.add(chckbxmntmDisabled);
		
		chckbxmntmGmOnly = new JCheckBoxMenuItem("GM only");
		chckbxmntmGmOnly.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		chckbxmntmGmOnly.addActionListener(arg0 ->
		{
			chckbxmntmEnabled.setSelected(false);
			chckbxmntmDisabled.setSelected(false);
			chckbxmntmGmOnly.setSelected(true);
			LoginServer.getInstance().setStatus(ServerStatus.STATUS_GM_ONLY);
			for (GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
			{
				gsi.setStatus(ServerStatus.STATUS_GM_ONLY);
			}
			LoginServer.getInstance().LOGGER.info("Status changed to GM only.");
		});
		mnStatus.add(chckbxmntmGmOnly);
		
		// Align menu items bar to right after this.
		// menuBar.add(Box.createHorizontalGlue());
		
		JMenu mnHelp = new JMenu("Help");
		mnHelp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmAbout.addActionListener(arg0 ->
		{
			new frmAbout();
		});
		mnHelp.add(mntmAbout);
		
		// Set icons.
		List<Image> icons = new ArrayList<>();
		icons.add(new ImageIcon("..\\images\\l2jmobius_16x16.png").getImage());
		icons.add(new ImageIcon("..\\images\\l2jmobius_32x32.png").getImage());
		icons.add(new ImageIcon("..\\images\\l2jmobius_64x64.png").getImage());
		icons.add(new ImageIcon("..\\images\\l2jmobius_128x128.png").getImage());
		frmLoginServer.setIconImages(icons);
		
		// Center frame to screen.
		frmLoginServer.setLocationRelativeTo(null);
		
		// Limit console lines to 500.
		txtrConsole.getDocument().addDocumentListener(new LimitLinesDocumentListener(500));
		
		// Redirect output to text area.
		redirectSystemStreams();
		
		// Show SplashScreen.
		new SplashScreen("..\\images\\splash.png", 5000, frmLoginServer);
	}
	
	// Set where the text is redirected. In this case, txtrConsole.
	void updateTextArea(String text)
	{
		SwingUtilities.invokeLater(() ->
		{
			txtrConsole.append(text);
			txtrConsole.setCaretPosition(txtrConsole.getText().length());
		});
	}
	
	// Method that manages the redirect.
	private void redirectSystemStreams()
	{
		OutputStream out = new OutputStream()
		{
			@Override
			public void write(int b)
			{
				updateTextArea(String.valueOf((char) b));
			}
			
			@Override
			public void write(byte[] b, int off, int len)
			{
				updateTextArea(new String(b, off, len));
			}
			
			@Override
			public void write(byte[] b)
			{
				write(b, 0, b.length);
			}
		};
		
		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}
}
