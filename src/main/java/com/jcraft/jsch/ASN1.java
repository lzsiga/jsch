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

/** Unlike the previous ones, the following constructors
 *  fetch 'type' and 'length' from the byte-array themself
 */
  public ASN1(byte[] b) throws ASN1Exception{
    super(b);
    constructorHelper(b, 0, b.length);
  }

  public ASN1(byte[] b, int start, int length) throws ASN1Exception{
    super(b);
    constructorHelper(b, start, length);
  }

  public ASN1(Buffer b) throws ASN1Exception{
    super(b);
    constructorHelper(b.buffer, b.s, b.index-b.s);
  }

  private void constructorHelper(byte[] b, int start, int length)
  throws ASN1Exception{
    if (start<0 || length<0 || start+length>b.length){
      throw new ASN1Exception(String.format(
        "jcraft.jsch.ASN1: Invalid parameters for constructor"+
        " (buffer.length=%d start=%d length=%d)",
        b.length, start, index));
    }
    this.buffer=b;
    this.s=start;
    this.index=start+length;

    int[] asn1type= {0};
    int[] partlen= {0};
    int rc= this.getASN1PartHead(asn1type, partlen);
    if(rc!=0){
      throw new ASN1Exception(
        "jcraft.jsch.ASN1: Couldn't fetch ASN1-type or length from byte-array");
    }
    this.asn1Type=asn1type[0];
    this.index=this.s+partlen[0];
  }

  public ASN1[] peekContents() throws ASN1Exception {
    ASN1 atmp=new ASN1(this);
    return atmp.getContents();
  }

  public ASN1[] getContents() throws ASN1Exception {
    java.util.List<ASN1> work= new java.util.ArrayList<ASN1>();

    while(s<index){
      ASN1 atmp= getASN1Part();
      if(atmp==null){
        throw new ASN1Exception(
          "jcraft.jsch.ASN1.getContent: Error parsing ASN1-element for child-elements");
      }
      work.add(atmp);
    }

    ASN1[] ret= new ASN1[work.size()];
    work.toArray(ret);
    return ret;
  }

  public boolean equals(int pAsn1Type, byte[] pBytes){
    return asn1Type==pAsn1Type && equals(pBytes);
  }

  public String toString(){
    String tmp= super.toString();
    return tmp+String.format(";asn1Type=0x%02x", asn1Type);
  }
}
