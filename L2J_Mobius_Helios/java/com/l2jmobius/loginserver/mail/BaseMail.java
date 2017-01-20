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
package com.l2jmobius.loginserver.mail;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.l2jmobius.Config;
import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.loginserver.mail.MailSystem.MailContent;

/**
 * @author mrTJO
 */
public class BaseMail implements Runnable
{
	private static final Logger _log = Logger.getLogger(BaseMail.class.getName());
	
	private MimeMessage _messageMime = null;
	
	private class SmtpAuthenticator extends Authenticator
	{
		private final PasswordAuthentication _auth;
		
		public SmtpAuthenticator()
		{
			_auth = new PasswordAuthentication(Config.EMAIL_SYS_USERNAME, Config.EMAIL_SYS_PASSWORD);
		}
		
		@Override
		public PasswordAuthentication getPasswordAuthentication()
		{
			return _auth;
		}
	}
	
	public BaseMail(String account, String mailId, String... args)
	{
		final String mailAddr = getUserMail(account);
		
		if (mailAddr == null)
		{
			return;
		}
		
		final MailContent content = MailSystem.getInstance().getMailContent(mailId);
		if (content == null)
		{
			return;
		}
		
		final String message = compileHtml(account, content.getText(), args);
		
		final Properties mailProp = new Properties();
		mailProp.put("mail.smtp.host", Config.EMAIL_SYS_HOST);
		mailProp.put("mail.smtp.auth", Config.EMAIL_SYS_SMTP_AUTH);
		mailProp.put("mail.smtp.port", Config.EMAIL_SYS_PORT);
		mailProp.put("mail.smtp.socketFactory.port", Config.EMAIL_SYS_PORT);
		mailProp.put("mail.smtp.socketFactory.class", Config.EMAIL_SYS_FACTORY);
		mailProp.put("mail.smtp.socketFactory.fallback", Config.EMAIL_SYS_FACTORY_CALLBACK);
		final SmtpAuthenticator authenticator = (Config.EMAIL_SYS_SMTP_AUTH ? new SmtpAuthenticator() : null);
		
		final Session mailSession = Session.getDefaultInstance(mailProp, authenticator);
		
		try
		{
			_messageMime = new MimeMessage(mailSession);
			_messageMime.setSubject(content.getSubject());
			try
			{
				_messageMime.setFrom(new InternetAddress(Config.EMAIL_SYS_ADDRESS, Config.EMAIL_SERVERINFO_NAME));
			}
			catch (UnsupportedEncodingException e)
			{
				_log.warning("Sender Address not Valid!");
			}
			_messageMime.setContent(message, "text/html");
			_messageMime.setRecipient(Message.RecipientType.TO, new InternetAddress(mailAddr));
		}
		catch (MessagingException e)
		{
			_log.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
	
	private String compileHtml(String account, String html, String[] args)
	{
		if (args != null)
		{
			for (int i = 0; i < args.length; i++)
			{
				html = html.replace("%var" + i + "%", args[i]);
			}
		}
		html = html.replace("%accountname%", account);
		return html;
	}
	
	private String getUserMail(String username)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(Config.EMAIL_SYS_SELECTQUERY))
		{
			statement.setString(1, username);
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					return rset.getString(Config.EMAIL_SYS_DBFIELD);
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("Cannot select user mail: Exception");
		}
		return null;
	}
	
	@Override
	public void run()
	{
		try
		{
			if (_messageMime != null)
			{
				Transport.send(_messageMime);
			}
		}
		catch (MessagingException e)
		{
			_log.warning("Error encounterd while sending email");
		}
	}
}
