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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class WhereWalker {


//	private boolean isSystemCol(String colName) {
//		for (PropertyDefinition pd : typeDefinition.getPropertyDefinitions().values()) {
//			pd.getQueryName().equals(colName);
//		}
//
//	}

	public DBObject walk(Tree node) {
		if(node==null)
			return new BasicDBObject();
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
	protected DBObject walkOtherPredicate(Tree node) {
		throw new CmisRuntimeException("Unknown node type: " + node.getType() + " (" + node.getText() + ")");
	}

	public DBObject walkNot(Tree opNode, Tree node) {
		walk(node);
		return null;
	}

	public DBObject walkAnd(Tree opNode, Tree leftNode, Tree rightNode) {
		DBObject and = new BasicDBObject();
		and.put("$and", new DBObject[] { walk(leftNode), walk(rightNode) });
		return and;
	}

	public DBObject walkOr(Tree opNode, Tree leftNode, Tree rightNode) {
		DBObject or = new BasicDBObject();
		or.put("$or", new DBObject[] { walk(leftNode), walk(rightNode) });
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

	public DBObject walkSearchExpr(Tree node) {
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

	public DBObject walkEquals(Tree opNode, Tree leftNode, Tree rightNode) {
		DBObject equals = new BasicDBObject();
		equals.put(walkExpr(leftNode).toString(), walkExpr(rightNode));
		return equals;
	}

	public DBObject walkNotEquals(Tree opNode, Tree leftNode, Tree rightNode) {
		DBObject notEquals = new BasicDBObject();
		notEquals.put(walkExpr(leftNode).toString(), new BasicDBObject("$ne", walkExpr(rightNode)));

		return notEquals;
	}

	public DBObject walkGreaterThan(Tree opNode, Tree leftNode, Tree rightNode) {
		DBObject greaterThan = new BasicDBObject();
		greaterThan.put(walkExpr(leftNode).toString(), new BasicDBObject("$gt", walkExpr(rightNode)));

		return greaterThan;
	}

	public DBObject walkGreaterOrEquals(Tree opNode, Tree leftNode, Tree rightNode) {
		DBObject greaterOrEquals = new BasicDBObject();
		greaterOrEquals.put(walkExpr(leftNode).toString(), new BasicDBObject("$gte", walkExpr(rightNode)));

		return greaterOrEquals;
	}

	public DBObject walkLessThan(Tree opNode, Tree leftNode, Tree rightNode) {
		DBObject lessThan = new BasicDBObject();
		lessThan.put(walkExpr(leftNode).toString(), new BasicDBObject("$lt", walkExpr(rightNode)));

		return lessThan;
	}

	public DBObject walkLessOrEquals(Tree opNode, Tree leftNode, Tree rightNode) {
		DBObject lessOrEquals = new BasicDBObject();
		lessOrEquals.put(walkExpr(leftNode).toString(), new BasicDBObject("$lte", walkExpr(rightNode)));

		return lessOrEquals;
	}

	public DBObject walkIn(Tree opNode, Tree colNode, Tree listNode) {
		DBObject in = new BasicDBObject();
		in.put(walkExpr(colNode).toString(), new BasicDBObject("$in", walkExpr(listNode)));

		return in;
	}

	public DBObject walkNotIn(Tree opNode, Tree colNode, Tree listNode) {
		DBObject notIn = new BasicDBObject();
		notIn.put(walkExpr(colNode).toString(), new BasicDBObject("$nin", walkExpr(listNode)));

		return notIn;
	}

	public DBObject walkInAny(Tree opNode, Tree colNode, Tree listNode) {
		// 多值字段的时候用anyIn
		return walkIn(opNode, colNode, listNode);
	}

	public DBObject walkNotInAny(Tree opNode, Tree colNode, Tree listNode) {
		// 多值字段的时候用not any in
		return walkNotIn(opNode, colNode, listNode);
	}

	public DBObject walkEqAny(Tree opNode, Tree literalNode, Tree colNode) {
		return walkEquals(opNode, colNode, literalNode);
	}

	public DBObject walkIsNull(Tree opNode, Tree colNode) {
		DBObject isNull = new BasicDBObject();
		isNull.put(walkExpr(colNode).toString(), new BasicDBObject("$exists", false));

		return isNull;
	}

	public DBObject walkIsNotNull(Tree opNode, Tree colNode) {
		DBObject isNotNull = new BasicDBObject();
		isNotNull.put(walkExpr(colNode).toString(), new BasicDBObject("$exists", true));

		return isNotNull;
	}

	public DBObject walkLike(Tree opNode, Tree colNode, Tree stringNode) {
		DBObject like = new BasicDBObject();
		like.put(walkExpr(colNode).toString(), "/" + walkExpr(stringNode) + "/");

		return like;
	}

	public DBObject walkNotLike(Tree opNode, Tree colNode, Tree stringNode) {

		DBObject notLike = new BasicDBObject();
		notLike.put(walkExpr(colNode).toString(), new BasicDBObject("$not", "/" + walkExpr(stringNode) + "/"));

		return notLike;
	}

	public DBObject walkContains(Tree opNode, Tree qualNode, Tree queryNode) {
		if (qualNode != null) {
			return walkSearchExpr(qualNode);
		}
		return walkSearchExpr(queryNode);
	}

	public DBObject walkInFolder(Tree opNode, Tree qualNode, Tree paramNode) {
		if (qualNode != null) {
			walkExpr(qualNode);
		}
		walkExpr(paramNode);
		return null;
	}

	public DBObject walkInTree(Tree opNode, Tree qualNode, Tree paramNode) {
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

	protected DBObject walkTextAnd(Tree node) {
		return null;
	}

	protected DBObject walkTextOr(Tree node) {
		return null;
	}

	protected DBObject walkTextMinus(Tree node) {
		return null;
	}

	protected DBObject walkTextWord(Tree node) {
		return null;
	}

	protected DBObject walkTextPhrase(Tree node) {
		return null;
	}

	protected DBObject walkScore(Tree node) {
		return null;
	}

}
