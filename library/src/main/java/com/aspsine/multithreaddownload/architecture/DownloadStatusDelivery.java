package com.aspsine.multithreaddownload.architecture;

/**
 * Created by Aspsine on 2015/7/15.
 */
public interface DownloadStatusDelivery {

    void post(DownloadStatus status);

}
