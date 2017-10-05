package bankdroid.smskey.bank.test;

import bankdroid.smskey.bank.Bank;
import bankdroid.smskey.bank.BankDescriptor;
import bankdroid.smskey.bank.Expression;
import junit.framework.TestCase;

import java.util.regex.Pattern;

public abstract class BankTestBase extends TestCase {

    public void negativeTest(final String phoneNumber) throws Exception {
        final Bank[] bank = BankDescriptor.findByPhoneNumber(phoneNumber);

        //check phone number matching
        assertFalse("Negative test failed for number: " + phoneNumber, bank != null && bank.length > 0);

    }

    public void detestBank(final String bankName, final String phoneNumber, final String message) throws Exception {
        final Bank[] banks = testBank(bankName, phoneNumber);

        for (final Bank bank : banks) {
            if (bank.getName().equals(bankName)) {
                final String extracted = bank.extractCode(message);
                assertNull(bankName + ": Code assertion failed as code found " + extracted, extracted);
            }
        }
    }

    public void testBank(final String bankName, final String phoneNumber, final String message, final String code) throws Exception {
        final Bank[] banks = testBank(bankName, phoneNumber);

        String extracted = null;
        for (final Bank bank : banks) {
            if (bank.getName().equals(bankName)) {
                final String extractedIn = bank.extractCode(message);
                if (extractedIn != null)
                    extracted = extractedIn;
                assertNull(bankName + ": Inverse code assertion failed for code " + code,
                        bank.extractCode("Sikeres bejelentkezas - FHB NetB@nk. Felhasznalonev: Minta Janos. Idopont: 2009.08.24 14:30"));
            }
        }
        assertTrue(bankName + ": Code assertion failed for code " + code + " <> " + extracted, code.equals(extracted));
    }

    public Bank[] testBank(final String bankName, final String phoneNumber) throws Exception {
        final Bank[] banks = BankDescriptor.findByPhoneNumber(phoneNumber);

        assertTrue(bankName + ": no bank was found.", banks != null && banks.length > 0);

        Bank bank = null;
        for (final Bank bank2 : banks) {
            if (bank2.getName().equals(bankName)) {
                bank = bank2;

                //check phone number matching
                assertTrue(bankName + ": Failed to realize phone number.", bankName.equals(bank.getName()));

                //check expressions
                for (final Expression exp : bank.getExtractExpressions()) {
                    final String pattern = exp.getExpression();
                    try {
                        final Pattern compiled = Pattern.compile(pattern);
                        assertNotNull(bankName + ": compiled patter is null: " + pattern, compiled);
                    } catch (final Exception e) {
                        e.printStackTrace();
                        assertTrue(bankName + ": compilation of pattern failed: " + pattern, false);
                    }
                }
            }
        }

        //check phone number matching
        assertNotNull(bankName + ": Bank not found with this name.", bank);

        return banks;
    }
}
