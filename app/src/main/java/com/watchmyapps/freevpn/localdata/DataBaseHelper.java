package com.watchmyapps.freevpn.localdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.watchmyapps.freevpn.localdata.model.Server;
import com.watchmyapps.freevpn.utils.ConnectionQuality;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by saugat on 29.09.2018.
 */

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "database.db";
    private static final String TBL_SERVERS = "servers_table";
    private static final String TAG = "DataBaseHelper";

    private static final String CODE_ID = "_id";
    private static final String CODE_HOST_NAME = "hostName";
    private static final String CODE_IP = "ip";
    private static final String CODE_SCORE = "score";
    private static final String CODE_PING = "ping";
    private static final String CODE_SPEED = "speed";
    private static final String CODE_COUNTRY_LONG = "countryLong";
    private static final String CODE_COUNTRY_SHORT = "countryShort";
    private static final String CODE_NUM_VPN_SESSIONS = "numVpnSessions";
    private static final String CODE_UPTIME = "uptime";
    private static final String CODE_TOTAL_USERS = "totalUsers";
    private static final String CODE_TOTAL_TRAFFIC = "totalTraffic";
    private static final String CODE_LOG_TYPE = "logType";
    private static final String CODE_OPERATOR = "operator";
    private static final String CODE_MESSAGE = "message";
    private static final String CODE_CONFIG_DATA = "configData";
    private static final String CODE_TYPE = "type";
    private static final String CODE_QUALITY = "quality";
    private static final String CODE_CITY = "city";
    private static final String CODE_REGION_NAME = "regionName";
    private static final String CODE_LAT = "lat";
    private static final String CODE_LON = "lon";


    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db, TBL_SERVERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TBL_SERVERS);
        onCreate(db);
    }

    private void createTable(SQLiteDatabase db, String name) {
        db.execSQL("create table " + name + "("
                + CODE_ID + " integer primary key,"
                + CODE_HOST_NAME + " text,"
                + CODE_IP + " text,"
                + CODE_SCORE + " text,"
                + CODE_PING + " text,"
                + CODE_SPEED + " text,"
                + CODE_COUNTRY_LONG + " text,"
                + CODE_COUNTRY_SHORT + " text,"
                + CODE_NUM_VPN_SESSIONS + " text,"
                + CODE_UPTIME + " text,"
                + CODE_TOTAL_USERS + " text,"
                + CODE_TOTAL_TRAFFIC + " text,"
                + CODE_LOG_TYPE + " text,"
                + CODE_OPERATOR + " text,"
                + CODE_MESSAGE + " text,"
                + CODE_CONFIG_DATA + " text,"
                + CODE_QUALITY + " integer,"
                + CODE_CITY + " text,"
                + CODE_TYPE + " integer,"
                + CODE_REGION_NAME + " text,"
                + CODE_LAT + " real,"
                + CODE_LON + " real,"
                + "UNIQUE ("
                + CODE_HOST_NAME
                + ") ON CONFLICT IGNORE"
                + ")");
    }

    public void setInactive(String ip) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CODE_QUALITY, 0);
        db.update(TBL_SERVERS, values, CODE_IP + " = ?", new String[] {ip});

        db.close();
    }

    public boolean setIpInfo(JSONArray response, List<Server> serverList) {
        boolean result = false;
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject ipInfo = new JSONObject(response.get(i).toString());
                String city = ipInfo.get(CODE_CITY).toString();

                ContentValues values = new ContentValues();
                values.put(CODE_CITY, city);
                values.put(CODE_REGION_NAME, ipInfo.get(CODE_REGION_NAME).toString());
                values.put(CODE_LAT, ipInfo.getDouble(CODE_LAT));
                values.put(CODE_LON, ipInfo.getDouble(CODE_LON));
                db.update(TBL_SERVERS, values, CODE_IP + " = ?", new String[] {ipInfo.get("query").toString()});

                serverList.get(i).setCity(city);
                result = true;
            } catch (JSONException e) {
                result = false;
                e.printStackTrace();
            }
        }
        db.close();

        return result;
    }

    public void clearTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TBL_SERVERS, null, null);
        db.close();
    }

    public void putLine(String line, int type) {
        String[] data = line.split(",");
        if (data.length == 15) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            contentValues.put(CODE_HOST_NAME, data[0]);
            contentValues.put(CODE_IP, data[1]);
            contentValues.put(CODE_SCORE, data[2]);
            contentValues.put(CODE_PING, data[3]);
            contentValues.put(CODE_SPEED, data[4]);
            contentValues.put(CODE_COUNTRY_LONG, data[5]);
            contentValues.put(CODE_COUNTRY_SHORT, data[6]);
            contentValues.put(CODE_NUM_VPN_SESSIONS, data[7]);
            contentValues.put(CODE_UPTIME, data[8]);
            contentValues.put(CODE_TOTAL_USERS, data[9]);
            contentValues.put(CODE_TOTAL_TRAFFIC, data[10]);
            contentValues.put(CODE_LOG_TYPE, data[11]);
            contentValues.put(CODE_OPERATOR, data[12]);
            contentValues.put(CODE_MESSAGE, data[13]);
            contentValues.put(CODE_CONFIG_DATA, data[14]);
            contentValues.put(CODE_TYPE, type);

            contentValues.put(CODE_QUALITY,
                    ConnectionQuality.getConnectionQuality(data[4], data[7], data[3]));

            db.insert(TBL_SERVERS, null, contentValues);
            db.close();
        }
    }

    public List<Server> getCountriesByServers() {
        List<Server> countryList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TBL_SERVERS,
                null,
                null,
                null,
                CODE_COUNTRY_LONG,
                "MAX(" + CODE_QUALITY + ")",
                null);

        if (cursor.moveToFirst()) {
            do {
                countryList.add(parseServer(cursor));
            } while (cursor.moveToNext());
        } else {
            Log.d(TAG ,"0 rows");
        }

        cursor.close();
        db.close();

        return countryList;
    }


    public List<Server> getServersFromCountry(String country) {
        List<Server> serverList = new ArrayList<Server>();
        if (country != null) {
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query(TBL_SERVERS,
                    null,
                    CODE_COUNTRY_SHORT + "=?",
                    new String[]{country},
                    null,
                    null,
                    CODE_QUALITY + " DESC");

            if (cursor.moveToFirst()) {
                do {
                    serverList.add(parseServer(cursor));
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG ,"0 rows");
            }

            cursor.close();
            db.close();
        }

        return serverList;
    }

    private Server getRandomBestServers(Cursor cursor, SQLiteDatabase db) {
        List<Server> serverListExcellent = new ArrayList<Server>();
        List<Server> serverListGood = new ArrayList<Server>();
        List<Server> serverListBad = new ArrayList<Server>();

        if (cursor.moveToFirst()) {
            do {
                switch (cursor.getInt(16)) {
                    case 1:
                        serverListBad.add(parseServer(cursor));
                        break;
                    case 2:
                        serverListGood.add(parseServer(cursor));
                        break;
                    case 3:
                        serverListExcellent.add(parseServer(cursor));
                        break;
                }

            } while (cursor.moveToNext());
        } else {
            Log.d(TAG ,"0 rows");
        }

        cursor.close();
        db.close();

        Random random = new Random();
        if (serverListExcellent.size() > 0) {
            return serverListExcellent.get(random.nextInt(serverListExcellent.size()));
        } else if (serverListGood.size() > 0) {
            return serverListGood.get(random.nextInt(serverListGood.size()));
        } else if (serverListBad.size() > 0) {
            return serverListBad.get(random.nextInt(serverListBad.size()));
        }

        return null;
    }

    public Server getSimilarServer(String country, String ip) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "
                + TBL_SERVERS
                + " WHERE "
                + CODE_QUALITY
                + " <> 1 AND "
                + CODE_COUNTRY_LONG
                + " = ? AND "
                + CODE_IP
                + " <> ?", new String[] {country, ip});


        return getRandomBestServers(cursor, db);
    }

    public Server getGoodRandomServer(String country) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        if (country != null) {
            cursor = db.rawQuery("SELECT * FROM "
                    + TBL_SERVERS
                    + " WHERE "
                    + CODE_QUALITY
                    + " <> 0 AND "
                    + CODE_COUNTRY_LONG
                    + " = ?", new String[] {country});
        } else {
            cursor = db.rawQuery("SELECT * FROM "
                    + TBL_SERVERS
                    + " WHERE "
                    + CODE_QUALITY
                    + " <> 0", null);
        }

        return getRandomBestServers(cursor, db);
    }

    private Server parseServer(Cursor cursor) {
        return new Server(
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getString(8),
                cursor.getString(9),
                cursor.getString(10),
                cursor.getString(11),
                cursor.getString(12),
                cursor.getString(13),
                cursor.getString(14),
                cursor.getString(15),
                cursor.getInt(16),
                cursor.getString(17),
                cursor.getInt(18),
                cursor.getString(19),
                cursor.getDouble(20),
                cursor.getDouble(21)
        );
    }
}
