package com.jcraft.jsch;

/** Trying to collect OID (OBJECT ID) literals into one place
 *  In ASN1, they are stored as "0x06 <length-1byte> <binary-oid>"
 *  Mind you, many of these objects has synonyms, e.g. nistp384=ansip384r1=secp384r1
 */
public class OID {
public static final byte[]
/* 1.2.840.10045.2.1 */   OID_ecPublicKey = {0x2a, (byte)0x86, 0x48, (byte)0xce, 0x3d, 0x02, 0x01},
/* 1.2.840.10045.3.1.7 */ OID_nistp256    = {0x2a, (byte)0x86, 0x48, (byte)0xce, 0x3d, 0x03, 0x01, 0x07},
/* 1.3.132.0.34 */        OID_nistp384    = {0x2b, (byte)0x81, 0x04, 0x00, 0x22},
/* 1.3.132.0.35 */        OID_nistp521    = {0x2b, (byte)0x81, 0x04, 0x00, 0x23};
}
