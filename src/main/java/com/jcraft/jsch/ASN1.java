package com.jcraft.jsch;

public class ASN1 extends Buffer {
  public static final int
/* bits 2^7 and 2^6 */
    UNIVERSAL      = 0x00,
    APPLICATION    = 0x40,
    CONTEXT_DEFINED= 0x80,
    PRIVATE        = 0xc0,
/* bit 2^5 */
    PRIMITIVE      = 0x00,
    CONSTRUCTED    = 0x20,
/* actual values */
/* UNIVERSAL, PRIMITIVE */
    END_OF_CONTENT = 0x00,
    BOOLEAN        = 0x01,
    INTEGER        = 0x02,
    BIT_STRING     = 0x03,
    OCTET_STRING   = 0x04,
    NULL           = 0x05,
    OBJECT         = 0x06,
/* UNIVERSAL, CONSTRUCTED */
    SEQUENCE       = 0x30,
    SEQUENCE_OF    = 0x30,
    SET            = 0x31,
    SET_OF         = 0x31,
/* CONTEXT_DEFINED, CONSTRUCTED */
    PARAM_0        = 0xA0,
    PARAM_1        = 0xA1;

  int asn1Type;

  public ASN1(int pAsn1Type, Buffer pBuffer){
    super(pBuffer);
    asn1Type= pAsn1Type;
  }

  public ASN1(int pAsn1Type, byte[] buf){
    super(buf, true);
    asn1Type= pAsn1Type;
  }

  public ASN1(int pAsn1Type, int capacity){
    super(capacity);
    asn1Type= pAsn1Type;
  }

  public boolean equals(int pAsn1Type, byte[] pBytes){
    return asn1Type==pAsn1Type && equals(pBytes);
  }

  public String toString(){
    String tmp= super.toString();
    return tmp+String.format(";asn1Type=0x%02x", asn1Type);
  }
}
