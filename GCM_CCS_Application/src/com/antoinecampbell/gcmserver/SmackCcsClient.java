package com.antoinecampbell.gcmserver;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.xmlpull.v1.XmlPullParser;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;

/**
 * Sample Smack implementation of a client for GCM Cloud Connection Server.
 * 
 * <p>
 * For illustration purposes only.
 */
public class SmackCcsClient
{

    Logger logger = Logger.getLogger("SmackCcsClient");

    public static final String GCM_SERVER = "gcm.googleapis.com";
    public static final int GCM_PORT = 5235;
    public static final String GCM_SENDER_ID = "741497573968";
    public static final String GCM_SERVER_KEY = "AIzaSyAz7WWXqpwtPwMdbxSMywWnciOfvfMFVgk";
    public static final long GCM_TIME_TO_LIVE = 60L * 60L * 24L * 7L * 4L; // 4 Weeks

    public static final String GCM_ELEMENT_NAME = "gcm";
    public static final String GCM_NAMESPACE = "google:mobile:data";

    static Random random = new Random();
    XMPPConnection connection;
    ConnectionConfiguration config;

    private static Dao<User, String> userDao;
    private static Dao<NotificationKey, String> notificationDao;

    /**
     * XMPP Packet Extension for GCM Cloud Connection Server.
     */
    class GcmPacketExtension extends DefaultPacketExtension
    {
	String json;

	public GcmPacketExtension(String json)
	{
	    super(GCM_ELEMENT_NAME, GCM_NAMESPACE);
	    this.json = json;
	}

	public String getJson()
	{
	    return json;
	}

	@Override
	public String toXML()
	{
	    return String.format("<%s xmlns=\"%s\">%s</%s>", GCM_ELEMENT_NAME, GCM_NAMESPACE, json,
		    GCM_ELEMENT_NAME);
	}

	public Packet toPacket()
	{
	    return new Message()
	    {
		// Must override toXML() because it includes a <body>
		@Override
		public String toXML()
		{

		    StringBuilder buf = new StringBuilder();
		    buf.append("<message");
		    if (getXmlns() != null)
		    {
			buf.append(" xmlns=\"").append(getXmlns()).append("\"");
		    }
		    if (getLanguage() != null)
		    {
			buf.append(" xml:lang=\"").append(getLanguage()).append("\"");
		    }
		    if (getPacketID() != null)
		    {
			buf.append(" id=\"").append(getPacketID()).append("\"");
		    }
		    if (getTo() != null)
		    {
			buf.append(" to=\"").append(StringUtils.escapeForXML(getTo())).append("\"");
		    }
		    if (getFrom() != null)
		    {
			buf.append(" from=\"").append(StringUtils.escapeForXML(getFrom())).append("\"");
		    }
		    buf.append(">");
		    buf.append(GcmPacketExtension.this.toXML());
		    buf.append("</message>");
		    return buf.toString();
		}
	    };
	}
    }

    public SmackCcsClient()
    {
	// Add GcmPacketExtension
	ProviderManager.getInstance().addExtensionProvider(GCM_ELEMENT_NAME, GCM_NAMESPACE,
		new PacketExtensionProvider()
		{

		    @Override
		    public PacketExtension parseExtension(XmlPullParser parser) throws Exception
		    {
			String json = parser.nextText();
			GcmPacketExtension packet = new GcmPacketExtension(json);
			return packet;
		    }
		});
    }

    /**
     * Returns a random message id to uniquely identify a message.
     * 
     * <p>
     * Note: This is generated by a pseudo random number generator for
     * illustration purpose, and is not guaranteed to be unique.
     * 
     */
    public String getRandomMessageId()
    {
	return "m-" + Long.toString(random.nextLong());
    }

    /**
     * Sends a downstream GCM message.
     */
    public void send(String jsonRequest)
    {
	Packet request = new GcmPacketExtension(jsonRequest).toPacket();
	connection.sendPacket(request);
    }

