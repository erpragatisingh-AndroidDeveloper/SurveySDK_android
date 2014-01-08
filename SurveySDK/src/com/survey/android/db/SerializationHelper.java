package com.survey.android.db;

import com.survey.android.util.Log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

public class SerializationHelper {
	public static final String DB_NAME = "paused_surveys.db";
	public static final int DB_VERSION = 43;
	// ********************* names of used tables *******************************************
	public static final String TABLE_SURVEYS = "surveys";
	public static final String C_ID = BaseColumns._ID;	
	public static final String C_ARRAY = "array"; 
	
	private Context context;
	private SQLiteDatabase db;

	public SQLiteDatabase getSQLiteDatabase() {
		return db;
	}

	// insert statement
	@SuppressWarnings("unused")
	private SQLiteStatement insertStmtSurveys;
	
	
	// sql string for insert into appropriate tables
	private static final String INSERT_SURVEYS = "INSERT INTO "
			+ TABLE_SURVEYS
			+ "(array) VALUES (?);";

	public SerializationHelper(Context context) {
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
		this.insertStmtSurveys = this.db.compileStatement(INSERT_SURVEYS);
	}

	public boolean pauseSurvey(byte[] array) {

		boolean result = true;
		this.db.beginTransaction();
		try {
			this.db.delete(TABLE_SURVEYS, null, null);
			ContentValues dataToInsert = new ContentValues();                          
			dataToInsert.put(C_ARRAY,array);
			db.insert(TABLE_SURVEYS, null, dataToInsert);
//			this.insertStmtSurveys.bindBlob(0, array);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("DB", e.getMessage());
			result = false;
		} finally {
			db.endTransaction();
			if(db.isOpen()){
				db.close();
			}
		}
		return result;
	}

	/**
	 * Delete all records from tables
	 */
	public void deleteAll() {
		this.db.delete(TABLE_SURVEYS, null, null);
	}
	
	/**
	 * 
	 * @return
	 */
	public byte[] reloadPauseSurvey(){
		byte[] result=null;
		try{
			Cursor cursor = this.db.query(TABLE_SURVEYS, new String[] {
					"array"}, null, null, null, null, null);
			if (cursor.moveToFirst()) {
				// do {
				result = cursor.getBlob(0);				
			}
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}

			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			
		}
		catch(Exception e){
			e.printStackTrace();
			result=null;
		}
		return result;
	}


	/**
	 * Checks if there is paused survey 
	 * @return true - there is paused survey false - there is no paused survey
	 */
	public boolean isPaused(){
		try{
		    String count = "SELECT count(*) FROM "+TABLE_SURVEYS;
		    Cursor mcursor = db.rawQuery(count, null);
		    mcursor.moveToFirst();
		    int icount = mcursor.getInt(0);
		    
		    if (mcursor != null && !mcursor.isClosed()) {
		    	mcursor.close();
			}
		    return icount==1;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	
	// public List<String> selectAll() {
	// List<String> list = new ArrayList<String>();
	// Cursor cursor = this.db.query(TABLE_NAME, new String[] { "name" },
	// null, null, null, null, "name desc");
	// if (cursor.moveToFirst()) {
	// do {
	// list.add(cursor.getString(0));
	// } while (cursor.moveToNext());
	// }
	// if (cursor != null && !cursor.isClosed()) {
	// cursor.close();
	// }
	// return list;
	// }

	private static class OpenHelper extends SQLiteOpenHelper {
		OpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sqlSurvey = String.format("CREATE TABLE %s ( %s long,%s BLOB);",TABLE_SURVEYS, C_ID, C_ARRAY);
			db.execSQL(sqlSurvey);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d("Example",
					"Upgrading database, this will drop tables and recreate.");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_SURVEYS);
			onCreate(db);
		}
	}
}