/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/*
Copyright (c) 2015-2018 ymnk, JCraft,Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright 
     notice, this list of conditions and the following disclaimer in 
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.jcraft.jsch;

public class KeyPairECDSA extends KeyPair{
  static class ECDSA_Variant {
    public final String sMethodName; /* "ecdsa-sha2-nistp256" */
    public final byte[] bMethodName;
    public final String sCurveName;  /* "nistp256" */
    public final byte[] bCurveName;
    public final int keySize;       /* bits: 256, 384, 521 */
    public final byte[] oid;        /* 1.2.840.10045.3.1.7, 1.3.132.0.34, 1.3.132.0.35 */

    public ECDSA_Variant (String pCurveName, int pKeySize, byte[] pOid) {
      sMethodName= "ecdsa-sha2-"+pCurveName;
      bMethodName= Util.str2byte(sMethodName);
      sCurveName= pCurveName;
      bCurveName= Util.str2byte(sCurveName);
      keySize= pKeySize;
      oid= pOid;
    }
  }

  static final ECDSA_Variant variants[] = {
    new ECDSA_Variant("nistp256", 256, OID.nistp256),
    new ECDSA_Variant("nistp384", 384, OID.nistp384),
    new ECDSA_Variant("nistp521", 521, OID.nistp521)
  };

  private static ECDSA_Variant findVariantByMethod(byte[] method) {
    for(int i=0; i<variants.length; ++i) {
      if(Util.array_equals(method, variants[i].bMethodName)){
        return variants[i];
      }
    }
    return null;
  }

  private static ECDSA_Variant findVariantByMethod(String method) {
    for(int i=0; i<variants.length; ++i) {
      if(method.equals(variants[i].sMethodName)){
        return variants[i];
      }
    }
    return null;
  }

  private static ECDSA_Variant findVariantByCurve(byte[] curve) {
    for(int i=0; i<variants.length; ++i) {
      if(Util.array_equals(curve, variants[i].bCurveName)){
        return variants[i];
      }
    }
    return null;
  }

  private static ECDSA_Variant findVariantByBits(int bits) {
    for(int i=0; i<variants.length; ++i) {
      if(bits==variants[i].keySize){
        return variants[i];
      }
    }
    return null;
  }

  private static ECDSA_Variant findVariantByPubBytes(int bytes) {
    for(int i=0; i<variants.length; ++i) {
      int publen= ((variants[i].keySize+7)/8)*2;
      if (bytes>=publen && bytes<=publen+2) { /* public key is often prefixed with 0x04;
                                                 sometimes with 0x00 0x04 */
        return variants[i];
      }
    }
    return null;
  }

  private static ECDSA_Variant findVariantByPrvBytes(int bytes) {
    for(int i=0; i<variants.length; ++i) {
      int prvlen= (variants[i].keySize+7)/8;
      if (bytes>=prvlen && bytes<=prvlen+1) { /* private key is sometimes prefixed with 0x00 (if MSB is set) */
        return variants[i];
      }
    }
    return null;
  }

  private static ECDSA_Variant findVariantByOid(byte[] oid) {
    for(int i=0; i<variants.length; ++i) {
      if(Util.array_equals(oid, variants[i].oid)){
        return variants[i];
      }
    }
    return null;
  }

  private ECDSA_Variant variant= variants[0]; /* default to nistp256 */
