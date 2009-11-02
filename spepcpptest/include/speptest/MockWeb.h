#ifndef __MOCKWEB_H
#define __MOCKWEB_H

#include <map>
#include <string>

#include <sys/types.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <microhttpd.h>

#define MOCKWEB_PORT 10080

namespace speptest {

	class MockWeb {
		public:
		class Request {
			public:
			std::string url;
			std::string method;
			std::string version;
			const char *uploadData;
			size_t uploadDataSize;
		};
		class Response {
			public:
			std::string document;
			std::map<std::string,std::string> headers;
		};

		typedef int (*MockWebHookType)(Request&, Response&);

		int dispatch(Request& req, Response& resp);

		MockWeb();
		~MockWeb();
		const std::string& getBaseURL();
		void hook(const std::string& path, MockWebHookType function);

		private:
		std::string _baseURL;
		std::map<std::string,MockWebHookType> _hooks;
		struct MHD_Daemon *_daemon;
	};

	int mockWebDefaultHook(MockWeb::Request& req, MockWeb::Response& resp);
}

#endif
