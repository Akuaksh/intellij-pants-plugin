// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.service.scala;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.project.ModuleData;
import com.intellij.openapi.util.text.StringUtil;
import com.twitter.intellij.pants.service.project.PantsResolverExtension;
import com.twitter.intellij.pants.service.project.model.ProjectInfo;
import com.twitter.intellij.pants.service.project.model.TargetInfo;
import com.twitter.intellij.pants.util.PantsConstants;
import com.twitter.intellij.pants.util.PantsScalaUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class ScalaFacetResolver implements PantsResolverExtension {
  private static final Logger LOG = Logger.getInstance(ScalaFacetResolver.class);

  @Override
  public void resolve(
    ProjectInfo projectInfo, Map<String, DataNode<ModuleData>> modules
  ) {
    final List<String> scalaJars = new ArrayList<String>();
    for (String libId : projectInfo.getLibraries().keySet()) {
      if (PantsScalaUtil.isScalaLib(libId)) {
        scalaJars.addAll(projectInfo.getLibraries(libId));
      }
    }

    for (Map.Entry<String, TargetInfo> entry : projectInfo.getTargets().entrySet()) {
      final String mainTarget = entry.getKey();
      final TargetInfo targetInfo = entry.getValue();
      final DataNode<ModuleData> moduleDataNode = modules.get(mainTarget);
      if (moduleDataNode == null) {
        continue; // shouldn't happened because we created all modules for each target
      }
      if (targetInfo.is_scala()) {
        // todo(fkorotkov): provide Scala info from the goal
        createScalaFacetFromJars(moduleDataNode, scalaJars);
      }
    }
  }

  private void createScalaFacetFromJars(@NotNull DataNode<ModuleData> moduleDataNode, List<String> scalaLibJars) {
    final ScalaModelData scalaModelData = new ScalaModelData(PantsConstants.SYSTEM_ID);
    final Set<File> files = new HashSet<File>();
    for (String jarPath : scalaLibJars) {
      for (String scalaLibNameToAdd : PantsScalaUtil.getScalaLibNamesToAdd()) {
        findAndAddScalaLib(files, jarPath, scalaLibNameToAdd);
      }
    }
    if (!files.isEmpty()) {
      scalaModelData.setScalaCompilerJars(files);
      moduleDataNode.createChild(ScalaModelData.KEY, scalaModelData);
    }
  }

  private void findAndAddScalaLib(Set<File> files, String jarPath, String libName) {
    final File compilerFile = new File(StringUtil.replace(jarPath, "scala-library", libName));
    if (compilerFile.exists()) {
      files.add(compilerFile);
    }
    else {
      LOG.warn("Could not find scala library path" + compilerFile);
    }
  }
}
