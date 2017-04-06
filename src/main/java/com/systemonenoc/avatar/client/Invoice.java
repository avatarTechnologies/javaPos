package com.systemonenoc.avatar.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.util.Base64Utils;

public class Invoice {
	
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	
	static {
		dateFormat.setLenient(false);
	}
	
	protected static final int defaultLength = 512;
	protected static final int defaultLineLength = 256;
	protected static final byte[] emptyByteArray = new byte[0];
	
	public static enum Type {
		Training, Normal, Proforma
	}
	
	public static enum TransactionType {
		Sale, Refund
	}
	
	private static final Type[] types = Invoice.Type.values();
	private static final TransactionType[] transactionTypes = Invoice.TransactionType.values();
	
	public static final String getType(String value) {
		if ("Pro".startsWith(value)) {
			return "Pro Forma";
		}
		for (TransactionType type : transactionTypes) {
			if (type.name().startsWith(value)) {
				return type.name();
			}
		}
		for (Type type : types) {
			if (type.name().startsWith(value)) {
				return type.name();
			}
		}
		return value;
	}
	
	private int source;
	private String tin;
	private String date;
	private String place;
	private String bid;
	private String ccid;
	private String phone;
	private String itype;
	private String ttype;
	private String mcr;
	private String clientMcr;
	private String ino;
	private String journal;
	private BigDecimal hr = BigDecimal.ZERO;
	private BigDecimal hb = BigDecimal.ZERO;
	private BigDecimal ht = BigDecimal.ZERO;
	private BigDecimal mr = BigDecimal.ZERO;
	private BigDecimal mb = BigDecimal.ZERO;
	private BigDecimal mt = BigDecimal.ZERO;
	private BigDecimal lr = BigDecimal.ZERO;
	private BigDecimal lb = BigDecimal.ZERO;
	private BigDecimal lt = BigDecimal.ZERO;
	private BigDecimal zr = BigDecimal.ZERO;
	private BigDecimal zb = BigDecimal.ZERO;
	private BigDecimal zt = BigDecimal.ZERO;
	private BigDecimal cr = BigDecimal.ZERO;
	private BigDecimal ct = BigDecimal.ZERO;
	private List<InvoiceLine> lines = new ArrayList<>();
	private byte[] linesHashed;
	private Date invoiceDate;
	private byte[] internalDataHash;
	private byte[] signature;
	private int invoiceCounter;
	private int partialCounter;
	private byte[] encryptedCounters;
	
	public byte[] signatureData() throws IOException {
		return signatureData(tin, invoiceDate, bid, itype, ttype, mcr, ino, hr, hb, ht, mr, mb, mt, lr, lb, lt, zr, zb,
				zt, internalDataHash);
	}
	
