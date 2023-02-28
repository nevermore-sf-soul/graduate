/*
 *
 *   Copyright 2012-2013 University Of Southern California
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.workflowsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.workflowsim.FileItem;

/**
 * ReplicaCatalog stores all the data information and where (site) there are
 *
 * @author Weiwei Chen
 * @since WorkflowSim Toolkit 1.0
 * @date Apr 9, 2013
 */
public class myreplicalog {
    /**
     * Map from file name to a file object
     */
    private static Map<String, FileItem> fileName2File;
    /**
     * Map from file to a list of data storage
     */
    private static Map<String, List<String>> dataReplicaCatalog;


    public myreplicalog()
    {
        dataReplicaCatalog = new HashMap<>();
        fileName2File = new HashMap<>();
    }

    /**
     * Gets the file object based its file name
     *
     * @param fileName, file name
     * @return file object
     */
    public  FileItem getFile(String fileName) {
        return fileName2File.get(fileName);
    }

    /**
     * Adds a file name and the associated file object
     *
     * @param fileName, the file name
     * @param file , the file object
     */
    public  void setFile(String fileName, FileItem file) {
        fileName2File.put(fileName, file);
    }

    /**
     * Checks whether a file exists
     *
     * @param fileName file name
     * @return boolean, whether the file exist
     */
    public  boolean containsFile(String fileName) {
        return fileName2File.containsKey(fileName);
    }

}
