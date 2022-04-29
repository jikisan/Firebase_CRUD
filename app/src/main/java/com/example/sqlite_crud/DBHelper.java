package com.example.sqlite_crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATA_VERSION = 1;
    private static final String DATABASE_NAME = "productDB";

    private static final String table_NAME = "product";

    private static final String ID = "ID";
    private static final String Name = "Name";
    private static final String Desc = "Description";
    private static final String Price = "Price";
    private static final String Quantity = "Quantity";
    private static final String Status = "Status" ;



    public DBHelper( Context context) {
        super(context, DATABASE_NAME , null, DATA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        String CREATE_TABLE = "CREATE TABLE " + table_NAME + "(" + ID + " Integer PRIMARY KEY AUTOINCREMENT,"
                + Name + " TEXT,"
                + Desc + " TEXT,"
                + Price + " Integer,"
                + Quantity + " Integer,"
                + Status + " TEXT" + ")";
        DB.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("DROP TABLE IF EXISTS " + table_NAME);
        onCreate(DB);
    }

    public Boolean insertUserData( String Name, String Desc, int Price, int Quantity, String Status){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Name", Name);
        contentValues.put("Description", Desc);
        contentValues.put("Price", Price);
        contentValues.put("Quantity", Quantity);
        contentValues.put("Status", Status);

        long result = DB.insert(table_NAME, null, contentValues);

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Boolean updateUserData (String Name, String Desc, int Price, int Quantity, String Status){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Name", Name);
        contentValues.put("Description", Desc);
        contentValues.put("Price", Price);
        contentValues.put("Quantity", Quantity);
        contentValues.put("Status", Status);

        Cursor cursor = DB.rawQuery("Select * from product where Name=?", new String[]{Name});

        if(cursor.getCount()>0){
            long result = DB.update(table_NAME, contentValues,"Name=?", new String[] {Name});

            if(result == -1){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }

    }

    public Boolean deleteUserData ( String Name){
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from product where Name=?", new String[]{Name});

        if(cursor.getCount()>0){
            long result = DB.delete(table_NAME,"Name=?", new String[] {Name});

            if(result == -1){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    public Cursor getData (){
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from product ", null);

        return cursor;
    }


}