	public static byte[] signatureData(String tin, Date date, String bid, String itype, String ttype, String mcr,
			String ino, BigDecimal highRate, BigDecimal highBase, BigDecimal highTax, BigDecimal midRate,
			BigDecimal midBase, BigDecimal midTax, BigDecimal lowRate, BigDecimal lowBase, BigDecimal lowTax,
			BigDecimal zeroRate, BigDecimal zeroBase, BigDecimal zeroTax, byte[] internalDataHash) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(defaultLength);
		DataOutputStream dataStream = new DataOutputStream(byteStream);
		dataStream.writeUTF(tin);
		// Hack para truncar los milisegundos, si no se truncan el getTime que
		// había usaba el de la máquina
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, 0);
		dataStream.writeLong(cal.getTimeInMillis());
		writeUTF(dataStream, bid);
		dataStream.writeUTF(itype);
		dataStream.writeUTF(ttype);
		dataStream.writeUTF(mcr);
		dataStream.writeUTF(ino);
		dataStream.write(highRate.stripTrailingZeros().unscaledValue().toByteArray());
		dataStream.write(highBase.stripTrailingZeros().unscaledValue().toByteArray());
		dataStream.write(highTax.stripTrailingZeros().unscaledValue().toByteArray());
		dataStream.write(midRate.stripTrailingZeros().unscaledValue().toByteArray());
		dataStream.write(midBase.stripTrailingZeros().unscaledValue().toByteArray());
		dataStream.write(midTax.stripTrailingZeros().unscaledValue().toByteArray());
		dataStream.write(lowRate.stripTrailingZeros().unscaledValue().toByteArray());
		dataStream.write(lowBase.stripTrailingZeros().unscaledValue().toByteArray());
		dataStream.write(lowTax.stripTrailingZeros().unscaledValue().toByteArray());
		dataStream.write(zeroRate.stripTrailingZeros().unscaledValue().toByteArray());
		dataStream.write(zeroBase.stripTrailingZeros().unscaledValue().toByteArray());
		dataStream.write(zeroTax.stripTrailingZeros().unscaledValue().toByteArray());
		dataStream.write(internalDataHash);
		return byteStream.toByteArray();
	}
	
	public static final void writeBigDecimal(DataOutputStream dataStream, BigDecimal bigDecimal) throws IOException {
		byte[] byteArray = bigDecimal.unscaledValue().toByteArray();
		dataStream.write((byte) byteArray.length);
		dataStream.write(byteArray);
		dataStream.write((byte) bigDecimal.scale());
	}
	
	public static final void writeUTF(DataOutputStream dataStream, String value) throws IOException {
		dataStream.writeUTF(value == null ? "" : value);
	}
	
	public byte[] serialize() throws IOException {
		ByteArrayOutputStream byteStream;
		if (lines != null) {
			byteStream = new ByteArrayOutputStream(defaultLength + lines.size() * defaultLineLength);
		} else {
			byteStream = new ByteArrayOutputStream(defaultLength);
		}
		DataOutputStream dataStream = new DataOutputStream(byteStream);
		dataStream.writeUTF(tin);
		dataStream.writeUTF(date);
		dataStream.writeLong(invoiceDate.getTime());
		writeUTF(dataStream, place);
		writeUTF(dataStream, bid);
		writeUTF(dataStream, ccid);
		writeUTF(dataStream, phone);
		dataStream.writeUTF(itype);
		dataStream.writeUTF(ttype);
		dataStream.writeUTF(mcr);
		writeUTF(dataStream, clientMcr);
		dataStream.writeUTF(ino);
		writeBigDecimal(dataStream, hr);
		writeBigDecimal(dataStream, hb);
		writeBigDecimal(dataStream, ht);
		writeBigDecimal(dataStream, mr);
		writeBigDecimal(dataStream, mb);
		writeBigDecimal(dataStream, mt);
		writeBigDecimal(dataStream, lr);
		writeBigDecimal(dataStream, lb);
		writeBigDecimal(dataStream, lt);
		writeBigDecimal(dataStream, zr);
		writeBigDecimal(dataStream, zb);
		writeBigDecimal(dataStream, zt);
		writeBigDecimal(dataStream, cr);
		writeBigDecimal(dataStream, ct);
		dataStream.writeInt(invoiceCounter);
		dataStream.writeInt(partialCounter);
		dataStream.writeUTF(encodedString(internalDataHash));
		dataStream.writeUTF(encodedString(signature));
		dataStream.writeUTF(encodedString(encryptedCounters));
		writeUTF(dataStream, journal);
		if (lines != null) {
			for (InvoiceLine line : lines) {
				if (line != null) {
					writeUTF(dataStream, line.getEan());
					writeUTF(dataStream, line.getName());
					writeBigDecimal(dataStream, line.getQuantity());
					writeBigDecimal(dataStream, line.getBase());
					writeBigDecimal(dataStream, line.getDiscount());
					writeUTF(dataStream, line.getCode());
					writeBigDecimal(dataStream, line.getTotal());
					writeUTF(dataStream, line.getCurrentHash());
				}
			}
		}
		return byteStream.toByteArray();
	}
	
	public static final String encodedString(byte[] data) {
		if (data == null) {
			return "";
		}
		return new String(Base64Utils.encode(data));
	}
	
	public String toUrlQuery(String endpoint, String internalData, String signature) {
		try {
			StringBuilder sb = new StringBuilder(endpoint);
			append(sb, '?', "tin", tin);
			append(sb, '&', "date", date);
			append(sb, '&', "bid", bid);
			append(sb, '&', "itype", itype.substring(0, 1));
			append(sb, '&', "ttype", ttype.substring(0, 1));
			append(sb, '&', "mcr", mcr);
			append(sb, '&', "ino", ino);
			append(sb, '&', "data", internalData);
			append(sb, '&', "sign", signature);
			append(sb, '&', "hr", hr);
			append(sb, '&', "hb", hb);
			append(sb, '&', "ht", ht);
			append(sb, '&', "mr", mr);
			append(sb, '&', "mb", mb);
			append(sb, '&', "mt", mt);
			append(sb, '&', "lr", lr);
			append(sb, '&', "lb", lb);
			append(sb, '&', "lt", lt);
			append(sb, '&', "zr", zr);
			append(sb, '&', "zb", zb);
			append(sb, '&', "zt", zt);
			append(sb, '&', "cr", cr);
			append(sb, '&', "ct", ct);
			return sb.toString();
		} catch (IllegalStateException | IOException e) {
			return "Unable to generate verification url: " + e.getMessage();
		}
	}
	
	private static final void append(StringBuilder sb, char separator, String name, String value)
			throws UnsupportedEncodingException {
		if (value == null) {
			return;
		}
		value = value.trim();
		if (!value.isEmpty()) {
			sb.append(separator).append(name).append('=').append(URLEncoder.encode(value, "UTF-8"));
		}
	}
	
	private static final DecimalFormat decimalFormat = new DecimalFormat("0.######");
	
	private static final void append(StringBuilder sb, char separator, String name, BigDecimal value)
			throws UnsupportedEncodingException {
		if (value == null) {
			return;
		}
		if (BigDecimal.ZERO.compareTo(value) != 0) {
			sb.append(separator).append(name).append('=').append(decimalFormat.format(value));
		}
	}
	
	public void setTin(String tin) {
		this.tin = tin;
	}
	
	@Override
	public String toString() {
		return "Invoice [tin=" + tin + ", date=" + date + ", place=" + place + ", bid=" + bid + ", ccid=" + ccid
				+ ", phone=" + phone + ", itype=" + itype + ", ttype=" + ttype + ", mcr=" + mcr + ", clientMcr="
				+ clientMcr + ", ino=" + ino + ", journal=" + journal + ", hr=" + hr + ", hb=" + hb + ", ht=" + ht
				+ ", mr=" + mr + ", mb=" + mb + ", mt=" + mt + ", lr=" + lr + ", lb=" + lb + ", lt=" + lt + ", zr=" + zr
				+ ", zb=" + zb + ", zt=" + zt + ", cr=" + cr + ", ct=" + ct + ", lines=" + lines + ", linesHashed="
				+ Arrays.toString(linesHashed) + ", invoiceDate=" + invoiceDate + ", internalDataHash="
				+ Arrays.toString(internalDataHash) + ", signature=" + Arrays.toString(signature) + ", invoiceCounter="
				+ invoiceCounter + ", partialCounter=" + partialCounter + ", encryptedCounters="
				+ Arrays.toString(encryptedCounters) + "]";
	}
	
	public String getTin() {
		return tin;
	}
	
	public void setDate(String date) throws ParseException {
		this.date = date;
		invoiceDate = date == null ? null : dateFormat.parse(date);
	}
	
	public String getDate() {
		return date;
	}
	
	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
		date = invoiceDate == null ? null : dateFormat.format(invoiceDate);
	}
	
	public Date getInvoiceDate() {
		return invoiceDate;
	}
	
	public void setPlace(String place) {
		this.place = place;
	}
	
	public String getPlace() {
		return place;
	}
	
	public void setBid(String bid) {
		this.bid = bid;
	}
	
	public String getBid() {
		return bid;
	}
	
	public void setCcid(String ccid) {
		this.ccid = ccid;
	}
	
	public String getCcid() {
		return ccid;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public void setItype(String itype) {
		this.itype = itype;
	}
	
	public String getItype() {
		return itype;
	}
	
	public void setTtype(String ttype) {
		this.ttype = ttype;
	}
	
	public String getTtype() {
		return ttype;
	}
	
	public void setMcr(String mcr) {
		this.mcr = mcr;
	}
	
	public String getMcr() {
		return mcr;
	}
	
	public void setIno(String ino) {
		this.ino = ino;
	}
	
	public String getIno() {
		return ino;
	}
	
	public void setJournal(String journal) {
		this.journal = journal;
	}
	
	public String getJournal() {
		return journal;
	}
	
	public BigDecimal getHr() {
		return hr;
	}
	
	public void setHr(BigDecimal hr) {
		this.hr = roundHack(hr);
	}
	
	public BigDecimal getHb() {
		return hb;
	}
	
	public void setHb(BigDecimal hb) {
		this.hb = roundHack(hb);
	}
	
	public BigDecimal getHt() {
		return ht;
	}
	
	public void setHt(BigDecimal ht) {
		this.ht = roundHack(ht);
	}
	
	public BigDecimal getMr() {
		return mr;
	}
	
	public void setMr(BigDecimal mr) {
		this.mr = roundHack(mr);
	}
	
	public BigDecimal getMb() {
		return mb;
	}
	
	public void setMb(BigDecimal mb) {
		this.mb = roundHack(mb);
	}
	
	public BigDecimal getMt() {
		return mt;
	}
	
	public void setMt(BigDecimal mt) {
		this.mt = roundHack(mt);
	}
	
	public BigDecimal getLr() {
		return lr;
	}
	
	public void setLr(BigDecimal lr) {
		this.lr = roundHack(lr);
	}
	
	public BigDecimal getLb() {
		return lb;
	}
	
	public void setLb(BigDecimal lb) {
		this.lb = roundHack(lb);
	}
	
	public BigDecimal getLt() {
		return lt;
	}
	
	public void setLt(BigDecimal lt) {
		this.lt = roundHack(lt);
	}
	
	public BigDecimal getZr() {
		return zr;
	}
	
	public void setZr(BigDecimal zr) {
		this.zr = roundHack(zr);
	}
	
	public BigDecimal getZb() {
		return zb;
	}
	
	public void setZb(BigDecimal zb) {
		this.zb = roundHack(zb);
	}
	
	public BigDecimal getZt() {
		return zt;
	}
	
	public void setZt(BigDecimal zt) {
		this.zt = roundHack(zt);
	}
	
	public BigDecimal getCt() {
		return ct;
	}
	
	public void setCt(BigDecimal ct) {
		this.ct = ct;
	}
	
	public byte[] getInternalDataHash() {
		return internalDataHash;
	}
	
	public void setInternalDataHash(byte[] internalDataHash) {
		this.internalDataHash = internalDataHash;
	}
	
	public int getInvoiceCounter() {
		return invoiceCounter;
	}
	
	public void setInvoiceCounter(int invoiceCounter) {
		this.invoiceCounter = invoiceCounter;
	}
	
	public int getPartialCounter() {
		return partialCounter;
	}
	
	public void setPartialCounter(int partialCounter) {
		this.partialCounter = partialCounter;
	}
	
	public List<InvoiceLine> getLines() {
		return lines;
	}
	
	public void setLines(List<InvoiceLine> lines) {
		this.lines = lines;
	}
	
	public byte[] getSignature() {
		return signature;
	}
	
	public void setSignature(byte[] signature) {
		this.signature = signature;
	}
	
	public byte[] getLinesHashed() {
		return linesHashed;
	}
	
	public void setLinesHashed(byte[] linesHashed) {
		this.linesHashed = linesHashed;
	}
	
	public byte[] getEncryptedCounters() {
		return encryptedCounters;
	}
	
	public void setEncryptedCounters(byte[] encryptedCounters) {
		this.encryptedCounters = encryptedCounters;
	}
	
	public String getClientMcr() {
		return clientMcr;
	}
	
	public void setClientMcr(String clientMcr) {
		this.clientMcr = clientMcr;
	}
	
	public BigDecimal roundHack(BigDecimal value) {
		return value.round(new MathContext(15, RoundingMode.HALF_UP));
	}
	
	public int getSource() {
		return source;
	}
	
	public void setSource(int source) {
		this.source = source;
	}
	
	public BigDecimal getCr() {
		return cr;
	}
	
	public void setCr(BigDecimal cr) {
		this.cr = cr;
	}
}
