package DelFolderPack;

import java.io.*;

public class DeleteFolder {
/**
 * Delete a folder and all content folder & files.
 * @param folder
 */
  public void rmdir(final File folder) {     
      if (folder.isDirectory()) {           // Check if 'folder' is a real folder
          File[] list = folder.listFiles(); // Storing all file names in an array
          if (list != null) {               // Checking list value is null or not to check folder containts atlest one file
              for (int i = 0; i < list.length; i++) {    
                  File tmpF = list[i];
                  if (tmpF.isDirectory()) {   // if another folder is found within this folder, remove that folder recursively
                      rmdir(tmpF);
                  }
                  tmpF.delete();             // delete this file or folder (after deleting its possible contents)
              }
          }
          if (!folder.delete()) {            // if not able to delete folder print message
            System.out.println("can't delete folder : " + folder);
          }
      }
  }
}


