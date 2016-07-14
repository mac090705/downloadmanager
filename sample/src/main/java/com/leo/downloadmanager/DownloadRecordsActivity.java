package com.leo.downloadmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leo.download.DownloadInfo;
import com.leo.download.DownloadManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadRecordsActivity extends AppCompatActivity {

    @BindView(R.id.recycler)
    RecyclerView mRecycler;

    private List<DownloadInfo> mRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_records);
        ButterKnife.bind(this);
        mRecords = DownloadManager.getInstance(this).queryAll();
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setAdapter(new RecordsAdapter());
    }

    public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ViewHolder> {


        @Override
        public RecordsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_records, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecordsAdapter.ViewHolder holder, int position) {
            DownloadInfo info = mRecords.get(position);
            holder.mUrlText.setText(info.getUrl());
            holder.mDirText.setText(info.getDir());
            holder.mNameText.setText(info.getName());
            holder.mSizeText.setText(String.valueOf(info.getTotalSize()));
            holder.mStatusText.setText(String.valueOf(info.getStatus()));
            holder.mStartText.setText(String.valueOf(info.getStartTime()));
            holder.mMimeType.setText(info.getMimetype());
        }

        @Override
        public int getItemCount() {
            return mRecords.size();
        }

        protected class ViewHolder extends RecyclerView.ViewHolder{
            @BindView(R.id.url_text)
            TextView mUrlText;
            @BindView(R.id.dir_text)
            TextView mDirText;
            @BindView(R.id.name_text)
            TextView mNameText;
            @BindView(R.id.size_text)
            TextView mSizeText;
            @BindView(R.id.status_text)
            TextView mStatusText;
            @BindView(R.id.start_text)
            TextView mStartText;
            @BindView(R.id.mime_type)
            TextView mMimeType;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }
}
