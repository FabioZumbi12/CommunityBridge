package org.ruhlendavis.mc.communitybridge;

import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import net.netmanagers.api.SQL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.ruhlendavis.mc.utility.Log;

/**
 * Class representing the interface to the web application.
 *
 * @author Feaelin (Iain E. Davis) <iain@ruhlendavis.org>
 */
public class WebApplication
{
	private CommunityBridge plugin;
	private Configuration config;
	private Log log;
	private SQL sql;

	private Map<String, String> playerUserIDs = new HashMap();

	public WebApplication(CommunityBridge plugin, Configuration config, Log log, SQL sql)
	{
		this.config = config;
		this.plugin = plugin;
		this.log = log;
		setSQL(sql);
	}

	/**
	 * Returns a given player's web application user ID.
	 *
	 * @param String containing the player's name.
	 * @return String containing the player's  web application user ID.
	 */
	public String getUserID(String playerName)
	{
		return playerUserIDs.get(playerName);
	}

	/**
	 * Returns true if the user's avatar column contains data.
	 *
	 * @param String The player's name.
	 * @return boolean True if the user has an avatar.
	 */
	public boolean playerHasAvatar(String playerName)
	{
		final String errorBase = "Error during WebApplication.playerHasAvatar(): ";
		String query;

		query = "SELECT `" + config.requireAvatarTableName + "`.`" + config.requireAvatarAvatarColumn + "` "
					+ "FROM `" + config.requireAvatarTableName + "` "
					+ "WHERE `" + config.requireAvatarUserIDColumn + "` = '" + getUserID(playerName) + "'";

		log.finest(query);

		try
		{
			String avatar = null;
			ResultSet result = sql.sqlQuery(query);

			if (result.next())
			{
				avatar = result.getString(config.requireAvatarAvatarColumn);
			}

			if (avatar == null || avatar.isEmpty())
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		catch (SQLException error)
		{
			log.severe(errorBase + error.getMessage());
			return false;
		}
		catch (MalformedURLException error)
		{
			log.severe(errorBase + error.getMessage());
			return false;
		}
		catch (InstantiationException error)
		{
			log.severe(errorBase + error.getMessage());
			return false;
		}
		catch (IllegalAccessException error)
		{
			log.severe(errorBase + error.getMessage());
			return false;
		}
	}

	/**
	 * Fetches the user's post count from the web application.
	 *
	 * @param String The player's name.
	 * @return int Number of posts.
	 */
	public int getUserPostCount(String playerName)
	{
		final String errorBase = "Error during WebApplication.playerHasAvatar(): ";
		String query;

		query = "SELECT `" + config.requirePostsTableName + "`.`" + config.requirePostsPostCountColumn + "` "
					+ "FROM `" + config.requirePostsTableName + "` "
					+ "WHERE `" + config.requirePostsUserIDColumn + "` = '" + getUserID(playerName) + "'";

		log.finest(query);

		try
		{
			ResultSet result = sql.sqlQuery(query);

			if (result.next())
			{
				return result.getInt(config.requirePostsPostCountColumn);
			}
			else
			{
				return 0;
			}
		}
		catch (SQLException error)
		{
			log.severe(errorBase + error.getMessage());
			return 0;
		}
		catch (MalformedURLException error)
		{
			log.severe(errorBase + error.getMessage());
			return 0;
		}
		catch (InstantiationException error)
		{
			log.severe(errorBase + error.getMessage());
			return 0;
		}
		catch (IllegalAccessException error)
		{
			log.severe(errorBase + error.getMessage());
			return 0;
		}
	}

	/**
	 * Returns a given player's web application user ID.
	 *
	 * @param String containing the player's name.
	 * @return String containing the player's  web application user ID.
	 */
	public int getUserIDint(String playerName)
	{
		if (playerUserIDs.get(playerName) == null)
		{
			return 0;
		}
		return Integer.parseInt(playerUserIDs.get(playerName));
	}

	/**
	 * Returns true if the player is registered on the web application.
	 * @param String The name of the player.
	 * @return boolean True if the player is registered.
	 */
	public boolean isPlayerRegistered(String playerName)
	{
		return !(getUserID(playerName) == null || getUserID(playerName).isEmpty());
	}

	/**
	 * Retrieves user IDs for all connected players, usually only necessary
	 * after a reload.
	 */
	public void loadOnlineUserIDsFromDatabase()
	{
		Player [] players =	Bukkit.getOnlinePlayers();

		for (Player player : players)
		{
			loadUserIDfromDatabase(player.getName());
		}
	}

	/**
	 * Performs the database query that should be done when a player connects.
	 *
	 * @param String containing the player's name.
	 */
	public void loadUserIDfromDatabase(String playerName)
	{
		final String errorBase = "Error during WebApplication.onPreLogin(): ";
		String query = "SELECT `" + config.linkingTableName + "`.`" + config.linkingUserIDColumn + "` "
								 + "FROM `" + config.linkingTableName + "`";

		if (config.linkingUsesKey)
		{
			query = query
						+ "WHERE `" + config.linkingKeyColumn + "` = '" + config.linkingKeyName + "' "
						+ "AND `" + config.linkingValueColumn + "` = '" + playerName + "' ";
		}
		else
		{
			query = query	+ "WHERE LOWER(`" + config.linkingPlayerNameColumn + "`) = LOWER('" + playerName + "') ";
		}
		query = query + "ORDER BY `" + config.linkingUserIDColumn + "` DESC";

		log.finest(query);

		try
		{
			String userID = null;
			ResultSet result = sql.sqlQuery(query);

			if (result.next())
			{
				userID = result.getString(config.linkingUserIDColumn);
			}
			if (userID == null)
			{
				log.finest("User ID for " + playerName + " not found.");
			}
			else
			{
				log.finest("User ID '" + userID + "' associated with " + playerName + ".");
				playerUserIDs.put(playerName, userID);
			}
		}
		catch (SQLException error)
		{
			log.severe(errorBase + error.getMessage());
		}
		catch (MalformedURLException error)
		{
			log.severe(errorBase + error.getMessage());
		}
		catch (InstantiationException error)
		{
			log.severe(errorBase + error.getMessage());
		}
		catch (IllegalAccessException error)
		{
			log.severe(errorBase + error.getMessage());
		}
	} // loadUserIDfromDatabase()

	/**
	 * Performs operations when a player joins
	 *
	 * @param String The player who joined.
	 */
	public void onJoin(final String playerName)
	{
		runUpdateStatisticsTask(playerName);
	}

	/**
	 * Performs operations when a player quits.
	 *
	 * @param String containing the player's name.
	 */
	public void onQuit(final String playerName)
	{
		// Only keep user IDs for connected players on hand.
		playerUserIDs.remove(playerName);
		runUpdateStatisticsTask(playerName);
	}

	/**
	 * If statistics is enabled, this method sets up an update statistics task
	 * for the given player.
	 *
	 * @param String The player's name.
	 */
	private void runUpdateStatisticsTask(final String playerName)
	{
		if (config.statisticsEnabled)
		{
			Bukkit.getScheduler().runTaskAsynchronously(plugin,	new Runnable()
			{
				@Override
				public void run()
				{
					updateStatistics(playerName);
				}
			});
		}
	}

	/**
	 * Sets the SQL object. Typically used during a reload.
	 *
	 * @param SQL SQL object to set.
	 */
	public final void setSQL(SQL sql)
	{
		this.sql = sql;
	}

	private void updateStatistics(String playerName)
	{

	}

} // WebApplication class