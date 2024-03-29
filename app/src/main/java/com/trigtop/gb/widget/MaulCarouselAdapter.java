package com.trigtop.gb.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.trigtop.gb.R;
import com.trigtop.gb.util.ContantUtil;

public class MaulCarouselAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private OnItemStateListener mListener;

    public MaulCarouselAdapter(Context context) {
        mContext = context;
    }

    public void setOnItemStateListener(OnItemStateListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(View.inflate(mContext, R.layout.item_recyclerview_maul, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final RecyclerViewHolder viewHolder = (RecyclerViewHolder) holder;
//        viewHolder.mName.setText(ContantUtil.TEST_DATAS[position]);
        viewHolder.mImageView.setImageDrawable(ContextCompat.getDrawable(mContext,
                ContantUtil.getImgResourceId(position)));
    }

    @Override
    public int getItemCount() {
        return ContantUtil.TEST_DATAS.length / 2;
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        FocusRelativeLayout mRelativeLayout;
        TextView mName;
        ImageView mImageView;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.tv_item_tip);
            mRelativeLayout = itemView.findViewById(R.id.fl_main_layout);
            mImageView = itemView.findViewById(R.id.iv_item);
            mRelativeLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public interface OnItemStateListener {
        void onItemClick(View view, int position);
    }
}
