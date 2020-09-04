package com.zhy.base;

import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public abstract class BaseMultiUploader {
    private final AppCompatActivity mContext;

    private final Map<Integer, String> mListPath = new LinkedHashMap<>();
    private final Map<Integer, String> mListUploaded = new LinkedHashMap<>();
    private final Map<Integer, Long> mListProgress = new LinkedHashMap<>();

    private boolean mUploading;
    private Callback mCallback;
    private long mTotalFilesSize;

    public BaseMultiUploader(AppCompatActivity context) {
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
     * @param listPath 文件路径
     */
    public final boolean uploadFile(List<String> listPath) {
        if (listPath == null || listPath.isEmpty())
            return false;

        if (mUploading)
            return false;

        mUploading = true;
        mListPath.clear();
        mListUploaded.clear();
        uploadFileInternal(listPath);
        return true;
    }

    private void uploadFileInternal(List<String> paths) {
        if (paths == null || paths.size() == 0)
            throw new IllegalArgumentException("path is empty");

        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);
            File file = new File(path);
            if (!file.exists()) {
                throw new IllegalArgumentException("file not exists:" + path);
            }

            mListPath.put(i, path);
            mListUploaded.put(i, "");
            mTotalFilesSize += file.length();
        }

        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);
            UploadProxy impl = implUploadProxy();
            impl.setTag(i);
            impl.onUploadImpl(path);
        }
    }

    protected abstract UploadProxy implUploadProxy();

    /**
     * 通知本次上传成功
     *
     * @param result 返回结果
     */
    protected final void notifyUploadSuccess(UploadProxy impl, String result) {
        mListUploaded.put(impl.getTag(), result);
        mListPath.remove(impl.getTag());
        if (mListPath.size() == 0) {
            ArrayList<String> list = new ArrayList<>(mListUploaded.values());
            notifyCallbackSuccess(list);
        }
    }

    protected final void notifyUploadProgress(int tag, long bytesWrite, long contentLength) {
        mListProgress.put(tag, bytesWrite);
        long cur_progress = 0;
        for (Long progress : mListProgress.values()) {
            cur_progress += progress;
        }
        mCallback.onProgress(cur_progress, mTotalFilesSize);
    }

    /**
     * 通知本次上传失败
     *
     * @param msg 错误消息
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

    private void notifyCallbackError(final String msg) {
        if (!mUploading) {
            //不通知
            return;
        }
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

        /**
         * 上传进度
         *
         * @param progress       当前进度
         * @param total_progress 总进度
         */
        void onProgress(long progress, long total_progress);
    }

    public abstract static class UploadProxy {

        private int tag;

        public UploadProxy() {

        }

        public void setTag(int tag) {
            this.tag = tag;
        }

        public int getTag() {
            return tag;
        }

        public UploadProxy get() {
            return this;
        }

        protected abstract void onUploadImpl(String path);
    }
}
