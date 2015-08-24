package com.example.kavitha.simpletodo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by kavitha on 8/23/15.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "SimpleToDo.db";
    public static final String TODO_TABLE_NAME = "todo_items";
    public static final String TODO_COLUMN_ID = "id";
    public static final String TODO_COLUMN_DESCRIPTION = "description";
    public static final String TODO_COLUMN_TYPE = "type";
    public static final String TODO_COLUMN_DATE = "date";
    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, 1);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table todo_items " +
                        "(id integer primary key, description text, type integer, date long)"
        );
        db.execSQL(
                "create unique index todo_items_nodups on todo_items (description, type)"
        );
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS todo_items");
        onCreate(db);
    }

    public boolean insertTodoItem  (String description, int type)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("description", description);
        contentValues.put("type", type);
        contentValues.put("date", System.currentTimeMillis());
        try {
            db.insertOrThrow("todo_items", null, contentValues);
            return true;
        }
        catch(SQLiteConstraintException e) {
            return false;
        }
    }

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from todo_items where id="+id+"", null );
        return res;
    }

    public Cursor getData(String des, int type){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from todo_items where description = '" + des + "' and type = " + type, null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TODO_TABLE_NAME);
        return numRows;
    }

    public boolean updateTodoItem (TodoValue oldVal, TodoValue newVal)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("description", newVal.mItemString);
        contentValues.put("type", newVal.mItemType);
        try {
            db.update("todo_items", contentValues, "description = ? and type = ? ", new String[]{oldVal.mItemString, Integer.toString(oldVal.mItemType)});
            return true;
        }
        catch(SQLiteConstraintException e) {
            return false;
        }
    }

    public Integer deleteTodoItem (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("todo_item",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public Integer deleteTodoItem (TodoValue delVal)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("todo_items",
                "description = ? and type = ?",
                new String[] { delVal.mItemString, Integer.toString(delVal.mItemType) });
    }

    public ArrayList<TodoValue> getAllTodoItems(String column)
    {
        ArrayList<TodoValue> array_list = new ArrayList<TodoValue>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select description, type from todo_items ORDER BY " + column, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(new TodoValue(res.getString(res.getColumnIndex(TODO_COLUMN_DESCRIPTION)), res.getInt(res.getColumnIndex(TODO_COLUMN_TYPE))));
            res.moveToNext();
        }
        return array_list;
    }

    public static class TodoValue {
        String mItemString;
        int mItemType;
        public TodoValue(String itemString, int itemType) {
            mItemString = itemString;
            mItemType = itemType;
        }
    }

}
