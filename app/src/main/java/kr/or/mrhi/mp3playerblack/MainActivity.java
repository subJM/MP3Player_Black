package kr.or.mrhi.mp3playerblack;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    //  =================메인 레이아웃=================
    private BottomNavigationView bottomMenu;
    private EditText edt_Search;
    private ImageButton ibSearch;
    //  =================드로우 레이아웃 ===============
    private DrawerLayout drawer;
    private RecyclerView drawFavorRecyclerView, drawRecyclerView;
    private LinearLayout rightMenu;
    //  ====================플레이어=====================
    private ImageView ivAlbumArt, ivRoof, ivPre, ivPlay, ivNext, ivFavorite;
    private TextView tvTitle, tvArtist;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean isPlaying = false;
    private boolean isLooping = false;
    private int itemPosition;
    private int isFrag = 1;
    //  ====================아이템리스트==================
    private DBHelper dbHelper;
    private ItemAdapter itemAdapter;
    private ItemAdapter favorAdapter;
    private ItemAdapter searchAdapter;
    private ItemAdapter drawRecyclerAdapter;
    private ItemAdapter drawFavorRecyclerAdapter;
    private ArrayList<MusicData> musiclist = new ArrayList<>();
    private ArrayList<MusicData> favorList = new ArrayList<>();
    private ArrayList<MusicData> searchList = new ArrayList<>();
    private String str;