// private byte[] curveName=variant.bCurveName;
// private int key_size=variant.keySize;
  private byte[] r_array;
  private byte[] s_array;
  private byte[] prv_array;

  public KeyPairECDSA(JSch jsch){
    this(jsch, null, null, null, null);
  }

  public KeyPairECDSA(JSch jsch, byte[] pubkey){
    this(jsch, null, null, null, null);

    if(pubkey!=null){
      parsePublicKey (pubkey); /* should we report error? Exception? */
    }
  }

  public KeyPairECDSA(JSch jsch,
                      byte[] pCurveName,
                      byte[] r_array,
                      byte[] s_array,
                      byte[] prv_array) {
    super(jsch);
    if(pCurveName!=null) {
      variant= findVariantByCurve(pCurveName);
/*    if(variant==null) throw new JSchException("Invalid curve name for ECDSA"); */

    } else if (prv_array!=null) {
      variant= findVariantByPrvBytes(prv_array.length);
/*    if (variant==null) {
        String msg= String.format("Invalid private-key length (%d bytes) for ECDSA", prv_array.length);
        throw new JSchException(msg);
      } */
    }
    this.r_array = r_array;
    this.s_array = s_array;
    this.prv_array = prv_array;
  }

  void generate(int key_size) throws JSchException{
    variant= findVariantByBits(key_size);
    if(variant==null) {
        String msg= String.format("Invalid key length (%d bits) for ECDSA", key_size);
        throw new JSchException(msg);
    }
    try{
      Class c=Class.forName(jsch.getConfig("keypairgen.ecdsa"));
      KeyPairGenECDSA keypairgen=(KeyPairGenECDSA)(c.newInstance());
      keypairgen.init(key_size);
      prv_array=keypairgen.getD();
      r_array=keypairgen.getR();
      s_array=keypairgen.getS();
      keypairgen=null;
    }
    catch(Exception e){
      if(e instanceof Throwable)
        throw new JSchException(e.toString(), (Throwable)e);
      throw new JSchException(e.toString());
    }
  }

  private static final byte[] begin = 
    Util.str2byte("-----BEGIN EC PRIVATE KEY-----");
  private static final byte[] end =
    Util.str2byte("-----END EC PRIVATE KEY-----");

  byte[] getBegin(){ return begin; }
  byte[] getEnd(){ return end; }

  byte[] getPrivateKey(){

    byte[] tmp = new byte[1]; tmp[0]=1;

    byte[] oid = variants[
                        (r_array.length>=64) ? 2 :
                       ((r_array.length>=48) ? 1 : 0)
                     ].oid;

    byte[] point = toPoint(r_array, s_array);

    int bar = ((point.length+1)&0x80)==0 ? 3 : 4;
    byte[] foo = new byte[point.length+bar];
    System.arraycopy(point, 0, foo, bar, point.length);
    foo[0]=0x03;                     // BITSTRING 
    if(bar==3){
      foo[1]=(byte)(point.length+1);
    }
    else {
      foo[1]=(byte)0x81;
      foo[2]=(byte)(point.length+1);
    }
    point = foo;

    int content=
      1+countLength(tmp.length) + tmp.length +
      1+countLength(prv_array.length) + prv_array.length +
      1+countLength(oid.length) + oid.length +
      1+countLength(point.length) + point.length;

    int total=
      1+countLength(content)+content;   // SEQUENCE

    byte[] plain=new byte[total];
    int index=0;
    index=writeSEQUENCE(plain, index, content);
    index=writeINTEGER(plain, index, tmp);
    index=writeOCTETSTRING(plain, index, prv_array);
    index=writeDATA(plain, (byte)0xa0, index, oid);
    index=writeDATA(plain, (byte)0xa1, index, point);

    return plain;
  }

  /**
   * This function parses a public key
   *
   * supported format: "SSH2 PUBLIC KEY" which is equal to the single-line
   * format of OpenSSH, also it is the 'Public' part of the .PPK file
   * length(4) "ecdsa-sha2-nistp256" (384, 521)
   * length(4) "nistp256" (384, 521)
   * length(4) 0x04 (it means uncompressed data) + public key (2*32,2*48,2*66 byte)
   * sometimes there is 0x00 before 0x04 (why?)
   */
  boolean parsePublicKeySSH2(Buffer buf) {

/* 1st part: method name -- should be one of the three known values */
    int partlen= buf.getInt();
    if (partlen<0 || partlen>buf.getLength()) return false;

    byte[] bPart= new byte[partlen];
    buf.getByte(bPart);

    variant= findVariantByMethod(bPart);
    if(variant==null)
      return false;

/* 2nd part: curve name -- should be compatible with the previous */
    partlen= buf.getInt();
    if (partlen<0 || partlen>buf.getLength()) return false;

    bPart= new byte[partlen];
    buf.getByte(bPart);
    if(!Util.array_equals(bPart, variant.bCurveName))
      return false;

/* 3nd part: binary key 2*((keysize+7)/8) bytes
   it should be prefixed with 0x04 (also 0x00 is possible before 0x04) */
    partlen= buf.getInt();
    if (partlen<0 || partlen>buf.getLength()) return false;

    byte[][] brs=fromPoint(buf, variant.keySize);
    if (brs==null)
      return false;
    r_array = brs[0];
    s_array = brs[1];

    return true;
  }

