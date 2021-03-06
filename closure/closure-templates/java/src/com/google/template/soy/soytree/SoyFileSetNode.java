/*
 * Copyright 2008 Google Inc.
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

package com.google.template.soy.soytree;

import com.google.template.soy.base.IdGenerator;
import com.google.template.soy.base.SoySyntaxException;
import com.google.template.soy.soytree.SoyNode.SplitLevelTopNode;


/**
 * Node representing a Soy file set (the root of the Soy parse tree).
 *
 * <p> Important: Do not use outside of Soy code (treat as superpackage-private).
 *
 */
public class SoyFileSetNode extends AbstractParentSoyNode<SoyFileNode>
    implements SplitLevelTopNode<SoyFileNode> {


  /** The node id generator for this parse tree. */
  private final IdGenerator nodeIdGen;


  /**
   * @param id The id for this node.
   * @param nodeIdGen The node id generator for this parse tree.
   * @throws SoySyntaxException If a syntax error is found.
   */
  public SoyFileSetNode(int id, IdGenerator nodeIdGen) throws SoySyntaxException {
    super(id);
    this.nodeIdGen = nodeIdGen;
  }


  /**
   * Copy constructor.
   * @param orig The node to copy.
   */
  protected SoyFileSetNode(SoyFileSetNode orig) {
    super(orig);
    this.nodeIdGen = orig.nodeIdGen.clone();
  }


  @Override public Kind getKind() {
    return Kind.SOY_FILE_SET_NODE;
  }


  /** Returns the node id generator for this parse tree. */
  public IdGenerator getNodeIdGenerator() {
    return nodeIdGen;
  }


  @Override public String toSourceString() {
    throw new UnsupportedOperationException();
  }


  @Override public SoyFileSetNode clone() {
    return new SoyFileSetNode(this);
  }

}