    /**
     * Handles an upstream data message from a device application.
     * 
     * <p>
     * This sample echo server sends an echo message back to the device.
     * Subclasses should override this method to process an upstream message.
     */
    public void handleIncomingDataMessage(Map<String, Object> jsonObject)
    {
	@SuppressWarnings("unchecked")
	Map<String, String> payload = (Map<String, String>) jsonObject.get("data");
	
	String from = jsonObject.get("from").toString();
	// PackageName of the application that sent this message.
	String category = jsonObject.get("category").toString();
	logger.log(Level.INFO, "Application: " + category);
	
	String action = payload.get("action");
	if(action.equalsIgnoreCase("com.antoinecampbell.gcmdemo.REGISTER"))
	{
	    String name = payload.get("name").toString();
	    // Store username and registrationID in DB
	    addUser(name, from);
	    // Send an REGISTER response back
	    payload.put("message", "Registration successful");
	    String echo = createJsonMessage(from, getRandomMessageId(), payload, null, null, false);
	    send(echo);
	    logger.info("Adding new user: " + name + ":" + from);
	}
	else if(action.equalsIgnoreCase("com.antoinecampbell.gcmdemo.UNREGISTER"))
	{
	    removeUser(from);
	    logger.info("Removing ID: " + from);
	}
	else if(action.equalsIgnoreCase("com.antoinecampbell.gcmdemo.ECHO"))
	{
	    // Send an ECHO response back
	    String echo = createJsonMessage(from, getRandomMessageId(), payload, null, null, false);
	    send(echo);
	}
	else if(action.equalsIgnoreCase("com.antoinecampbell.gcmdemo.BROADCAST"))
	{
	    // Send an Broadcast response back
	    broadcastMesage(payload);
	}
	else if(action.equalsIgnoreCase("com.antoinecampbell.gcmdemo.NOTIFICATION"))
	{
	    // Send a Notification response back
	    try
	    {
		User user = userDao.queryForId(from);
		NotificationKey notificationKey = notificationDao.queryForId(user.getName());
		notificationMessage(notificationKey, payload);
	    }
	    catch (SQLException e)
	    {
		e.printStackTrace();
	    }
	}
	else 
	{
	    logger.warning("Unkown action sent: " + action);
	}
    }

    /**
     * Handles an ACK.
     * 
     * <p>
     * By default, it only logs a INFO message, but subclasses could override it
     * to properly handle ACKS.
     */
    public void handleAckReceipt(Map<String, Object> jsonObject)
    {
	String messageId = jsonObject.get("message_id").toString();
	String from = jsonObject.get("from").toString();
	logger.log(Level.INFO, "handleAckReceipt() from: " + from + ", messageId: " + messageId);
    }

    /**
     * Handles a NACK.
     * 
     * <p>
     * By default, it only logs a INFO message, but subclasses could override it
     * to properly handle NACKS.
     */
    public void handleNackReceipt(Map<String, Object> jsonObject)
    {
	String messageId = jsonObject.get("message_id").toString();
	String from = jsonObject.get("from").toString();
	logger.log(Level.INFO, "handleNackReceipt() from: " + from + ", messageId: " + messageId);
    }

    /**
     * Creates a JSON encoded GCM message.
     * 
     * @param to
     *            RegistrationId of the target device (Required).
     * @param messageId
     *            Unique messageId for which CCS will send an "ack/nack"
     *            (Required).
     * @param payload
     *            Message content intended for the application. (Optional).
     * @param collapseKey
     *            GCM collapse_key parameter (Optional).
     * @param timeToLive
     *            GCM time_to_live parameter (Optional).
     * @param delayWhileIdle
     *            GCM delay_while_idle parameter (Optional).
     * @return JSON encoded GCM message.
     */
    public static String createJsonMessage(String to, String messageId, Map<String, String> payload,
	    String collapseKey, Long timeToLive, Boolean delayWhileIdle)
    {
	Map<String, Object> message = new HashMap<String, Object>();
	message.put("to", to);
	if (collapseKey != null)
	{
	    message.put("collapse_key", collapseKey);
	}
	if (timeToLive != null)
	{
	    message.put("time_to_live", timeToLive);
	}
	if (delayWhileIdle != null && delayWhileIdle)
	{
	    message.put("delay_while_idle", true);
	}
	message.put("message_id", messageId);
	message.put("data", payload);
	return JSONValue.toJSONString(message);
    }

