package main.baidumaplocation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;

import java.util.ArrayList;

import main.baidumaplocation.R;

/**
 * @Author Lino-082
 * @Date 2017/6/10 10:56
 */

public class MyAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<PoiItem> poiItemArrayList;
    private ImageView currentSelectItem;
    private int selectIndex;

    public MyAdapter(Context context, ArrayList<PoiItem> poiItemArrayList, ImageView currentSelectItem, int selectIndex) {
        mContext = context;
        this.poiItemArrayList = poiItemArrayList;
        this.currentSelectItem = currentSelectItem;
        this.selectIndex = selectIndex;
    }

    @Override
    public int getCount() {
        return poiItemArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return poiItemArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        //布局加载器
        LayoutInflater inflater = LayoutInflater.from(mContext);
        //加载location_item布局
        View view1 = inflater.inflate(R.layout.activity_baidu_poi_item, null);

        //修改文字和字体
        TextView v1 = (TextView) view1.findViewById(R.id.act_baidu_name);
        TextView v2 = (TextView) view1.findViewById(R.id.act_baidu_address);
        ImageView iv = (ImageView) view1.findViewById(R.id.checked_iv);
        v1.setText(poiItemArrayList.get(i).getTitle());

        v2.setText(poiItemArrayList.get(i).getSnippet());
        if (selectIndex == i) {
            iv.setVisibility(View.VISIBLE);
            currentSelectItem = iv;
        } else {
            iv.setVisibility(View.INVISIBLE);
        }
        return view1;
    }
}
