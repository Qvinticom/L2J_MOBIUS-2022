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
package com.l2jmobius;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.l2jmobius.actions.MassRecryptor;
import com.l2jmobius.actions.MassTxtPacker;
import com.l2jmobius.actions.MassTxtUnpacker;
import com.l2jmobius.actions.OpenDat;
import com.l2jmobius.clientcryptor.DatFile;
import com.l2jmobius.clientcryptor.crypt.DatCrypter;
import com.l2jmobius.config.ConfigDebug;
import com.l2jmobius.config.ConfigWindow;
import com.l2jmobius.data.GameDataName;
import com.l2jmobius.forms.JPopupTextArea;
import com.l2jmobius.util.DebugUtil;
import com.l2jmobius.util.Util;
import com.l2jmobius.xml.CryptVersionParser;
import com.l2jmobius.xml.Descriptor;
import com.l2jmobius.xml.DescriptorParser;
import com.l2jmobius.xml.DescriptorWriter;

public class L2ClientDat extends JFrame
{
	private static JPopupTextArea textPaneLog;
	private static JPopupTextArea textPaneMain;
	private static JComboBox<String> jComboBoxChronicle;
	private static JComboBox<String> jComboBoxDecrypt;
	private static JComboBox<String> jComboBoxEncrypt;
	private static File currentFileWindow;
	
