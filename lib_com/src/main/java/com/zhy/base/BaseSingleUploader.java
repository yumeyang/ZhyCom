package com.zhy.base;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传类
 */
public abstract class BaseSingleUploader {
    private final AppCompatActivity mContext;

    private final List<String> mListPath = new ArrayList<>();
    private final List<String> mListUploaded = new ArrayList<>();
    private boolean mUploading;
    private Callback mCallback;

    public BaseSingleUploader(AppCompatActivity context) {
        mContext = context;
    }

    /**
     * 设置回调
     *
     * @param callback
     */
    public final void setCallback(Callback callback) {
        mCallback = callback;
    }

    protected final AppCompatActivity getContext() {
        return mContext;
    }

    /**
     * 上传文件
     *
     * @param paths
     * @return
     */
    public final boolean uploadFile(String... paths) {
        if (paths == null || paths.length <= 0)
            return false;

        return uploadFile(Arrays.asList(paths));
    }

    /**
     * 上传文件
     *
     * @param listPath
     */
    public final boolean uploadFile(List<String> listPath) {
        if (listPath == null || listPath.isEmpty())
            return false;

        if (mUploading)
            return false;

        mUploading = true;
        mListPath.clear();
        mListPath.addAll(listPath);
        mListUploaded.clear();

        final String path = mListPath.remove(0);
        uploadFileInternal(path);
        return true;
    }

    private void uploadFileInternal(String path) {
        if (TextUtils.isEmpty(path))
            throw new IllegalArgumentException("path is empty");

        final File file = new File(path);
        if (!file.exists())
            throw new IllegalArgumentException("file not exists:" + path);

        onUploadImpl(path);
    }

    /**
     * 子类上传实现，上传结果需要通知{@link #notifyUploadSuccess(String)}或者{@link #notifyUploadError(String)}
     *
     * @param path
     */
    protected abstract void onUploadImpl(String path);

    /**
     * 通知本次上传成功
     *
     * @param result
     */
    protected final void notifyUploadSuccess(String result) {
        mListUploaded.add(result);
        if (mListPath.isEmpty()) {
            notifyCallbackSuccess(new ArrayList<String>(mListUploaded));
        } else {
            final String path = mListPath.remove(0);
            uploadFileInternal(path);
        }
    }

    /**
     * 通知本次上传失败
     *
     * @param msg
     */
    protected final void notifyUploadError(final String msg) {
        notifyCallbackError(msg);
    }

    private void notifyCallbackSuccess(final List<String> listResult) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            mUploading = false;
            mCallback.onSuccess(listResult);
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    notifyCallbackSuccess(listResult);
                }
            });
        }
    }

    private final void notifyCallbackError(final String msg) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            mUploading = false;
            mCallback.onError(msg);
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    notifyCallbackError(msg);
                }
            });
        }
    }

    public interface Callback {
        /**
         * 上传成功
         *
         * @param listPath 上传结果
         */
        void onSuccess(List<String> listPath);

        /**
         * 上传失败
         *
         * @param msg
         */
        void onError(String msg);
    }
}
