# MVVMDemo
Mvvm架构的Demo

# 需求

写一个列表展示，具体直接clone下代码，跑一下

## 结构

![ZqwktK.png](https://s2.ax1x.com/2019/07/17/ZqwktK.png)

+ model包存放着数据bean和负责数据获取，存储的model，以及给VIewModel的回调
+ view_model包用于存放ViewModel
+ NewsAdapter是RV的适配器

这样的结构就是Mvvm，Model不与View直接交互，Model的数据改变的影响会在ViewModel中，由ViewModel进行通知和协调VIew，而VIew只处理UI相关的逻辑，当View想处理数据时，也是通过ViewModel，让ViewModel进行对Model的操作。

## Bean



~~~java

public class NewsBean {
    private String title;
    private String content;

    public NewsBean(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "NewsBean{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}

~~~

##　Model

这个我们负责去加载数据，并且和ViewModel通过接口交互

~~~java
//负责加载数据
public class NewsModel {
    private List<NewsBean> newsBeans = new ArrayList<>();

    //回调是同步的，你可以在这里传入Handler来处理同步回调，你也可以在ViewModel的回调处理，这里只是我懒
    public void load(final Handler handler, final IModel listence) {
        //模拟去异步网络请求数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 10; i++) {
                        NewsBean newsBean = new NewsBean(System.currentTimeMillis() + "", "内容");
                        newsBeans.add(newsBean);
                    }

                    Thread.sleep(2000);

                    //同步回调
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            listence.success();
                        }
                    };

                    handler.post(r);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //回调是异步的
    public void load(final Context context, final IModel listence) {
        //模拟去网络请求数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 10; i++) {
                        NewsBean newsBean = new NewsBean(System.currentTimeMillis() + "", "内容");
                        newsBeans.add(newsBean);
                    }

                    Thread.sleep(2000);


                    listence.success();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public List<NewsBean> get() {
        return newsBeans;
    }
}

~~~

## ViewModel

用来作为View和Model的中介

~~~java

//用来关联NewMoudle和view，可以看到实现了IModel接口，用来接收Model的回调
public class NewsViewModel extends ViewModel implements IModel  {
    //列表大小,这个由于是直接显示，和ui逻辑没有什么关系，我们使用DataBinding给他绑定界面
    public ObservableField<String> listSize;

    //是否正在刷新,MutableLiveData这个类可以在被设置值的时候产生回调，我们用在Activity中监听，并且他的生命周期会被和监听者绑定，所以不用担心内存泄露问题
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
    
    //这两个是IModel的回调
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

~~~



## View

~~~java
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
        
        //我们ViewModel有些字段是直接显示的，所以用DataBinding绑定
        binding.setNewsViewModel(newsViewModel);
        //这个是用来处理回调的
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

        //设置对VIewModel某个字段监听
        newsViewModel.isFlash.observe(this, flash);

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newsViewModel.load(handler);
            }
        });


        binding.rv.setLayoutManager(new LinearLayoutManager(this));
        //这一步你可以考虑在viewModel处理，也可以在这里处理
        adapter = new NewsAdapter(newsViewModel.getdata());

        binding.rv.setAdapter(adapter);

    }
}
~~~

# Adapter

~~~java

//直接显示的数据我们用DataBinding绑定
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
~~~

## 布局文件

~~~xml

<?xml version="1.0" encoding="utf-8"?>
<layout>
    <data>
        <variable
            name="newsViewModel"
            type="com.example.mvvmsdemo.view_model.NewsViewModel" />
    </data>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity"
    android:orientation="vertical">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text='@{"新闻共有："+newsViewModel.listSize+"条"}'
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <Button
        android:id="@+id/button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="刷新"/>


    <android.support.v7.widget.RecyclerView
        android:id="@+id/rv"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

    </android.support.v7.widget.RecyclerView>


</LinearLayout>
</layout>
~~~

item的布局文件

~~~xml
<?xml version="1.0" encoding="utf-8"?>
<layout>
    <data>
        <variable
            name="news"
            type="com.example.mvvmsdemo.model.bean.NewsBean" />
    </data>
<LinearLayout
    android:orientation="vertical"
    xmlns:android="http://schemas.android.com/apk/res/android" android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <TextView
        android:textSize="20sp"
        android:textColor="#000"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@{news.title}"/>

    <TextView
        android:layout_marginTop="10dp"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@{news.content}"/>

</LinearLayout>
</layout>
~~~

