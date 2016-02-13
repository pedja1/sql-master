package com.afstd.sqlitecommander.app.gcm;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by pedja on 27.10.15. 14.38.
 * This class is part of the Politika
 * Copyright © 2015 ${OWNER}
 */
public class MyGcmListenerService extends GcmListenerService
{
    public static final int NOTIFICATION_ID = 234567123;
    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        System.out.println();
        /*String message = data.getString("message");
        JSONParser parser = new JSONParser(message);
        parser.parsePushNotification();
        if(parser.getParseObject() != null)
        {
            postNotification();
        }*/
    }

    private void postNotification()
    {
        /*List<News> news = App.get().getDaoSession().getNewsDao().queryRaw("INNER JOIN NOTIFICATION_NEWS ON NOTIFICATION_NEWS.news_id = T._id");
        if(news.isEmpty())return;

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Class<? extends Activity> activity = news.size() > 1 ? MainActivity.class : NewsActivity.class;
        Intent dataIntent = new Intent(this, activity);
        if(news.size() > 1)
        {
            dataIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        else
        {
            dataIntent.putExtra(NewsActivity.INTENT_EXTRA_NEWS_ID, news.get(0).getNews_id());
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(activity);
        stackBuilder.addNextIntent(dataIntent);

        PendingIntent contentIntent = stackBuilder.getPendingIntent(234567123, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        mBuilder.setSmallIcon(R.mipmap.ic_launcher);

        String title = generateNotificationTitle(news);
        String text = generateNotificationText(news);

        mBuilder.setContentTitle(title);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));

        mBuilder.setNumber(news.size());
        mBuilder.setContentText(text);
        mBuilder.setAutoCancel(true);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND|Notification.FLAG_ONLY_ALERT_ONCE);
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);

        mBuilder.setContentIntent(contentIntent);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());*/
    }

    /*private String generateNotificationTitle(List<News> newNews)
    {
        if(newNews.size() == 1)
        {
            return newNews.get(0).getTitleForLanguage();
        }
        else
        {
            return getString(R.string.app_name);
        }
    }

    private String generateNotificationText(List<News> newNews)
    {
        //TODO [server, sta je ovo?]iskoristi naslove kao na gmail app
        if(newNews.size() == 1)
        {
            return Html.fromHtml(newNews.get(0).getShortDescForLanguage()).toString();
        }
        else
        {
            return getString(R.string.new_news_notification_text, newNews.size(), getCountDescriptorForCount(newNews.size()));
        }
    }

    private String getCountDescriptorForCount(int size)
    {
        int lastDigit = size % 10;
        int[] fileStrings = new int[]{R.string.nova, R.string.nove, R.string.nove, R.string.nove, R.string.novih,
                R.string.novih, R.string.novih, R.string.novih, R.string.novih, R.string.novih,
                R.string.novih};
        return getString(fileStrings[lastDigit]);
    }*/
}
