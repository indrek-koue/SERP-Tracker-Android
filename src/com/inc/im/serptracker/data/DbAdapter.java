package com.inc.im.serptracker.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbAdapter {

	private SQLiteDatabase mDb;
	private static Context mCtx;
	private DatabaseHelper mDbHelper;

	private static final String DATABASE_NAME = "appdata";

	//private static final int DATABASE_VERSION = 2;
	private static final String TABLE_PROFILE = "profile";
	private static final String KEY_PROFILE_TABLE_ID = "_id";
	private static final String KEY_PROFILE_TABLE_URL = "url";

	private static final String TABLE_KEYWORDS = "profile_keywords";
	private static final String KEY_KEYWORDS_TABLE_ID = "_id";
	private static final String KEY_KEYWORDS_TABLE_KEYWORD = "keyword";
	private static final String KEY_KEYWORDS_TABLE_POSTION = "position";
	private static final String KEY_KEYWORDS_TABLE_PARENTID = "parentid";

	private static final String PROFILE_TABLE_CREATE = "CREATE TABLE "
			+ TABLE_PROFILE + " (" + KEY_PROFILE_TABLE_ID
			+ " INTEGER PRIMARY KEY, " + KEY_PROFILE_TABLE_URL
			+ " TEXT NOT NULL);";

	private static final String KEYWORDS_TABLE_CREATE = "CREATE TABLE "
			+ TABLE_KEYWORDS + " (" + KEY_KEYWORDS_TABLE_ID
			+ " INTEGER PRIMARY KEY, " + KEY_KEYWORDS_TABLE_KEYWORD
			+ " TEXT NOT NULL, " + KEY_KEYWORDS_TABLE_POSTION + " INTEGER, "
			+ KEY_KEYWORDS_TABLE_PARENTID + " INTEGER NOT NULL);";

	public DbAdapter(Context ctx) {
		DbAdapter.mCtx = ctx;
	}

	public DbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public Boolean insertProfile(UserProfile profile) {

		Boolean headerInsertIsSuccess = false;
		Boolean keywordInsertIsSuccess = false;

		Long parentId = 0l;

		open();

		if (profile != null && profile.url != null) {

			// insert header
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_PROFILE_TABLE_URL, profile.url);
			parentId = mDb.insert(TABLE_PROFILE, null, initialValues);

			headerInsertIsSuccess = parentId == -1 ? false : true;

			// insert keywords
			for (Keyword keyword : profile.keywords) {

				ContentValues initialValuesKeywords = new ContentValues();
				initialValuesKeywords.put(KEY_KEYWORDS_TABLE_KEYWORD,
						keyword.value);
				initialValuesKeywords
						.put(KEY_KEYWORDS_TABLE_PARENTID, parentId);

				Long l = mDb
						.insert(TABLE_KEYWORDS, null, initialValuesKeywords);

				keywordInsertIsSuccess = l == -1 ? false : true;

			}
		}

		close();

		return (headerInsertIsSuccess && keywordInsertIsSuccess);
	}

	public ArrayList<UserProfile> loadAllProfiles() {

		ArrayList<UserProfile> profiles = null;

		open();

		// query from headers
		// String[] columns = { KEY_PROFILE_TABLE_ID, KEY_PROFILE_TABLE_URL };

		Cursor profileHeaderCur = mDb.query(TABLE_PROFILE, null, null, null,
				null, null, null);

		if (profileHeaderCur != null && profileHeaderCur.getCount() != 0) {
			profileHeaderCur.moveToFirst();

			profiles = new ArrayList<UserProfile>();
			
			do {

				int profileId = profileHeaderCur.getInt(profileHeaderCur
						.getColumnIndex(KEY_PROFILE_TABLE_ID));
				String profileUrl = profileHeaderCur.getString(profileHeaderCur
						.getColumnIndex(KEY_PROFILE_TABLE_URL));

				// we have name and ID now lets get the keywords
				ArrayList<Keyword> keywords = new ArrayList<Keyword>();

				Cursor keywordsCur = mDb.query(TABLE_KEYWORDS, null,
						KEY_KEYWORDS_TABLE_PARENTID + " = " + profileId, null,
						null, null, null);

				if (keywordsCur != null)
					keywordsCur.moveToFirst();

				do {

					String keyword = keywordsCur.getString(keywordsCur
							.getColumnIndex(KEY_KEYWORDS_TABLE_KEYWORD));

					keywords.add(new Keyword(keyword));
				} while (keywordsCur.moveToNext());

				// done - now repeat
				profiles.add(new UserProfile(profileId, profileUrl, keywords));

			} while (profileHeaderCur.moveToNext());
		}
		return profiles;

	}

	public void trunkTables() {

		open();

		mDb.execSQL("drop table if exists " + TABLE_PROFILE);
		mDb.execSQL("drop table if exists " + TABLE_KEYWORDS);

		mDb.execSQL(PROFILE_TABLE_CREATE);
		mDb.execSQL(KEYWORDS_TABLE_CREATE);

		close();

	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, 2);
		}

		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// Called when the database is created for the first time. This is
			// where the creation of tables and the initial population of the
			// tables should happen.

			Log.i("MY", "PROFILE TABLE CREATE: " + PROFILE_TABLE_CREATE
					+ " KEYWORDS TABLE CREATE " + KEYWORDS_TABLE_CREATE);
			db.execSQL(PROFILE_TABLE_CREATE);
			db.execSQL(KEYWORDS_TABLE_CREATE);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Called when the database needs to be upgraded. The implementation
			// should use this method to drop tables, add tables, or do anything
			// else it needs to upgrade to the new schema version.

			db.execSQL("drop table if exists " + TABLE_PROFILE);
			db.execSQL("drop table if exists " + TABLE_KEYWORDS);

		}

	}

}