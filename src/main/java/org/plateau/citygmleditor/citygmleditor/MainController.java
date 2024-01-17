/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.plateau.citygmleditor.citygmleditor;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ResourceBundle;

import org.plateau.citygmleditor.importers.gml.GmlImporter;

import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

/**
 * ToolbarController class for main fxml file.
 */
public class MainController implements Initializable {
    public VBox centerPanel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ドロップによるGMLインポート
        centerPanel.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                boolean hasSupportedFile = false;
                fileLoop: for (File file : db.getFiles()) {
                    if (file.getName().matches(".*\\.gml")) {
                        hasSupportedFile = true;
                        break fileLoop;
                    }
                }
                if (hasSupportedFile)
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        centerPanel.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File supportedFile = null;
                fileLoop: for (File file : db.getFiles()) {
                    if (file.getName().matches(".*\\.gml")) {
                        supportedFile = file;
                        break fileLoop;
                    }
                }
                if (supportedFile != null) {
                    // workaround for RT-30195
                    if (supportedFile.getAbsolutePath().indexOf('%') != -1) {
                        supportedFile = new File(URLDecoder.decode(supportedFile.getAbsolutePath()));
                    }
                    try {
                        Node root = GmlImporter.loadGml(supportedFile.toString());
                        CityGMLEditorApp.getSceneContent().setContent(root);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}
