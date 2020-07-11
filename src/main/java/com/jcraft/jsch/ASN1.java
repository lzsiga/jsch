package com.jcraft.jsch;

public class ASN1 extends Buffer {
/* bits 2^7 and 2^6 */
  public static final int UNIVERSAL=       0x00;
  public static final int APPLICATION=     0x40;
  public static final int CONTEXT_DEFINED= 0x80;
  public static final int PRIVATE=         0xc0;
/* bit 2^5 */
  public static final int PRIMITIVE=       0x00;
  public static final int CONSTRUCTED=     0x20;
/* actual values */
/* UNIVERSAL, PRIMITIVE */
  public static final int END_OF_CONTENT = 0x00;
  public static final int BOOLEAN        = 0x01;
  public static final int INTEGER        = 0x02;
  public static final int BIT_STRING     = 0x03;
  public static final int OCTET_STRING   = 0x04;
  public static final int NULL           = 0x05;
  public static final int OBJECT         = 0x06;
/* UNIVERSAL, CONSTRUCTED */
  public static final int SEQUENCE       = 0x30;
  public static final int SEQUENCE_OF    = 0x30;
  public static final int SET            = 0x31;
  public static final int SET_OF         = 0x31;

  int asn1Type;

  public ASN1 (int pAsn1Type, Buffer pBuffer) {
    super(pBuffer.buffer, true);
    asn1Type= pAsn1Type;
  }

  public ASN1 (int pAsn1Type, byte[] buf) {
    super(buf, true);
    asn1Type= pAsn1Type;
  }

  public ASN1 (int pAsn1Type, int capacity) {
    super(capacity);
    asn1Type= pAsn1Type;
  }
}