	public L2ClientDat()
	{
		setTitle("L2ClientDat decoder");
		setMinimumSize(new Dimension(1000, 600));
		this.setSize(new Dimension(ConfigWindow.WINDOW_WIDTH, ConfigWindow.WINDOW_HEIGHT));
		getContentPane().setLayout(new BorderLayout());
		setDefaultCloseOperation(3);
		setLocationRelativeTo(null);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent evt)
			{
				ConfigWindow.save("WINDOW_HEIGHT", String.valueOf(L2ClientDat.this.getHeight()));
				ConfigWindow.save("WINDOW_WIDTH", String.valueOf(L2ClientDat.this.getWidth()));
				System.exit(0);
			}
		});
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BorderLayout());
		JPanel buttonPane0 = new JPanel();
		JLabel structureLabel = new JLabel("structure:");
		buttonPane0.add(structureLabel);
		jComboBoxChronicle = new JComboBox<>();
		jComboBoxChronicle.setModel(new DefaultComboBoxModel<>(Util.getFilesNames("./structure/", ".xml")));
		jComboBoxChronicle.setSelectedItem(ConfigWindow.CURRENT_CHRONICLE);
		jComboBoxChronicle.addActionListener(e -> saveComboBox(jComboBoxChronicle, "CURRENT_CHRONICLE"));
		buttonPane0.add(jComboBoxChronicle);
		JLabel decryptLabel = new JLabel("decrypt:");
		buttonPane0.add(decryptLabel);
		jComboBoxDecrypt = new JComboBox<>();
		jComboBoxDecrypt.setModel(new DefaultComboBoxModel<>(CryptVersionParser.getInstance().getDecryptKeys().keySet().toArray(new String[CryptVersionParser.getInstance().getDecryptKeys().keySet().size()])));
		jComboBoxDecrypt.setSelectedItem(ConfigWindow.CURRENT_DECRYPT);
		jComboBoxDecrypt.addActionListener(e -> saveComboBox(jComboBoxDecrypt, "CURRENT_DECRYPT"));
		buttonPane0.add(jComboBoxDecrypt);
		JLabel encryptLabel = new JLabel("encrypt:");
		buttonPane0.add(encryptLabel);
		jComboBoxEncrypt = new JComboBox<>();
		jComboBoxEncrypt.setModel(new DefaultComboBoxModel<>(CryptVersionParser.getInstance().getEncryptKey().keySet().toArray(new String[CryptVersionParser.getInstance().getDecryptKeys().keySet().size()])));
		jComboBoxEncrypt.setSelectedItem(ConfigWindow.CURRENT_ENCRYPT);
		jComboBoxEncrypt.addActionListener(e -> saveComboBox(jComboBoxEncrypt, "CURRENT_ENCRYPT"));
		buttonPane0.add(jComboBoxEncrypt);
		buttonPane.add(buttonPane0, "First");
		JPanel buttonPane1 = new JPanel();
		JButton open = new JButton();
		open.setText("Open");
		open.addActionListener(this::openSelectFileWindow);
		buttonPane1.add(open);
		JButton save = new JButton();
		save.setText("Save txt");
		save.addActionListener(this::saveActionPerformed);
		buttonPane1.add(save);
		JButton saveAs = new JButton();
		saveAs.setText("Save dat");
		saveAs.addActionListener(this::saveAsActionPerformed);
		buttonPane1.add(saveAs);
		JButton massTxtUnpack = new JButton();
		massTxtUnpack.setText("Extract all to txt");
		massTxtUnpack.addActionListener(this::massTxtUnpackActionPerformed);
		buttonPane1.add(massTxtUnpack);
		JButton massTxtPack = new JButton();
		massTxtPack.setText("Download all dat");
		massTxtPack.addActionListener(this::massTxtPackActionPerformed);
		buttonPane1.add(massTxtPack);
		JButton massRecrypt = new JButton();
		massRecrypt.setText("Patch dat");
		massRecrypt.addActionListener(this::massRecryptActionPerformed);
		buttonPane1.add(massRecrypt);
		buttonPane.add(buttonPane1);
		JSplitPane jsp = new JSplitPane(0, false);
		jsp.setResizeWeight(0.7);
		jsp.setOneTouchExpandable(true);
		textPaneMain = new JPopupTextArea();
		textPaneMain.setBackground(new Color(41, 49, 52));
		textPaneMain.setForeground(Color.WHITE);
		textPaneMain.setFont(new Font("Verdana", 1, 12));
		JScrollPane jScrollPane1 = new JScrollPane();
		jScrollPane1.setAutoscrolls(true);
		jScrollPane1.setViewportView(textPaneMain);
		jsp.setTopComponent(jScrollPane1);
		textPaneLog = new JPopupTextArea();
		textPaneLog.setBackground(new Color(41, 49, 52));
		textPaneLog.setForeground(Color.GREEN);
		textPaneLog.setEditable(false);
		JScrollPane jScrollPane2 = new JScrollPane();
		jScrollPane2.setViewportView(textPaneLog);
		jScrollPane2.setAutoscrolls(true);
		jsp.setBottomComponent(jScrollPane2);
		getContentPane().add(buttonPane, "First");
		getContentPane().add(jsp);
		pack();
		setVisible(true);
	}
	
	private void massTxtPackActionPerformed(ActionEvent evt)
	{
		JFileChooser fileopen = new JFileChooser();
		fileopen.setFileSelectionMode(1);
		fileopen.setAcceptAllFileFilterUsed(false);
		fileopen.setCurrentDirectory(new File(ConfigWindow.FILE_OPEN_CURRENT_DIRECTORY_PACK));
		fileopen.setPreferredSize(new Dimension(600, 600));
		int ret = fileopen.showDialog(null, "Open");
		if (ret == 0)
		{
			currentFileWindow = fileopen.getSelectedFile();
			ConfigWindow.save("FILE_OPEN_CURRENT_DIRECTORY_PACK", currentFileWindow.getPath());
			addLogConsole("---------------------------------------", true);
			addLogConsole("selected folder: " + currentFileWindow.getPath(), true);
			try
			{
				MassTxtPacker.getInstance().pack(String.valueOf(jComboBoxChronicle.getSelectedItem()), currentFileWindow.getPath(), CryptVersionParser.getInstance().getEncryptKey(String.valueOf(jComboBoxEncrypt.getSelectedItem())));
			}
			catch (Exception ex)
			{
				Logger.getLogger(L2ClientDat.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	private void massTxtUnpackActionPerformed(ActionEvent evt)
	{
		JFileChooser fileopen = new JFileChooser();
		fileopen.setFileSelectionMode(1);
		fileopen.setAcceptAllFileFilterUsed(false);
		fileopen.setCurrentDirectory(new File(ConfigWindow.FILE_OPEN_CURRENT_DIRECTORY_UNPACK));
		fileopen.setPreferredSize(new Dimension(600, 600));
		int ret = fileopen.showDialog(null, "Open");
		if (ret == 0)
		{
			currentFileWindow = fileopen.getSelectedFile();
			ConfigWindow.save("FILE_OPEN_CURRENT_DIRECTORY_UNPACK", currentFileWindow.getPath());
			addLogConsole("---------------------------------------", true);
			addLogConsole("selected folder: " + currentFileWindow.getPath(), true);
			try
			{
				MassTxtUnpacker.getInstance().unpack(String.valueOf(jComboBoxChronicle.getSelectedItem()), currentFileWindow.getPath(), CryptVersionParser.getInstance().getDecryptKey(String.valueOf(jComboBoxDecrypt.getSelectedItem())));
			}
			catch (Exception ex)
			{
				Logger.getLogger(L2ClientDat.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	private void massRecryptActionPerformed(ActionEvent evt)
	{
		JFileChooser fileopen = new JFileChooser();
		fileopen.setFileSelectionMode(1);
		fileopen.setAcceptAllFileFilterUsed(false);
		fileopen.setCurrentDirectory(new File(ConfigWindow.FILE_OPEN_CURRENT_DIRECTORY));
		fileopen.setPreferredSize(new Dimension(600, 600));
		int ret = fileopen.showDialog(null, "Open");
		if (ret == 0)
		{
			currentFileWindow = fileopen.getSelectedFile();
			ConfigWindow.save("FILE_OPEN_CURRENT_DIRECTORY", currentFileWindow.getPath());
			addLogConsole("---------------------------------------", true);
			addLogConsole("selected folder: " + currentFileWindow.getPath(), true);
			try
			{
				MassRecryptor.getInstance().recrypt(String.valueOf(jComboBoxChronicle.getSelectedItem()), currentFileWindow.getPath(), CryptVersionParser.getInstance().getDecryptKey(String.valueOf(jComboBoxDecrypt.getSelectedItem())), CryptVersionParser.getInstance().getEncryptKey(String.valueOf(jComboBoxEncrypt.getSelectedItem())));
			}
			catch (Exception ex)
			{
				Logger.getLogger(L2ClientDat.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	private void openSelectFileWindow(ActionEvent evt)
	{
		textPaneMain.removeAll();
		JFileChooser fileopen = new JFileChooser();
		fileopen.setFileSelectionMode(0);
		fileopen.setAcceptAllFileFilterUsed(false);
		fileopen.setFileFilter(new FileNameExtensionFilter(".dat, .ini, .txt, .htm", "dat", "ini", "txt", "htm"));
		fileopen.setSelectedFile(new File(ConfigWindow.LAST_FILE_SELECTED));
		fileopen.setPreferredSize(new Dimension(600, 600));
		int ret = fileopen.showDialog(null, "Open");
		if (ret == 0)
		{
			currentFileWindow = fileopen.getSelectedFile();
			ConfigWindow.save("LAST_FILE_SELECTED", currentFileWindow.getAbsolutePath());
			addLogConsole("---------------------------------------", true);
			addLogConsole("Open file: " + currentFileWindow.getName(), true);
			try
			{
				OpenDat.start(String.valueOf(jComboBoxChronicle.getSelectedItem()), currentFileWindow, currentFileWindow.getName(), CryptVersionParser.getInstance().getDecryptKey(String.valueOf(jComboBoxDecrypt.getSelectedItem())));
			}
			catch (Exception ex)
			{
				Logger.getLogger(L2ClientDat.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	private void saveActionPerformed(ActionEvent evt)
	{
		JFileChooser fileSave = new JFileChooser();
		fileSave.setCurrentDirectory(new File(ConfigWindow.FILE_SAVE_CURRENT_DIRECTORY));
		if (currentFileWindow != null)
		{
			fileSave.setSelectedFile(new File(currentFileWindow.getName().split("\\.")[0] + ".txt"));
			fileSave.setFileFilter(new FileNameExtensionFilter(".txt", "txt"));
			fileSave.setAcceptAllFileFilterUsed(false);
			fileSave.setPreferredSize(new Dimension(600, 600));
			int ret = fileSave.showSaveDialog(null);
			if (ret == 0)
			{
				File f = fileSave.getSelectedFile();
				try
				{
					PrintWriter out = new PrintWriter(new FileOutputStream(f.getPath()), true);
					ConfigWindow.save("FILE_SAVE_CURRENT_DIRECTORY", f.getParentFile().toString());
					out.print(textPaneMain.getText());
					out.close();
				}
				catch (Exception out)
				{
					// empty catch block
				}
				addLogConsole("---------------------------------------", true);
				addLogConsole("Saved: " + f.getPath(), true);
			}
		}
		else
		{
			addLogConsole("No open file!", true);
		}
	}
	
	/*
	 * Enabled aggressive block sorting Enabled unnecessary exception pruning Enabled aggressive exception aggregation
	 */
	private void saveAsActionPerformed(ActionEvent evt)
	{
		if (currentFileWindow == null)
		{
			addLogConsole("Error saving dat. No file name.", true);
			return;
		}
		byte[] buff = null;
		DatCrypter crypter = null;
		if (currentFileWindow.getName().endsWith(".dat") || currentFileWindow.getName().endsWith(".txt"))
		{
			try
			{
				Descriptor desc = DescriptorParser.getInstance().findDescriptorForFile(String.valueOf(jComboBoxChronicle.getSelectedItem()), currentFileWindow.getName().replace("txt", "dat"));
				if (desc != null)
				{
					crypter = CryptVersionParser.getInstance().getEncryptKey(String.valueOf(jComboBoxEncrypt.getSelectedItem()));
					buff = DescriptorWriter.parseData(currentFileWindow, crypter, desc, textPaneMain.getText());
					GameDataName.getInstance().checkAndUpdate(currentFileWindow.getParent(), crypter);
				}
				addLogConsole("Not found the structure of the file: " + currentFileWindow.getName(), true);
			}
			catch (Exception e)
			{
				DebugUtil.getLogger().error(e.getMessage(), e);
				return;
			}
		}
		else if (currentFileWindow.getName().endsWith(".ini"))
		{
			crypter = CryptVersionParser.getInstance().getEncryptKey(String.valueOf(jComboBoxEncrypt.getSelectedItem()));
			buff = textPaneMain.getText().getBytes();
		}
		if (buff == null)
		{
			addLogConsole("buff == null.", true);
			return;
		}
		try
		{
			if (ConfigDebug.ENCRYPT && (crypter != null))
			{
				DatFile.encrypt(buff, currentFileWindow.getPath(), crypter);
			}
		}
		catch (Exception e)
		{
			DebugUtil.getLogger().error(e.getMessage(), e);
			return;
		}
		addLogConsole("Packed successfully.", true);
	}
	
	private void saveComboBox(JComboBox<?> jComboBox, String param)
	{
		ConfigWindow.save(param, String.valueOf(jComboBox.getSelectedItem()));
	}
	
	public static void main(String[] args)
	{
		ConfigWindow.load();
		ConfigDebug.load();
		CryptVersionParser.getInstance();
		Util.compileJavaClass("./structure/format/");
		DescriptorParser.getInstance();
		try
		{
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			{
				if (!"Nimbus".equals(info.getName()))
				{
					continue;
				}
				UIManager.setLookAndFeel(info.getClassName());
				break;
			}
		}
		catch (Exception ex)
		{
			Logger.getLogger(L2ClientDat.class.getName()).log(Level.SEVERE, null, ex);
		}
		EventQueue.invokeLater(L2ClientDat::new);
	}
	
	public static void addLogConsole(String log, boolean isLog)
	{
		if (isLog)
		{
			DebugUtil.getLogger().info(log);
		}
		textPaneLog.append(log + "\n");
	}
	
	public static void addText(String log)
	{
		textPaneMain.setText(log);
	}
}
