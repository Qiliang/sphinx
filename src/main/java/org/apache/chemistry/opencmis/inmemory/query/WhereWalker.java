package org.apache.chemistry.opencmis.inmemory.query;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.Tree;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.server.support.query.CalendarHelper;
import org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer;
import org.apache.chemistry.opencmis.server.support.query.TextSearchLexer;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import com.mongodb.BasicDBObject;

public class WhereWalker {
	
	BasicBSONObject d;
	
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

	    public BasicBSONObject walkAnd(Tree opNode, Tree leftNode, Tree rightNode) {
	        walk(leftNode);
	        walk(rightNode);
	        return null;
	    }

	    public BSONObject walkOr(Tree opNode, Tree leftNode, Tree rightNode) {
	        walk(leftNode);
	        walk(rightNode);
	        return null;
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
	        walkExpr(leftNode);
	        walkExpr(rightNode);
	        return null;
	    }

	    public BSONObject walkNotEquals(Tree opNode, Tree leftNode, Tree rightNode) {
	        walkExpr(leftNode);
	        walkExpr(rightNode);
	        return null;
	    }

	    public BSONObject walkGreaterThan(Tree opNode, Tree leftNode, Tree rightNode) {
	        walkExpr(leftNode);
	        walkExpr(rightNode);
	        return null;
	    }

	    public BSONObject walkGreaterOrEquals(Tree opNode, Tree leftNode, Tree rightNode) {
	        walkExpr(leftNode);
	        walkExpr(rightNode);
	        return null;
	    }

	    public BSONObject walkLessThan(Tree opNode, Tree leftNode, Tree rightNode) {
	        walkExpr(leftNode);
	        walkExpr(rightNode);
	        return null;
	    }

	    public BSONObject walkLessOrEquals(Tree opNode, Tree leftNode, Tree rightNode) {
	        walkExpr(leftNode);
	        walkExpr(rightNode);
	        return null;
	    }

	    public BSONObject walkIn(Tree opNode, Tree colNode, Tree listNode) {
	        walkExpr(colNode);
	        walkExpr(listNode);
	        return null;
	    }

	    public BSONObject walkNotIn(Tree opNode, Tree colNode, Tree listNode) {
	        walkExpr(colNode);
	        walkExpr(listNode);
	        return null;
	    }

	    public BSONObject walkInAny(Tree opNode, Tree colNode, Tree listNode) {
	        walkExpr(colNode);
	        walkExpr(listNode);
	        return null;
	    }

	    public BSONObject walkNotInAny(Tree opNode, Tree colNode, Tree listNode) {
	        walkExpr(colNode);
	        walkExpr(listNode);
	        return null;
	    }

	    public BSONObject walkEqAny(Tree opNode, Tree literalNode, Tree colNode) {
	        walkExpr(literalNode);
	        walkExpr(colNode);
	        return null;
	    }

	    public BSONObject walkIsNull(Tree opNode, Tree colNode) {
	        walkExpr(colNode);
	        return null;
	    }

	    public BSONObject walkIsNotNull(Tree opNode, Tree colNode) {
	        walkExpr(colNode);
	        return null;
	    }

	    public BSONObject walkLike(Tree opNode, Tree colNode, Tree stringNode) {
	        walkExpr(colNode);
	        walkExpr(stringNode);
	        return null;
	    }

	    public BSONObject walkNotLike(Tree opNode, Tree colNode, Tree stringNode) {
	        walkExpr(colNode);
	        walkExpr(stringNode);
	        return null;
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
	        return null;
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
