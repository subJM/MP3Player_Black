package kr.or.mrhi.mp3playerblack;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.Holer> implements Parcelable {
    private Context context;
    private ArrayList<MusicData> list = new ArrayList<>();
    private OnItemClickListener mListener;
    private SimpleDateFormat sdf;

    public ItemAdapter(Context context, ArrayList<MusicData> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public Holer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.music_item,parent,false);
        Holer holder = new Holer(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holer holder, int position) {
        //앨범 이미지를 먼저 가져와서 아이템에 배치
        Bitmap albumImg = getAlbumImg(context, Long.parseLong(list.get(position).getAlbumArt()),110);
        if(albumImg != null) {
            holder.ivAlbumArt.setImageBitmap(albumImg);
        }
        //아이템(뷰홀더 안의 객체) 나머지 데이터 배치
        sdf = new SimpleDateFormat("mm:ss");
        String str = sdf.format(Integer.parseInt(list.get(position).getDuration()));
        holder.tvTitle.setText(list.get(position).getTitle());
        holder.tvArtist.setText(list.get(position).getArtist());
        holder.tvTime.setText(str);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    protected ItemAdapter(Parcel in) {
    }


    public static final Creator<ItemAdapter> CREATOR = new Creator<ItemAdapter>() {
        @Override
        public ItemAdapter createFromParcel(Parcel in) {
            return new ItemAdapter(in);
        }

        @Override
        public ItemAdapter[] newArray(int size) {

            return new ItemAdapter[size];
        }
    };

    @Override
    public int getItemCount() {
        return (list != null ? list.size() :0);
    }
    public class Holer extends RecyclerView.ViewHolder {
        ImageView ivAlbumArt;
        TextView tvTitle,tvArtist,tvTime;

        public Holer(@NonNull View v) {
            super(v);
            ivAlbumArt = v.findViewById(R.id.ivAlbumArt);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvArtist = v.findViewById(R.id.tvArtist);
            tvTime = v.findViewById(R.id.tvTime);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(v, position);
                    }
                }
            });
        }
    }

    //앨범의 이미지를 가져오는 메소드
    public Bitmap getAlbumImg(Context context, long albumArt, int maxSize) {
        // url -> 컨텐트 리졸버 -> 상대방 컨텐트 프로바이더
        BitmapFactory.Options options = new BitmapFactory.Options();
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://media/external/audio/albumart/" + albumArt);

        if(uri != null) {
            ParcelFileDescriptor pfd = null;
            try {
                pfd = resolver.openFileDescriptor(uri,"r");

                //메모리할당을 막았다가 비트맵으로 전환할 때 할당 실시
                options.inJustDecodeBounds = true;
                int scale = 0;
                if(options.outHeight > maxSize || options.outWidth > maxSize) {
                    scale = (int)Math.pow(2,(int)Math.round(Math.log(maxSize /
                            (double)Math.max(options.outHeight,options.outWidth)) / Math.log(0.5)));
                }

                //메모리할당 실시
                options.inJustDecodeBounds = false;
                options.inSampleSize = scale;
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor(),null,options);

                //정의된 수치(maxSize)보다 크다면 조정하고 뷰에 표시
                if(bitmap != null) {
                    if(options.outWidth != maxSize || options.outHeight != maxSize){
                        Bitmap btm = Bitmap.createScaledBitmap(bitmap, maxSize, maxSize,true);
                        bitmap.recycle();
                        bitmap = btm;
                    }
                }
                return bitmap;

            } catch (FileNotFoundException e) {
                Log.d("Context Resolver","Resolver Error");
            }finally {
                try {
                    if(pfd != null)
                        pfd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener (OnItemClickListener listener) {
        this.mListener = listener;
    }

}
