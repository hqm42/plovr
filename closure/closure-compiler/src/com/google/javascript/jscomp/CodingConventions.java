/*
 * Copyright 2011 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.CodingConvention.Bind;
import com.google.javascript.jscomp.CodingConvention.ObjectLiteralCast;
import com.google.javascript.jscomp.CodingConvention.SubclassRelationship;
import com.google.javascript.jscomp.CodingConvention.SubclassType;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Helper classes for dealing with coding conventions.
 */
public class CodingConventions {

  private CodingConventions() {}

  /** Gets the default coding convention. */
  public static CodingConvention getDefault() {
    return new DefaultCodingConvention();
  }

  /**
   * A convention that wraps another.
   *
   * When you want to support a new library, you should subclass this
   * delegate, and override the methods that you want to customize.
   *
   * This way, a person using jQuery and Closure Library can create a new
   * coding convention by creating a jQueryCodingConvention that delegates
   * to a ClosureCodingConvention that delegates to a DefaultCodingConvention.
   */
  public static class Proxy implements CodingConvention {

    protected final CodingConvention nextConvention;

    protected Proxy(CodingConvention convention) {
      this.nextConvention = convention;
    }

    @Override
    public boolean isConstant(String variableName) {
      return nextConvention.isConstant(variableName);
    }

    @Override public boolean isConstantKey(String keyName) {
      return nextConvention.isConstantKey(keyName);
    }

    @Override
    public boolean isValidEnumKey(String key) {
      return nextConvention.isValidEnumKey(key);
    }

    @Override
    public boolean isOptionalParameter(Node parameter) {
      return nextConvention.isOptionalParameter(parameter);
    }

    @Override
    public boolean isVarArgsParameter(Node parameter) {
      return nextConvention.isVarArgsParameter(parameter);
    }

    @Override
    public boolean isExported(String name, boolean local) {
      return nextConvention.isExported(name, local);
    }


    @Override
    public final boolean isExported(String name) {
      return isExported(name, false) || isExported(name, true);
    }

    @Override
    public boolean isPrivate(String name) {
      return nextConvention.isPrivate(name);
    }

    @Override
    public SubclassRelationship getClassesDefinedByCall(Node callNode) {
      return nextConvention.getClassesDefinedByCall(callNode);
    }

    @Override
    public boolean isSuperClassReference(String propertyName) {
      return nextConvention.isSuperClassReference(propertyName);
    }

    @Override
    public String extractClassNameIfProvide(Node node, Node parent) {
      return nextConvention.extractClassNameIfProvide(node, parent);
    }

    @Override
    public String extractClassNameIfRequire(Node node, Node parent) {
      return nextConvention.extractClassNameIfRequire(node, parent);
    }

    @Override
    public String getExportPropertyFunction() {
      return nextConvention.getExportPropertyFunction();
    }

    @Override
    public String getExportSymbolFunction() {
      return nextConvention.getExportSymbolFunction();
    }

    @Override
    public List<String> identifyTypeDeclarationCall(Node n) {
      return nextConvention.identifyTypeDeclarationCall(n);
    }

    @Override
    public void applySubclassRelationship(FunctionType parentCtor,
        FunctionType childCtor, SubclassType type) {
      nextConvention.applySubclassRelationship(
          parentCtor, childCtor, type);
    }

    @Override
    public String getAbstractMethodName() {
      return nextConvention.getAbstractMethodName();
    }

    @Override
    public String getSingletonGetterClassName(Node callNode) {
      return nextConvention.getSingletonGetterClassName(callNode);
    }

    @Override
    public void applySingletonGetter(FunctionType functionType,
        FunctionType getterType, ObjectType objectType) {
      nextConvention.applySingletonGetter(
          functionType, getterType, objectType);
    }

    @Override
    public DelegateRelationship getDelegateRelationship(Node callNode) {
      return nextConvention.getDelegateRelationship(callNode);
    }


    @Override
    public void applyDelegateRelationship(
        ObjectType delegateSuperclass, ObjectType delegateBase,
        ObjectType delegator, FunctionType delegateProxy,
        FunctionType findDelegate) {
      nextConvention.applyDelegateRelationship(
          delegateSuperclass, delegateBase, delegator,
          delegateProxy, findDelegate);
    }

    @Override
    public String getDelegateSuperclassName() {
      return nextConvention.getDelegateSuperclassName();
    }

    @Override
    public void checkForCallingConventionDefiningCalls(
        Node n, Map<String, String> delegateCallingConventions) {
      nextConvention.checkForCallingConventionDefiningCalls(
          n, delegateCallingConventions);
    }

    @Override
    public void defineDelegateProxyPrototypeProperties(
        JSTypeRegistry registry, Scope scope,
        List<ObjectType> delegateProxyPrototypes,
        Map<String, String> delegateCallingConventions) {
      nextConvention.defineDelegateProxyPrototypeProperties(
          registry, scope, delegateProxyPrototypes, delegateCallingConventions);
    }

    @Override
    public String getGlobalObject() {
      return nextConvention.getGlobalObject();
    }

    @Override
    public Collection<AssertionFunctionSpec> getAssertionFunctions() {
      return nextConvention.getAssertionFunctions();
    }

