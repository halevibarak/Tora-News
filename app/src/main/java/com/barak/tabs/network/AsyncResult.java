package com.barak.tabs.network;

import com.barak.tabs.model.ChangeLogDialog;

/**
 * Created by kstanoev on 1/14/2015.
 */
public interface AsyncResult
{
    void onResult(ChangeLogDialog changeLogDialog);
}