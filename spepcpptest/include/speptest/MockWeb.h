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
		class Hook {
			public:
			virtual ~Hook();
			virtual int serve(Request&, Response&) = 0;
		};

		int dispatch(Request& req, Response& resp);

		MockWeb();
		~MockWeb();
		const std::string& getBaseURL();
		void hook(const std::string& path, Hook* function);

		private:
		std::string _baseURL;
		std::map<std::string, Hook*> _hooks;
		struct MHD_Daemon *_daemon;
	};

	class MockWebDefaultHook : public MockWeb::Hook {
		public:
		virtual ~MockWebDefaultHook();
		virtual int serve(MockWeb::Request& req, MockWeb::Response& resp);
	};
}

#endif