    /**
     * Creates a JSON encoded ACK message for an upstream message received from
     * an application.
     * 
     * @param to
     *            RegistrationId of the device who sent the upstream message.
     * @param messageId
     *            messageId of the upstream message to be acknowledged to CCS.
     * @return JSON encoded ack.
     */
    public static String createJsonAck(String to, String messageId)
    {
	Map<String, Object> message = new HashMap<String, Object>();
	message.put("message_type", "ack");
	message.put("to", to);
	message.put("message_id", messageId);
	return JSONValue.toJSONString(message);
    }

    /**
     * Connects to GCM Cloud Connection Server using the supplied credentials.
     * 
     * @param username
     *            GCM_SENDER_ID@gcm.googleapis.com
     * @param password
     *            API Key
     * @throws XMPPException
     */
    public void connect(String username, String password) throws XMPPException
    {
	config = new ConnectionConfiguration(GCM_SERVER, GCM_PORT);
	config.setSecurityMode(SecurityMode.enabled);
	config.setReconnectionAllowed(true);
	config.setRosterLoadedAtLogin(false);
	config.setSendPresence(false);
	config.setSocketFactory(SSLSocketFactory.getDefault());

	// NOTE: Set to true to launch a window with information about packets
	// sent and received
	config.setDebuggerEnabled(true);

	// -Dsmack.debugEnabled=true
	XMPPConnection.DEBUG_ENABLED = true;

	connection = new XMPPConnection(config);
	connection.connect();

	connection.addConnectionListener(new ConnectionListener()
	{

	    @Override
	    public void reconnectionSuccessful()
	    {
		logger.info("Reconnecting..");
	    }

	    @Override
	    public void reconnectionFailed(Exception e)
	    {
		logger.log(Level.INFO, "Reconnection failed.. ", e);
	    }

	    @Override
	    public void reconnectingIn(int seconds)
	    {
		logger.log(Level.INFO, "Reconnecting in %d secs", seconds);
	    }

	    @Override
	    public void connectionClosedOnError(Exception e)
	    {
		logger.log(Level.INFO, "Connection closed on error.");
	    }

	    @Override
	    public void connectionClosed()
	    {
		logger.info("Connection closed.");
	    }
	});

	// Handle incoming packets
	connection.addPacketListener(new PacketListener()
	{

	    @Override
	    public void processPacket(Packet packet)
	    {
		logger.log(Level.INFO, "Received: " + packet.toXML());
		Message incomingMessage = (Message) packet;
		GcmPacketExtension gcmPacket = (GcmPacketExtension) incomingMessage
			.getExtension(GCM_NAMESPACE);
		String json = gcmPacket.getJson();
		try
		{
		    @SuppressWarnings("unchecked")
		    Map<String, Object> jsonObject = (Map<String, Object>) JSONValue.parseWithException(json);

		    // present for "ack"/"nack", null otherwise
		    Object messageType = jsonObject.get("message_type");

		    if (messageType == null)
		    {
			// Normal upstream data message
			handleIncomingDataMessage(jsonObject);

			// Send ACK to CCS
			String messageId = jsonObject.get("message_id").toString();
			String from = jsonObject.get("from").toString();
			String ack = createJsonAck(from, messageId);
			send(ack);
		    }
		    else if ("ack".equals(messageType.toString()))
		    {
			// Process Ack
			handleAckReceipt(jsonObject);
		    }
		    else if ("nack".equals(messageType.toString()))
		    {
			// Process Nack
			handleNackReceipt(jsonObject);
		    }
		    else
		    {
			logger.log(Level.WARNING, "Unrecognized message type (%s)", messageType.toString());
		    }
		}
		catch (ParseException e)
		{
		    logger.log(Level.SEVERE, "Error parsing JSON " + json, e);
		}
		catch (Exception e)
		{
		    logger.log(Level.SEVERE, "Couldn't send echo.", e);
		}
	    }
	}, new PacketTypeFilter(Message.class));

	// Log all outgoing packets
	connection.addPacketInterceptor(new PacketInterceptor()
	{
	    @Override
	    public void interceptPacket(Packet packet)
	    {
		logger.log(Level.INFO, "Sent: {0}", packet.toXML());
	    }
	}, new PacketTypeFilter(Message.class));

	connection.login(username, password);
    }

