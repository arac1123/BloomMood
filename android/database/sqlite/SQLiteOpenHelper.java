package android.database.sqlite;

/**
 * Minimal stub of android.database.sqlite.SQLiteOpenHelper for desktop/tests.
 * Provides a no-arg constructor and a getReadableDatabase() implementation.
 */
public abstract class SQLiteOpenHelper {
    public SQLiteOpenHelper() {
    }

    public SQLiteDatabase getReadableDatabase() {
        return new SQLiteDatabase();
    }
}
