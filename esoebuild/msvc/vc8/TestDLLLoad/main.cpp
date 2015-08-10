#include <windows.h>
#include <iostream>
#include <string>


//Returns the last Win32 error, in string format. Returns an empty string if there is no error.
std::string GetLastErrorAsString()
{
	//Get the error message, if any.
	DWORD errorMessageID = ::GetLastError();
	if (errorMessageID == 0)
		return std::string(); //No error message has been recorded

	LPSTR messageBuffer = nullptr;
	size_t size = FormatMessageA(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
		NULL, errorMessageID, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPSTR)&messageBuffer, 0, NULL);

	std::string message(messageBuffer, size);

	//Free the buffer.
	LocalFree(messageBuffer);

	return message;
}


int main() {

	HINSTANCE hInstance = LoadLibrary(L"spep-iis-module.dll");

	if (!hInstance) {
		std::cout << "Failed to load library" << std::endl;
		std::cout << "Error was: " << GetLastErrorAsString() << std::endl;
		return 0;
	}

	void* function = GetProcAddress(hInstance, "RegisterModule");

	if (!function) {
		std::cout << "Failed to get function address: " << std::endl;
		std::cout << "Error was: " << GetLastErrorAsString() << std::endl;
		return 0;
	}

	std::cout << "Loaded dll and function RegisterModule" << std::endl;

	//std::string randomString = "\0";

	return 0;
}