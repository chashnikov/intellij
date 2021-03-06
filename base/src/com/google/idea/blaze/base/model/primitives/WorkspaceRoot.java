/*
 * Copyright 2016 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.base.model.primitives;

import com.google.idea.blaze.base.settings.Blaze;
import com.google.idea.blaze.base.settings.BlazeImportSettings;
import com.google.idea.blaze.base.settings.BlazeImportSettingsManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import java.io.Serializable;
import javax.annotation.Nullable;

/** Represents a workspace root */
public class WorkspaceRoot implements Serializable {
  public static final long serialVersionUID = 1L;

  private final File directory;

  public WorkspaceRoot(File directory) {
    this.directory = directory;
  }

  /**
   * Get the workspace root for a project
   *
   * @param blazeSettings settings for the project in question
   * @return the path to workspace root that is used for the project
   */
  public static WorkspaceRoot fromImportSettings(BlazeImportSettings blazeSettings) {
    return new WorkspaceRoot(new File(blazeSettings.getWorkspaceRoot()));
  }

  /**
   * Tries to load the import settings for the given project and get the workspace root directory.
   * <br>
   * Unlike {@link #fromProject}, it will silently return null if this is not a blaze project.
   */
  @Nullable
  public static WorkspaceRoot fromProjectSafe(Project project) {
    if (Blaze.isBlazeProject(project)) {
      return fromProject(project);
    }
    return null;
  }

  /**
   * Tries to load the import settings for the given project and get the workspace root directory.
   */
  public static WorkspaceRoot fromProject(Project project) {
    BlazeImportSettings importSettings =
        BlazeImportSettingsManager.getInstance(project).getImportSettings();
    if (importSettings == null) {
      throw new IllegalStateException("null BlazeImportSettings.");
    }
    return fromImportSettings(importSettings);
  }

  public File fileForPath(WorkspacePath workspacePath) {
    return new File(directory, workspacePath.relativePath());
  }

  public File directory() {
    return directory;
  }

  public WorkspacePath workspacePathFor(VirtualFile file) {
    return workspacePathFor(file.getPath());
  }

  public boolean isInWorkspace(VirtualFile file) {
    return isInWorkspace(file.getPath());
  }

  /**
   * Returns the WorkspacePath for the given absolute file, if it's a child of this WorkspaceRoot.
   * Otherwise returns null.
   */
  @Nullable
  public WorkspacePath workspacePathForSafe(File absoluteFile) {
    if (isInWorkspace(absoluteFile)) {
      return workspacePathFor(absoluteFile);
    }
    return null;
  }

  public WorkspacePath workspacePathFor(File file) {
    return workspacePathFor(file.getPath());
  }

  public boolean isInWorkspace(File file) {
    return isInWorkspace(file.getPath());
  }

  private WorkspacePath workspacePathFor(String path) {
    if (!isInWorkspace(path)) {
      throw new IllegalArgumentException(
          String.format("File '%s' is not under workspace %s", path, directory));
    }
    if (directory.getPath().length() == path.length()) {
      return new WorkspacePath("");
    }
    return new WorkspacePath(path.substring(directory.getPath().length() + 1));
  }

  private boolean isInWorkspace(String path) {
    return FileUtil.isAncestor(directory.getPath(), path, false);
  }

  @Override
  public String toString() {
    return directory.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    WorkspaceRoot that = (WorkspaceRoot) o;
    return directory.equals(that.directory);
  }

  @Override
  public int hashCode() {
    return directory.hashCode();
  }
}
