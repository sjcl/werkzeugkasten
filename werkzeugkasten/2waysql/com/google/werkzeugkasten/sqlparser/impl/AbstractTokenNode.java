package com.google.werkzeugkasten.sqlparser.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.werkzeugkasten.sqlparser.SqlExecutionContext;
import com.google.werkzeugkasten.sqlparser.Status;
import com.google.werkzeugkasten.sqlparser.TokenLeaf;
import com.google.werkzeugkasten.sqlparser.TokenNode;

public abstract class AbstractTokenNode extends AbstractToken implements
		TokenNode {

	protected List<TokenLeaf> children = new ArrayList<TokenLeaf>();

	public AbstractTokenNode(int offset) {
		super(offset);
	}

	protected void executeChildren(SqlExecutionContext parameter) {
		List<Status> tmp = parameter.getStatusCopy();
		try {
			parameter.resetStatus();
			for (TokenLeaf t : getChildren()) {
				parameter.addStatus(t.execute(parameter));
			}
		} finally {
			tmp.addAll(parameter.getStatusCopy());
			parameter.setStatus(tmp);
		}
	}

	public void addChild(TokenLeaf token) {
		children.add(token);
	}

	protected List<TokenLeaf> getChildren() {
		return this.children;
	}
}