    @Override
    public Bind describeFunctionBind(Node n) {
      return nextConvention.describeFunctionBind(n);
    }

    @Override
    public boolean isPropertyTestFunction(Node call) {
      return nextConvention.isPropertyTestFunction(call);
    }

    @Override
    public ObjectLiteralCast getObjectLiteralCast(NodeTraversal t,
        Node callNode) {
      return nextConvention.getObjectLiteralCast(t, callNode);
    }
  }


  /**
   * The default coding convention.
   * Should be at the bottom of all proxy chains.
   */
  private static class DefaultCodingConvention implements CodingConvention {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isConstant(String variableName) {
      return false;
    }

    @Override
    public boolean isConstantKey(String variableName) {
      return false;
    }

    @Override
    public boolean isValidEnumKey(String key) {
      return key != null && key.length() > 0;
    }

    @Override
    public boolean isOptionalParameter(Node parameter) {
      // be as lax as possible, but this must be mutually exclusive from
      // var_args parameters.
      return !isVarArgsParameter(parameter);
    }

    @Override
    public boolean isVarArgsParameter(Node parameter) {
      // be as lax as possible
      return parameter.getParent().getLastChild() == parameter;
    }

    @Override
    public boolean isExported(String name, boolean local) {
      return local && name.startsWith("$super");
    }

    @Override
    public boolean isExported(String name) {
      return isExported(name, false) || isExported(name, true);
    }

    @Override
    public boolean isPrivate(String name) {
      return false;
    }

    @Override
    public SubclassRelationship getClassesDefinedByCall(Node callNode) {
      return null;
    }

    @Override
    public boolean isSuperClassReference(String propertyName) {
      return false;
    }

    @Override
    public String extractClassNameIfProvide(Node node, Node parent) {
      String message = "only implemented in GoogleCodingConvention";
      throw new UnsupportedOperationException(message);
    }

    @Override
    public String extractClassNameIfRequire(Node node, Node parent) {
      String message = "only implemented in GoogleCodingConvention";
      throw new UnsupportedOperationException(message);
    }

    @Override
    public String getExportPropertyFunction() {
      return null;
    }

    @Override
    public String getExportSymbolFunction() {
      return null;
    }

    @Override
    public List<String> identifyTypeDeclarationCall(Node n) {
      return null;
    }

    @Override
    public void applySubclassRelationship(FunctionType parentCtor,
        FunctionType childCtor, SubclassType type) {
      // do nothing
    }

    @Override
    public String getAbstractMethodName() {
      return null;
    }

    @Override
    public String getSingletonGetterClassName(Node callNode) {
      return null;
    }

    @Override
    public void applySingletonGetter(FunctionType functionType,
        FunctionType getterType, ObjectType objectType) {
      // do nothing.
    }

    @Override
    public DelegateRelationship getDelegateRelationship(Node callNode) {
      return null;
    }

    @Override
    public void applyDelegateRelationship(
        ObjectType delegateSuperclass, ObjectType delegateBase,
        ObjectType delegator, FunctionType delegateProxy,
        FunctionType findDelegate) {
      // do nothing.
    }

    @Override
    public String getDelegateSuperclassName() {
      return null;
    }

    @Override
    public void checkForCallingConventionDefiningCalls(Node n,
        Map<String, String> delegateCallingConventions) {
      // do nothing.
    }

    @Override
    public void defineDelegateProxyPrototypeProperties(
        JSTypeRegistry registry, Scope scope,
        List<ObjectType> delegateProxyPrototypes,
        Map<String, String> delegateCallingConventions) {
      // do nothing.
    }

    @Override
    public String getGlobalObject() {
      return "window";
    }

    @Override
    public boolean isPropertyTestFunction(Node call) {
      return false;
    }

    @Override
    public ObjectLiteralCast getObjectLiteralCast(NodeTraversal t,
        Node callNode) {
      return null;
    }

    @Override
    public Collection<AssertionFunctionSpec> getAssertionFunctions() {
      return Collections.emptySet();
    }

    @Override
    public Bind describeFunctionBind(Node n) {
      // It would be nice to be able to identify a fn.bind call
      // but that requires knowing the type of "fn".

      if (n.getType() != Token.CALL) {
        return null;
      }

      Node callTarget = n.getFirstChild();
      String name = callTarget.getQualifiedName();
      if (name != null) {
        if (name.equals("Function.prototype.bind.call")) {
          // goog.bind(fn, self, args...);
          Node fn = callTarget.getNext();
          if (fn == null) {
            return null;
          }
          Node thisValue = safeNext(fn);
          Node parameters = safeNext(thisValue);
          return new Bind(fn, thisValue, parameters);
        }
      }

      if (callTarget.getType() == Token.GETPROP
          && callTarget.getLastChild().getString().equals("bind")
          && callTarget.getFirstChild().getType() == Token.FUNCTION) {
        // (function(){}).bind(self, args...);
        Node fn = callTarget.getFirstChild();
        Node thisValue = callTarget.getNext();
        Node parameters = safeNext(thisValue);
        return new Bind(fn, thisValue, parameters);
      }

      return null;
    }

    private Node safeNext(Node n) {
      if (n != null) {
        return n.getNext();
      }
      return null;
    }
  }
}
