package bankdroid.smskey.bank.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import bankdroid.smskey.bank.Bank;
import bankdroid.smskey.bank.BankDescriptor;
import bankdroid.smskey.bank.Expression;

public class Convert
{
	public static String escapeStringBackslash( final String str )
	{
		int i = str.indexOf('\\');
		if ( i < 0 )
			return str;
		final StringBuilder edit = new StringBuilder(str);
		while ( i >= 0 )
		{
			edit.insert(i, '\\');
			i += 2;
			i = edit.indexOf("\\", i);
		}

		return edit.toString();
	}

	public static String escapeStringQuote( final String str )
	{
		int i = str.indexOf('"');
		if ( i < 0 )
			return str;
		final StringBuilder edit = new StringBuilder(str);
		while ( i >= 0 )
		{
			edit.insert(i, '\\');
			i += 2;
			i = edit.indexOf("\"", i);
		}

		return edit.toString();
	}

	private static String escapeString( final String expression )
	{
		return escapeStringQuote(escapeStringBackslash(expression));
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main( final String[] args ) throws ParserConfigurationException, SAXException, IOException
	{
		final Bank[] defaultBanks = BankDescriptor.getDefaultBanks();

		final List<String> countries = new ArrayList<String>();
		for ( int i = 0; i < defaultBanks.length; i++ )
		{
			final String country = defaultBanks[i].getCountryCode();
			if ( !countries.contains(country) )
				countries.add(country);
		}

		for ( final String country : countries )
		{
			final FileOutputStream fos = new FileOutputStream(
					"../bankdroid.smskey.bank/src/bankdroid/smskey/bank/Banks_" + country + ".java");
			final PrintWriter pw = new PrintWriter(fos);

			pw.println("package bankdroid.smskey.bank;");

			pw.println("class Banks_" + country + " extends AbstractBanks {");
			pw.println("@Override");
			pw.println("public void init(){");

			for ( final Bank bank : defaultBanks )
			{
				if ( bank.getCountryCode().equals(country) )
				{
					final StringBuilder phoneString = new StringBuilder();
					for ( final String pn : bank.getPhoneNumbers() )
					{
						if ( phoneString.length() > 0 )
							phoneString.append(",");
						phoneString.append("\"").append(pn).append("\"");
					}

					final StringBuilder expressionString = new StringBuilder();
					for ( final Expression exp : bank.getExtractExpressions() )
					{
						if ( expressionString.length() > 0 )
							expressionString.append(",// transactionSigning, regexp\n");
						expressionString.append("new Expression(").append(exp.isTransactionSign()).append(",\"")
								.append(escapeString(exp.getExpression())).append("\")");
					}

					//						public Bank( final int id, final String name, final int expiry, final String[] phoneNumber,
					//								final Expression[] extractExpression, final String countryCode )
					pw.printf("addBank(new Bank(-1, \"%s\", %d, // id, bankName, expiry\n"//
							+ "new String[] {%s}, // phoneNumbers\n" //
							+ "new Expression[] {// expressions\n" //
							+ "%s// transactionSigning, regexp\n" //
							+ "}, \"%s\")); // country\n\n", bank.getName(), bank.getExpiry(), phoneString.toString(),
							expressionString.toString(), bank.getCountryCode());
				}
			}

			pw.println("}}");
			pw.close();
			fos.close();
		}

		System.out.println(" ready.");
	}
}
