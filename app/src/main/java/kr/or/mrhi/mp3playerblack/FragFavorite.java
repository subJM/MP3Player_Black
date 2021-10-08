package kr.or.mrhi.mp3playerblack;

import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragFavorite extends Fragment {
    private RecyclerView favoriteView;
    private LinearLayoutManager linearLayoutManager;
    private ItemAdapter favorAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_favor,container,false);
        favoriteView = view.findViewById(R.id.favoriteView);

        favorAdapter = getArguments().getParcelable("favorAdapter");
        linearLayoutManager = new LinearLayoutManager(getContext());
        favoriteView.setLayoutManager(linearLayoutManager);
        favoriteView.setAdapter(favorAdapter);


        return view;
    }
}
