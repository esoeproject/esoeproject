#include <windows.h>
#include <iostream>
#include <string>

int main() {

	HINSTANCE hInstance = LoadLibrary(L"spep-iis-module.dll");

	if (!hInstance) {
		std::cout << "failed to loaded library" << std::endl;
	}

	void* function = GetProcAddress(hInstance, "RegisterModule");

	if (!function) {
		std::cout << "failed to get function address" << std::endl;
	}

	std::string randomString = "\0";

	return 0;
}