/** This method parses a single line public key (of type ECDSA)
  * Example:
  *  ecdsa-sha2-nistp521 AAAAE...yQQ== projects@HP-11653
  *  ^identifier         ^data(base64) ^comment
  * the 'data' part (after base64-decode) can be parsed with 'parsePublicKeySSH2'
  */
  boolean parsePublicKeySingleLine(Buffer buf) {
    String stmp= null;
    try{
      stmp= new String (buf.buffer, buf.s, buf.index-buf.s, "ISO-8859-1");
    }catch(java.io.UnsupportedEncodingException e) {
      return false;
    }
    String sparts[]=stmp.trim().split("\\s+");
    if(sparts.length<2) return false;

/* 1st part: method, human readable, eg: ecdsa-sha2-nistp521 */
    variant=findVariantByMethod(sparts[0]);
    if(variant==null) return false;

/* 2nd part: binary data encoded in Base64, we forward it to `parsePublicKeySSH` */
    byte[] public_base64= null;
    byte[] public_binary= null;
    try {
      public_base64=sparts[1].getBytes("ISO-8859-1");
      public_binary=Util.fromBase64(public_base64, 0, public_base64.length);
    }catch(java.io.UnsupportedEncodingException e){
      return false;
    }catch(JSchException ex){
      return false;
    }
    boolean success=parsePublicKeySSH2(new Buffer(public_binary, true));
    if(success){
/* 3rd part (optional): comment */
      if(sparts.length>=3){
        publicKeyComment= sparts[2];
      }
    }
    return success;
  }

  /**
   * This function parses a public key in "EC PUBLIC KEY" format
   * which is ASN1
   * 'ssh-keygen -e -m PKCS8' generates this
   * sample:
   *   3059 SEQUENCE, length=0x59
   *   3013 SEQUENCE, length=0x13
   *   0607 OBJECT, length=0x07, 2a8648ce3d0201   OID 1.2.840.10045.2.1   ecPublicKey
   *   0608 OBJECT, length=0x08, 2a8648ce3d030107 OID 1.2.840.10045.3.1.7 prime256v1
   *   0342 BIT STRING length=0x42, 000467a36ebe40..0e1e7bcf
   */
  boolean parsePublicKeyPkcs8(Buffer buf) {
    if (buf.getLength()<64) return false;

    ASN1 aTotal= buf.getASN1Part();
    if (aTotal==null || aTotal.asn1Type!=ASN1.SEQUENCE) return false;

    ASN1 aId= aTotal.getASN1Part();
    if (aId==null || aId.asn1Type!=ASN1.SEQUENCE) return false;

    ASN1 aOid1= aTotal.getASN1Part();
    if(aOid1==null || !aOid1.equals(ASN1.OBJECT, OID.ecPublicKey))
      return false;

    ASN1 aOid2= aTotal.getASN1Part();
    if (aOid2==null || aOid2.asn1Type!=ASN1.OBJECT)
      return false;
    variant= findVariantByOid(aOid2.peekContent());
    if(variant==null){
      return false;
    }

    ASN1 aPubKey= aTotal.getASN1Part();
    if(aPubKey==null || aPubKey.asn1Type!=ASN1.BIT_STRING)
      return false;
    byte[][] brs= fromPoint(aPubKey, variant.keySize);
    r_array = brs[0];
    s_array = brs[1];

    return true;
  }

  /**
   * This function parses a public key
   * It guesses the file-format from the first byte
   *
   * supported formats:
   *  'single line' (see method 'parsePublicKeySingleLine')
   *  'SSH2 PUBLIC KEY' (see method parsePublicKeySingleLine)
   */
   boolean parsePublicKey(byte[] pubkey) {
     if (pubkey.length<64 || pubkey.length>8192) return false;
     Buffer buf= new Buffer(pubkey,true);

     int byte1= buf.peekByte();
     if(byte1==0x00) return parsePublicKeySSH2(buf);
     else if(byte1==(int)'e')  return parsePublicKeySingleLine(buf);
     else if(byte1==(int)0x30) return parsePublicKeyPkcs8(buf);
     return false;
   }

  /**
   * This function parses an "EC PRIVATE KEY" (ASN1 format)
   * Defined in RFC5915:
   * ECPrivateKey ::= SEQUENCE {
   *   version        INTEGER { ecPrivkeyVer1(1) } (ecPrivkeyVer1),
   *   privateKey     OCTET STRING
   *   parameters [0] ECParameters {{ NamedCurve }} OPTIONAL,
   *   publicKey  [1] BIT STRING OPTIONAL
   *
   * 'ssh-keygen' calls this 'PEM format (-m PEM)'
   * 'puttygen' calls this 'old OpenSSH format (-O private-openssh)'
   * 'openssl pkey' and 'openssl asn1parse' can process this format
   * sample: 
   *    0:d=0  hl=2 l= 120 cons: SEQUENCE	    3078
   *    2:d=1  hl=2 l=   1 prim:  INTEGER           0201 01
   *    5:d=1  hl=2 l=  33 prim:  OCTET STRING      0421 0093BC4014AC...
   *   40:d=1  hl=2 l=  10 cons:  cont [ 0 ]        A00A
   *   42:d=2  hl=2 l=   8 prim:   OBJECT           0608 1.2.840.10045.3.1.7 (prime256v1)
   *   52:d=1  hl=2 l=  68 cons:  cont [ 1 ]        A144
   *   54:d=2  hl=2 l=  66 prim:   BIT STRING	    0342 000467a36ebe...
   */
  boolean parseOpenSSHPem(byte[] plain){
    try{
      Buffer buf= new Buffer(plain, true);
      ASN1 aTot= buf.getASN1Part();
      if (aTot.asn1Type!=ASN1.SEQUENCE)
        return false;

      ASN1 aVer= aTot.getASN1Part();
      if (aVer.asn1Type!=ASN1.INTEGER || aVer.getLength()!=1 || aVer.peekByte()!=0x01)
        return false;

      ASN1 aPriv= aTot.getASN1Part();
      if(aPriv.asn1Type!=ASN1.OCTET_STRING)
        return false;
      if(aPriv.peekByte()==0x00)
        aPriv.getByte();
      prv_array= aPriv.peekContent();
      variant= findVariantByPrvBytes(prv_array.length);

      ASN1 aPar1=aTot.getASN1Part();
      if(aPar1.asn1Type!=ASN1.PARAM_0)
        return false;
      ASN1 aPrime=aPar1.getASN1Part();
      if(!aPrime.equals(ASN1.OBJECT, variant.oid))
        return false;

      ASN1 aPar2=aTot.getASN1Part();
      if(aPar2.asn1Type!=ASN1.PARAM_1)
        return false;
      ASN1 aPub=aPar2.getASN1Part();
      if(aPub.asn1Type!=ASN1.BIT_STRING)
        return false;

      byte[][] tmp=fromPoint(aPub.peekContent());
      r_array = tmp[0];
      s_array = tmp[1];
    }
    catch(Exception e){
      //System.err.println(e);
      //e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * This function parses the 'Private' part from PuTTY's .PPK file
   * format: offset=0: length(4)
   *         offset=4: private key (32/48/66 bytes; + possible leading 0x00)
   */
  boolean parsePuttyPrivateKey(byte[] plain){
    Buffer buf=new Buffer(plain, true);
    if (buf.getLength()<4) return false;
    int keylen= buf.getInt();
    if (keylen<=0 || keylen>buf.getLength()) return false;
    int byte1= buf.peekByte();
    if (byte1==0) { /* skip leading 0x00 */
       --keylen;
       buf.getByte();
    }
    prv_array= new byte[keylen];
    buf.getByte(prv_array);
    return true;
  }

  boolean parse(byte[] plain){
    try{

      if(vendor==VENDOR_FSECURE){
        /*
	if(plain[0]!=0x30){              // FSecure
	  return true;
	}
	return false;
        */
	return false;
      }
      else if(vendor==VENDOR_PUTTY){
        return parsePuttyPrivateKey(plain);

      } else{
        return parseOpenSSHPem(plain);
      }
    }
    catch(Exception e){
      //System.err.println(e);
      //e.printStackTrace();
      return false;
    }
  }

  public byte[] getPublicKeyBlob(){
    byte[] foo = super.getPublicKeyBlob();

    if(foo!=null) return foo;

    if(r_array==null) return null;

    byte[][] tmp = new byte[3][];
    tmp[0] = variant.bMethodName;
    tmp[1] = variant.bCurveName;
    tmp[2] = new byte[1+r_array.length+s_array.length];
    tmp[2][0] = 4;   // POINT_CONVERSION_UNCOMPRESSED
    System.arraycopy(r_array, 0, tmp[2], 1, r_array.length);
    System.arraycopy(s_array, 0, tmp[2], 1+r_array.length, s_array.length);

    return Buffer.fromBytes(tmp).peekContent();
  }

  byte[] getKeyTypeName(){
    return variant.bMethodName;
  }

  public int getKeyType(){
    return ECDSA;
  }

  public int getKeySize(){
    return variant.keySize;
  }

  public byte[] getSignature(byte[] data){
    try{
      Class c=Class.forName((String)jsch.getConfig(variant.sMethodName));
      SignatureECDSA ecdsa=(SignatureECDSA)(c.newInstance());
      ecdsa.init();
      ecdsa.setPrvKey(prv_array);

      ecdsa.update(data);
      byte[] sig = ecdsa.sign();

      byte[][] tmp = new byte[2][];
      tmp[0] = variant.bMethodName;
      tmp[1] = sig;
      return Buffer.fromBytes(tmp).peekContent();
    }
    catch(Exception e){
      //System.err.println("e "+e);
    }
    return null;
  }

  public Signature getVerifier(){
    try{
      Class c=Class.forName((String)jsch.getConfig(variant.sMethodName));
      final SignatureECDSA ecdsa=(SignatureECDSA)(c.newInstance());
      ecdsa.init();

      if(r_array == null && s_array == null && getPublicKeyBlob()!=null){
        Buffer buf = new Buffer(getPublicKeyBlob());
        buf.getString();    // ecdsa-sha2-nistp256
        buf.getString();    // nistp256
        byte[][] tmp = fromPoint(buf.getString());
        r_array = tmp[0];
        s_array = tmp[1];
      } 
      ecdsa.setPubKey(r_array, s_array);
      return ecdsa;
    }
    catch(Exception e){
      //System.err.println("e "+e);
    }
    return null;
  }

  static KeyPair fromSSHAgent(JSch jsch, Buffer buf) throws JSchException {

    byte[][] tmp = buf.getBytes(5, "invalid key format");

    byte[] name = tmp[1];       // nistp256
    byte[][] foo = fromPoint(tmp[2]);
    byte[] r_array = foo[0];
    byte[] s_array = foo[1];

    byte[] prv_array = tmp[3];
    KeyPairECDSA kpair = new KeyPairECDSA(jsch,
                                          name,
                                          r_array, s_array,
                                          prv_array);
    kpair.publicKeyComment = new String(tmp[4]);
    kpair.vendor=VENDOR_OPENSSH;
    return kpair;
  }

  public byte[] forSSHAgent() throws JSchException {
    if(isEncrypted()){
      throw new JSchException("key is encrypted.");
    }
    Buffer buf = new Buffer();
    buf.putString(variant.bMethodName);
    buf.putString(variant.bCurveName);
    buf.putString(toPoint(r_array, s_array));
    buf.putString(prv_array);
    buf.putString(Util.str2byte(publicKeyComment));
    byte[] result = new byte[buf.getLength()];
    buf.getByte(result, 0, result.length);
    return result;
  }

  static byte[] toPoint(byte[] r_array, byte[] s_array) {
    byte[] tmp = new byte[1+r_array.length+s_array.length];
    tmp[0]=0x04;
    System.arraycopy(r_array, 0, tmp, 1, r_array.length);
    System.arraycopy(s_array, 0, tmp, 1+r_array.length, s_array.length);
    return tmp;
  }

  static byte[][] fromPoint(byte[] point, int keySize) {
    return fromPoint(new Buffer(point, true), keySize);
  }

  static byte[][] fromPoint(byte[] point) {
    return fromPoint(new Buffer(point, true), 0);
  }

  static byte[][] fromPoint(Buffer b, int keySize) {
    int publen= keySize>0? ((keySize+7)/8)*2: 0;
    while(b.index-b.s>publen+1 && b.buffer[b.s]==0x00)
      ++b.s;
    if(b.buffer[b.s]!=0x04) return null;
    ++b.s;
    if(publen>0){
      if(b.index-b.s<publen) return null;
    } else {
      publen=b.index-b.s;
      if(publen%2==1 || publen>64) return null;
    }
    byte[][] tmp = new byte[2][];
    byte[] r_array = new byte[publen/2];
    byte[] s_array = new byte[publen/2];
    System.arraycopy(b.buffer, b.s, r_array, 0, publen/2);
    System.arraycopy(b.buffer, b.s+publen/2, s_array, 0, publen/2);
    b.s+=publen;
    tmp[0] = r_array;
    tmp[1] = s_array;

    return tmp;
  }

  public void dispose(){
    super.dispose();
    Util.bzero(prv_array);
  }
}
