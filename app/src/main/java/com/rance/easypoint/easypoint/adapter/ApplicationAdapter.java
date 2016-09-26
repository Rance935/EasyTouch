package com.rance.easypoint.easypoint.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.rance.easypoint.easypoint.R;
import com.rance.easypoint.easypoint.model.ApkModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/9/21.
 */
public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private List<ApkModel> mApkModels;

    public ApplicationAdapter(Context context) {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_application, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final ApkModel mApkModel = mApkModels.get(position);
        mApkModel.setChecked(false);
        viewHolder.itemApplicationIcon.setImageDrawable(mApkModel.getApkIcon());
        viewHolder.itemApplicationName.setText(mApkModel.getAppName());
        viewHolder.itemApplicationVersion.setText(mApkModel.getVersionName());
        viewHolder.itemApplicationChose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mApkModel.setChecked(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mApkModels == null ? 0 : mApkModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mApkModels.get(position).getType();
    }

    /**
     * 为适配器添加数据
     * @param mApkModels
     */
    public void setDate(List<ApkModel> mApkModels){
        this.mApkModels = mApkModels;
        notifyDataSetChanged();
    }

    /**
     * 获得适配器数据
     */
    public List<ApkModel> getDate(){
        return mApkModels;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_application_icon)
        ImageView itemApplicationIcon;
        @BindView(R.id.item_application_name)
        TextView itemApplicationName;
        @BindView(R.id.item_application_version)
        TextView itemApplicationVersion;
        @BindView(R.id.item_application_chose)
        CheckBox itemApplicationChose;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
