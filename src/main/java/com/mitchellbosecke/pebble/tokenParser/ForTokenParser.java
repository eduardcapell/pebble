/*******************************************************************************
 * This file is part of Pebble.
 * 
 * Original work Copyright (c) 2009-2013 by the Twig Team
 * Modified work Copyright (c) 2013 by Mitchell Bösecke
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.mitchellbosecke.pebble.tokenParser;

import com.mitchellbosecke.pebble.error.SyntaxException;
import com.mitchellbosecke.pebble.lexer.Token;
import com.mitchellbosecke.pebble.lexer.TokenStream;
import com.mitchellbosecke.pebble.node.Node;
import com.mitchellbosecke.pebble.node.NodeBody;
import com.mitchellbosecke.pebble.node.NodeExpression;
import com.mitchellbosecke.pebble.node.NodeFor;
import com.mitchellbosecke.pebble.node.expression.NodeExpressionDeclaration;
import com.mitchellbosecke.pebble.node.expression.NodeExpressionVariableName;
import com.mitchellbosecke.pebble.utils.Function;

public class ForTokenParser extends AbstractTokenParser {

	@Override
	public Node parse(Token token) throws SyntaxException {
		TokenStream stream = this.parser.getStream();
		int lineNumber = token.getLineNumber();

		// skip the 'for' token
		stream.next();

		// get the iteration variable
		NodeExpressionDeclaration iterationVariable = this.parser.getExpressionParser().parseDeclarationExpression();

		stream.expect(Token.Type.NAME, "in");

		// get the iterable variable
		NodeExpression iterable = this.parser.getExpressionParser().parseExpression();

		stream.expect(Token.Type.EXECUTE_END);

		NodeBody body = this.parser.subparse(decideForFork);

		NodeBody elseBody = null;

		if (stream.current().test(Token.Type.NAME, "else")) {
			// skip the 'else' token
			stream.next();
			stream.expect(Token.Type.EXECUTE_END);
			elseBody = this.parser.subparse(decideForEnd);
		}

		// skip the 'endfor' token
		stream.next();

		stream.expect(Token.Type.EXECUTE_END);

		return new NodeFor(lineNumber, iterationVariable, (NodeExpressionVariableName) iterable, body, elseBody);
	}

	private Function<Boolean, Token> decideForFork = new Function<Boolean, Token>() {
		@Override
		public Boolean execute(Token token) {
			return token.test(Token.Type.NAME, "else", "endfor");
		}
	};

	private Function<Boolean, Token> decideForEnd = new Function<Boolean, Token>() {
		@Override
		public Boolean execute(Token token) {
			return token.test(Token.Type.NAME, "endfor");
		}
	};

	@Override
	public String getTag() {
		return "for";
	}
}