    /**
     * 
     * @param name
     * @param gcm_id
     */
    public void addUser(String name, String gcm_id)
    {
	try
	{
	    final User user = userDao.createIfNotExists(new User(name, gcm_id));
	    addUserNotificationKey(user);
	}
	catch (SQLException e)
	{
	    e.printStackTrace();
	}
    }
    
    /**
     * 
     * @param gcm_id
     */
    public void removeUser(String gcm_id)
    {
	try
	{
	    User user = userDao.queryForId(gcm_id);
	    userDao.deleteById(gcm_id);
	    removeUserNotification(user);
	    
	    // If all user's have been removed from a notification key remove the key
	   List<User> users =  userDao.queryForEq("name", user.getName());
	   if(users.size() == 0)
	   {
	       notificationDao.deleteById(user.getName());
	   }
	}
	catch (SQLException e)
	{
	    e.printStackTrace();
	}
    }
    
    /**
     * Send GCM message to all registered devices
     * @param payload
     * 		message data
     */
    public void broadcastMesage(Map<String, String> payload)
    {
	try
	{
	    List<User> users =  userDao.queryForAll();
	    for(User user : users)
	    {
		String broadcast = createJsonMessage(
			user.getGcm_id(), getRandomMessageId(), payload, null, null, false);
		send(broadcast);
	    }
	}
	catch (SQLException e)
	{
	    e.printStackTrace();
	}
    }
    
    /**
     * Send GCM message to all devices with specified notification key
     * @param notificationKey
     * 		user's notification key
     * @param payload
     * 		message data
     */
    public void notificationMessage(NotificationKey notificationKey, Map<String, String> payload)
    {
	payload.put("notification_key", notificationKey.getNotificaciton_key());
	send(createJsonMessage(
		notificationKey.getNotificaciton_key(), getRandomMessageId(), payload, null, GCM_TIME_TO_LIVE, false));
    }
    
    /**
     * Send GCM message to all devices with specified notification key 
     * that a new device was added
     * @param notificationKey
     */
    public void notificationRegisteredMessage(NotificationKey notificationKey)
    {
	Map<String, String> payload = new HashMap<String, String>();
	payload.put("notification_key", notificationKey.getNotificaciton_key());
	payload.put("action", "com.antoinecampbell.gcmdemo.NOTIFICATION");
	payload.put("message", "New Device Registered");
	send(createJsonMessage(
		notificationKey.getNotificaciton_key(), getRandomMessageId(), payload, null, GCM_TIME_TO_LIVE, false));
    }
    