//  =================


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //객채 찾기
        findViewByIdFunc();

        //권한 설정하기
        requestPermissionFun();

        //sd카드와 DB를 비교하여 최신 자료 가져오기
        getFindMP3();

        //어댑터 연결하기
        matchAdapterFunc();

        //첫번째 화면 셋팅하기
        setFirstView();

        //핸들러로 이벤트 설정하기
        eventHandler();

    }

    private void findViewByIdFunc() {
        bottomMenu = findViewById(R.id.bottomMenu);
        ibSearch = findViewById(R.id.ibSearch);
        edt_Search = findViewById(R.id.edt_Search);

        drawer = findViewById(R.id.drawer);
        rightMenu = findViewById(R.id.rightMenu);
        drawRecyclerView = findViewById(R.id.drawRecyclerView);
        drawFavorRecyclerView = findViewById(R.id.drawFavorRecyclerView);
        rightMenu = findViewById(R.id.rightMenu);

        ivAlbumArt = findViewById(R.id.ivAlbumArt);
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        ivRoof = findViewById(R.id.ivRoof);
        ivPre = findViewById(R.id.ivPre);
        ivPlay = findViewById(R.id.ivPlay);
        ivNext = findViewById(R.id.ivNext);
        ivFavorite = findViewById(R.id.ivFavorite);
        seekBar = findViewById(R.id.seekBar);
    }

    private void matchAdapterFunc() {
        itemAdapter = new ItemAdapter(this, musiclist);
        favorAdapter = new ItemAdapter(this, favorList);
        searchAdapter = new ItemAdapter(this, searchList);
        drawRecyclerAdapter = new ItemAdapter(this, musiclist);
        drawFavorRecyclerAdapter = new ItemAdapter(this, favorList);

//      ========드로우레이아웃 설정==========
        drawRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        drawFavorRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        drawRecyclerView.setAdapter(drawFavorRecyclerAdapter);
        drawFavorRecyclerView.setAdapter(drawRecyclerAdapter);
        drawRecyclerAdapter.notifyDataSetChanged();
        drawFavorRecyclerAdapter.notifyDataSetChanged();
    }

    private void eventHandler() {
//      ==================플레이어====================
        ivRoof.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLooping == false) {
                    ivRoof.setImageResource(R.drawable.ic_baseline_replay_click);
                    mediaPlayer.setLooping(true);
                    isLooping = true;
                } else if (isLooping == true) {
                    ivRoof.setImageResource(R.drawable.ic_baseline_replay_24);
                    mediaPlayer.setLooping(false);
                    isLooping = false;
                }
            }
        });
        ivPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemPosition -= 1;
                if (itemPosition < 0) {
                    itemPosition = (musiclist.size() - 1);
                    setPlayMusic(itemPosition);
                } else if (itemPosition < musiclist.size() - 1) {
                    setPlayMusic(itemPosition);
                }
            }
        });
        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying == true) {
                    mediaPlayer.pause();
                    ivPlay.setImageResource(R.drawable.play_white);
                    isPlaying = false;
                } else {
                    setPlayMusic(itemPosition);
                }
            }
        });
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemPosition += 1;
                if (itemPosition <= musiclist.size() - 1) {
                    setPlayMusic(itemPosition);
                } else if (itemPosition > musiclist.size() - 1) {
                    itemPosition = 0;
                    setPlayMusic(itemPosition);
                }
            }
        });
        ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musiclist.get(itemPosition).getFavorit().equals(1)) {
                    musiclist.get(itemPosition).setFavorite("0");
                    ivFavorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    dbHelper.updateFavorit(dbHelper.getWritableDatabase(), "0", musiclist.get(itemPosition).getId());
                    favorList.remove(musiclist.get(itemPosition));
                    favorAdapter.notifyDataSetChanged();
                } else {
                    musiclist.get(itemPosition).setFavorite("1");
                    ivFavorite.setImageResource(R.drawable.ic_baseline_favorite_24);
                    dbHelper.updateFavorit(dbHelper.getWritableDatabase(), "1", musiclist.get(itemPosition).getId());
                    favorList.add(musiclist.get(itemPosition));
                    favorAdapter.notifyDataSetChanged();
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //움직이면 mPlayer가 seekBar의 좌표로 이동
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
//      ==================아이템====================
        bottomMenu.setOnItemSelectedListener(this);

        itemAdapter.setOnItemClickListener(new ItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                itemPosition = position;
                ivPlay.setImageResource(R.drawable.ic_baseline_stop_circle_24);
                ivPlay.setClickable(true);
                setPlayMusic(position);
            }
        });
        searchAdapter.setOnItemClickListener(new ItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                itemPosition = position;
                ivPlay.setImageResource(R.drawable.ic_baseline_stop_circle_24);
                ivPlay.setClickable(true);
                setPlayMusic(position);
            }
        });
        favorAdapter.setOnItemClickListener(new ItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                itemPosition = position;
                ivPlay.setImageResource(R.drawable.ic_baseline_stop_circle_24);
                ivPlay.setClickable(true);
                setPlayMusic(position);
            }
        });
        drawRecyclerAdapter.setOnItemClickListener(new ItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                itemPosition = position;
                ivPlay.setImageResource(R.drawable.ic_baseline_stop_circle_24);
                ivPlay.setClickable(true);
                setPlayMusic(position);
            }
        });
        drawFavorRecyclerAdapter.setOnItemClickListener(new ItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                itemPosition = position;
                ivPlay.setImageResource(R.drawable.ic_baseline_stop_circle_24);
                ivPlay.setClickable(true);
                setPlayMusic(position);
            }
        });


        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                str = edt_Search.getText().toString();
                searchList = dbHelper.getSearchList(str);
                searchAdapter = new ItemAdapter(getApplicationContext(),searchList);
                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                FragSearch fragSearch = new FragSearch();

                Bundle bundle2 = new Bundle(1);
                bundle2.putParcelable("searchAdapter", searchAdapter);
                fragSearch.setArguments(bundle2);

                ft2.replace(R.id.fl_Recycler, fragSearch);
                ft2.commit();
                searchAdapter.notifyDataSetChanged();

                ibSearch.setVisibility(View.VISIBLE);
                edt_Search.setVisibility(View.VISIBLE);
                isFrag = 3;

            }
        });
        edt_Search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                str = charSequence.toString();
                searchList = dbHelper.getSearchList(str);
                searchAdapter = new ItemAdapter(getApplicationContext(),searchList);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sortName:
                if (isFrag != 1) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    FragItem fragItem = new FragItem();

                    Bundle bundle = new Bundle(1);
                    bundle.putParcelable("itemAdapter", itemAdapter);
                    fragItem.setArguments(bundle);

                    ft.replace(R.id.fl_Recycler, fragItem);
                    ft.commit();
                    itemAdapter.notifyDataSetChanged();

                    ibSearch.setVisibility(View.INVISIBLE);
                    edt_Search.setVisibility(View.INVISIBLE);
                    isFrag = 1;
                }

                break;
            case R.id.sortFavor:
                if (isFrag != 2) {
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    FragFavorite fragFavorite = new FragFavorite();

                    Bundle bundle1 = new Bundle(1);
                    bundle1.putParcelable("favorAdapter", favorAdapter);
                    fragFavorite.setArguments(bundle1);

                    ft1.replace(R.id.fl_Recycler, fragFavorite);
                    ft1.commit();
                    favorAdapter.notifyDataSetChanged();

                    ibSearch.setVisibility(View.INVISIBLE);
                    edt_Search.setVisibility(View.INVISIBLE);
                    isFrag = 2;
                }
                break;
            case R.id.search:
                if (isFrag != 3) {
                    FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                    FragSearch fragSearch = new FragSearch();

                    Bundle bundle2 = new Bundle(1);
                    bundle2.putParcelable("searchAdapter", searchAdapter);
                    fragSearch.setArguments(bundle2);

                    ft2.replace(R.id.fl_Recycler, fragSearch);
                    ft2.commit();
                    searchAdapter.notifyDataSetChanged();

                    ibSearch.setVisibility(View.VISIBLE);
                    edt_Search.setVisibility(View.VISIBLE);
                    isFrag = 3;

                }
                break;
            case R.id.btnDraw:
                drawer.openDrawer(GravityCompat.START);
                drawRecyclerAdapter.notifyDataSetChanged();
                drawFavorRecyclerAdapter.notifyDataSetChanged();
                break;
        }
        return false;
    }

    private void searchFragFunc() {
        if (isFrag != 3) {
            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
            FragSearch fragSearch = new FragSearch();

            Bundle bundle2 = new Bundle(1);
            bundle2.putParcelable("searchAdapter", searchAdapter);
            fragSearch.setArguments(bundle2);

            ft2.replace(R.id.fl_Recycler, fragSearch);
            ft2.commit();
            searchAdapter.notifyDataSetChanged();

            ibSearch.setVisibility(View.VISIBLE);
            edt_Search.setVisibility(View.VISIBLE);
            isFrag = 3;

        }
    }


    private void setPlayMusic(int position) {
        mediaPlayer.stop();
        mediaPlayer.reset();
        //좋아요 버튼 정의
        if (musiclist.get(itemPosition).getFavorit().equals(1)) {
            ivFavorite.setImageResource(R.drawable.ic_baseline_favorite_24);
        } else {
            ivFavorite.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        //재생화면 재설정
        Bitmap albumImg = getAlbumImg(getApplicationContext(), Long.parseLong(musiclist.get(position).getAlbumArt()), 110);
        if (albumImg != null) {
            ivAlbumArt.setImageBitmap(albumImg);
        } else {
            ivAlbumArt.setImageResource(R.drawable.tiffany);
        }
        tvTitle.setText(musiclist.get(position).getTitle());
        tvArtist.setText(musiclist.get(position).getArtist());

        //음악재생
        Uri uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                musiclist.get(position).getId());
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            ivPlay.setImageResource(R.drawable.ic_baseline_stop_circle_24);
            seekBar.setProgress(0);
            seekBar.setMax(Integer.parseInt(musiclist.get(position).getDuration()));

            setSeekbarThread();

            //재생완료 리스너
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    ivNext.callOnClick();
                }
            });
            isPlaying = true;
        } catch (IOException e) {
            Log.e("uri", "uri Error");
        }

    }

    private void setFirstView() {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        FragItem fragItem = new FragItem();

        Bundle bundle = new Bundle(1);
        bundle.putParcelable("itemAdapter", itemAdapter);
        fragItem.setArguments(bundle);

        ft.replace(R.id.fl_Recycler, fragItem);
        ft.commit();

        ivPlay.setImageResource(R.drawable.ic_baseline_stop_circle_24);
        ivPlay.setClickable(false);
    }


    private void requestPermissionFun() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MODE_PRIVATE);
    }

    public Bitmap getAlbumImg(Context context, long albumArt, int maxSize) {
        // url -> 컨텐트 리졸버 -> 상대방 컨텐트 프로바이더
        BitmapFactory.Options options = new BitmapFactory.Options();
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://media/external/audio/albumart/" + albumArt);

        if (uri != null) {
            ParcelFileDescriptor pfd = null;
            try {
                pfd = resolver.openFileDescriptor(uri, "r");

                //메모리할당을 막았다가 비트맵으로 전환할 때 할당 실시
                options.inJustDecodeBounds = true;
                int scale = 0;
                if (options.outHeight > maxSize || options.outWidth > maxSize) {
                    scale = (int) Math.pow(2, (int) Math.round(Math.log(maxSize /
                            (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
                }

                //메모리할당 실시
                options.inJustDecodeBounds = false;
                options.inSampleSize = scale;
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor(), null, options);

                //정의된 수치(maxSize)보다 크다면 조정하고 뷰에 표시
                if (bitmap != null) {
                    if (options.outWidth != maxSize || options.outHeight != maxSize) {
                        Bitmap btm = Bitmap.createScaledBitmap(bitmap, maxSize, maxSize, true);
                        bitmap.recycle();
                        bitmap = btm;
                    }
                }
                return bitmap;

            } catch (FileNotFoundException e) {
                Log.d("Context Resolver", "Resolver Error");
            } finally {
                try {
                    if (pfd != null)
                        pfd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void setSeekbarThread() {
        Thread thread1 = new Thread(() -> {
            //런타임 값 배정하기
            while (isPlaying) {
                runOnUiThread(() -> {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                });
                SystemClock.sleep(400);
            }
        });
        thread1.start();
    }

    private void getFindMP3() {
        //MusicTBL을 가져오기
        dbHelper = dbHelper.getInstance(getApplicationContext());

        //전체음악파일 추출
        musiclist = dbHelper.dbMatchToSdCard();

        //db최신화
        boolean returnValue = dbHelper.insertMusicTBL(musiclist);
        if (returnValue) {
            Log.d("music DB", "db접근 성공");
        } else {
            Log.d("music DB", "db접근 실패");
        }

        //좋아요체크된 파일 추출
        favorList = dbHelper.getFavorite();

        //검색 파일 추출
        searchList = dbHelper.getSearchList(str);


    }
}