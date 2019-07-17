package com.example.mvvmsdemo;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.mvvmsdemo.databinding.RvItemBinding;
import com.example.mvvmsdemo.model.bean.NewsBean;

import java.util.List;

public class NewsAdapter  extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    List<NewsBean> list;
    class ViewHolder extends RecyclerView.ViewHolder {
        RvItemBinding binding;
        public ViewHolder(RvItemBinding binding){
            super(binding.getRoot());
            this.binding=binding;
        }
    }

    public NewsAdapter(List<NewsBean> list){
        this.list=list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        RvItemBinding binding= DataBindingUtil
                .inflate(LayoutInflater.from(viewGroup.getContext()),R.layout.rv_item,viewGroup,false);


        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.binding.setNews(list.get(i));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
