package com.mrd.bitlib.model;

import com.mrd.bitlib.util.HexUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class TransactionTest {

   @Test
   public void testIsRbf() throws Exception {
      // tx with sequence number maxint
      final Transaction txSeqFFFFFFFF = Transaction.fromBytes(HexUtils.toBytes("010000000190F55E38240AB795F7E11DF5E272D699B525C0E50AF69733FDA1B583C4273C23000000008A47304402206A945B64AE1478F1ED1374D3F1BD0B9DB66B424804EFB3CE59B38293A25F5CAF022037695FE1B04A07569E0F6C019AA406492294151F3A0E88CAE2C53ED713D5C6D50141048FD4539156A8AD9EF7E885CEAD013BF5377DE67E05AA34B662C6431F684C1D61D83B690FB8FEEEB69C305B4043F8D3DBB4F2847FEAF767E8EAC1E51E9C4425B2FFFFFFFF025E31920B000000001976A914152E9F6874A0768ADA32F1C8C5FC80337FD7EE5988AC00E1F505000000001976A914D3F9702528B302DBADCBBE26E91001C2E453814088ACB4270600"));
      assertFalse(txSeqFFFFFFFF.isRbfAble());

      // tx with sequence number maxint-1
      final Transaction txSeqFEFFFFFF = Transaction.fromBytes(HexUtils.toBytes("010000000190F55E38240AB795F7E11DF5E272D699B525C0E50AF69733FDA1B583C4273C23000000008A47304402206A945B64AE1478F1ED1374D3F1BD0B9DB66B424804EFB3CE59B38293A25F5CAF022037695FE1B04A07569E0F6C019AA406492294151F3A0E88CAE2C53ED713D5C6D50141048FD4539156A8AD9EF7E885CEAD013BF5377DE67E05AA34B662C6431F684C1D61D83B690FB8FEEEB69C305B4043F8D3DBB4F2847FEAF767E8EAC1E51E9C4425B2FEFFFFFF025E31920B000000001976A914152E9F6874A0768ADA32F1C8C5FC80337FD7EE5988AC00E1F505000000001976A914D3F9702528B302DBADCBBE26E91001C2E453814088ACB4270600"));
      assertFalse(txSeqFEFFFFFF.isRbfAble());

      // tx with sequence number maxint-2
      final Transaction txSeqFDFFFFFF = Transaction.fromBytes(HexUtils.toBytes("010000000190F55E38240AB795F7E11DF5E272D699B525C0E50AF69733FDA1B583C4273C23000000008A47304402206A945B64AE1478F1ED1374D3F1BD0B9DB66B424804EFB3CE59B38293A25F5CAF022037695FE1B04A07569E0F6C019AA406492294151F3A0E88CAE2C53ED713D5C6D50141048FD4539156A8AD9EF7E885CEAD013BF5377DE67E05AA34B662C6431F684C1D61D83B690FB8FEEEB69C305B4043F8D3DBB4F2847FEAF767E8EAC1E51E9C4425B2FDFFFFFF025E31920B000000001976A914152E9F6874A0768ADA32F1C8C5FC80337FD7EE5988AC00E1F505000000001976A914D3F9702528B302DBADCBBE26E91001C2E453814088ACB4270600"));
      assertTrue(txSeqFDFFFFFF.isRbfAble());
   }
}