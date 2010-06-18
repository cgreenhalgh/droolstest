/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Nottingham
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Nottingham
   nor the names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

</COPYRIGHT>

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

*/
// win32bluetooth.cpp : Defines the entry point for the console application.
//

#include <stdio.h>
#include <winsock2.h>
#include <Windows.h>
#include <ws2bth.h>
#include <BluetoothAPIs.h>
//#include <tchar.h>

#include "equip_ect_components_bluetoothdiscover_BluetoothDiscover.h"

static void addToKnown(char* buffer, int bufferLength, BLUETOOTH_ADDRESS address) {
	if (strlen(buffer)+(1+6*3+1) >= bufferLength) {
		fprintf(stderr,"ERROR: buffer overflow in pollBluetooth/addToKnown (buffer len %d)\n", bufferLength);
		return;
	}
	if (buffer[0]!=0) 
		strcat(buffer, ",");
	sprintf(buffer+strlen(buffer), "%02x:%02x:%02x:%02x:%02x:%02x",
		address.rgBytes[5], 
		address.rgBytes[4], 
		address.rgBytes[3], 
		address.rgBytes[2], 
		address.rgBytes[1], 
		address.rgBytes[0]);
}

// 0 = ok
int pollBluetooth(bool discover, int delay, bool probeAuthenticated, char* buffer, int bufferLength) {

	buffer[0] = '\0';

	BLUETOOTH_FIND_RADIO_PARAMS btfrp;
	btfrp.dwSize = sizeof(btfrp);
	HANDLE hRadio;
	HBLUETOOTH_RADIO_FIND hFind = BluetoothFindFirstRadio(&btfrp, &hRadio);
	if (hFind==NULL) {
		fprintf(stderr,"ERROR in BluetoothFindFirstRadio: %d\n", GetLastError());
		return -1;
	}
	bool worked = false;
	do {
		BLUETOOTH_RADIO_INFO RadioInfo;
		memset(&RadioInfo, 0, sizeof(RadioInfo));
		RadioInfo.dwSize = sizeof(RadioInfo);
		fprintf(stderr,"Sizeof RadioInfo = %d\n", sizeof(RadioInfo));
		DWORD res = BluetoothGetRadioInfo(hRadio, &RadioInfo);
		if (res==ERROR_SUCCESS) {
			worked = true;
			fprintf(stderr,"Got info on BT radio...\n");
			/* typedef struct {  
			DWORD dwSize;  
			BLUETOOTH_ADDRESS address;  
			WCHAR szName[BLUETOOTH_MAX_NAME_SIZE];  
			ULONG ulClassofDevice;  
			USHORT lmpSubversion;  
			USHORT manufacturer;
			} BLUETOOTH_RADIO_INFO;
			*/
			fprintf(stderr,"  address %02x.%02x.%02x.%02x.%02x.%02x, name %S, class %ld, subversion %d, manufacturer %d\n",
				RadioInfo.address.rgBytes[0], RadioInfo.address.rgBytes[1], RadioInfo.address.rgBytes[2],
				RadioInfo.address.rgBytes[3], RadioInfo.address.rgBytes[4], RadioInfo.address.rgBytes[5],
				RadioInfo.szName, RadioInfo.ulClassofDevice, RadioInfo.lmpSubversion, RadioInfo.manufacturer);

			fprintf(stderr,"UNKNOWN:\n");
			// unknown => must actually be here
			BLUETOOTH_DEVICE_SEARCH_PARAMS btsp;
			/*typedef struct {  DWORD dwSize;  BOOL fReturnAuthenticated;  BOOL fReturnRemembered;  BOOL fReturnUnknown;  BOOL fReturnConnected;  BOOL fIssueInquiry;  UCHAR cTimeoutMultiplier;  HANDLE hRadio;
			} BLUETOOTH_DEVICE_SEARCH_PARAMS;*/
			if (discover) {
				memset(&btsp, 0, sizeof(btsp));
				btsp.dwSize = sizeof(btsp);
				btsp.cTimeoutMultiplier = delay; //??
				btsp.fIssueInquiry = true;
				// matching appears to be by logical OR on the underlying records.
				// this is a pain since authenticated devices sit in there all
				// the time, and match (say) authenticated, but you can't tell if they
				// were just discovered or not. The last seen is updated even if just
				// from provided as remembered. 
				//   so you only seem to be able to detect presence/absense of 
				// UNKNOWN devices in this way, since they are culled from the table
				// (it appears) by the (new) inquiry.
				btsp.fReturnUnknown = true; //btsp.fReturnAuthenticated = true; //btsp.fReturnConnected = true;
				btsp.fReturnRemembered = false;
				btsp.hRadio = hRadio;
				BLUETOOTH_DEVICE_INFO btdi;
				memset(&btdi, 0, sizeof(btdi));
				btdi.dwSize = sizeof(btdi);
				HBLUETOOTH_DEVICE_FIND hDFind = BluetoothFindFirstDevice(&btsp, &btdi);
				if (hDFind==NULL) {
					fprintf(stderr,"ERROR in BluetoothFindFirstDevice: %d\n", GetLastError());

				}
				else {
					do {
						/*typedef struct _BLUETOOTH_DEVICE_INFO {  DWORD dwSize;  BLUETOOTH_ADDRESS Address;  ULONG ulClassofDevice;  BOOL fConnected;  BOOL fRemembered;  BOOL fAuthenticated;  SYSTEMTIME stLastSeen;  SYSTEMTIME stLastUsed;  WCHAR szName[BLUETOOTH_MAX_NAME_SIZE];
						} BLUETOOTH_DEVICE_INFO;*/
						fprintf(stderr,"Device address %02x.%02x.%02x.%02x.%02x.%02x, class %ld, connected %d, remembered %d, authenticated %d, seen %02d:%02d.%03d, name %S\n",
							btdi.Address.rgBytes[0], 
							btdi.Address.rgBytes[1], 
							btdi.Address.rgBytes[2], 
							btdi.Address.rgBytes[3], 
							btdi.Address.rgBytes[4], 
							btdi.Address.rgBytes[5],
							btdi.ulClassofDevice,
							btdi.fConnected,
							btdi.fRemembered,
							btdi.fAuthenticated,
							btdi.stLastSeen.wMinute, btdi.stLastSeen.wSecond, btdi.stLastSeen.wMilliseconds,
							btdi.szName);

						addToKnown(buffer, bufferLength, btdi.Address);

					} while (BluetoothFindNextDevice(hDFind, &btdi));
					BluetoothFindDeviceClose(hDFind);
				}
			}
			if (probeAuthenticated) {
				fprintf(stderr,"KNOWN/AUTHENTICATED:\n");
				// authenticaed - may not be here
				//BLUETOOTH_DEVICE_SEARCH_PARAMS btsp;
				memset(&btsp, 0, sizeof(btsp));
				btsp.dwSize = sizeof(btsp);
				btsp.cTimeoutMultiplier = 10; //??
				btsp.fIssueInquiry = false;
				btsp.fReturnUnknown = false; btsp.fReturnAuthenticated = true; //btsp.fReturnConnected = true;
				btsp.fReturnRemembered = true;
				btsp.hRadio = hRadio;
				BLUETOOTH_DEVICE_INFO btdi;
				memset(&btdi, 0, sizeof(btdi));
				btdi.dwSize = sizeof(btdi);
				HBLUETOOTH_DEVICE_FIND hDFind = BluetoothFindFirstDevice(&btsp, &btdi);
				if (hDFind==NULL) {
					fprintf(stderr,"ERROR in BluetoothFindFirstDevice: %d\n", GetLastError());
				}
				else {
					do {
						/*typedef struct _BLUETOOTH_DEVICE_INFO {  DWORD dwSize;  BLUETOOTH_ADDRESS Address;  ULONG ulClassofDevice;  BOOL fConnected;  BOOL fRemembered;  BOOL fAuthenticated;  SYSTEMTIME stLastSeen;  SYSTEMTIME stLastUsed;  WCHAR szName[BLUETOOTH_MAX_NAME_SIZE];
						} BLUETOOTH_DEVICE_INFO;*/
						fprintf(stderr,"Try to connect to Device address %02x.%02x.%02x.%02x.%02x.%02x, class %ld, connected %d, remembered %d, authenticated %d, seen %02d:%02d.%03d, name %S\n",
							btdi.Address.rgBytes[0], 
							btdi.Address.rgBytes[1], 
							btdi.Address.rgBytes[2], 
							btdi.Address.rgBytes[3], 
							btdi.Address.rgBytes[4], 
							btdi.Address.rgBytes[5],
							btdi.ulClassofDevice,
							btdi.fConnected,
							btdi.fRemembered,
							btdi.fAuthenticated,
							btdi.stLastSeen.wMinute, btdi.stLastSeen.wSecond, btdi.stLastSeen.wMilliseconds,
							btdi.szName);

						if (btdi.fConnected) {
							fprintf(stderr,"Already connected\n");
							addToKnown(buffer, bufferLength, btdi.Address);
						} else {
							// try to connect
							SOCKADDR_BTH addr;
							memset(&addr, 0, sizeof(addr));
							addr.addressFamily = AF_BTH;
							addr.btAddr = btdi.Address.ullLong;
							addr.port = 0;
							addr.serviceClassId = SDP_PROTOCOL_UUID;//OBEXObjectPushServiceClass_UUID;
							//SDP_PROTOCOL_UUID;// BASE_UUID 00000000-0000-1000-8000-00805F9B34FB
							//SDP uuid16 0x0001 in MS 4Bytes

							/* The WinSock DLL is acceptable. Proceed. */
							SOCKET sock = socket(AF_BTH, SOCK_STREAM, BTHPROTO_RFCOMM);
							if (sock==INVALID_SOCKET) {
								fprintf(stderr,"ERROR opening bluetooth socket: %d\n", WSAGetLastError());
							} else {
								ULONG bAuth = FALSE;
								if (setsockopt(sock, SOL_RFCOMM, SO_BTH_AUTHENTICATE, (char*)&bAuth, sizeof(bAuth))!=0) {
									fprintf(stderr,"ERROR setting bluetooth socket authenticate to false: %d\n", WSAGetLastError());
									//		getchar();
									//		return -1;
								}
								// error 10049 - denied? 10060 - not reachable?
								if (connect(sock, (sockaddr*)&addr, sizeof(addr))!=0) {
									fprintf(stderr,"ERROR connecting: %d\n", WSAGetLastError());
									if (WSAGetLastError()==WSAETIMEDOUT)
										;
									else {
										fprintf(stderr,"(but assumed in range as not timed out!)\n");
										addToKnown(buffer, bufferLength, btdi.Address);
									}
								} else {
									fprintf(stderr,"Connected OK?! (%d)\n", WSAGetLastError());
									shutdown(sock, SD_BOTH);
									addToKnown(buffer, bufferLength, btdi.Address);
								}
							}
						}
					} while (BluetoothFindNextDevice(hDFind, &btdi));
					BluetoothFindDeviceClose(hDFind);
				}
			}
		} else {
			fprintf(stderr,"ERROR getting info on BT radio: %d\n", res);
		}
	} while(BluetoothFindNextRadio(hFind, &hRadio));
	BluetoothFindRadioClose(hFind);

	return worked ? 0 : -1;
}
/*				
if (!btdi.fAuthenticated) {
PWCHAR pszPasskey = NULL;
ULONG ulPasskeyLength = 0;
DWORD status = BluetoothAuthenticateDevice(NULL, hRadio, &btdi, pszPasskey, ulPasskeyLength);
if (status==ERROR_SUCCESS) {
fprintf(stderr,"Authenticated OK (? %d)\n", GetLastError());
} else {
fprintf(stderr,"ERROR authenticating: %d/%d\n", status, GetLastError());
}
}
*/
/*	Never worked for me yet
WSAQUERYSET qsRestrictions;
memset(&qsRestrictions, 0, sizeof(qsRestrictions));
qsRestrictions.dwSize = sizeof(qsRestrictions);
//qsRestrictions.
//GenericNetworkingServiceClass_UUID
GUID guid = OBEXObjectPushServiceClass_UUID;//GenericFileTransferServiceClass_UUID;//OBEXFileTransferServiceClass_UUID;
//OBEXObjectPushServiceClass_UUID;//SerialPortServiceClass_UUID;
qsRestrictions.lpServiceClassId = &guid;
DWORD dwControlFlags =LUP_FLUSHCACHE | LUP_RETURN_ALIASES| LUP_DEEP;//LUP_RETURN_ALL;
HANDLE hLookup;
if (WSALookupServiceBegin(&qsRestrictions, dwControlFlags, &hLookup)!=0) {
fprintf(stderr,"ERROR doing WSALookupServiceBegin: %d\n", WSAGetLastError());
if (WSAGetLastError()==WSA_E_NO_MORE) 
fprintf(stderr,"(no more)");
} else {
WSAQUERYSET qsResults;
DWORD dwBufferLength = sizeof(qsResults);
while (WSALookupServiceNext(hLookup, dwControlFlags, &dwBufferLength, &qsResults)==0) {
//typedef struct _WSAQuerySet {  DWORD dwSize;  LPTSTR lpszServiceInstanceName;  LPGUID lpServiceClassId;  LPWSAVERSION lpVersion;  LPTSTR lpszComment;  DWORD dwNameSpace;  LPGUID lpNSProviderId;  LPTSTR lpszContext;  DWORD dwNumberOfProtocols;  LPAFPROTOCOLS lpafpProtocols;  LPTSTR lpszQueryString;  DWORD dwNumberOfCsAddrs;  LPCSADDR_INFO lpcsaBuffer;  DWORD dwOutputFlags;  LPBLOB lpBlob;
} WSAQUERYSET, *PWSAQUERYSETW;

_tprintf(TEXT("lookup res: name %s, guid %d-%d-%d-%u.%u.%u.%u.%u.%u.%u.%u\n"),
qsResults.lpszServiceInstanceName,
qsResults.lpServiceClassId->Data1,
qsResults.lpServiceClassId->Data2,
qsResults.lpServiceClassId->Data3,
qsResults.lpServiceClassId->Data4[0],
qsResults.lpServiceClassId->Data4[1],
qsResults.lpServiceClassId->Data4[2],
qsResults.lpServiceClassId->Data4[3],
qsResults.lpServiceClassId->Data4[4],
qsResults.lpServiceClassId->Data4[5],
qsResults.lpServiceClassId->Data4[6],
qsResults.lpServiceClassId->Data4[7]);

}
if (WSAGetLastError()!=WSA_E_NO_MORE) {
fprintf(stderr,"ERROR doing WSALookupServiceNext: %d\n", WSAGetLastError());
}
WSALookupServiceEnd(hLookup);
}
*/
/*
int main(int argc, char* argv[])
{
	WORD wVersionRequested;
	WSADATA wsaData;
	int err;

	wVersionRequested = MAKEWORD( 2, 2 );

	err = WSAStartup( wVersionRequested, &wsaData );
	if ( err != 0 ) {
		// Tell the user that we could not find a usable 
		// WinSock DLL.                                  
		return -1;
	}

	//Confirm that the WinSock DLL supports 2.2.
	if ( LOBYTE( wsaData.wVersion ) != 2 ||
		HIBYTE( wsaData.wVersion ) != 2 ) {
			// Tell the user that we could not find a usable 
			// WinSock DLL.                                  
			WSACleanup( );
			return -1; 
		}
#define BUFFER_SIZE 2000
		char buf[BUFFER_SIZE];
		int status = pollBluetooth(true, 10, true, buf, BUFFER_SIZE);
		if (status==0)
			fprintf(stderr,"RESULT: %s\n", buf);
		else
			fprintf(stderr,"FAILED\n");

		getchar();
}
*/
jstring JNICALL Java_equip_ect_components_bluetoothdiscover_BluetoothDiscover_pollBluetooth
(JNIEnv *env, jobject obj, jboolean discover, jint delay, jboolean probeAuthenticated) {
#define BUFFER_SIZE 2000
		char buf[BUFFER_SIZE];
		int status = pollBluetooth((bool)discover,(int) delay, (bool)probeAuthenticated, buf, BUFFER_SIZE);
		if (status==0) {
			jchar chars[BUFFER_SIZE];
			char *f;
			jchar *t;
			jsize len;
			for (f=buf, t=chars, len=0; *f!=0; f++, t++, len++) 
				*t = (jchar)*f;
			jstring s = (*env).NewString(&(chars[0]), len); 
			return s;
		}
		return NULL;
}

