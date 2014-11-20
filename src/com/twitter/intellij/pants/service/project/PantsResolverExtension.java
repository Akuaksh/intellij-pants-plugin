// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.service.project;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.project.ModuleData;
import com.twitter.intellij.pants.service.project.model.ProjectInfo;

import java.util.Map;

public interface PantsResolverExtension {
  ExtensionPointName<PantsResolverExtension> EP_NAME = ExtensionPointName.create("com.intellij.plugins.pants.projectResolver");

  public void resolve(
    ProjectInfo projectInfo, Map<String, DataNode<ModuleData>> modules
  );
}
