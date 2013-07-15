package org.apache.chemistry.opencmis.inmemory.query;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.Tree;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.server.support.query.CalendarHelper;
import org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer;
import org.apache.chemistry.opencmis.server.support.query.TextSearchLexer;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

public class WhereWalker {

	TypeDefinition typeDefinition;

	public WhereWalker(TypeDefinition typeDefinition) {
		typeDefinition = this.typeDefinition;
	}

//	private boolean isSystemCol(String colName) {
//		for (PropertyDefinition pd : typeDefinition.getPropertyDefinitions().values()) {
//			pd.getQueryName().equals(colName);
//		}
//
//	}

	public BSONObject walk(Tree node) {
		switch (node.getType()) {
		case CmisQlStrictLexer.NOT:
			return walkNot(node, node.getChild(0));
		case CmisQlStrictLexer.AND:
			return walkAnd(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.OR:
			return walkOr(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.EQ:
			return walkEquals(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.NEQ:
			return walkNotEquals(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.GT:
			return walkGreaterThan(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.GTEQ:
			return walkGreaterOrEquals(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.LT:
			return walkLessThan(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.LTEQ:
			return walkLessOrEquals(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.IN:
			return walkIn(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.NOT_IN:
			return walkNotIn(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.IN_ANY:
			return walkInAny(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.NOT_IN_ANY:
			return walkNotInAny(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.EQ_ANY:
			return walkEqAny(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.IS_NULL:
			return walkIsNull(node, node.getChild(0));
		case CmisQlStrictLexer.IS_NOT_NULL:
			return walkIsNotNull(node, node.getChild(0));
		case CmisQlStrictLexer.LIKE:
			return walkLike(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.NOT_LIKE:
			return walkNotLike(node, node.getChild(0), node.getChild(1));
		case CmisQlStrictLexer.CONTAINS:
			if (node.getChildCount() == 1) {
				return walkContains(node, null, node.getChild(0));
			} else {
				return walkContains(node, node.getChild(0), node.getChild(1));
			}
		case CmisQlStrictLexer.IN_FOLDER:
			if (node.getChildCount() == 1) {
				return walkInFolder(node, null, node.getChild(0));
			} else {
				return walkInFolder(node, node.getChild(0), node.getChild(1));
			}
		case CmisQlStrictLexer.IN_TREE:
			if (node.getChildCount() == 1) {
				return walkInTree(node, null, node.getChild(0));
			} else {
				return walkInTree(node, node.getChild(0), node.getChild(1));
			}
		case CmisQlStrictLexer.BOOL_LIT:
			walkBoolean(node);
			return null;
		case CmisQlStrictLexer.NUM_LIT:
			walkNumber(node);
			return null;
		case CmisQlStrictLexer.STRING_LIT:
			walkString(node);
			return null;
		case CmisQlStrictLexer.TIME_LIT:
			walkTimestamp(node);
			return null;
		case CmisQlStrictLexer.IN_LIST:
			walkList(node);
			return null;
		case CmisQlStrictLexer.COL:
			walkCol(node);
			return null;
		case CmisQlStrictLexer.ID:
			walkId(node);
			return null;
		case CmisQlStrictLexer.SCORE:
			return walkScore(node);
		default:
			return walkOtherPredicate(node);
		}
	}

	/** For extensibility. */
	protected BasicBSONObject walkOtherPredicate(Tree node) {
		throw new CmisRuntimeException("Unknown node type: " + node.getType() + " (" + node.getText() + ")");
	}

	public BasicBSONObject walkNot(Tree opNode, Tree node) {
		walk(node);
		return null;
	}

	public BSONObject walkAnd(Tree opNode, Tree leftNode, Tree rightNode) {
		BSONObject and = new BasicBSONObject();
		and.put("$and", new BSONObject[] { walk(leftNode), walk(rightNode) });
		return and;
	}

	public BSONObject walkOr(Tree opNode, Tree leftNode, Tree rightNode) {
		BSONObject or = new BasicBSONObject();
		or.put("$or", new BSONObject[] { walk(leftNode), walk(rightNode) });
		return or;
	}

	public Object walkExpr(Tree node) {
		switch (node.getType()) {
		case CmisQlStrictLexer.BOOL_LIT:
			return walkBoolean(node);
		case CmisQlStrictLexer.NUM_LIT:
			return walkNumber(node);
		case CmisQlStrictLexer.STRING_LIT:
			return walkString(node);
		case CmisQlStrictLexer.TIME_LIT:
			return walkTimestamp(node);
		case CmisQlStrictLexer.IN_LIST:
			return walkList(node);
		case CmisQlStrictLexer.COL:
			return walkCol(node);
		case CmisQlStrictLexer.ID:
			return walkId(node);
		default:
			return walkOtherExpr(node);
		}
	}

	public BSONObject walkSearchExpr(Tree node) {
		switch (node.getType()) {
		case TextSearchLexer.TEXT_AND:
			return walkTextAnd(node);
		case TextSearchLexer.TEXT_OR:
			return walkTextOr(node);
		case TextSearchLexer.TEXT_MINUS:
			return walkTextMinus(node);
		case TextSearchLexer.TEXT_SEARCH_WORD_LIT:
			return walkTextWord(node);
		case TextSearchLexer.TEXT_SEARCH_PHRASE_STRING_LIT:
			return walkTextPhrase(node);
		default:
			walkOtherExpr(node);
			return null;
		}
	}

	/** For extensibility. */
	protected Object walkOtherExpr(Tree node) {
		throw new CmisRuntimeException("Unknown node type: " + node.getType() + " (" + node.getText() + ")");
	}

	public BSONObject walkEquals(Tree opNode, Tree leftNode, Tree rightNode) {
		BSONObject equals = new BasicBSONObject();
		equals.put(walkExpr(leftNode).toString(), walkExpr(rightNode));
		return equals;
	}

	public BSONObject walkNotEquals(Tree opNode, Tree leftNode, Tree rightNode) {
		BSONObject notEquals = new BasicBSONObject();
		notEquals.put(walkExpr(leftNode).toString(), new BasicBSONObject("$ne", walkExpr(rightNode)));

		return notEquals;
	}

	public BSONObject walkGreaterThan(Tree opNode, Tree leftNode, Tree rightNode) {
		BSONObject greaterThan = new BasicBSONObject();
		greaterThan.put(walkExpr(leftNode).toString(), new BasicBSONObject("$gt", walkExpr(rightNode)));

		return greaterThan;
	}

	public BSONObject walkGreaterOrEquals(Tree opNode, Tree leftNode, Tree rightNode) {
		BSONObject greaterOrEquals = new BasicBSONObject();
		greaterOrEquals.put(walkExpr(leftNode).toString(), new BasicBSONObject("$gte", walkExpr(rightNode)));

		return greaterOrEquals;
	}

	public BSONObject walkLessThan(Tree opNode, Tree leftNode, Tree rightNode) {
		BSONObject lessThan = new BasicBSONObject();
		lessThan.put(walkExpr(leftNode).toString(), new BasicBSONObject("$lt", walkExpr(rightNode)));

		return lessThan;
	}

	public BSONObject walkLessOrEquals(Tree opNode, Tree leftNode, Tree rightNode) {
		BSONObject lessOrEquals = new BasicBSONObject();
		lessOrEquals.put(walkExpr(leftNode).toString(), new BasicBSONObject("$lte", walkExpr(rightNode)));

		return lessOrEquals;
	}

	public BSONObject walkIn(Tree opNode, Tree colNode, Tree listNode) {
		BSONObject in = new BasicBSONObject();
		in.put(walkExpr(colNode).toString(), new BasicBSONObject("$in", walkExpr(listNode)));

		return in;
	}

	public BSONObject walkNotIn(Tree opNode, Tree colNode, Tree listNode) {
		BSONObject notIn = new BasicBSONObject();
		notIn.put(walkExpr(colNode).toString(), new BasicBSONObject("$nin", walkExpr(listNode)));

		return notIn;
	}

	public BSONObject walkInAny(Tree opNode, Tree colNode, Tree listNode) {
		// 多值字段的时候用anyIn
		return walkIn(opNode, colNode, listNode);
	}

	public BSONObject walkNotInAny(Tree opNode, Tree colNode, Tree listNode) {
		// 多值字段的时候用not any in
		return walkNotIn(opNode, colNode, listNode);
	}

	public BSONObject walkEqAny(Tree opNode, Tree literalNode, Tree colNode) {
		return walkEquals(opNode, colNode, literalNode);
	}

	public BSONObject walkIsNull(Tree opNode, Tree colNode) {
		BSONObject isNull = new BasicBSONObject();
		isNull.put(walkExpr(colNode).toString(), new BasicBSONObject("$exists", false));

		return isNull;
	}

	public BSONObject walkIsNotNull(Tree opNode, Tree colNode) {
		BSONObject isNotNull = new BasicBSONObject();
		isNotNull.put(walkExpr(colNode).toString(), new BasicBSONObject("$exists", true));

		return isNotNull;
	}

	public BSONObject walkLike(Tree opNode, Tree colNode, Tree stringNode) {
		BSONObject like = new BasicBSONObject();
		like.put(walkExpr(colNode).toString(), "/" + walkExpr(stringNode) + "/");

		return like;
	}

	public BSONObject walkNotLike(Tree opNode, Tree colNode, Tree stringNode) {

		BSONObject notLike = new BasicBSONObject();
		notLike.put(walkExpr(colNode).toString(), new BasicBSONObject("$not", "/" + walkExpr(stringNode) + "/"));

		return notLike;
	}

	public BSONObject walkContains(Tree opNode, Tree qualNode, Tree queryNode) {
		if (qualNode != null) {
			return walkSearchExpr(qualNode);
		}
		return walkSearchExpr(queryNode);
	}

	public BSONObject walkInFolder(Tree opNode, Tree qualNode, Tree paramNode) {
		if (qualNode != null) {
			walkExpr(qualNode);
		}
		walkExpr(paramNode);
		return null;
	}

	public BSONObject walkInTree(Tree opNode, Tree qualNode, Tree paramNode) {
		if (qualNode != null) {
			walkExpr(qualNode);
		}
		walkExpr(paramNode);
		return null;
	}

	public Object walkList(Tree node) {
		int n = node.getChildCount();
		List<Object> res = new ArrayList<Object>(n);
		for (int i = 0; i < n; i++) {
			res.add(walkExpr(node.getChild(i)));
		}
		return res;
	}

	public Object walkBoolean(Tree node) {
		String s = node.getText();
		return Boolean.valueOf(s);
	}

	public Object walkNumber(Tree node) {
		String s = node.getText();
		if (s.contains(".") || s.contains("e") || s.contains("E")) {
			return Double.valueOf(s);
		} else {
			return Long.valueOf(s);
		}
	}

	public Object walkString(Tree node) {
		String s = node.getText();
		s = s.substring(1, s.length() - 1);
		s = s.replace("''", "'"); // unescape quotes
		return s;
	}

	public Object walkTimestamp(Tree node) {
		String s = node.getText();
		s = s.substring(s.indexOf('\'') + 1, s.length() - 1);
		return CalendarHelper.fromString(s);
	}

	public Object walkCol(Tree node) {
		return node.getChild(0);
	}

	public Object walkId(Tree node) {
		return null;
	}

	protected BSONObject walkTextAnd(Tree node) {
		return null;
	}

	protected BSONObject walkTextOr(Tree node) {
		return null;
	}

	protected BSONObject walkTextMinus(Tree node) {
		return null;
	}

	protected BSONObject walkTextWord(Tree node) {
		return null;
	}

	protected BSONObject walkTextPhrase(Tree node) {
		return null;
	}

	protected BSONObject walkScore(Tree node) {
		return null;
	}

}
