package kr.or.mrhi.mp3playerblack;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    private Context context;
    private static DBHelper dbHelper;
    int version;
    int nVersion;

    public DBHelper(@Nullable Context context) {
        super(context, "MusicDB", null, 1);
        this.context = context;
    }


    //비교하기위해 DB에 있는 테이블을 가져온다.
    public static DBHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(context);
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase sdb) {
        sdb.execSQL("Create table if not exists MusicTBL(" +
                "id Text  primary key," +
                "artist Text," +
                "title Text," +
                "albumArt Text," +
                "duration Text," +
                "favorite Text" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sdb, int oldVersion, int newVersion) {
        nVersion = version + 1;
        sdb.execSQL("drop table if exists MusicTBL;");
        onCreate(sdb);
    }

    public boolean insertMusicTBL(ArrayList<MusicData> list) {
        boolean flag = false;
        SQLiteDatabase sdb = getWritableDatabase();
        try {
            //db에서 전체 조회 후 비교
            for (MusicData md : list) {
                ArrayList<MusicData> musicList = getDBList();
                if (musicList.contains(md) == false) {
//                    md.setTitle(md.getTitle().replaceAll("'", "''"));
//                    md.setArtist(md.getTitle().replaceAll("'", "''"));

                    String data = String.format("insert into MusicTBL values('%s','%s','%s','%s','%s','%s');",
                            md.getId(), md.getArtist(), md.getTitle(), md.getAlbumArt(), md.getDuration(), md.getFavorit());
                    sdb.execSQL(data);
                    flag = true;
                }
            }
        } catch (Exception e) {
            Log.d("DB인설트", "DB인설트 오류" + e.toString());
        } finally {
            sdb.close();
        }
        return flag;

    }

    //좋아요 리스트 셀렉트
    public ArrayList<MusicData> getFavorite() {
        SQLiteDatabase sdb = this.getReadableDatabase();
        //좋아요 리스트 select
        ArrayList<MusicData> list = new ArrayList<>();
        Cursor cursor = sdb.rawQuery("select * from musicTBL where favorite = '1';", null);
        try {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String artist = cursor.getString(1);
                String title = cursor.getString(2);
                String albumArt = cursor.getString(3);
                String duration = cursor.getString(4);
                String favor = cursor.getString(5);

                MusicData musicData = new MusicData(id, artist, title, albumArt, duration, favor);
                list.add(musicData);
            }
            for (MusicData md : list) {
                Log.d("즐겨찾기리스트", md.toString());
            }
        } catch (Exception e) {
            Log.e("selectMusicTBLFavor", "select Error(favor)");
        } finally {
            cursor.close();
            sdb.close();
        }
        return list;
    }

    //좋아요 버튼 업데이트
    public boolean updateFavorit(SQLiteDatabase sdb, String favorite, String id) {
        boolean flag = false;
        try {
            String data = String.format("update MusicTBL set favorite= '%s' where id = '%s';", favorite, id);
            sdb.execSQL(data);
            flag = true;

        } catch (Exception e) {
            e.toString();
        } finally {
            sdb.close();
        }
        return flag;
    }

    public ArrayList<MusicData> selectFavorit() {
        ArrayList<MusicData> list = new ArrayList<>();
        SQLiteDatabase sdb = getReadableDatabase();
        String favorite = null;
        Cursor cursor = null;
        try {
            cursor = sdb.rawQuery("select * from MusicTBL where favorit ='1';", null);
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String artist = cursor.getString(1);
                String title = cursor.getString(2);
                String albumArt = cursor.getString(3);
                String duration = cursor.getString(4);
                String favor = cursor.getString(6);

                MusicData musicData = new MusicData(id, artist, title, albumArt, duration, favor);
                list.add(musicData);
            }

        } catch (Exception e) {
            e.toString();
        } finally {
            cursor.close();
            sdb.close();
        }
        return list;
    }

    //sd카드에서 파일 추출하기
    public ArrayList<MusicData> getSDCardList() {
        ArrayList<MusicData> sdList = new ArrayList<>();
        SQLiteDatabase sdb = getWritableDatabase();

        String[] colum = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
        };

        //커서 = title의 asc순 쿼리문
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                colum, null, null, colum[2] + " ASC");
        try {
            int idCursor = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int artistCursor = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int titleCursor = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int albumArtCursor = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int durationCursor = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);


            while (cursor.moveToNext()) {
                String id = cursor.getString(idCursor);//
                String artist = cursor.getString(artistCursor);//가수이름
                String title = cursor.getString(titleCursor);
                String albumArt = cursor.getString(albumArtCursor);
                String duration = cursor.getString(durationCursor);


                MusicData musicData = new MusicData(id, artist, title, albumArt, duration, "0");
                sdList.add(musicData);
            }

            for (MusicData md : sdList) {
                Log.d("음악파일저장SD", md.toString());
            }
        } catch (Exception e) {
            Log.e("음악플레이어", "컨텐트프로바이더 mp3 로딩 에러발생" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
                sdb.close();
            }
        }
        return sdList;
    }

    //DB리스트에 있는것을 추출한다.
    public ArrayList<MusicData> getDBList() {
        ArrayList<MusicData> dbList = new ArrayList<>();
        SQLiteDatabase sdb = getReadableDatabase();

        Cursor cursor = sdb.rawQuery("select * from musicTBL", null);
        try {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);//
                String artist = cursor.getString(1);//가수이름
                String title = cursor.getString(2);
                String albumArt = cursor.getString(3);
                String duration = cursor.getString(4);
                String favorite = cursor.getString(5);

                MusicData musicData = new MusicData(id, artist, title, albumArt, duration, favorite);
                dbList.add(musicData);
            }
            Log.d("음악파일저장DB", dbList.toString());
        } catch (Exception e) {
            Log.e("음악플레이어", "컨텐트프로바이더 mp3 로딩 에러발생" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return dbList;
    }

    //DB와 SD카드에 저장되있는 것을 비교한다.
    public ArrayList<MusicData> dbMatchToSdCard() {
        //db
        ArrayList<MusicData> dbList = getDBList();

        //sdcard
        ArrayList<MusicData> sdList = getSDCardList();


        //2. db가 비어있다면 sd카드내용을 그대로 리턴
        if (dbList == null) {
            insertMusicTBL(sdList);
            return sdList;

        }
        //1. db가 sd카드의 내용을 모두 가지고 있다면 sd의 내용을 리턴
        if (dbList.containsAll(sdList)) {
            return dbList;
        }


        //3. db에 없는 내용이 sd카드에 존재할 경우 db에 추가
        if (!dbList.containsAll(sdList)) {
            //table 다시 만들고 다시 작성한다.
//            version = nVersion;
//            nVersion += 1;
//            onUpgrade(getWritableDatabase(), version, nVersion);
            boolean flag = insertMusicTBL(sdList);
            Log.d("3번인설트뮤직", sdList.toString());
            return sdList;
        }
        return null;
    }


    public ArrayList<MusicData> getSearchList(String searchItem) {
        SQLiteDatabase sdb = getWritableDatabase();
        ArrayList<MusicData> list = new ArrayList<>();

        //가수 검색와 곡명검색
        Cursor cursor = sdb.rawQuery("select * from musicTBL where artist like '%" + searchItem + "%' or title like '%" + searchItem + "%';", null);
        try {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String artist = cursor.getString(1);
                String title = cursor.getString(2);
                String albumArt = cursor.getString(3);
                String duration = cursor.getString(4);
                String favor = cursor.getString(5);

                MusicData musicData = new MusicData(id, artist, title, albumArt, duration, favor);
                list.add(musicData);
                Log.d("getSearchList", "select Error(search)" + list.toString());
            }
        } catch (Exception e) {
            Log.e("getSearchList", "select Error(search)");
        } finally {
            cursor.close();
        }

        if (list == null) {
            Toast.makeText(context, "조회된 정보가 없습니다", Toast.LENGTH_SHORT).show();
        } else {
            return list;
        }
        return null;
    }
}
