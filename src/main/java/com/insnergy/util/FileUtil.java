package com.insnergy.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class FileUtil {
  
  public List<File> getFileListInFolder(final File folder) {
    return getFileListInFolder(folder, null);
  }
  
  private List<File> getFileListInFolder(final File folder, List<File> result) {
    if (result == null) {
      result = new ArrayList<File>();
    }
    if (folder != null) {
      for (final File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
          getFileListInFolder(fileEntry, result);
        } else {
          result.add(fileEntry);
        }
      }
    }
    return result;
  }
  
}
