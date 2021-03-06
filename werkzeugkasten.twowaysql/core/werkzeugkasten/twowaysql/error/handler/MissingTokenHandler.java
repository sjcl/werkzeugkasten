package werkzeugkasten.twowaysql.error.handler;

import org.antlr.runtime.MissingTokenException;
import org.antlr.runtime.RecognitionException;

public class MissingTokenHandler extends MismatchedTokenHandler {

	public MissingTokenHandler(String ruleName) {
		super(ruleName);
	}

	public Class<? extends RecognitionException> getHadleType() {
		return MissingTokenException.class;
	}

}
