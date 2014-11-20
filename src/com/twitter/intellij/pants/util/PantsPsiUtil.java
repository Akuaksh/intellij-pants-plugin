// Copyright 2014 Pants project contributors (see CONTRIBUTORS.md).
// Licensed under the Apache License, Version 2.0 (see LICENSE).

package com.twitter.intellij.pants.util;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.openapi.util.text.StringUtil.unquoteString;

public class PantsPsiUtil {

  public static List<Target> findTargets(@Nullable PsiFile file) {
    if (file == null) {
      return Collections.emptyList();
    }
    final List<Target> targets = new ArrayList<Target>();
    for (PyExpressionStatement statement : PsiTreeUtil.findChildrenOfType(file, PyExpressionStatement.class)) {
      final Target target = findTarget(statement);
      if (target != null) {
        targets.add(target);
      }
    }
    return targets;
  }

  @Nullable
  public static Target findTarget(@NotNull PyExpressionStatement statement) {
    final PyCallExpression expression = PsiTreeUtil.findChildOfType(statement, PyCallExpression.class);
    final PyExpression callee = expression != null ? expression.getCallee() : null;
    final PyArgumentList argumentList = expression != null ? expression.getArgumentList() : null;
    final PyKeywordArgument nameArgument = argumentList != null ? argumentList.getKeywordArgument("name") : null;
    final PyExpression valueExpression = nameArgument != null ? nameArgument.getValueExpression() : null;
    if (valueExpression != null && callee != null) {
      return new Target(unquoteString(valueExpression.getText()), callee.getText(), expression);
    }
    return null;
  }

  @NotNull
  public static Map<String, PyReferenceExpression> findTargetDefinitions(@NotNull PyFile pyFile) {
    final PyFunction buildFileAliases = pyFile.findTopLevelFunction("build_file_aliases");
    final PyStatement[] statements =
      buildFileAliases != null ? buildFileAliases.getStatementList().getStatements() : PyStatement.EMPTY_ARRAY;
    final Map<String, PyReferenceExpression> result = new HashMap<String, PyReferenceExpression>();
    for (PyStatement statement : statements) {
      if (!(statement instanceof PyReturnStatement)) {
        continue;
      }
      final PyExpression returnExpression = ((PyReturnStatement)statement).getExpression();
      if (!(returnExpression instanceof PyCallExpression)) {
        continue;
      }
      final PyArgumentList argumentList = ((PyCallExpression)returnExpression).getArgumentList();
      final Collection<PyKeywordArgument> targetDefinitions = PsiTreeUtil.findChildrenOfType(argumentList, PyKeywordArgument.class);
      for (PyKeywordArgument targets : targetDefinitions) {
        final PyExpression targetsExpression = targets != null ? targets.getValueExpression() : null;
        if (targetsExpression instanceof PyDictLiteralExpression) {
          for (PyKeyValueExpression keyValueExpression : ((PyDictLiteralExpression)targetsExpression).getElements()) {
            final PyExpression keyExpression = keyValueExpression.getKey();
            final PyExpression valueExpression = keyValueExpression.getValue();
            if (keyExpression instanceof PyStringLiteralExpression) {
              result.put(
                ((PyStringLiteralExpression)keyExpression).getStringValue(),
                valueExpression instanceof PyReferenceExpression ? (PyReferenceExpression)valueExpression : null
              );
            }
          }
        }
      }
    }
    return result;
  }
}