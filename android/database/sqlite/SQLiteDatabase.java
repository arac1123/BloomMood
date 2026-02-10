package android.database.sqlite;

import android.database.Cursor;

/**
 * Minimal stub of android.database.sqlite.SQLiteDatabase for desktop/tests.
 * Only provides the `query` signature used in `py.java` and returns null.
 */
public class SQLiteDatabase {
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy,
            String having, String orderBy) {
        return null;
    }
}
