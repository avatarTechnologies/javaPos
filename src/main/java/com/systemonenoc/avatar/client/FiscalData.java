package com.systemonenoc.avatar.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;

public class FiscalData {
	/**
	 * The structure of the Internal Counters object is: InternalCounters ::= {
	 * NORMAL_TRANSACTIONS_COUNTER INTEGER_COUNTER, SALES_COUNTER
	 * INTEGER_COUNTER, REFUNDS_COUNTER INTEGER_COUNTER,
	 * LAST_AUDITED_TRANSACTION_COUNTER INTEGER_COUNTER,
	 * CURRENTLY_AUDITED_TRANSACTION_COUNTER INTEGER_COUNTER SALES_VALUE_COUNTER
	 * DECIMAL_COUNTER, REFUNDS_VALUE_COUNTER DECIMAL_COUNTER,
	 * SALES_TAX_VALUE_COUNTER DECIMAL_COUNTER, REFUNDS_TAX_VALUE_COUNTER
	 * DECIMAL_COUNTER, }
	 */
	public static byte[] internalData(int invoiceCounter, int salesCounter, int refundsCounter,
			int lastAuditedTransactionId, int currentlyAuditedTransactionId, BigDecimal salesAmount,
			BigDecimal refundsAmount, BigDecimal salesTax, BigDecimal taxReturn) {
		
		byte[] invoiceCounterBytea = longToBCDArray(invoiceCounter);
		byte[] salesCounterBytea = longToBCDArray(salesCounter);
		byte[] refundsCounterBytea = longToBCDArray(refundsCounter);
		byte[] lastAuditedTransactionIdBytea = longToBCDArray(lastAuditedTransactionId);
		byte[] currentlyAuditedTransactionIdBytea = longToBCDArray(currentlyAuditedTransactionId);
		
		byte[] salesAmountBytea = toDecimalCounter(salesAmount);
		byte[] refundsAmountBytea = toDecimalCounter(refundsAmount);
		byte[] salesTaxBytea = toDecimalCounter(salesTax);
		byte[] taxReturnBytea = toDecimalCounter(taxReturn);
		
		Integer lengthOfIntegerCounters = invoiceCounterBytea.length + salesCounterBytea.length
				+ refundsCounterBytea.length + lastAuditedTransactionIdBytea.length
				+ currentlyAuditedTransactionIdBytea.length;
		Integer lengthOfDecimalCounters = salesAmountBytea.length + refundsAmountBytea.length + salesTaxBytea.length
				+ taxReturnBytea.length;
		
		int length = 4 + lengthOfIntegerCounters + 4 + lengthOfDecimalCounters;
		
		ByteBuffer buffer = ByteBuffer.allocate(length);
		
		buffer.put(lengthOfIntegerCounters.byteValue());
		
		buffer.put(invoiceCounterBytea);
		buffer.put(salesCounterBytea);
		buffer.put(refundsCounterBytea);
		buffer.put(lastAuditedTransactionIdBytea);
		buffer.put(currentlyAuditedTransactionIdBytea);
		
		buffer.put(lengthOfDecimalCounters.byteValue());
		
		buffer.put(salesAmountBytea);
		buffer.put(refundsAmountBytea);
		buffer.put(salesTaxBytea);
		buffer.put(taxReturnBytea);
		
		return buffer.array();
	}
	
	/*
	 * long number to bcd byte array e.g. 123 --> (0000) 0001 0010 0011 e.g. 12
	 * ---> 0001 0010
	 */
	public static byte[] longToBCDArray(long num) {
		int digits = 0;
		
		long temp = num;
		while (temp != 0) {
			digits++;
			temp /= 10;
		}
		
		int byteLen = 6;// ;digits % 2 == 0 ? digits / 2 : (digits + 1) / 2;
		boolean isOdd = digits % 2 != 0;
		
		byte bcd[] = new byte[byteLen];
		
		for (int i = 0; i < digits; i++) {
			byte tmp = (byte) (num % 10);
			
			if (i == digits - 1 && isOdd) {
				bcd[i / 2] = tmp;
			} else if (i % 2 == 0) {
				bcd[i / 2] = tmp;
			} else {
				byte foo = (byte) (tmp << 4);
				bcd[i / 2] |= foo;
			}
			
			num /= 10;
		}
		
		for (int i = 0; i < byteLen / 2; i++) {
			byte tmp = bcd[i];
			bcd[i] = bcd[byteLen - i - 1];
			bcd[byteLen - i - 1] = tmp;
		}
		
		return bcd;
	}
	
	public static byte[] toDecimalCounter(BigDecimal value) {
		byte[] intValue = toByteArray(value.intValue());
		byte[] intPart = { (byte) intValue.length };
		intPart = concat(intPart, intValue);
		
		BigDecimal remainder = value.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));
		byte[] decValue = toByteArray(fromBigDecimalToInt(remainder));
		byte[] decPart = { (byte) decValue.length };
		decPart = concat(decPart, decValue);
		
		byte[] result = { (byte) (intPart.length + decPart.length) };
		result = concat(result, intPart);
		result = concat(result, decPart);
		return result;
	}
	
	public static byte[] concat(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
	
	public static byte[] toByteArray(BigDecimal value, int length) {
		return ByteBuffer.allocate(length).putInt(fromBigDecimalToInt(value)).array();
	}
	
	public static byte[] toByteArray(int value, int length) {
		return ByteBuffer.allocate(length).putShort((short) value).array();
	}
	
	public static byte[] toByteArray(int value) {
		byte[] result = {};
		do {
			int units = value % 100;
			int dec = units / 10;
			int unit = units % 10;
			byte[] pair = { (byte) ((16 * dec + unit) & 0xFF) };
			result = concat(pair, result);
			value = value / 100;
		} while (value > 0);
		
		return result;
	}
	
	public static int fromBigDecimalToInt(BigDecimal value) {
		return value.setScale(0, RoundingMode.HALF_UP).intValueExact();
	}
}
