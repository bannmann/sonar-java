/*
 * SonarQube Java
 * Copyright (C) 2012-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks;

import java.util.EnumSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.BinaryExpressionTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.UnaryExpressionTree;

@Rule(key = "S9876543210")
public class BitwiseOperatorCheck extends BaseTreeVisitor implements JavaFileScanner {

  private static final Set<Tree.Kind> BITWISE_UNARY_OPERATORS = EnumSet.of(
    Tree.Kind.BITWISE_COMPLEMENT);

  private static final Set<Tree.Kind> BITWISE_BINARY_OPERATORS = EnumSet.of(
    Tree.Kind.AND,
    Tree.Kind.OR,
    Tree.Kind.XOR,
    Tree.Kind.LEFT_SHIFT,
    Tree.Kind.RIGHT_SHIFT,
    Tree.Kind.UNSIGNED_RIGHT_SHIFT);

  private static final Set<Tree.Kind> BITWISE_ASSIGNMENTS = EnumSet.of(
    Tree.Kind.LEFT_SHIFT_ASSIGNMENT,
    Tree.Kind.RIGHT_SHIFT_ASSIGNMENT,
    Tree.Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT,
    Tree.Kind.AND_ASSIGNMENT,
    Tree.Kind.XOR_ASSIGNMENT,
    Tree.Kind.OR_ASSIGNMENT);

  private static final Set<Tree.Kind> BOOLEAN_WHITELIST = EnumSet.of(
    Tree.Kind.AND,
    Tree.Kind.OR,
    Tree.Kind.XOR,
    Tree.Kind.AND_ASSIGNMENT,
    Tree.Kind.XOR_ASSIGNMENT,
    Tree.Kind.OR_ASSIGNMENT);

  private JavaFileScannerContext context;

  @Override
  public void scanFile(JavaFileScannerContext context) {
    this.context = context;
    scan(context.getTree());
  }

  @Override
  public void visitUnaryExpression(UnaryExpressionTree tree) {
    checkExpression(tree, BITWISE_UNARY_OPERATORS);
    super.visitUnaryExpression(tree);
  }

  @Override
  public void visitBinaryExpression(BinaryExpressionTree tree) {
    checkExpression(tree, BITWISE_BINARY_OPERATORS);
    super.visitBinaryExpression(tree);
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    checkExpression(tree, BITWISE_ASSIGNMENTS);
    super.visitAssignmentExpression(tree);
  }

  private void checkExpression(ExpressionTree tree, Set<Tree.Kind> bitwiseOperators) {
    Tree.Kind operator = tree.kind();
    if (bitwiseOperators.contains(operator) && !isWhitelisted(operator, tree)) {
      raiseIssue(tree);
    }
  }

  private boolean isWhitelisted(Tree.Kind operator, ExpressionTree tree) {
    return BOOLEAN_WHITELIST.contains(operator) && isBooleanContext(tree);
  }

  private boolean isBooleanContext(ExpressionTree tree) {
    Type symbolType = tree.symbolType();
    return symbolType.isPrimitive(Type.Primitives.BOOLEAN) || symbolType.is(Boolean.class.getCanonicalName());
  }

  private void raiseIssue(Tree tree) {
    context.reportIssue(this, getOperator(tree), "Avoid using bitwise operators.");
  }

  private Tree getOperator(Tree tree) {
    if (tree instanceof UnaryExpressionTree) {
      return ((UnaryExpressionTree)tree).operatorToken();
    }
    if (tree instanceof BinaryExpressionTree) {
      return ((BinaryExpressionTree)tree).operatorToken();
    }
    if (tree instanceof AssignmentExpressionTree) {
      return ((AssignmentExpressionTree)tree).operatorToken();
    }
    throw new IllegalStateException("Unexpected tree: " + tree.getClass().getSimpleName() + ".");
  }
}
