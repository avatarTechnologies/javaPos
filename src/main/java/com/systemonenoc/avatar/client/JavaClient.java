package com.systemonenoc.avatar.client;

import java.math.BigDecimal;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaClient {
	
	private final static Logger logger = LoggerFactory.getLogger(JavaClient.class);
	
	private final static String G5 = "g5";
	private final static String VSDC = "vsdc";
	
	static String pemFile;
	static String privateKey;
	static String host;
	static String tin;
	static String mrc;
	
	static BigDecimal taxA = new BigDecimal(0);
	static BigDecimal taxB = new BigDecimal(18);
	static BigDecimal taxC = new BigDecimal(16);
	static BigDecimal taxD = new BigDecimal(32);
	static int transactions = 0;
	static String tt = "Sale";
	static String it = "Normal";
	static String pl = "";
	static String bid;
	static String ccid;
	static String phone;
	static String cl;
	static String p12;
	static String pass;
	
	public static void main(String[] args) throws Exception {
		try {
			Options options = new Options();
			
			options.addRequiredOption("t", "tin", true, "TIN number");
			options.addRequiredOption("m", "mrc", true, "Machine registration code");
			options.addRequiredOption("e", "endpoint", true, "Endpoint Url ");
			
			options.addOption("cl", "client", true, "Client type. g5(defautl) or vsdc]");
			
			options.addOption("p", "private", true, "RSA private key");
			options.addOption("c", "cert", true, "ATAX public certificate");
			options.addOption("p12", "p12", true, "Client certificate for mutual auth.");
			options.addOption("pass", "password", true, "p12 password");
			
			options.addOption("h", "help", false, "Help command");
			
			options.addOption("tr", "transactions", true, "Number of transactions to send. One single thread.");
			options.addOption("tA", "taxA", true, "taxA. Default value is 0");
			options.addOption("tB", "taxB", true, "taxB. Default value is 18");
			options.addOption("tC", "taxC", true, "taxC. Default value is 16");
			options.addOption("tD", "taxD", true, "taxD. Default value is 32");
			options.addOption("tt", "tType", true, "Transaction type. Sale|Refund");
			options.addOption("it", "iType", true, "Invoice type. Normal|Pro Forma|Training");
			options.addOption("pl", "place", true, "Merchant location");
			options.addOption("bid", "bid", true, "Buyer TIN");
			options.addOption("ccid", "cid", true, "Buyer Cost Center Id");
			options.addOption("phone", "phone", true, "Buyer phone");
			
			CommandLineParser parser = new DefaultParser();
			CommandLine line = parser.parse(options, args);
			
			if (line.hasOption("h")) {
				final HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("Avatar java client [options]", options);
				System.exit(1);
			}
			
			tin = line.getOptionValue("tin");
			mrc = line.getOptionValue("mrc");
			host = line.getOptionValue("endpoint");
			privateKey = line.getOptionValue("private");
			pemFile = line.getOptionValue("cert");
			cl = line.getOptionValue("cl");
			
			// Optional parameters
			if (line.hasOption("transactions")) {
				transactions = Integer.parseInt(line.getOptionValue("transactions"));
			}
			if (line.hasOption("taxA")) {
				taxA = BigDecimal.valueOf(Long.parseLong(line.getOptionValue("taxA")));
			}
			if (line.hasOption("taxB")) {
				taxB = BigDecimal.valueOf(Long.parseLong(line.getOptionValue("taxB")));
			}
			if (line.hasOption("taxC")) {
				taxC = BigDecimal.valueOf(Long.parseLong(line.getOptionValue("taxC")));
			}
			if (line.hasOption("taxD")) {
				taxD = BigDecimal.valueOf(Long.parseLong(line.getOptionValue("taxD")));
			}
			if (line.hasOption("tt")) {
				tt = line.getOptionValue("tt");
			}
			if (line.hasOption("it")) {
				it = line.getOptionValue("it");
			}
			if (line.hasOption("pl")) {
				pl = line.getOptionValue("pl");
			}
			if (line.hasOption("bid")) {
				bid = line.getOptionValue("bid");
			}
			if (line.hasOption("ccid")) {
				ccid = line.getOptionValue("ccid");
			}
			if (line.hasOption("phone")) {
				phone = line.getOptionValue("phone");
			}
			if (line.hasOption("cl")) {
				cl = line.getOptionValue("cl");
				if (!cl.toLowerCase().equals(G5) && !cl.toLowerCase().equals(VSDC)) {
					logger.error("Unknown client type. Please use vsdc or g5");
					return;
				}
				if (cl.toLowerCase().equals(VSDC)) {
					if (!line.hasOption("p12") || !line.hasOption("pass")) {
						logger.error("Please check vsdc params.");
						return;
					} else {
						p12 = line.getOptionValue("p12");
						pass = line.getOptionValue("pass");
					}
				}
			} else {
				cl = G5;
			}
			new JavaClient().send();
			
		} catch (ParseException e) {
			logger.error("{}", e);
		}
		
	}
	
	public void send() throws Exception {
		logger.debug("send");
		
		for (int i = 0; i <= transactions; i++) {
			Invoice invoice = InvoiceService.createInvoice(tin, tt, it, mrc, bid, phone, ccid, pl, taxA, taxB, taxC,
					taxD);
			if (cl.toLowerCase().equals(G5)) {
				if (pemFile == null) {
					logger.error("Client's private key is mandatory if your are emulating a G5.");
					return;
				}
				if (privateKey == null) {
					logger.error("Server's Public key is mandatory if your are emulating a G5.");
					return;
				}
				CryptoProvider.load(getClass().getClassLoader().getResource(pemFile).getFile(),
						getClass().getClassLoader().getResource(privateKey).getFile());
				RestClient.sendG5(invoice, host);
			} else if (cl.toLowerCase().equals(VSDC)) {
				RestClient.sendVsdc(invoice, host, p12, pass);
			}
		}
	}
	
}
