package bankdroid.smskey.bank;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BankDescriptor {
    private static Bank[] defaultBanks = null;

    public static Bank[] getDefaultBanks() throws ParserConfigurationException, SAXException, IOException {
        if (defaultBanks == null) {

            final List<Bank> result = new ArrayList<Bank>();
            new Banks_HU().append(result);

            defaultBanks = result.toArray(new Bank[result.size()]);
        }
        return defaultBanks;
    }

    public static Bank[] findByPhoneNumber(String phoneNumber) throws ParserConfigurationException, SAXException,
            IOException {
        phoneNumber = phoneNumber.trim();
        final Bank[] banks = getDefaultBanks();
        final List<Bank> bankFound = new ArrayList<Bank>();
        for (final Bank bank : banks) {
            if (bank.isBankPhoneNumber(phoneNumber)) {
                bankFound.add(bank);
            }
        }
        return bankFound.toArray(new Bank[bankFound.size()]);
    }

}
