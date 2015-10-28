package com.aspsine.multithreaddownload.core;

import android.net.Uri;

/**
 * Created by Aspsine on 2015/4/20.
 */
public class DownloadRequest {
    private Uri mUri;

    private Uri mDestinationUri;

    private CharSequence mTitle;

    private CharSequence mDescription;

    private boolean mScannable;

    private DownloadRequest() {
    }

    private DownloadRequest(Uri uri, Uri destinationUri, CharSequence title, CharSequence description, boolean scannable) {
        this.mUri = uri;
        this.mDestinationUri = destinationUri;
        this.mTitle = title;
        this.mDescription = description;
        this.mScannable = scannable;
    }

    public Uri getUri() {
        return mUri;
    }

    public Uri getDestinationUri() {
        return mDestinationUri;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public CharSequence getDescription() {
        return mDescription;
    }

    public boolean isScannable() {
        return mScannable;
    }

    public static class Builder {

        private Uri mUri;

        private Uri mDestinationUri;

        private CharSequence mTitle;

        private CharSequence mDescription;

        private boolean mScannable;

        public Builder() {
        }

        public Builder setUri(Uri uri) {
            this.mUri = uri;
            return this;
        }

        public Builder setDestinationUri(Uri destinationUri) {
            this.mDestinationUri = destinationUri;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.mTitle = title;
            return this;
        }

        public Builder setDescription(CharSequence description) {
            this.mDescription = description;
            return this;
        }

        public Builder setScannable(boolean scannable) {
            this.mScannable = scannable;
            return this;
        }

        public DownloadRequest build() {
            DownloadRequest request = new DownloadRequest(mUri, mDestinationUri, mTitle, mDescription, mScannable);
            return request;
        }
    }
}
