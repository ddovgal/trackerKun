package ua.ddovgal.trackerKunBot.service;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;

public class TelegramAppender extends AppenderSkeleton {

    private final String BOT_API_URL = "https://api.telegram.org/bot";

    private int receiverId;
    private String botToken;
    private String appName;

    private boolean isNotified;

    public TelegramAppender() {
    }

    public TelegramAppender(int receiverId, String botToken) {
        this.receiverId = receiverId;
        this.botToken = botToken;
    }

    public TelegramAppender(int receiverId, String botToken, String appName) {
        this.receiverId = receiverId;
        this.botToken = botToken;
        this.appName = appName;
    }

    @Override
    protected void append(LoggingEvent loggingEvent) throws RuntimeException {
        if ((receiverId != 0) && (botToken != null)) {
            final String urlString = BOT_API_URL + botToken + "/sendMessage";
            try {
                final URL url = new URL(urlString);
                final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                final DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
                final String message = (appName != null ? "[" + appName + "]:   " : "") + layout.format(loggingEvent);
                final String outString = "{" + "\"chat_id\":" + receiverId + "," + "\"text\":\"" + message + "\"}";
                writer.write(outString.getBytes("UTF-8"));
                writer.flush();
                writer.close();
                final int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    LogLog.warn("Response is not OK. Maybe, you didn't yet start conversation ?");
                }
            } catch (IOException e) {
                LogLog.error("Failed to send message.", e);
            }
        } else {
            if (!isNotified) {
                isNotified = true;
                LogLog.error("You did not define receiver's chat ID or sender bot token.");
            }
        }
    }

    public void close() {
    }

    public boolean requiresLayout() {
        return true;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
