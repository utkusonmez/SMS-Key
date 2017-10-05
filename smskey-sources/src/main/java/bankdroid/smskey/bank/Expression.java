/**
 *
 */
package bankdroid.smskey.bank;

import java.io.Serializable;

/**
 * WARNING! The transaction sign flag is not persisted into seperate table, but a character is indicating that the expression
 * is related to transaction signing. The character is defined in the TRANSACTION_SIGN_PREFIX constant in this class.
 * This prefix is only used during the serialization.
 *
 * @author Gabe
 */
public class Expression implements Serializable {
    private static final long serialVersionUID = -285195022405029198L;

    private final static String TRANSACTION_SIGN_PREFIX = "#";

    private boolean transactionSign = false;
    private String expression = null;

    public Expression(final boolean transactionSign, final String expression) {
        super();
        this.transactionSign = transactionSign;
        this.expression = expression;
    }

    public Expression(String persistedExpression) {
        super();
        this.transactionSign = persistedExpression.startsWith(TRANSACTION_SIGN_PREFIX);
        if (transactionSign)
            persistedExpression = persistedExpression.substring(1);
        this.expression = persistedExpression;
    }

    @Override
    public String toString() {
        return (transactionSign ? TRANSACTION_SIGN_PREFIX : "") + expression;
    }

    public boolean isTransactionSign() {
        return transactionSign;
    }

    public void setTransactionSign(final boolean transactionSign) {
        this.transactionSign = transactionSign;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(final String expression) {
        this.expression = expression;
    }
}