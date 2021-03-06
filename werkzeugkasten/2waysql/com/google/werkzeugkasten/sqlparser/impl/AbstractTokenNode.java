package com.google.werkzeugkasten.sqlparser.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.werkzeugkasten.sqlparser.SqlConstructionContext;
import com.google.werkzeugkasten.sqlparser.TokenLeaf;
import com.google.werkzeugkasten.sqlparser.TokenNode;
import com.google.werkzeugkasten.util.ClassUtil;

public abstract class AbstractTokenNode extends AbstractToken implements
		TokenNode {

	protected List<TokenLeaf> children = new ArrayList<TokenLeaf>();

	public AbstractTokenNode(int offset) {
		super(offset);
	}

	protected void executeChildren(SqlConstructionContext parameter) {
		parameter.setSiblings(getChildren());
		for (TokenLeaf t : getChildren()) {
			TokenNode node = ClassUtil.as(TokenNode.class, t);
			if (node != null) {
				parameter.setChildren(node.getChildren());
			}
			t.execute(parameter);
		}
	}

	public void addChild(TokenLeaf token) {
		children.add(token);
	}

	public List<TokenLeaf> getChildren() {
		return this.children;
	}

	public String toString() {
		StringBuilder stb = new StringBuilder();
		stb.append(super.toString());
		stb.append(" ");
		stb.append(getChildren());
		return stb.toString();
	}
}
