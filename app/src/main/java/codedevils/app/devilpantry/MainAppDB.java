package codedevils.app.devilpantry;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainAppDB extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 1;
    private static String dbName = "mainapp";
    private String dbPath;
    private SQLiteDatabase appDB;
    private final Context context;

    public MainAppDB(Context context){
        super(context, dbName, null, DATABASE_VERSION);
        this.context = context;
        dbPath = context.getFilesDir().getPath() + "/";
    }

    public void createDB() throws IOException{
        this.getReadableDatabase();
        try{
            copyDB();
        }
        catch (IOException ex){
            ex.getMessage();
        }
    }

    public void copyDB() throws IOException{
        try{
            if(!checkDB()){
                InputStream is = context.getResources().openRawResource(R.raw.mainapp);
                File f = new File(dbPath);
                if(!f.exists()){
                    f.mkdirs();
                }
                String op = dbPath + dbName + ".db";
                OutputStream os = new FileOutputStream(op);
                byte[] buffer = new byte[1024];
                int length;
                while((length = is.read(buffer)) > 0){
                    os.write(buffer, 0, length);
                }
                os.flush();
                os.close();
                is.close();
            }
        }
        catch (IOException ex){
            ex.getMessage();
        }
    }

    public boolean checkDB(){
        SQLiteDatabase checker = null;
        boolean ret = false;
        try {
            String path = dbPath + dbName + ".db";
            File f = new File(path);
            if(f.exists()){
                checker = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
                if(checker != null){
                    Cursor tabs = checker.rawQuery("SELECT name FROM sqlite_master WHERE type='table' and name='pantry';", null);
                    boolean tabExists = false;
                    if(tabs != null){
                        tabs.moveToNext();
                        tabExists = !tabs.isAfterLast();
                    }
                    if(tabExists){
                        ret = true;
                    }
                }
            }
        }
        catch (SQLiteException ex){
            ex.getMessage();
        }
        if(checker != null){
            checker.close();
        }
        return ret;
    }

    public SQLiteDatabase openDB() throws SQLiteException{
        String path = dbPath + dbName + ".db";
        if(checkDB()){
            appDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
        }
        else{
            try{
                this.copyDB();
                appDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
            }
            catch (Exception ex){
                ex.getMessage();
            }
        }
        return appDB;
    }

    @Override
    public synchronized void close(){
        if(appDB != null){
            appDB.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
