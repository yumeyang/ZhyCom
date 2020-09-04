package com.zhy.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zhy.ZRecycleEasyAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * yhz on 2020/8/21
 * BaseSimpleEasyAdapter 单布局使用(getItemViewType()只有一种的时候使用)
 */
public abstract class BaseSimpleEasyAdapter<T, VH extends RecyclerView.ViewHolder> extends ZRecycleEasyAdapter {

    protected ArrayList<T> mList = new ArrayList<T>();

    @Override
    public void whenBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindViewHolder((VH) holder, position, mList.get(position));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(), null);
        return getViewHolder(view);
    }

    protected abstract void bindViewHolder(VH holder, int position, T model);

    /**
     * 生成泛型ViewHolder
     *
     * @param view
     * @return
     */
    protected VH getViewHolder(View view) {
        Type type = getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) type;
        Class modelClass = (Class) pt.getActualTypeArguments()[1];
        try {
            Constructor con = modelClass.getConstructor(View.class);
            return (VH) con.newInstance(view);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    /**
     * 设置数据
     *
     * @param list
     */
    public void setData(List<T> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public ArrayList<T> getList() {
        return mList;
    }

    protected abstract int getLayoutId();
}
