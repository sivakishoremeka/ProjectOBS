package com.obs.payapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.obs.object.PrintInfo;

//import java.util.Vector;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import android.util.Log;
import android.widget.Toast;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
class ConnectedThread extends Thread {
	private static final String TAG = "ConnectedThread";
	private final BluetoothSocket mmSocket;
	private final InputStream mmInStream;
	private final OutputStream mmOutStream;
	//public int nofByts = 0;
	private Context ctx;

	/////////////////// For print variables ////////////////////
	int[] packetBuffer = new int[5000];   
	String ComData=null;
	int noBytes = 0,ct = 0,nofByts=0,k=0,totDataLen;;
	//private int tempPacket[];
	//	private int packetLength = 0;
	//private boolean available = false;
	//private boolean timeoutFlag = false;
	/////////////////// For print variables ////////////////////
	byte[] fpDatabytes = null;
	byte ENROLL_ID= 0x21;
	byte ILV_OK=0x00;
	byte ILVSTS_OK=0x00;  
	byte ILVSTS_HIT=0x01;
	byte ILVSTS_NO_HIT=0x02;

	File directory=null;
	private BluetoothDevice device;
	private BluetoothChatService btChatService;
	public ConnectedThread(BluetoothSocket socket, BluetoothDevice device, BluetoothChatService btChatService,Context c) {
		ctx  =c;
		this.btChatService=btChatService;
		Log.d(TAG, "create ConnectedThread");
		mmSocket = socket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		this.setDevice(device);
		// Get the BluetoothSocket input and output streams
		try {
			if(socket!=null){
				tmpIn =  socket.getInputStream();
				tmpOut = socket.getOutputStream();
			}
			else{
				System.out.println("Socket is closed by the server");
			}
		} catch (IOException e) {
			Log.e(TAG, "temp sockets not created", e);
		}
		mmInStream = tmpIn;
		mmOutStream = tmpOut;
	}
	//To reset printer for finger print scan
	public void FPReset() {
		byte[] reset = new byte[8];
		reset[0] = (byte) 0x7e;
		reset[1] = (byte) 0xbb;
		reset[2] = (byte) 0x04;
		reset[3] = (byte) 0xbf;
		//reset[4] = (byte) 0x42;
		//reset[5] = (byte) 0x04;
		//reset[6] = (byte) 0x45;

		try {
			Thread.sleep(2000);
			mmOutStream.write(reset);
			mmOutStream.flush();
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG,"IO Error");
		}
	}
	public void PrintdataFun(PrintInfo printinfo){
		String PrintData1;
        String PrintData2;
        Date date= new Date();
		SimpleDateFormat  formater = new SimpleDateFormat("dd MMM yyyy");
		PrintData1 =        "        PG Cable        ";
        PrintData2 = 	    "  Pharganj, New Delhi.  "+
		                    "========================"+
				            "                        ";
        PrintData2 +=       " Receipt Id: " +printinfo.getReceiptId()+"           ".substring(printinfo.getReceiptId().length());
        PrintData2 +=	    "                        ";
        PrintData2 +=       " Date  : "+(formater.format(date))+"    ";
        PrintData2 +=	    "                        ";
		PrintData2 +=       " Id    : "+printinfo.getClientId()+"               ".substring(printinfo.getClientId().length());
		PrintData2 +=       "                        ";
		String ClientName = " Name  : "+printinfo.getClientName();
                                
		if(ClientName.length()<=24){
			PrintData2 += ClientName+"                        ".substring(ClientName.length()-1);
		}
		else
		{
			while(ClientName.length()/24>=1)
			{
				PrintData2 +=ClientName.substring(0, 24);
				ClientName="         "+ClientName.substring(24);
			}
			if(ClientName.length()<24){
				PrintData2 += ClientName+"                        ".substring(ClientName.length());
			}
		}
		PrintData2 +=	    "                        ";
		PrintData2 +=	    "H/W Details:"+printinfo.getHardwareDetails();
		PrintData2 +=	    "                        ";
			if(printinfo.getPaymentCode().equalsIgnoreCase("CA"))
				PrintData2+=" Mode  : CASH"+"                        ".substring(printinfo.getClientId().length()+13-1);
			else
				PrintData2+=" Mode  : CHEQUE"+"                        ".substring(printinfo.getClientId().length()+16-1);
		PrintData2 +=	    "                        ";
		PrintData2 +=       " Amount: "+printinfo.getAmountPaid()+"                           ".substring(printinfo.getClientId().length()+8);;
		PrintData2 +=       "                        ";
		PrintData2 +=       " Agent :"+printinfo.getName()+"               ".substring(printinfo.getClientId().length()+9-1);
		PrintData2 +=        "========================";
		try {
			Thread.sleep(2000);			
		} catch (Exception e) {
			e.printStackTrace();			
		}			
		Log.i("ConnectedThread", PrintData1);
		Log.i("ConnectedThread", PrintData2);
		PrinterData(PrintData1,4);
		PrinterData(PrintData2,1);
		//PrinterData(PrintData,2);
		// PrinterData(PrintData,3);
		//  PrinterData(PrintData,4);                  
		PrinterData(" ",1);
		PrinterData(" ",1);
		PrinterData(" ",1);
		PrinterData(" ",1);
		cancel();
	}

	public void cancel() {
		try {
			mmSocket.close();
		} catch (IOException e) {
			Log.e(TAG, "close() of connect socket failed", e);
		}
	}
	public void setDevice(BluetoothDevice device) {
		this.device = device;
	}
	public BluetoothDevice getDevice() {
		return device;
	}
	private  int PrinterData(String data,int Font)
	{
		int i=0,j=0,col=0,la=0;
		char[] tempBuffer = new char[1000];
		int[] dataBuffer = new int[5000];
		//  byte[] b = new byte[2];
		System.out.println(data);
		data.getChars(0,data.length(),tempBuffer,col);
		totDataLen = data.length();
		/////////////////////// PACKET FORMAT /////////////////////
		for(i=0;la<data.length();)
		{
			if(Font == 1 || Font ==2)
			{
				if(Font == 1)
					dataBuffer[i++] = 0xf0;
				else if(Font == 2)
					dataBuffer[i++] = 0xf1;
				for(j=0;j<24;j++)
				{
					if(la<data.length())
					{
						dataBuffer[i++] = tempBuffer[la++];
					}
					else
						break;
				}
			}
			else if(Font == 3 || Font == 4)
			{
				if(Font ==3)
					dataBuffer[i++] = 0xf2;
				else if(Font ==4)
					dataBuffer[i++] = 0xf3;
				for(j=0;j<42;j++)
				{
					if(la<data.length())
					{
						dataBuffer[i++] = tempBuffer[la++];
					}
					else
						break;
				}
			}
		}
		/////////////////////// PACKET FORMAT /////////////////////
		try {
			SendPrint(dataBuffer, i,Font);
		} catch (IOException ex) {

		}
		return 1;
	}

	public int SendPrint(int [] pData,int pLength,int Font) throws IOException
	{
		int []pBuff = new int[1000];
		int n,lrc=0,space=0,i=0,iBreak=0,dataPack = 0,pPos=0,pInc=0;
		int iEvLength=0;

		if(Font == 1 || Font ==2)
			dataPack = pLength/125 + 1;
		else if(Font == 3 || Font ==4)
			dataPack = pLength/86 + 1;

		System.out.println(dataPack);
		if(Font ==1 || Font ==2)
			space = pLength%125;
		else if(Font ==3 || Font ==4)
			space = pLength%86;
		/////////////////// APPENDING SPACES///////////////////
		if(Font == 1 || Font ==2)
		{
			for(i=0;;i++)
			{
				iBreak = space % 25;
				if(iBreak == 0)
					break;
				pData[pLength++] = 0x20;
				space++;
			}
		}
		else if(Font == 3 || Font ==4)
		{
			for(i=0;;i++)
			{
				iBreak = space % 43;
				if(iBreak == 0)
					break;
				pData[pLength++] = 0x20;
				space++;
			}
		}
		/////////////////// APPENDING SPACES///////////////////
		while(dataPack > 0)
		{
			pBuff[0] = 0x7e;
			pBuff[1] = 0xb2;
			if(Font == 1 || Font ==2)
			{
				pPos = pInc *125;
				if(dataPack > 1)
					iEvLength = 125;
				else
					iEvLength = pLength - pPos;
			}
			else if(Font == 3 || Font ==4)
			{
				pPos = pInc *86;
				if(dataPack > 1)
					iEvLength = 86;
				else
					iEvLength = pLength - pPos;
			}
			System.out.println("iEvLength="+iEvLength+"Pos="+pPos);
			System.arraycopy(pData, pPos, pBuff, 2, iEvLength);
			iEvLength = iEvLength +2;
			pBuff[iEvLength++] = 0x04;


			for(n=1;n<iEvLength;n++)
			{
				lrc^=pBuff[n];
			}
			pBuff[iEvLength++] = lrc;
			try {
				for(k=0;k<iEvLength;k++)
					mmOutStream.write(pBuff[k]);
				mmOutStream.flush();            
				ReadPrinterresp();
			} catch (IOException ex) {
				ex.printStackTrace();
			}                          

			dataPack--;
			pInc++;
			lrc = 0;
		}
		return 1;
	}

	String stringToHex(String str) {
		char[] chars = str.toCharArray();
		StringBuffer strBuffer = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			strBuffer.append(Integer.toHexString((int) chars[i]));
		}
		return strBuffer.toString();
	}
	public void ReadPrinterresp()
	{
		int iResp = 0;
		try {
			iResp = mmInStream.read();
			if(iResp!= 128 || iResp!= 0)
			{                
				switch(iResp){
				case 65:
					Toast.makeText(ctx, "ERROR...PAPER OUT", Toast.LENGTH_LONG).show();
					//	                       BluetoothSearch.append("paper out");
					//	                        Display.getDisplay(thisMidlet).setCurrent(BluetoothSearch);
					break;
				case 66:
					Toast.makeText(ctx, "ERROR...PLATEN OPEN", Toast.LENGTH_LONG).show();
					//	                        BluetoothSearch.append("platen open");
					//	                         Display.getDisplay(thisMidlet).setCurrent(BluetoothSearch);
					break;
				case 72:
					Toast.makeText(ctx, "ERROR..TEMP HIGH", Toast.LENGTH_LONG).show();
					//	                        BluetoothSearch.append("Error !!!! TEMP HIGH");
					//	                         Display.getDisplay(thisMidlet).setCurrent(BluetoothSearch);
					break;
				case 80:
					Toast.makeText(ctx, "ERROR..TEMP LOW", Toast.LENGTH_LONG).show();
					//	                         BluetoothSearch.append("Error !!!! TEMP TOO LOW");
					//	                          Display.getDisplay(thisMidlet).setCurrent(BluetoothSearch);
					break;
				case 96:
					Toast.makeText(ctx, "ERROR..Improper Voltage", Toast.LENGTH_LONG).show();
					//	                        BluetoothSearch.append("Error !!!! IMPROPER VOLTAGE");
					//	                         Display.getDisplay(thisMidlet).setCurrent(BluetoothSearch);
					break;
				}

			}
		}
		catch(Exception e){}
	}

	public void MagCard()
	{
		int iRetv=0;	
		byte [] reset= new byte[6];
		reset[0] = 0x7e;
		reset[1] = (byte) 0xbb;
		reset[2] = 0x04;
		reset[3] = (byte) 0xbf;

		try {
			Thread.sleep(2000);
			mmOutStream.write(reset);
			mmOutStream.flush();
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG,"IO Error");
		}		 
		System.out.println("inside iretv");
		iRetv = serialMagTrack1();
		if(iRetv == 178 || iRetv == 44)
		{
			System.out.println("inside iretv");
		}
		else
			serialMagTrack2();

		cancel();
	}
	public int vProcessData(String readBuffer, int lrcRecv)
	{
		int lrcCalc = 0,iLen = 0;
		String Track1Data = "";
		String Track2Data = "";


		try
		{
			lrcCalc = CalcLRC (new String(readBuffer));
			if (lrcCalc != lrcRecv)
			{
				System.out.print("LCReRROR ");
				writesingle((byte)0x44);
				//close();
				return 44;
			}
			else
			{
				writesingle((byte)0x80);
				//close();
				String scannedInput = readBuffer.toString();
				iLen = readBuffer.length();

				if(readBuffer.charAt(0) == 0xb0)
				{				
					Track1Data = Decryption (new String(readBuffer));
					System.out.println ("");
					System.out.println ("Track1Data-----");
					System.out.println (Track1Data);
					System.out.println ("");
					//Toast.makeText(ctx, Track1Data, Toast.LENGTH_LONG).show();
					Log.d(TAG, "Track 1 Data:"+Track1Data);

				}
				if(readBuffer.charAt(0) == 0xb1)
				{
					Track2Data = Decryption (new String(readBuffer));
					System.out.println ("");
					System.out.println ("Track2Data-----");
					System.out.println (Track2Data);
					System.out.println ("");
					//Toast.makeText(ctx, Track2Data, Toast.LENGTH_LONG).show();
					Log.d(TAG, "Track 2 Data:"+Track2Data);

				}
				if(readBuffer.charAt(0) == 0xb2)
				{
					System.out.println ("");
					System.out.print("Swipe Error----- ");
					System.out.println (readBuffer);
					System.out.println ("");
					Log.d(TAG, "Re-Swipe the Card");
					//Toast.makeText(ctx, "Re-Swipe the Card", Toast.LENGTH_LONG).show();
					return 178;
				}
			}
		}
		catch (IOException e) {}
		return 0;
	}

	public int CalcLRC(String encText)
	{
		int lrcCalc = 0, count;
		int temp;

		for (count = 0; count < encText.length() ;count++ )
		{
			temp =  encText.charAt(count);
			lrcCalc = lrcCalc ^ temp;

		}
		lrcCalc = lrcCalc ^ 4;
		return lrcCalc;
	}

	public String Decryption(String encText)
	{
		String EncDecKey="GODISGREAT";
		String TempInp="";

		int count,count1;
		char [] tempEnc=new char [encText.length()];
		int [] decrypted_info=new int [encText.length()];

		TempInp=encText.substring(1,encText.length());

		for(count=0;count<TempInp.length();count++)
		{
			decrypted_info[count]=TempInp.charAt(count);

			// System.out.print("Encrypted data=  "+decrypted_info[count]);
			// System.out.print(" ");

			for( count1=0;count1<( EncDecKey.charAt(count % 10) & 0x07 );count1++)
			{
				if((decrypted_info[count] & 0x01) > 0x00)
				{
					decrypted_info[count] =decrypted_info[count] >> 1;
		decrypted_info[count] =decrypted_info[count] | 0x80;
				}
				else
				{
					decrypted_info[count] =decrypted_info[count] >> 1;
				}
			}

			decrypted_info[count]^=0xFF;
			tempEnc[count]=(char) decrypted_info[count];
			//   System.out.println("   Decrypted Data=  "+tempEnc[count]);
		}
		String s=new String(tempEnc);
		return s;
	}

	public int serialMagTrack1()
	{
		StringBuffer readBuffer = new StringBuffer();
		int c, lrcRecv = 0,iStatus=0;
		try
		{

			//  System.out.println("\n Inside Mag");
			while ((c=mmInStream.read()) !=  0x7E) {}
			c = mmInStream.read();
			readBuffer.append((char) c);
			if (c == 0xb0 || c == 0xb1 || c == 0xb2)
			{
				while (true)
				{
					c=mmInStream.read();
					if (c == 0x04)
					{
						lrcRecv=mmInStream.read();
						break;
					}
					readBuffer.append((char) c);
				}
			}
			readBuffer.append((char)0x00);
			readBuffer.append((char)0x00);
			iStatus = vProcessData(new String(readBuffer), lrcRecv);
		}
		catch (IOException e) {}
		return iStatus;
	}
	public int serialMagTrack2()
	{
		StringBuffer readBuffer = new StringBuffer();
		int c, lrcRecv = 0,iStatus=0;
		try
		{	                
			//  System.out.println("\n Inside Mag");
			while ((c=mmInStream.read()) !=  0x7E) {}
			c = mmInStream.read();
			readBuffer.append((char) c);
			if (c == 0xb0 || c == 0xb1 || c == 0xb2)
			{
				while (true)
				{
					c=mmInStream.read();
					if (c == 0x04)
					{
						lrcRecv=mmInStream.read();
						break;
					}
					readBuffer.append((char) c);
				}
			}
			readBuffer.append((char)0x00);
			readBuffer.append((char)0x00);
			iStatus = vProcessData(new String(readBuffer), lrcRecv);
		}
		catch (IOException e) {}
		return iStatus;
	}
	public void writesingle(byte b) throws IOException{

		mmOutStream.write(b);
		mmOutStream.flush();
	}
	public void delay(int time)
	{
		try {
			Thread.sleep(time);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}		
}

