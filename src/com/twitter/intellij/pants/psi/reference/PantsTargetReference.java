// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.psi.reference;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.twitter.intellij.pants.util.PantsPsiUtil;
import com.twitter.intellij.pants.util.PantsUtil;
import com.twitter.intellij.pants.util.Target;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PantsTargetReference extends PantsPsiReferenceBase {
  public PantsTargetReference(@NotNull PsiElement element, @NotNull TextRange range, @Nls String text, @Nls String relativePath) {
    super(element, range, text, relativePath);
  }

  @Nullable
  private PsiFile findBuildFile() {
    if (StringUtil.isEmpty(getRelativePath())) {
      // same file reference
      return getElement().getContainingFile();
    }
    final VirtualFile buildFile = PantsUtil.findBUILDFile(findFile());
    if (buildFile == null) {
      return null;
    }
    final PsiManager psiManager = PsiManager.getInstance(getElement().getProject());
    return psiManager.findFile(buildFile);
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return ContainerUtil.map2Array(
      PantsPsiUtil.findTargets(findBuildFile()),
      new Function<Target, Object>() {
        @Override
        public Object fun(Target target) {
          return LookupElementBuilder.create(target.getName());
        }
      }
    );
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    final Target target = ContainerUtil.find(
      PantsPsiUtil.findTargets(findBuildFile()),
      new Condition<Target>() {
        @Override
        public boolean value(Target target) {
          return StringUtil.equalsIgnoreCase(getText(), target.getName());
        }
      }
    );
    return target != null ? target.getExpression() : null;
  }
}