    /**
     * Remove a user from the notification key
     * @param user
     * 		the user to be removed
     */
    public void removeUserNotification(User user)
    {
	try
	{
	    // Create GCM notification headers
	    CloseableHttpClient client = HttpClients.createDefault();
	    HttpPost post = new HttpPost("https://android.googleapis.com/gcm/notification");
	    post.addHeader("Content-Type", "application/json");
	    post.addHeader("project_id", GCM_SENDER_ID);
	    post.addHeader("Authorization", "key=" + GCM_SERVER_KEY);
	    
	    // Add device to be removed to the request
	    Map<String, Object> body = new HashMap<String, Object>();
	    List<NotificationKey> notificationKeys = notificationDao.queryForEq("name", user.getName());
	    if(notificationKeys.size() > 0)
	    {
		body.put("operation", "remove");
		body.put("notification_key", notificationKeys.get(0).getNotificaciton_key());
	    }
	    else 
	    {
		return;
	    }
	    body.put("notification_key_name", user.getName());
	    body.put("registration_ids", Arrays.asList(user.getGcm_id()));
	    
	    String temp = JSONObject.toJSONString(body);
	    StringEntity stringEntity = new StringEntity(temp, "UTF-8");
	    stringEntity.setContentType("application/json;charset=UTF-8");
	    stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
	    post.setEntity(stringEntity);
	    
	    try
	    {
		CloseableHttpResponse response = client.execute(post);
		String responseString = EntityUtils.toString(response.getEntity());
		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
		{
		    logger.info("Notification Key Response: " + responseString);
		    JSONObject key = (JSONObject) JSONValue.parse(responseString);
		    if (key.containsKey("notification_key"))
		    {
			logger.info("User removed from Notification Key: " + key.get("notification_key").toString());
		    }
		}
		else 
		{
		    logger.warning("Error with Notification key: " + responseString);
		}
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
	    }
	    finally
	    {
		client.close();
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }
    
    /**
     * Add a user to a notificaiton key
     * @param newUser
     * 		the user to be added
     */
    public void addUserNotificationKey(User newUser)
    {
	try
	{
	    // Create GCM notification headers
	    CloseableHttpClient client = HttpClients.createDefault();
	    HttpPost post = new HttpPost("https://android.googleapis.com/gcm/notification");
	    post.addHeader("Content-Type", "application/json");
	    post.addHeader("project_id", GCM_SENDER_ID);
	    post.addHeader("Authorization", "key=" + GCM_SERVER_KEY);
	    
	    // Create new notification key one does not exist, add otherwise
	    Map<String, Object> body = new HashMap<String, Object>();
	    List<NotificationKey> notificationKeys = notificationDao.queryForEq("name", newUser.getName());
	    if(notificationKeys.size() > 0)
	    {
		body.put("operation", "add");
		body.put("notification_key", notificationKeys.get(0).getNotificaciton_key());
	    }
	    else 
	    {
		body.put("operation", "create");
	    }
	    body.put("notification_key_name", newUser.getName());
	    
	    // Add all a user's devices to the request
	    List<User> users = userDao.queryForEq("name", newUser.getName());
	    List<String> ids = new ArrayList<String>();
	    for(User user : users)
	    {
		ids.add(user.getGcm_id());
	    }
	    body.put("registration_ids", ids);
	    
	    String temp = JSONObject.toJSONString(body);
	    StringEntity stringEntity = new StringEntity(temp, "UTF-8");
	    stringEntity.setContentType("application/json;charset=UTF-8");
	    stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
	    
	    post.setEntity(stringEntity);
	    
	    try
	    {
		CloseableHttpResponse response = client.execute(post);
		String responseString = EntityUtils.toString(response.getEntity());
		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
		{
		    logger.info("Notification Key Response: " + responseString);
		    JSONObject key = (JSONObject) JSONValue.parse(responseString);
		    if (key.containsKey("notification_key"))
		    {
			NotificationKey notificationKey = 
				new NotificationKey(newUser.getName(), key.get("notification_key").toString());
			notificationDao.createIfNotExists(notificationKey);
			notificationRegisteredMessage(notificationKey);
		    }
		}
		else 
		{
		    logger.warning("Error with Notification key: " + responseString);
		}
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
	    }
	    finally
	    {
		client.close();
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }
    
    public static void main(String[] args)
    {
	final String userName = GCM_SENDER_ID + "@gcm.googleapis.com";
	final String password = GCM_SERVER_KEY;

	SmackCcsClient ccsClient = new SmackCcsClient();
        String databaseURL = "jdbc:sqlite:gcm.db";
	try
	{
	    // Setup CCS client and create local SQLite database and tables if needed
	    ccsClient.connect(userName, password);
	    ConnectionSource connectionSource = new JdbcConnectionSource(databaseURL);
	    userDao = DaoManager.createDao(connectionSource, User.class);
	    TableUtils.createTableIfNotExists(connectionSource, User.class);
	    notificationDao = DaoManager.createDao(connectionSource, NotificationKey.class);
	    TableUtils.createTableIfNotExists(connectionSource, NotificationKey.class);
	}
	catch (XMPPException e)
	{
	    e.printStackTrace();
	}
	catch (SQLException e)
	{
	    e.printStackTrace();
	}
    }

}