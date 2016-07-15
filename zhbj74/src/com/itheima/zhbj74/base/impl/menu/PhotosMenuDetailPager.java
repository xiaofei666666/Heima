package com.itheima.zhbj74.base.impl.menu;

import java.util.ArrayList;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itheima.zhbj74.R;
import com.itheima.zhbj74.base.BaseMenuDetailPager;
import com.itheima.zhbj74.domain.PhotosBean;
import com.itheima.zhbj74.domain.PhotosBean.PhotoNews;
import com.itheima.zhbj74.global.GlobalConstants;
import com.itheima.zhbj74.utils.CacheUtils;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * 菜单详情页-组图
 * 
 * @author Kevin
 */
public class PhotosMenuDetailPager extends BaseMenuDetailPager implements
		OnClickListener {

	@ViewInject(R.id.lv_photo)
	private ListView lvPhoto;
	@ViewInject(R.id.gv_photo)
	private GridView gvPhoto;

	private ArrayList<PhotoNews> mNewsList;

	private ImageButton btnPhoto;

	public PhotosMenuDetailPager(Activity activity, ImageButton btnPhoto) {
		super(activity);
		btnPhoto.setOnClickListener(this);// 组图切换按钮设置点击事件
		this.btnPhoto = btnPhoto;
	}

	@Override
	public View initView() {
		View view = View.inflate(mActivity, R.layout.pager_photos_menu_detail, null);
		ViewUtils.inject(this, view);
		return view;
	}

	@Override
	public void initData() {
		String cache = CacheUtils.getCache(GlobalConstants.PHOTOS_URL, mActivity);
		
		if (!TextUtils.isEmpty(cache)) {
			//直接加载缓存
			processData(cache);
		}
		
		getDataFromServer();
	}

	private void getDataFromServer() {
		
		HttpUtils utils = new HttpUtils();
		
		utils.send(HttpMethod.GET, GlobalConstants.PHOTOS_URL, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				//请求成功
				String result = responseInfo.result;
				//解析数据
				processData(result);
				//保存缓存
				CacheUtils.setCache(GlobalConstants.PHOTOS_URL, result, mActivity);
				
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				// 请求失败
				error.printStackTrace();
				Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
				
			}
		});
		
	}

	protected void processData(String result) {
		
		Gson gson = new Gson();
		PhotosBean photosBean = gson.fromJson(result, PhotosBean.class);
		
		mNewsList = photosBean.data.news;
		
		lvPhoto.setAdapter(new PhotoAdapter());
		gvPhoto.setAdapter(new PhotoAdapter());// gridview的布局结构和listview完全一致,
												// 所以可以共用一个adapter
		
	}

	class PhotoAdapter extends BaseAdapter {
		
		private BitmapUtils mBitmapUtils;
		
		public PhotoAdapter(){
			mBitmapUtils = new BitmapUtils(mActivity);
			//设置默认图片
			mBitmapUtils.configDefaultLoadingImage(R.drawable.pic_item_list_default);
		}

		@Override
		public int getCount() {
			return mNewsList.size();
		}

		@Override
		public PhotoNews getItem(int position) {
			return mNewsList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View view;
			ViewHolder viewHolder;
			
			if (convertView == null) {
				view = View.inflate(mActivity, R.layout.list_item_photos, null);
				viewHolder = new ViewHolder();
				viewHolder.ivPic = (ImageView) view.findViewById(R.id.iv_pic);
				viewHolder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
				view.setTag(viewHolder);
				
			}else {
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}
			
			
			PhotoNews photoNews = mNewsList.get(position);
			
			viewHolder.tvTitle.setText(photoNews.title);
			mBitmapUtils.display(viewHolder.ivPic, photoNews.listimage);
			
			return view;
		}


	}

	static class ViewHolder {
		public ImageView ivPic;
		public TextView tvTitle;
	}

	private boolean isListView = true;// 标记当前是否是listview展示

	@Override
	public void onClick(View v) {
		if (isListView) {
			// 切成gridview
			lvPhoto.setVisibility(View.GONE);
			gvPhoto.setVisibility(View.VISIBLE);
			btnPhoto.setImageResource(R.drawable.icon_pic_list_type);

			isListView = false;
		} else {
			// 切成listview
			lvPhoto.setVisibility(View.VISIBLE);
			gvPhoto.setVisibility(View.GONE);
			btnPhoto.setImageResource(R.drawable.icon_pic_grid_type);

			isListView = true;
		}
	}

}
