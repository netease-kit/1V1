// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import android.content.res.AssetManager;
import com.netease.yunxin.kit.alog.ALog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetUtils {
  private static final String TAG = "AssetUtils";

  public static int copyAsset(AssetManager manager, String src, String dest, boolean overwrite) {
    if (manager == null) {
      ALog.e(TAG, "AssetManager is null");
      return -1;
    }

    File outfile = new File(dest);
    if (outfile.isDirectory()) {
      ALog.e(TAG, "copyAsset failed, " + dest + " is a director, use copyAssetRecursive");
      return -1;
    }

    if (!overwrite && outfile.exists()) {
      ALog.i(TAG, dest + " is exist, and not overwrite mode, skip");
      return 0;
    }

    InputStream input = null;
    OutputStream output = null;
    try {
      input = manager.open(src);
      output = new FileOutputStream(outfile);
      byte[] buf = new byte[1024];
      int read;
      while ((read = input.read(buf)) != -1) {
        output.write(buf, 0, read);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (input != null) {
          input.close();
        }
        if (output != null) {
          output.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return 0;
  }

  public static int copyAssetRecursive(
      AssetManager manager, String src, String dest, boolean overwrite) {
    int ret = 0;
    if (manager == null) {
      ALog.e(TAG, "AssetManager is null");
      ret = -1;
      return ret;
    }

    try {
      String[] list = manager.list(src);
      if (list.length == 0) {
        copyAsset(manager, src, dest, overwrite);
      } else {
        File file = new File(dest);
        boolean deleteResult = file.delete();
        ALog.d(TAG, "deleteResult = " + deleteResult);
        boolean mkdirResult = file.mkdir();
        ALog.d(TAG, "mkdirResult = " + mkdirResult);
        for (String path : list) {
          copyAssetRecursive(
              manager, src + File.separator + path, dest + File.separator + path, overwrite);
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    return ret;
  }
}
