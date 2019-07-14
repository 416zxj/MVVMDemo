package com.example.mvvmsdemo;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.mvvmsdemo.databinding.ActivityMainBinding;
import com.example.mvvmsdemo.view_model.NewsViewModel;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    NewsAdapter adapter;

    //用来刷新数据
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        //ViewModelProviders是谷歌提供的LifeCycle库的工具包，用来给任何对象绑定Activity或者碎片的生命周期的
        //这里由于VIewModel会被DataBinding和Activity持有，为了让我们的VIewModel生命周期和Activity一致，所以需要这样获取
        final NewsViewModel newsViewModel = ViewModelProviders.of(this).get(NewsViewModel.class);
        binding.setNewsViewModel(newsViewModel);
        handler=new Handler();


        //监听字段变换，因为该字段是用来控制ui状态变化的，在VIewModel里面使用的是LiveData。
        Observer<Boolean> flash = new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean s) {
                Log.d(TAG, "onChanged: "+s);
                if (s) {
                    binding.button.setText("正在刷新....");
                } else {
                    binding.button.setText("刷新");
                    adapter.notifyDataSetChanged();
                }
            }
        };

        newsViewModel.isFlash.observe(this, flash);

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newsViewModel.load(handler);
            }
        });


        binding.rv.setLayoutManager(new LinearLayoutManager(this));
        //这一步你可以考虑在viewModel处理
        adapter = new NewsAdapter(newsViewModel.getdata());

        binding.rv.setAdapter(adapter);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
