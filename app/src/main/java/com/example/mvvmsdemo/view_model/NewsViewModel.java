package com.example.mvvmsdemo.view_model;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.databinding.ObservableField;
import android.os.Handler;

import com.example.mvvmsdemo.moudle.IModel;
import com.example.mvvmsdemo.moudle.NewsModel;
import com.example.mvvmsdemo.moudle.bean.NewsBean;

import java.util.List;

//用来关联NewMoudle和view
public class NewsViewModel extends ViewModel implements IModel  {
    //列表大小,这个由于是直接显示，和ui逻辑没有什么关系，我们使用DataBinding给他绑定界面
    public ObservableField<String> listSize;

    //是否正在刷新,MutableLiveData这个类可以在被设置值的时候产生回调，我们用在Activity中监听
    public MutableLiveData<Boolean> isFlash;
    private NewsModel newsModel;

    public NewsViewModel(){
        listSize=new ObservableField<String>("0");
        newsModel=new NewsModel();
        isFlash=new MutableLiveData<>();
    }


    public List<NewsBean> getdata(){
        return newsModel.get();
    }

    //该回调为异步，所以不能刷新数据，你也可以自己处理
    public void load(Context context){
//        isFlash.setValue(true);
//        newsModel.load(context,this);
    }

    //刷新数据
    public void load(Handler handler){
        isFlash.setValue(true);
        newsModel.load(handler,this);
    }
    @Override
    public void success() {
        isFlash.setValue(false);
        listSize.set(newsModel.get().size()+"");
    }

    //这里假设我们只会加载成功，所以不处理加载错误的回调
    @Override
    public void fail() {

    }

}
