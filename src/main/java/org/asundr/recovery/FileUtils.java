/*
 * Copyright (c) 2025, Arun <trade-tracker-plugin.acwel@dralias.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.asundr.recovery;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;

class FileUtils
{
    final static String FILE_EXTENSION_PROFILE_HISTORY = "rtth";
    final static String FILE_DESCRIPTION_PROFILE_HISTORY = "RuneLite Trade Tracker history";

    public static void writeStringToFile(final String defaultFileName, final String str)
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(defaultFileName + "." + FILE_EXTENSION_PROFILE_HISTORY));
        final FileNameExtensionFilter filter = new FileNameExtensionFilter(FILE_DESCRIPTION_PROFILE_HISTORY, FILE_EXTENSION_PROFILE_HISTORY);
        fileChooser.setFileFilter(filter);
        final int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
            final File selectedFile = fileChooser.getSelectedFile();
            try (final BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile)))
            {
                writer.write(str);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String readStringFromFile(final String defaultFileName)
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(defaultFileName + "." + FILE_EXTENSION_PROFILE_HISTORY));
        final FileNameExtensionFilter filter = new FileNameExtensionFilter(FILE_DESCRIPTION_PROFILE_HISTORY, FILE_EXTENSION_PROFILE_HISTORY);
        fileChooser.setFileFilter(filter);
        final int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = fileChooser.getSelectedFile();
            final StringBuilder jsonString = new StringBuilder();
            try (final BufferedReader reader = new BufferedReader(new FileReader(selectedFile)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    jsonString.append(line);
                }
                return jsonString.toString();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
