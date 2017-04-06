package com.systemonenoc.avatar.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceService {
	
	private final static Logger logger = LoggerFactory.getLogger(InvoiceService.class);
	
	public static Invoice createInvoice(String tin, String tt, String it, String mrc, String bid, String phone,
			String ccid, String pl, BigDecimal taxA, BigDecimal taxB, BigDecimal taxC, BigDecimal taxD)
			throws Exception {
		logger.debug("createInvoice");
		
		Random rnd = new Random();
		Invoice invoice = new Invoice();
		invoice.setTin(tin);
		invoice.setTtype(tt);
		invoice.setDate("2017-12-" + (1 + rnd.nextInt(30)) + " 12:12:12 -0400");
		invoice.setIno("xxxyyy001");
		Date d = new Date();
		d.setDate((1 + rnd.nextInt(30)));
		invoice.setInvoiceDate(d);
		invoice.setItype(it);
		invoice.setJournal("$$date$$ $$nev$$ $$numberRecu$$ $$idata$$ $$signature$$");
		invoice.setMcr(mrc);
		// Header optional
		if (bid != null) {
			invoice.setBid(bid);
		}
		if (phone != null) {
			invoice.setPhone(phone);
		}
		if (ccid != null) {
			invoice.setCcid(ccid);
		}
		invoice.setPlace(pl);
		
		// Taxrates
		BigDecimal a = new BigDecimal(rnd.nextInt(100));
		invoice.setHb(a);
		invoice.setHr(taxA);
		invoice.setHt(taxAmount(a, taxA));
		BigDecimal b = new BigDecimal(rnd.nextInt(100));
		invoice.setMb(b);
		invoice.setMr(taxB);
		invoice.setMt(taxAmount(b, taxB));
		BigDecimal c = new BigDecimal(rnd.nextInt(100));
		invoice.setLb(c);
		invoice.setLr(taxC);
		invoice.setLt(taxAmount(c, taxC));
		BigDecimal dd = new BigDecimal(rnd.nextInt(100));
		invoice.setZb(dd);
		invoice.setZr(taxD);
		invoice.setZt(taxAmount(dd, taxD));
		invoice.setCr(BigDecimal.ZERO);
		invoice.setCt(BigDecimal.ZERO);
		return invoice;
	}
	
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
	
	private static BigDecimal taxAmount(BigDecimal base, BigDecimal rate) {
		return base.multiply(rate).divide(ONE_HUNDRED, 5, RoundingMode.HALF_UP);
	}
	
}
