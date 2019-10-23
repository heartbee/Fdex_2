package com.example.fdex_20;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import de.robv.android.xposed.XSharedPreferences;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ListView lv_apps;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		lv_apps = (ListView) findViewById(R.id.lv_apps);
		lv_apps.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		PackageManager packageManager = getPackageManager();
		List<PackageInfo> infos = packageManager.getInstalledPackages(0);
		
		List<AppInfo> appInfos = new ArrayList<>();
		
		for(PackageInfo info : infos){
			if((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
				AppInfo i = new AppInfo();
				i.setName(info.applicationInfo.loadLabel(packageManager).toString());
				i.setPackageName(info.packageName);
				i.setVersion(info.versionName);
				i.setIcon(info.applicationInfo.loadIcon(packageManager));
				
				appInfos.add(i);
			}
		}
		
		final AppAdapter adapter = new AppAdapter(appInfos, this);
		
		lv_apps.setAdapter(adapter);
		
		lv_apps.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				AppInfo appInfo = (AppInfo) adapter.getItem(position);
				SharedPreferences preferences = getSharedPreferences(getPackageName()+"_preferences", MODE_WORLD_READABLE);
				preferences.edit().putString("packageName", appInfo.getPackageName()).commit();
				preferences.edit().putString("appName", appInfo.getName()).commit();
				Toast.makeText(getApplicationContext(), "dump " + appInfo.getName(), Toast.LENGTH_SHORT).show();
			}
		});
		
	}
	
	class AppAdapter extends BaseAdapter{
		
		private List<AppInfo> infos;
		private Context context;
		
		public AppAdapter(List<AppInfo> infos, Context context) {
			super();
			this.infos = infos;
			this.context = context;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return infos == null ? 0 : infos.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return infos.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(R.layout.item_app, null);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tv_pkgname = (TextView) convertView.findViewById(R.id.tv_pkgname);
				holder.tv_version = (TextView) convertView.findViewById(R.id.tv_version);
				holder.iv_app = (ImageView) convertView.findViewById(R.id.iv_app);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			AppInfo info = infos.get(position);
			
			holder.tv_name.setText(info.getName());
			holder.tv_pkgname.setText(info.getPackageName());
			holder.tv_version.setText(info.getVersion());
			holder.iv_app.setImageDrawable(info.getIcon());
			
			
			return convertView;
		}
		
		
		class ViewHolder {
			public TextView tv_name;
			public TextView tv_version;
			public TextView tv_pkgname;
			public ImageView iv_app;
		}
	}
}
