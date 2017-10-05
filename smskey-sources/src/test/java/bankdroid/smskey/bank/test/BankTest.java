package bankdroid.smskey.bank.test;

public class BankTest extends BankTestBase
{
	public void testBasics() throws Exception
	{
		//country HU
		testBank("AXA Bank", "AXA BANK");
		testBank("Allianz Bank", "+36303444664");
		testBank("Budapest Bank", "+36309266245");
		testBank("Citibank", "+36303444455");
		testBank("Citibank", "+36707060444");
		testBank("ERSTE Bank", "+36303444481");
		testBank("ERSTE Bank", "0036303444481");
		testBank("ERSTE Bank", "+36209000741");
		testBank("ERSTE Bank", "+36707060177");
		testBank("FHB Bank", "+36303444043");
		testBank("K&H Bank", "+36209000703");
		testBank("K&H Bank", "06709000542");
		testBank("K&H Bank", "+36709000542");
		testBank("K&H Bank", "+36302030000");
		testBank("MKB Bank", "+36707060652");
		testBank("MKB Bank", "+36209000652");
		testBank("MKB Bank", "5833");
		testBank("OTP Bank", "+36309400700");
		testBank("OTP Bank", "+36209400700");
		testBank("OTP Bank", "06709400700");
		testBank("Raiffeisen Bank", "+36707060660");
		testBank("Raiffeisen Bank", "+36209000848");
		testBank("Raiffeisen Bank", "+36303444540");
		testBank("Takarékszövetkezet", "+36709000666");
		testBank("Unicredit Bank", "+36303444504");
		testBank("Unicredit Bank", "36303444504");
		testBank("Vodafone", "Vodafone");

		negativeTest("1234");
	}

	public void testPatterns() throws Exception
	{
		//HU
		testBank(
				"MKB Bank",
				"+36707060652",
				"MKB  NetBANKàr Forint àtutalàs rögzìtése. Kedvezményezett 109500090000000251280181, Összeg: 102662 HUF. Alàìrò jelszò:  PsNNd",
				"PsNNd");
		testBank(
				"Citibank",
				"+36707060444",
				"OAC - Online Aktivalasi kod: 406209,  Kartyaszam: XX3019; Kedvezmenyezett:  At www.citibank.hu Tel: +3612888888",
				"406209");

		testBank(
				"MKB Bank",
				"+36707060652",
				"MKB NetBANKar Forint atutalas ragzatase. Kedvezmanyezett 103000021231313213131, asszeg: 10000 HUF. Alaara jelsza: g985P",
				"g985P");

		testBank("Raiffeisen Bank", "+36209000848",
				"Az Ön Raiffeisen DirektNet egyszer hasznàlatos jelszava: 16824502 Raiffeisen Bank Zrt.", "16824502");
		testBank("Raiffeisen Bank", "+36209000848",
				"Your one-time Raiffeisen DirektNet password is: 94716205 Raiffeisen Bank Zrt.", "94716205");
		testBank("Raiffeisen Bank", "+36707060660",
				"Az Ön Raiffeisen DirektNet egyszer használatos jelszava: 76037367 Raiffeisen Bank Zrt.", "76037367");
		testBank("Raiffeisen Bank", "+36303444540",
				"Az Ön Raiffeisen DirektNet egyszer hasznàlatos jelszava: 95289740 Raiffeisen Bank Zrt.", "95289740");

		testBank("Unicredit Bank", "36303444504", "SpectraNet tranzakciòs kòd: 001-877 866", "001-877 866");
		testBank("K&H Bank", "+36302030000",
				"**K&H e-bank**Az ön egy bejelentkezés idötartamàra vonatkozò, ELSìDLEGES sms jelszava: ajsDGe",
				"ajsDGe");
		testBank("K&H Bank", "+36302030000",
				"**K&H e-bank** Az ön egy bejelentkezés idötartamàra vonatkozò, ELSìDLEGES sms jelszava: gmA47j",
				"gmA47j");
		testBank("K&H Bank", "+36209000703",
				"**K&H e-bank** Az ön egy bejelentkezès idötartamàra vonatkozò, ELSÖDLEGES sms jelszava: ggSh3e",
				"ggSh3e");
		testBank(
				"K&H Bank",
				"+36209000703",
				"  **K&H e-bank**  Szàmlaszàm: 11773391-21111710-00000000 Összeg: 4720 HUF A tranzakciòhoz tartozò MàSODLAGOS sms jelszò: Cf5XdP",
				"Cf5XdP");
		testBank("ERSTE Bank", "0036303444481", "Az On ERSTE NetBank  belepesi kodja: 1212121212", "1212121212");
		testBank(
				"ERSTE Bank",
				"0036303444481",
				"ERSTE NetBank forintatutalas. Kedvezmenyezett 11111111-22222222-3333333, osszeg 123123,11 HUF. Tranzakcios kod: 010-53353861",
				"53353861");
		testBank("ERSTE Broker", "0036303444481", "qHHS", "qHHS");
		testBank("ERSTE Broker", "0036303444481", "ab4R https://mobiltozsde.erstebroker.hu/mobilbroker/", "ab4R");
		testBank("ERSTE Broker", "0036303444481", "Z686 https://mobiltozsde.erstebroker.hu/mobilbroker/", "Z686");
		detestBank(
				"ERSTE Bank",
				"0036303444481",
				"Erste KàrtyaÖr ILLÉS BALÅZS VisaE kàrtya Vàsàrlàs: 2.600 HUF Idö: 2011.02.20 18:36 Hely: BUDAPEST 00053892 Uj egyenleg: 2.096.365 HUF");
		testBank("MKB Bank", "+36707060652", "MKB NetBANKàr Forint àtutalàs rögzìtése. Alàìrò jelszò: q9Kt4", "q9Kt4");
		testBank("MKB Bank", "+36707060652", "MKB NetBANKàr Postai csekkes àtutalàs Alàìrò jelszò: 4HhRb", "4HhRb");
		testBank(
				"OTP Bank",
				"06709400700",
				"OTPdirekt - Belföldi forint àtutalàs ...9356 szàmlàn 36.000 HUF összeggel 16720013-04301232-74292005 szàmlàra. Azonositò: 49965456  Jòvàhagyàs 19:55-ig.",
				"49965456");
		testBank(
				"OTP Bank",
				"+36309400700",
				"OTPdirekt - Belföldi forint àtutalàs ...8572 szàmlàn 15.000 HUF összeggel 10300002-51401526-12103289 szàmlàra. Azonositò: 83787839  Jòvàhagyàs 23:06-ig.",
				"83787839");
		testBank("OTP Bank", "+36309400700",
				"OTPdirekt - OTPdirekt bejelentkezés ...0572 szàmlàn. Azonositò: 64204461  Jòvàhagyàs 23:38-ig.",
				"64204461");
		testBank("OTP Bank", "+36309400700",
				"OTPdirekt - OTPdirekt bejelentkezés ...9990 szàmlàn. Azonositò: 60381223  Jòvàhagyàs 08:13-ig.",
				"60381223");

	}
}
